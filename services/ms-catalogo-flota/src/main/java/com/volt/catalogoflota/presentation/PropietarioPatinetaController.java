package com.volt.catalogoflota.presentation;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.volt.catalogoflota.business.PatinetaService;
import com.volt.catalogoflota.view.dto.MantenimientoRequest;
import com.volt.catalogoflota.view.dto.MantenimientoResponse;
import com.volt.catalogoflota.view.dto.PatinetaRequestDTO;
import com.volt.catalogoflota.view.dto.PatinetaResponseDTO;
import com.volt.catalogoflota.view.dto.UrlsRequest;
import com.volt.shared.security.TokenPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/catalogo/mis-patinetas")
@PreAuthorize("hasRole('PROPIETARIO')")
public class PropietarioPatinetaController {
	private final PatinetaService patinetaService;

	public PropietarioPatinetaController(PatinetaService patinetaService) {
		this.patinetaService = patinetaService;
	}

	@PostMapping
	public ResponseEntity<PatinetaResponseDTO> crear(
			@Valid @RequestBody PatinetaRequestDTO request,
			@AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		PatinetaResponseDTO creada = patinetaService.crearBorrador(request, principal, ip(httpRequest));
		return ResponseEntity.created(URI.create("/api/catalogo/mis-patinetas/" + creada.id())).body(creada);
	}

	@GetMapping
	public List<PatinetaResponseDTO> listar(@AuthenticationPrincipal TokenPrincipal principal) {
		return patinetaService.listarPorPropietario(principal);
	}

	@GetMapping("/{id}")
	public PatinetaResponseDTO obtener(@PathVariable String id,
			@AuthenticationPrincipal TokenPrincipal principal) {
		return patinetaService.obtenerParaPropietario(id, principal);
	}

	@PutMapping("/{id}")
	public PatinetaResponseDTO actualizar(
			@PathVariable String id,
			@Valid @RequestBody PatinetaRequestDTO request,
			@AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		return patinetaService.actualizar(id, request, principal, ip(httpRequest));
	}

	@PostMapping("/{id}/publicacion")
	public PatinetaResponseDTO enviarARevision(@PathVariable String id,
			@AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		return patinetaService.enviarARevision(id, principal, ip(httpRequest));
	}

	@PostMapping("/{id}/pausa")
	public PatinetaResponseDTO pausar(@PathVariable String id,
			@AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		return patinetaService.pausar(id, principal, ip(httpRequest));
	}

	@PostMapping("/{id}/reactivacion")
	public PatinetaResponseDTO reactivar(@PathVariable String id,
			@AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		return patinetaService.reactivar(id, principal, ip(httpRequest));
	}

	@PostMapping("/{id}/retiro")
	public PatinetaResponseDTO retirar(@PathVariable String id,
			@AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		return patinetaService.retirar(id, principal, ip(httpRequest));
	}

	@PostMapping("/{id}/mantenimiento")
	public PatinetaResponseDTO iniciarMantenimiento(
			@PathVariable String id,
			@Valid @RequestBody MantenimientoRequest request,
			@AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		return patinetaService.marcarMantenimiento(id, request, principal, ip(httpRequest));
	}

	@PostMapping("/{id}/mantenimiento/fin")
	public PatinetaResponseDTO finalizarMantenimiento(@PathVariable String id,
			@AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		return patinetaService.finalizarMantenimiento(id, principal, ip(httpRequest));
	}

	@GetMapping("/{id}/mantenimiento")
	public List<MantenimientoResponse> mantenimiento(@PathVariable String id,
			@AuthenticationPrincipal TokenPrincipal principal) {
		return patinetaService.historialMantenimiento(id, principal);
	}

	@PostMapping("/{id}/fotos")
	public PatinetaResponseDTO agregarFotos(
			@PathVariable String id,
			@Valid @RequestBody UrlsRequest request,
			@AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		return patinetaService.agregarFotos(id, request.urls(), principal, ip(httpRequest));
	}

	@PostMapping("/{id}/documentos")
	public PatinetaResponseDTO agregarDocumentos(
			@PathVariable String id,
			@Valid @RequestBody UrlsRequest request,
			@AuthenticationPrincipal TokenPrincipal principal,
			HttpServletRequest httpRequest) {
		return patinetaService.agregarDocumentos(id, request.urls(), principal, ip(httpRequest));
	}

	private static String ip(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		return forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
	}
}
