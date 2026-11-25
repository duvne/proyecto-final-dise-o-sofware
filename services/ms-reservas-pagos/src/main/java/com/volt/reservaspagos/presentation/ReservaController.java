package com.volt.reservaspagos.presentation;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volt.reservaspagos.model.domain.EstadoReserva;
import com.volt.reservaspagos.model.service.GestorReservas;
import com.volt.reservaspagos.view.dto.CancelarRequest;
import com.volt.reservaspagos.view.dto.ExtensionRequest;
import com.volt.reservaspagos.view.dto.FinalizarRequest;
import com.volt.reservaspagos.view.dto.ReprogramarRequest;
import com.volt.reservaspagos.view.dto.ReservaRequest;
import com.volt.reservaspagos.view.dto.ReservaResponse;
import com.volt.shared.api.PageResponse;
import com.volt.shared.security.TokenPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {
	private final GestorReservas gestorReservas;

	public ReservaController(GestorReservas gestorReservas) {
		this.gestorReservas = gestorReservas;
	}

	@PostMapping
	public ReservaResponse crear(@Valid @RequestBody ReservaRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return gestorReservas.crear(request, principal, ip(httpRequest));
	}

	@GetMapping("/mias")
	public PageResponse<ReservaResponse> misReservas(
			@RequestParam(required = false) EstadoReserva estado,
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamano,
			@AuthenticationPrincipal TokenPrincipal principal) {
		return gestorReservas.listarMias(principal, estado, pagina, tamano);
	}

	@GetMapping("/{id}")
	public ReservaResponse obtener(@PathVariable String id,
			@AuthenticationPrincipal TokenPrincipal principal) {
		return gestorReservas.obtenerParaUsuario(id, principal);
	}

	@GetMapping("/admin/todas")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public PageResponse<ReservaResponse> adminTodas(
			@RequestParam(required = false) EstadoReserva estado,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamano) {
		return gestorReservas.listarAdmin(estado, desde, hasta, pagina, tamano);
	}

	@PostMapping("/{id}/aprobar")
	public ReservaResponse aprobar(@PathVariable String id,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return gestorReservas.aprobar(id, principal, ip(httpRequest));
	}

	@PostMapping("/{id}/rechazar")
	public ReservaResponse rechazar(@PathVariable String id, @Valid @RequestBody(required = false) CancelarRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return gestorReservas.rechazar(id, request == null ? null : request.motivo(), principal, ip(httpRequest));
	}

	@PostMapping("/{id}/cancelar")
	public ReservaResponse cancelar(@PathVariable String id, @Valid @RequestBody(required = false) CancelarRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return gestorReservas.cancelar(id, request == null ? new CancelarRequest(null) : request,
				principal, ip(httpRequest));
	}

	@PostMapping("/{id}/fechas")
	public ReservaResponse reprogramar(@PathVariable String id, @Valid @RequestBody ReprogramarRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return gestorReservas.reprogramar(id, request, principal, ip(httpRequest));
	}

	@PostMapping("/{id}/extension")
	public ReservaResponse extender(@PathVariable String id, @Valid @RequestBody ExtensionRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return gestorReservas.extender(id, request.fechaFin(), principal, ip(httpRequest));
	}

	@PostMapping("/{id}/inicio")
	public ReservaResponse iniciar(@PathVariable String id,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return gestorReservas.iniciar(id, principal.userId(), ip(httpRequest));
	}

	@PostMapping("/{id}/fin")
	public ReservaResponse finalizar(@PathVariable String id, @Valid @RequestBody FinalizarRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return gestorReservas.finalizar(id, request, principal.userId(), ip(httpRequest));
	}

	private static String ip(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		return forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
	}
}
