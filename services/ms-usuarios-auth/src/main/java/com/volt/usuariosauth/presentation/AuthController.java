package com.volt.usuariosauth.presentation;

import java.net.URI;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.volt.shared.security.TokenPrincipal;
import com.volt.usuariosauth.business.RecuperacionService;
import com.volt.usuariosauth.business.UsuarioService;
import com.volt.usuariosauth.view.ActualizarPerfilRequest;
import com.volt.usuariosauth.view.AuthResponse;
import com.volt.usuariosauth.view.ConsentimientoRequest;
import com.volt.usuariosauth.view.LoginRequest;
import com.volt.usuariosauth.view.PerfilPropietarioRequest;
import com.volt.usuariosauth.view.RecuperacionConfirmRequest;
import com.volt.usuariosauth.view.RecuperacionRequest;
import com.volt.usuariosauth.view.RegistroUsuarioRequest;
import com.volt.usuariosauth.view.UsuarioResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final UsuarioService usuarioService;
	private final RecuperacionService recuperacionService;

	public AuthController(UsuarioService usuarioService, RecuperacionService recuperacionService) {
		this.usuarioService = usuarioService;
		this.recuperacionService = recuperacionService;
	}

	@PostMapping("/registro")
	public ResponseEntity<AuthResponse> registrar(
			@Valid @RequestBody RegistroUsuarioRequest request,
			HttpServletRequest httpRequest) {
		AuthResponse response = usuarioService.registrar(request, ip(httpRequest));
		return ResponseEntity.created(URI.create("/api/auth/usuarios/" + response.userId())).body(response);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
		return usuarioService.login(request, ip(httpRequest));
	}

	@PostMapping("/recuperacion")
	public Map<String, Object> solicitarRecuperacion(
			@Valid @RequestBody RecuperacionRequest request,
			HttpServletRequest httpRequest) {
		return recuperacionService.solicitar(request, ip(httpRequest));
	}

	@PostMapping("/recuperacion/confirmar")
	public Map<String, String> confirmarRecuperacion(
			@Valid @RequestBody RecuperacionConfirmRequest request,
			HttpServletRequest httpRequest) {
		return recuperacionService.confirmar(request, ip(httpRequest));
	}

	@GetMapping("/perfil")
	public UsuarioResponse perfil(@AuthenticationPrincipal TokenPrincipal principal) {
		return usuarioService.perfil(principal.userId());
	}

	@PutMapping("/perfil")
	public UsuarioResponse actualizarPerfil(
			@AuthenticationPrincipal TokenPrincipal principal,
			@Valid @RequestBody ActualizarPerfilRequest request,
			HttpServletRequest httpRequest) {
		return usuarioService.actualizarPerfil(principal.userId(), request, ip(httpRequest));
	}

	@PostMapping("/perfil/propietario")
	public UsuarioResponse activarPropietario(
			@AuthenticationPrincipal TokenPrincipal principal,
			@Valid @RequestBody PerfilPropietarioRequest request,
			HttpServletRequest httpRequest) {
		return usuarioService.activarPropietario(principal.userId(), request, ip(httpRequest));
	}

	@PostMapping("/consentimientos")
	public UsuarioResponse registrarConsentimiento(
			@AuthenticationPrincipal TokenPrincipal principal,
			@Valid @RequestBody ConsentimientoRequest request,
			HttpServletRequest httpRequest) {
		return usuarioService.registrarConsentimiento(principal.userId(), request, ip(httpRequest));
	}

	@DeleteMapping("/cuenta")
	public ResponseEntity<Void> eliminarCuenta(
			@AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		usuarioService.eliminarCuenta(principal.userId(), ip(httpRequest));
		return ResponseEntity.noContent().build();
	}

	private static String ip(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
