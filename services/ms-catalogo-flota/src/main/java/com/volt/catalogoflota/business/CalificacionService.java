package com.volt.catalogoflota.business;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.volt.catalogoflota.client.ReservaInfo;
import com.volt.catalogoflota.client.ReservasClient;
import com.volt.catalogoflota.client.UsuariosClient;
import com.volt.catalogoflota.document.CalificacionDocument;
import com.volt.catalogoflota.document.PatinetaDocument;
import com.volt.catalogoflota.persistence.ICalificacionDAO;
import com.volt.catalogoflota.persistence.IPatinetaDAO;
import com.volt.catalogoflota.view.dto.CalificacionRequest;
import com.volt.catalogoflota.view.dto.CalificacionResponse;
import com.volt.catalogoflota.view.dto.ModeracionCalificacionRequest;
import com.volt.catalogoflota.view.dto.ReplicaCalificacionRequest;
import com.volt.catalogoflota.view.dto.ReporteCalificacionRequest;
import com.volt.shared.audit.AuditoriaService;
import com.volt.shared.security.TokenPrincipal;

@Service
public class CalificacionService {
	private final ICalificacionDAO calificacionDAO;
	private final IPatinetaDAO patinetaDAO;
	private final ReservasClient reservasClient;
	private final UsuariosClient usuariosClient;
	private final AuditoriaService auditoriaService;

	public CalificacionService(
			ICalificacionDAO calificacionDAO,
			IPatinetaDAO patinetaDAO,
			ReservasClient reservasClient,
			UsuariosClient usuariosClient,
			AuditoriaService auditoriaService) {
		this.calificacionDAO = calificacionDAO;
		this.patinetaDAO = patinetaDAO;
		this.reservasClient = reservasClient;
		this.usuariosClient = usuariosClient;
		this.auditoriaService = auditoriaService;
	}

	public CalificacionResponse calificar(String patinetaId, CalificacionRequest request,
			TokenPrincipal principal, String ip) {
		PatinetaDocument patineta = patinetaDAO.buscarPorId(patinetaId)
				.orElseThrow(() -> new PatinetaNoEncontradaException(patinetaId));
		ReservaInfo reserva = reservasClient.obtenerReserva(request.reservaId());
		if (!patinetaId.equals(reserva.idPatineta())) {
			throw new IllegalArgumentException("La reserva no corresponde a la patineta indicada.");
		}
		if (!principal.userId().equals(reserva.idArrendatario())) {
			throw new AccesoDenegadoException("Solo el arrendatario de la reserva puede calificarla.");
		}
		if (!"FINALIZADA".equals(reserva.estado())) {
			throw new IllegalArgumentException("Solo se puede calificar una reserva finalizada.");
		}
		Instant checkout = reserva.fechaCheckout() == null ? reserva.fechaFin() : reserva.fechaCheckout();
		if (checkout == null || Instant.now().isAfter(checkout.plus(Duration.ofHours(72)))) {
			throw new IllegalArgumentException("El plazo de 72 horas para calificar ya vencio.");
		}
		boolean duplicada = calificacionDAO.listarPorPatineta(patinetaId, true).stream()
				.anyMatch(calificacion -> request.reservaId().equals(calificacion.reservaId())
						&& principal.userId().equals(calificacion.emisorId()));
		if (duplicada) {
			throw new IllegalArgumentException("Ya calificaste esta reserva.");
		}

		CalificacionDocument creada = calificacionDAO.guardar(new CalificacionDocument(
				UUID.randomUUID().toString(), patinetaId, patineta.idPropietario(), request.reservaId(),
				principal.userId(), request.puntaje(), request.comentario(), null, false, null, false,
				Instant.now()));
		recalcular(patinetaId);
		auditoriaService.registrar(principal.userId(), "CALIFICAR_RESERVA", "CALIFICACION", creada.id(),
				null, java.util.Map.of("puntaje", request.puntaje(), "patineta", patinetaId), ip);
		return CalificacionResponse.desde(creada);
	}

	public List<CalificacionResponse> listar(String patinetaId) {
		return calificacionDAO.listarPorPatineta(patinetaId, false).stream()
				.map(CalificacionResponse::desde).toList();
	}

	public CalificacionResponse replicar(String id, ReplicaCalificacionRequest request,
			TokenPrincipal principal, String ip) {
		CalificacionDocument actual = obtener(id);
		if (!principal.userId().equals(actual.propietarioId()) && !principal.esAdministrador()) {
			throw new AccesoDenegadoException("Solo el propietario puede responder esta calificacion.");
		}
		CalificacionDocument actualizada = new CalificacionDocument(actual.id(), actual.patinetaId(),
				actual.propietarioId(), actual.reservaId(), actual.emisorId(), actual.puntaje(),
				actual.comentario(), request.replica().trim(), actual.reportada(), actual.motivoReporte(),
				actual.oculta(), actual.fechaRegistro());
		calificacionDAO.actualizar(actualizada);
		auditoriaService.registrar(principal.userId(), "REPLICAR_CALIFICACION", "CALIFICACION", id,
				null, request.replica(), ip);
		return CalificacionResponse.desde(actualizada);
	}

	public CalificacionResponse reportar(String id, ReporteCalificacionRequest request,
			TokenPrincipal principal, String ip) {
		CalificacionDocument actual = obtener(id);
		CalificacionDocument actualizada = new CalificacionDocument(actual.id(), actual.patinetaId(),
				actual.propietarioId(), actual.reservaId(), actual.emisorId(), actual.puntaje(),
				actual.comentario(), actual.replica(), true, request.motivo(), actual.oculta(),
				actual.fechaRegistro());
		calificacionDAO.actualizar(actualizada);
		auditoriaService.registrar(principal.userId(), "REPORTAR_CALIFICACION", "CALIFICACION", id,
				null, request.motivo(), ip);
		return CalificacionResponse.desde(actualizada);
	}

	public CalificacionResponse moderar(String id, ModeracionCalificacionRequest request,
			String adminId, String ip) {
		CalificacionDocument actual = obtener(id);
		CalificacionDocument actualizada = new CalificacionDocument(actual.id(), actual.patinetaId(),
				actual.propietarioId(), actual.reservaId(), actual.emisorId(), actual.puntaje(),
				actual.comentario(), actual.replica(), actual.reportada(), actual.motivoReporte(),
				Boolean.TRUE.equals(request.oculta()), actual.fechaRegistro());
		calificacionDAO.actualizar(actualizada);
		recalcular(actual.patinetaId());
		auditoriaService.registrar(adminId, "MODERAR_CALIFICACION", "CALIFICACION", id,
				actual.oculta(), actualizada.oculta(), ip);
		return CalificacionResponse.desde(actualizada);
	}

	private void recalcular(String patinetaId) {
		List<CalificacionDocument> visibles = calificacionDAO.listarPorPatineta(patinetaId, false);
		double promedioCalculado = visibles.stream().mapToInt(CalificacionDocument::puntaje).average().orElse(0.0);
		double promedio = Math.round(promedioCalculado * 100.0) / 100.0;
		patinetaDAO.actualizarCalificacion(patinetaId, promedio, visibles.size());
		patinetaDAO.buscarPorId(patinetaId).ifPresent(patineta ->
				usuariosClient.actualizarReputacion(patineta.idPropietario(), promedio, visibles.size()));
	}

	private CalificacionDocument obtener(String id) {
		return calificacionDAO.buscarPorId(id)
				.orElseThrow(() -> new IllegalArgumentException("La calificacion no existe."));
	}
}
