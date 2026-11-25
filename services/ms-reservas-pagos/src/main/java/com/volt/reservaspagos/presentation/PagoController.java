package com.volt.reservaspagos.presentation;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.volt.reservaspagos.model.service.GestorReservas;
import com.volt.reservaspagos.view.dto.ContraCargoRequest;
import com.volt.reservaspagos.view.dto.ContraCargoResponse;
import com.volt.reservaspagos.view.dto.EstadoCuentaResponse;
import com.volt.reservaspagos.view.dto.ReportePagoResponse;
import com.volt.shared.security.TokenPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {
	private final GestorReservas gestorReservas;

	public PagoController(GestorReservas gestorReservas) {
		this.gestorReservas = gestorReservas;
	}

	@GetMapping("/mi-estado-cuenta")
	public EstadoCuentaResponse estadoCuenta(@AuthenticationPrincipal TokenPrincipal principal) {
		return gestorReservas.estadoCuenta(principal.userId());
	}

	@GetMapping("/liquidaciones")
	public java.util.List<com.volt.reservaspagos.view.dto.ReservaResponse> liquidaciones(
			@AuthenticationPrincipal TokenPrincipal principal) {
		return gestorReservas.liquidaciones(principal.userId());
	}

	@PostMapping("/liquidaciones/{id}/liberar")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public com.volt.reservaspagos.view.dto.ReservaResponse liberarPago(@PathVariable String id,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return gestorReservas.liberarPago(id, principal.userId(), ip(httpRequest));
	}

	@GetMapping("/reportes")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ReportePagoResponse reporte(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.Instant desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.Instant hasta) {
		return gestorReservas.reporte(desde, hasta);
	}

	@GetMapping("/contracargos")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public java.util.List<ContraCargoResponse> contracargos() {
		return gestorReservas.listarContraCargos().stream()
				.map(ContraCargoResponse::desde)
				.toList();
	}

	@PostMapping("/contracargos")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@ResponseStatus(HttpStatus.CREATED)
	public ContraCargoResponse registrarContraCargo(@Valid @RequestBody ContraCargoRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		var creado = gestorReservas.registrarContraCargo(request.idReserva(), request.idArrendatario(),
				request.monto(), request.motivo(), principal.userId(), ip(httpRequest));
		return ContraCargoResponse.desde(creado);
	}

	private static String ip(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		return forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
	}
}
