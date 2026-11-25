package com.volt.catalogoflota.presentation;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volt.catalogoflota.business.CalificacionService;
import com.volt.catalogoflota.business.PatinetaService;
import com.volt.catalogoflota.view.dto.CalificacionResponse;
import com.volt.catalogoflota.view.dto.ModeracionCalificacionRequest;
import com.volt.catalogoflota.view.dto.ModeracionPatinetaRequest;
import com.volt.catalogoflota.view.dto.PatinetaPageResponseDTO;
import com.volt.catalogoflota.view.dto.PatinetaResponseDTO;
import com.volt.shared.security.TokenPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/catalogo/admin")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminCatalogoController {
	private final PatinetaService patinetaService;
	private final CalificacionService calificacionService;

	public AdminCatalogoController(PatinetaService patinetaService, CalificacionService calificacionService) {
		this.patinetaService = patinetaService;
		this.calificacionService = calificacionService;
	}

	@GetMapping("/patinetas/pendientes")
	public PatinetaPageResponseDTO pendientes(
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamano) {
		return patinetaService.listarPendientes(pagina, tamano);
	}

	@PostMapping("/patinetas/{id}/moderacion")
	public PatinetaResponseDTO moderar(
			@PathVariable String id,
			@Valid @RequestBody ModeracionPatinetaRequest request,
			@AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		return patinetaService.moderar(id, request, principal.userId(), ip(httpRequest));
	}

	@PostMapping("/patinetas/{id}/punto/verificar")
	public PatinetaResponseDTO verificarPunto(
			@PathVariable String id,
			@AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		return patinetaService.verificarPunto(id, principal.userId(), ip(httpRequest));
	}

	@PostMapping("/calificaciones/{id}/moderacion")
	public CalificacionResponse moderarCalificacion(
			@PathVariable String id,
			@Valid @RequestBody ModeracionCalificacionRequest request,
			@AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		return calificacionService.moderar(id, request, principal.userId(), ip(httpRequest));
	}

	private static String ip(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		return forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
	}
}
