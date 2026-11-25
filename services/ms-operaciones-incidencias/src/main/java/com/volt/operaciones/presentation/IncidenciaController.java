package com.volt.operaciones.presentation;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.volt.operaciones.business.OpsService;
import com.volt.operaciones.presentation.dto.IncidenciaResponse;

@RestController
@RequestMapping("/api/ops/incidencias")
public class IncidenciaController {
	private final OpsService opsService;

	public IncidenciaController(OpsService opsService) {
		this.opsService = opsService;
	}

	@GetMapping("/reservas/{idReserva}")
	public List<IncidenciaResponse> porReserva(@PathVariable String idReserva) {
		return opsService.incidenciasPorReserva(idReserva).stream().map(IncidenciaResponse::desde).toList();
	}
}
