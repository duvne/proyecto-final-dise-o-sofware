package com.volt.usuariosauth.presentation;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volt.shared.api.PageResponse;
import com.volt.shared.security.TokenPrincipal;
import com.volt.usuariosauth.business.UsuarioService;
import com.volt.usuariosauth.domain.EstadoCuenta;
import com.volt.usuariosauth.view.AuditoriaResponse;
import com.volt.usuariosauth.view.SuspensionRequest;
import com.volt.usuariosauth.view.UsuarioResponse;
import com.volt.usuariosauth.view.VerificacionRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminUsuarioController {
	private final UsuarioService usuarioService;

	public AdminUsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping("/usuarios")
	public PageResponse<UsuarioResponse> listar(
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamano,
			@RequestParam(required = false) EstadoCuenta estado) {
		return usuarioService.listar(pagina, tamano, estado);
	}

	@GetMapping("/usuarios/{id}")
	public UsuarioResponse obtener(@PathVariable String id) {
		return usuarioService.obtener(id);
	}

	@PostMapping("/usuarios/{id}/verificacion")
	public UsuarioResponse verificar(
			@AuthenticationPrincipal TokenPrincipal principal,
			@PathVariable String id,
			@Valid @RequestBody VerificacionRequest request,
			HttpServletRequest httpRequest) {
		return usuarioService.verificar(principal.userId(), id,
				Boolean.TRUE.equals(request.aprobado()), request.motivo(), ip(httpRequest));
	}

	@PostMapping("/usuarios/{id}/suspension")
	public UsuarioResponse suspender(
			@AuthenticationPrincipal TokenPrincipal principal,
			@PathVariable String id,
			@Valid @RequestBody SuspensionRequest request,
			HttpServletRequest httpRequest) {
		return usuarioService.suspender(principal.userId(), id, request.motivo(), ip(httpRequest));
	}

	@PostMapping("/usuarios/{id}/reactivacion")
	public UsuarioResponse reactivar(
			@AuthenticationPrincipal TokenPrincipal principal,
			@PathVariable String id,
			HttpServletRequest httpRequest) {
		return usuarioService.reactivar(principal.userId(), id, ip(httpRequest));
	}

	@DeleteMapping("/usuarios/{id}")
	public void eliminar(
			@AuthenticationPrincipal TokenPrincipal principal,
			@PathVariable String id,
			HttpServletRequest httpRequest) {
		usuarioService.eliminarCuenta(id, ip(httpRequest));
	}

	@GetMapping("/auditoria")
	public PageResponse<AuditoriaResponse> auditoria(
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "50") int tamano) {
		var page = usuarioService.auditoria(pagina, tamano);
		return PageResponse.de(page.elementos().stream().map(AuditoriaResponse::desde).toList(),
				page.total(), page.pagina(), page.tamano());
	}

	private static String ip(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
