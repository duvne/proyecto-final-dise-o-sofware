package com.volt.reservaspagos.presentation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.volt.reservaspagos.model.service.GestorReservas;
import com.volt.reservaspagos.view.dto.DisputaRequest;
import com.volt.reservaspagos.view.dto.FinalizarRequest;
import com.volt.reservaspagos.view.dto.ReservaInternaResponse;
import com.volt.reservaspagos.view.dto.ReservaResponse;
import com.volt.shared.security.InternalKey;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservas/interno")
public class InternoReservaController {
	private final GestorReservas gestorReservas;
	private final String internalKey;

	public InternoReservaController(GestorReservas gestorReservas,
			@Value("${volt.internal-key:volt-internal-dev-key}") String internalKey) {
		this.gestorReservas = gestorReservas;
		this.internalKey = internalKey;
	}

	@GetMapping("/{id}")
	public ReservaInternaResponse obtener(@PathVariable String id, HttpServletRequest request) {
		validar(request);
		var reserva = gestorReservas.obtenerInterno(id);
		return new ReservaInternaResponse(reserva.id(), reserva.idArrendatario(), reserva.idPatineta(),
				reserva.idPropietario(), reserva.estado(), reserva.fechaInicio(), reserva.fechaFin(),
				reserva.fechaCheckIn(), reserva.fechaCheckout());
	}

	@PostMapping("/{id}/inicio")
	public ReservaResponse iniciar(@PathVariable String id, @RequestParam(defaultValue = "SISTEMA") String actorId,
			HttpServletRequest request) {
		validar(request);
		return gestorReservas.iniciar(id, actorId, request.getRemoteAddr());
	}

	@PostMapping("/{id}/fin")
	public ReservaResponse finalizar(@PathVariable String id, @Valid @RequestBody FinalizarRequest body,
			@RequestParam(defaultValue = "SISTEMA") String actorId, HttpServletRequest request) {
		validar(request);
		return gestorReservas.finalizar(id, body, actorId, request.getRemoteAddr());
	}

	@PostMapping("/{id}/disputa")
	public ReservaResponse abrirDisputa(@PathVariable String id, @Valid @RequestBody DisputaRequest body,
			@RequestParam(defaultValue = "SISTEMA") String actorId, HttpServletRequest request) {
		validar(request);
		return gestorReservas.abrirDisputa(id, body.motivo(), actorId);
	}

	private void validar(HttpServletRequest request) {
		if (!InternalKey.valida(request, internalKey)) {
			throw new ClaveInternaInvalidaException();
		}
	}

	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	static class ClaveInternaInvalidaException extends RuntimeException {
		ClaveInternaInvalidaException() {
			super("Clave interna invalida.");
		}
	}
}
