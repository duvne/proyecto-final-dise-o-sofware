package com.volt.usuariosauth.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtTokenService {
	private static final String ALGORITHM = "HmacSHA256";
	private static final String ISSUER = "volt-usuarios-auth";
	private static final Duration TOKEN_LIFETIME = Duration.ofMinutes(30);
	private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

	private final ObjectMapper objectMapper;
	private final byte[] signingKey;
	private final Clock clock;

	public JwtTokenService(
			ObjectMapper objectMapper,
			@Value("${JWT_SECRET:}") String secret) {
		this(objectMapper, secret, Clock.systemUTC());
	}

	JwtTokenService(ObjectMapper objectMapper, String secret, Clock clock) {
		byte[] key = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
		if (key.length < 32) {
			throw new IllegalStateException("Configura JWT_SECRET con al menos 32 bytes aleatorios.");
		}
		this.objectMapper = objectMapper;
		this.signingKey = key.clone();
		this.clock = clock;
	}

	public IssuedToken emitir(String userId, List<String> roles) {
		Instant issuedAt = clock.instant();
		Instant expiresAt = issuedAt.plus(TOKEN_LIFETIME);
		try {
			String header = ENCODER.encodeToString(objectMapper.writeValueAsBytes(
					Map.of("alg", "HS256", "typ", "JWT")));
			String claims = ENCODER.encodeToString(objectMapper.writeValueAsBytes(Map.of(
					"iss", ISSUER,
					"sub", userId,
					"roles", roles,
					"iat", issuedAt.getEpochSecond(),
					"exp", expiresAt.getEpochSecond())));
			String unsignedToken = header + "." + claims;
			return new IssuedToken(unsignedToken + "." + ENCODER.encodeToString(firmar(unsignedToken)), expiresAt);
		} catch (IOException exception) {
			throw new IllegalStateException("No se pudo crear el token de acceso.", exception);
		}
	}

	public TokenPrincipal validar(String token) {
		if (token == null) {
			throw new InvalidTokenException();
		}
		String[] segments = token.split("\\.", -1);
		if (segments.length != 3) {
			throw new InvalidTokenException();
		}
		try {
			String unsignedToken = segments[0] + "." + segments[1];
			byte[] suppliedSignature = DECODER.decode(segments[2]);
			if (!MessageDigest.isEqual(firmar(unsignedToken), suppliedSignature)) {
				throw new InvalidTokenException();
			}

			JsonNode header = objectMapper.readTree(DECODER.decode(segments[0]));
			JsonNode claims = objectMapper.readTree(DECODER.decode(segments[1]));
			if (!"HS256".equals(header.path("alg").asText())
					|| !"JWT".equals(header.path("typ").asText())
					|| !ISSUER.equals(claims.path("iss").asText())
					|| claims.path("exp").asLong(0) <= clock.instant().getEpochSecond()
					|| claims.path("iat").asLong(Long.MAX_VALUE) > clock.instant().getEpochSecond()
					|| !claims.path("sub").isTextual()
					|| claims.path("sub").asText().isBlank()
					|| !claims.path("roles").isArray()) {
				throw new InvalidTokenException();
			}

			List<String> roles = new ArrayList<>();
			for (JsonNode role : claims.path("roles")) {
				if (!role.isTextual() || role.asText().isBlank()) {
					throw new InvalidTokenException();
				}
				roles.add(role.asText());
			}
			return new TokenPrincipal(claims.path("sub").asText(), roles);
		} catch (IOException | IllegalArgumentException exception) {
			throw new InvalidTokenException();
		}
	}

	private byte[] firmar(String value) {
		try {
			Mac mac = Mac.getInstance(ALGORITHM);
			mac.init(new SecretKeySpec(signingKey, ALGORITHM));
			return mac.doFinal(value.getBytes(StandardCharsets.US_ASCII));
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("No se pudo firmar el token de acceso.", exception);
		}
	}
}
