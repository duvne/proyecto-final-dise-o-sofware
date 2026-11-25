package com.volt.operaciones.business;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.volt.operaciones.domain.ActaDocument;
import com.volt.operaciones.domain.IncidenciaDocument;
import com.volt.operaciones.persistence.IActaDAO;
import com.volt.operaciones.persistence.IIncidenciaDAO;
import com.volt.shared.audit.AuditoriaService;
import com.volt.shared.security.TokenPrincipal;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class OpsService {
	private final IActaDAO actaDAO;
	private final IIncidenciaDAO incidenciaDAO;
	private final AuditoriaService auditoriaService;

	public OpsService(IActaDAO actaDAO, IIncidenciaDAO incidenciaDAO, AuditoriaService auditoriaService) {
		this.actaDAO = actaDAO;
		this.incidenciaDAO = incidenciaDAO;
		this.auditoriaService = auditoriaService;
	}

	public ActaDocument createActa(String idReserva, List<String> incidenciasIds, String descripcion,
			String notasMejoras, List<String> fotoEvidencias, String observaciones, String estado,
			String actorId, HttpServletRequest httpServletRequest) {
		ActaDocument acta = new ActaDocument(java.util.UUID.randomUUID().toString(), idReserva,
				List.copyOf(incidenciasIds), descripcion, notasMejoras, List.copyOf(fotoEvidencias),
				observaciones, estado, java.time.Instant.now(), null);
		ActaDocument guardada = actaDAO.guardar(acta);
		auditoriaService.registrar(actorId, "CREAR_ACTA", "ACTA", guardada.id(), null, Map.of(
				"id_reserva", idReserva, "estado", estado), ip(httpServletRequest));
		return guardada;
	}

	public ActaDocument setActaResolucion(String idActa, String resolucion, String estado, String actorId,
			HttpServletRequest httpServletRequest) {
		ActaDocument acta = actaDAO.buscarPorId(idActa)
				.orElseThrow(() -> new IllegalArgumentException("Acta no encontrada."));
		ActaDocument actualizada = new ActaDocument(acta.id(), acta.idReserva(), acta.incidenciasIds(),
				acta.descripcion(), acta.notasMejoras(), acta.fotoEvidencias(),
				acta.observaciones() + "\n" + resolucion, estado, acta.fechaCreacion(), java.time.Instant.now());
		ActaDocument guardada = actaDAO.buscarPorId(acta.id()).map(current -> actaDAO.guardar(actualizada)).orElse(actaDAO.guardar(actualizada));
		auditoriaService.registrar(actorId, "RESOLVER_ACTA", "ACTA", guardada.id(), acta.estado(),
				estado, ip(httpServletRequest));
		return guardada;
	}

	public ActaDocument buscarActaPorReserva(String idReserva) {
		return actaDAO.buscarPorReserva(idReserva)
				.orElseThrow(() -> new IllegalArgumentException("No existe acta para esa reserva."));
	}

	public List<IncidenciaDocument> incidenciasPorReserva(String idReserva) {
		return incidenciaDAO.listarPorReserva(idReserva);
	}

	public List<ActaDocument> listActas() {
		return actaDAO.listar();
	}

	public static String ip(HttpServletRequest request) {
		String forwarded = request != null ? request.getHeader("X-Forwarded-For") : null;
		return forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : (request == null ? "SISTEMA" : request.getRemoteAddr());
	}
}
