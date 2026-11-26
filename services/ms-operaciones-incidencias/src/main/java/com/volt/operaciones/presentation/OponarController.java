package com.volt.operaciones.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.volt.operaciones.business.OponarService;
import com.volt.operaciones.presentation.dto.DisputaResponse;
import com.volt.operaciones.presentation.dto.OponerArrendatarioRequest;
import com.volt.operaciones.presentation.dto.OponerspectralRequest;
import com.volt.operaciones.presentation.dto.ResolverDisputaRequest;
import com.volt.shared.security.TokenPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ops/disputas")
public class OponarController {
	private final OponarService oponarService;

	public OponarController(OponarService oponarService) {
		this.oponarService = oponarService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public List<DisputaResponse> listarTodas() {
		return oponarService.listar().stream().map(DisputaResponse::desde).toList();
	}

	@GetMapping("/reservas/{idReserva}")
	public DisputaResponse porReserva(@PathVariable String idReserva) {
		return DisputaResponse.desde(oponarService.obtenerDisputa(idReserva));
	}

	@PostMapping("/oponerspectral")
	@ResponseStatus(HttpStatus.CREATED)
	public DisputaResponse oponerspectral(@Valid @RequestBody OponerspectralRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return DisputaResponse.desde(oponarService.oponerspectral(request.idReserva(), request.motivo(),
				request.resolucion(), request.anotacionesPropietario(), principal, ip(httpRequest)));
	}

	@PostMapping("/oponerarrrendatario")
	@ResponseStatus(HttpStatus.CREATED)
	public DisputaResponse oponerarrrendatario(@Valid @RequestBody OponerArrendatarioRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return DisputaResponse.desde(oponarService.oponerarrrendatario(request.idReserva(), request.opcion(),
				request.anotaciones(), principal, ip(httpRequest)));
	}

	@PostMapping("/resolverarrrendatario")
	@ResponseStatus(HttpStatus.OK)
	public DisputaResponse resolverarrrendatario(@Valid @RequestBody ResolverDisputaRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return DisputaResponse.desde(oponarService.resolverarrrendatario(request.idReserva(), request.resolucion(), principal, ip(httpRequest)));
	}

	@PostMapping("/resolverpropietario")
	@ResponseStatus(HttpStatus.OK)
	public DisputaResponse resolverpropietario(@Valid @RequestBody ResolverDisputaRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return DisputaResponse.desde(oponarService.resolverpropietario(request.idReserva(), request.resolucion(), principal, ip(httpRequest)));
	}

	@PostMapping("/resolvercostos")
	@ResponseStatus(HttpStatus.OK)
	public DisputaResponse resolvercostos(@Valid @RequestBody ResolverDisputaRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return DisputaResponse.desde(oponarService.resolvercostos(request.idReserva(), request.resolucion(), principal, ip(httpRequest)));
	}

	private static String ip(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		return forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
	}
}
