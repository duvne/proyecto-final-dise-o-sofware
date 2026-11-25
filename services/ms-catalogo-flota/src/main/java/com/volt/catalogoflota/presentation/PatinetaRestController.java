package com.volt.catalogoflota.presentation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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
import com.volt.catalogoflota.persistence.FiltroPatinetas;
import com.volt.catalogoflota.view.dto.CalificacionRequest;
import com.volt.catalogoflota.view.dto.CalificacionResponse;
import com.volt.catalogoflota.view.dto.PatinetaPageResponseDTO;
import com.volt.catalogoflota.view.dto.PatinetaResponseDTO;
import com.volt.shared.security.TokenPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/catalogo/patinetas")
public class PatinetaRestController {
	private final PatinetaService patinetaService;
	private final CalificacionService calificacionService;

	public PatinetaRestController(PatinetaService patinetaService, CalificacionService calificacionService) {
		this.patinetaService = patinetaService;
		this.calificacionService = calificacionService;
	}

	@GetMapping
	public List<PatinetaResponseDTO> listarActivas(@RequestParam(required = false) String categoria) {
		return patinetaService.listarActivas(categoria);
	}

	@GetMapping("/busqueda")
	public PatinetaPageResponseDTO buscarActivas(
			@RequestParam(required = false) String categoria,
			@RequestParam(required = false) String ciudad,
			@RequestParam(required = false) BigDecimal precioHoraMaximo,
			@RequestParam(required = false) Double autonomiaMinimaKm,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fin,
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamano) {
		FiltroPatinetas filtros = new FiltroPatinetas(categoria, ciudad, precioHoraMaximo,
				autonomiaMinimaKm, pagina, tamano);
		return patinetaService.buscarActivas(filtros, inicio, fin);
	}

	@GetMapping("/{id}")
	public PatinetaResponseDTO obtenerActiva(@PathVariable String id) {
		return patinetaService.obtenerActiva(id);
	}

	@GetMapping("/{id}/calificaciones")
	public List<CalificacionResponse> calificaciones(@PathVariable String id) {
		return calificacionService.listar(id);
	}

	@PostMapping("/{id}/calificaciones")
	public CalificacionResponse calificar(
			@PathVariable String id,
			@Valid @RequestBody CalificacionRequest request,
			@AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		return calificacionService.calificar(id, request, principal, ip(httpRequest));
	}

	private static String ip(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		return forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
	}
}
