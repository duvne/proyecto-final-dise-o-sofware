package com.volt.usuariosauth.view;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.volt.shared.security.IssuedToken;
import com.volt.usuariosauth.domain.Usuario;

public record AuthResponse(
		@JsonProperty("access_token") String accessToken,
		@JsonProperty("token_type") String tokenType,
		@JsonProperty("expires_in") long expiresInSeconds,
		@JsonProperty("user_id") String userId,
		String correo,
		List<String> roles,
		boolean verificado) {

	public AuthResponse {
		roles = List.copyOf(roles);
	}

	public static AuthResponse desde(IssuedToken token, Usuario usuario) {
		long segundos = Math.max(1, Duration.between(Instant.now(), token.expiresAt()).getSeconds());
		return new AuthResponse(
				token.value(),
				"Bearer",
				segundos,
				usuario.id(),
				usuario.correo(),
				usuario.roles(),
				usuario.verificado());
	}
}
