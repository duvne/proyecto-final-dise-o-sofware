package com.volt.reservaspagos.presentation;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volt.reservaspagos.model.service.GestorReservas;
import com.volt.reservaspagos.view.dto.FranjaResponse;

@RestController
@RequestMapping("/api/reservas/disponibilidad")
public class DisponibilidadController {
	private final GestorReservas gestorReservas;

	public DisponibilidadController(GestorReservas gestorReservas) {
		this.gestorReservas = gestorReservas;
	}

	@GetMapping("/ocupadas")
	public Map<String, List<String>> ocupadas(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fin) {
		return Map.of("patinetas_ocupadas", gestorReservas.patinetasOcupadas(inicio, fin));
	}

	@GetMapping("/{patinetaId}")
	public List<FranjaResponse> calendario(@PathVariable String patinetaId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta) {
		return gestorReservas.calendario(patinetaId, desde, hasta);
	}
}
