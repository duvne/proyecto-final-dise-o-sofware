package com.volt.catalogoflota.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.volt.catalogoflota.business.ZonaService;
import com.volt.catalogoflota.view.dto.ZonaRequest;
import com.volt.catalogoflota.view.dto.ZonaResponse;
import com.volt.shared.security.TokenPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/catalogo/zonas")
public class ZonaController {
	private final ZonaService zonaService;

	public ZonaController(ZonaService zonaService) {
		this.zonaService = zonaService;
	}

	@GetMapping
	public List<ZonaResponse> listar(@RequestParam(defaultValue = "true") boolean soloActivas) {
		return zonaService.listar(soloActivas);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@ResponseStatus(HttpStatus.CREATED)
	public ZonaResponse crear(@Valid @RequestBody ZonaRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return zonaService.crear(request, principal.userId(), ip(httpRequest));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ZonaResponse actualizar(@PathVariable String id, @Valid @RequestBody ZonaRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return zonaService.actualizar(id, request, principal.userId(), ip(httpRequest));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable String id, @AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		zonaService.eliminar(id, principal.userId(), ip(httpRequest));
	}

	private static String ip(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		return forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
	}
}
