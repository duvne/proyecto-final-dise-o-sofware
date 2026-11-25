package com.volt.catalogoflota.presentation;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.volt.catalogoflota.business.CalificacionService;
import com.volt.catalogoflota.view.dto.CalificacionResponse;
import com.volt.catalogoflota.view.dto.ReplicaCalificacionRequest;
import com.volt.catalogoflota.view.dto.ReporteCalificacionRequest;
import com.volt.shared.security.TokenPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/catalogo/calificaciones")
public class CalificacionController {
	private final CalificacionService calificacionService;

	public CalificacionController(CalificacionService calificacionService) {
		this.calificacionService = calificacionService;
	}

	@PostMapping("/{id}/replica")
	public CalificacionResponse replicar(@PathVariable String id,
			@Valid @RequestBody ReplicaCalificacionRequest request,
			@org.springframework.security.core.annotation.AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		return calificacionService.replicar(id, request, principal, ip(httpRequest));
	}

	@PostMapping("/{id}/reportar")
	public CalificacionResponse reportar(@PathVariable String id,
			@Valid @RequestBody ReporteCalificacionRequest request,
			@org.springframework.security.core.annotation.AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		return calificacionService.reportar(id, request, principal, ip(httpRequest));
	}

	private static String ip(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		return forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
	}
}
