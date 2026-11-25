package com.volt.usuariosauth.business;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.volt.shared.audit.AuditoriaService;
import com.volt.usuariosauth.domain.RecuperacionContrasena;
import com.volt.usuariosauth.domain.RecuperacionInvalidaException;
import com.volt.usuariosauth.domain.Usuario;
import com.volt.usuariosauth.persistence.IRecuperacionDAO;
import com.volt.usuariosauth.persistence.IUsuarioDAO;
import com.volt.usuariosauth.view.RecuperacionConfirmRequest;
import com.volt.usuariosauth.view.RecuperacionRequest;

@Service
public class RecuperacionService {
	private static final SecureRandom RANDOM = new SecureRandom();
	private static final long MINUTOS_VIGENCIA = 30;

	private final IUsuarioDAO usuarioDAO;
	private final IRecuperacionDAO recuperacionDAO;
	private final PasswordEncoder passwordEncoder;
	private final AuditoriaService auditoriaService;

	public RecuperacionService(
			IUsuarioDAO usuarioDAO,
			IRecuperacionDAO recuperacionDAO,
			PasswordEncoder passwordEncoder,
			AuditoriaService auditoriaService) {
		this.usuarioDAO = usuarioDAO;
		this.recuperacionDAO = recuperacionDAO;
		this.passwordEncoder = passwordEncoder;
		this.auditoriaService = auditoriaService;
	}

	public Map<String, Object> solicitar(RecuperacionRequest request, String ip) {
		String correo = request.correo().trim().toLowerCase();
		var usuario = usuarioDAO.buscarPorCorreo(correo);
		if (usuario.isEmpty()) {
			return Map.of("mensaje", "Si el correo existe, se envio un enlace de recuperacion.");
		}

		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		RecuperacionContrasena recuperacion = new RecuperacionContrasena(
				UUID.randomUUID().toString(),
				usuario.get().id(),
				hash(token),
				Instant.now().plusSeconds(MINUTOS_VIGENCIA * 60),
				false);
		recuperacionDAO.guardar(recuperacion);
		auditoriaService.registrar(usuario.get().id(), "SOLICITAR_RECUPERACION", "USUARIO",
				usuario.get().id(), null, null, ip);

		// En produccion este token viaja por correo; en el entorno local se retorna para pruebas.
		return Map.of(
				"mensaje", "Enlace de recuperacion generado. En produccion se envia por correo.",
				"token", token,
				"expira_en", recuperacion.expiraEn().toString());
	}

	public Map<String, String> confirmar(RecuperacionConfirmRequest request, String ip) {
		var recuperacion = recuperacionDAO.buscarVigente(hash(request.token()), Instant.now())
				.orElseThrow(RecuperacionInvalidaException::new);
		Usuario usuario = usuarioDAO.buscarPorId(recuperacion.usuarioId())
				.orElseThrow(RecuperacionInvalidaException::new);
		Usuario actualizado = usuario.conPasswordHash(passwordEncoder.encode(request.nuevaContrasena()));
		usuarioDAO.actualizar(actualizado);
		recuperacionDAO.marcarUsado(recuperacion.id());
		auditoriaService.registrar(usuario.id(), "CONFIRMAR_RECUPERACION", "USUARIO",
				usuario.id(), null, null, ip);
		return Map.of("mensaje", "Contrasena actualizada correctamente.");
	}

	private static String hash(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 no disponible.", exception);
		}
	}
}
