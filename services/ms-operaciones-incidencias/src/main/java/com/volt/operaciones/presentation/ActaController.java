package com.volt.operaciones.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.volt.operaciones.business.OpsService;
import com.volt.operaciones.presentation.dto.ActaResponse;
import com.volt.operaciones.presentation.dto.CrearActaRequest;
import com.volt.shared.security.TokenPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ops/actas")
public class ActaController {
	private final OpsService opsService;

	public ActaController(OpsService opsService) {
		this.opsService = opsService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ActaResponse crear(@Valid @RequestBody CrearActaRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return ActaResponse.desde(opsService.createActa(request.idReserva(), request.incidenciasIds(),
				request.descripcion(), request.notasMejoras(), request.fotoEvidencias(), request.observaciones(),
				request.estado(), principal.userId(), httpRequest));
	}

	@PutMapping("/{id}/resolucion")
	public ActaResponse resolver(@PathVariable String id, @Valid @RequestBody CrearActaRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return ActaResponse.desde(opsService.setActaResolucion(id, request.resolucion(), request.estado(),
				principal.userId(), httpRequest));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public List<ActaResponse> listar() {
		return opsService.listActas().stream().map(ActaResponse::desde).toList();
	}
}
