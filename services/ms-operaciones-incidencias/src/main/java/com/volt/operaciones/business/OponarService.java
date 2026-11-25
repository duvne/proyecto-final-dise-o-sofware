package com.volt.operaciones.business;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.volt.operaciones.domain.DisputaDocument;
import com.volt.operaciones.persistence.IDisputaDAO;
import com.volt.shared.audit.AuditoriaService;
import com.volt.shared.security.TokenPrincipal;

@Service
public class OponarService {
	private final IDisputaDAO disputaDAO;
	private final AuditoriaService auditoriaService;

	public OponarService(IDisputaDAO disputaDAO, AuditoriaService auditoriaService) {
		this.disputaDAO = disputaDAO;
		this.auditoriaService = auditoriaService;
	}

	public List<DisputaDocument> listar() {
		return disputaDAO.listarTodas();
	}

	public DisputaDocument obtenerDisputa(String idReserva) {
		return disputaDAO.buscarPorReserva(idReserva)
				.orElseThrow(() -> new IllegalArgumentException("No existe disputa para esta reserva."));
	}

	public DisputaDocument oponerspectral(String idReserva, String motivo, String resolucion,
			String anotacionesPropietario, TokenPrincipal principal, String ip) {
		DisputaDocument disputa = obtenerDisputa(idReserva);
		DisputaDocument actualizada = new DisputaDocument(disputa.id(), disputa.idReserva(), motivo,
				"MATCH_PROPIETARIO", anotacionesPropietario, disputa.anotacionesArrendatario(),
				disputa.estudioGeometricoJson(), disputa.compressionEvidenceJson(), disputa.curPosParamsJson(),
				disputa.sensoresExternosIds(), resolucion, disputa.fechaCreacion(), null);
		DisputaDocument guardada = disputaDAO.actualizar(actualizada);
		auditoriaService.registrar(principal.userId(), "OPONER_SPECTRAL", "DISPUTA", idReserva,
				disputa.estado(), guardada.estado(), ip);
		return guardada;
	}

	public DisputaDocument oponerarrrendatario(String idReserva, String opcion, String anotaciones,
			TokenPrincipal principal, String ip) {
		DisputaDocument disputa = obtenerDisputa(idReserva);
		DisputaDocument actualizada = new DisputaDocument(disputa.id(), disputa.idReserva(), disputa.motivo(),
				"OPCIONES_RESOLUCION", disputa.anotacionesPropietario(), anotaciones,
				disputa.estudioGeometricoJson(), disputa.compressionEvidenceJson(), disputa.curPosParamsJson(),
				disputa.sensoresExternosIds(), null, disputa.fechaCreacion(), null);
		DisputaDocument guardada = disputaDAO.actualizar(actualizada);
		auditoriaService.registrar(principal.userId(), "OPONER_ARRENDATARIO", "DISPUTA", idReserva,
				disputa.estado(), guardada.estado(), ip);
		return guardada;
	}

	public DisputaDocument resolverarrrendatario(String idReserva, String resolucion, TokenPrincipal principal, String ip) {
		DisputaDocument disputa = obtenerDisputa(idReserva);
		DisputaDocument actualizada = new DisputaDocument(disputa.id(), disputa.idReserva(), disputa.motivo(),
				"RO_MULTA", disputa.anotacionesPropietario(), disputa.anotacionesArrendatario(),
				disputa.estudioGeometricoJson(), disputa.compressionEvidenceJson(), disputa.curPosParamsJson(),
				disputa.sensoresExternosIds(), resolucion, disputa.fechaCreacion(), Instant.now());
		DisputaDocument guardada = disputaDAO.actualizar(actualizada);
		auditoriaService.registrar(principal.userId(), "RESOLVER_ARRENDATARIO", "DISPUTA", idReserva,
				disputa.estado(), guardada.estado(), ip);
		return guardada;
	}

	public DisputaDocument resolverpropietario(String idReserva, String resolucion, TokenPrincipal principal, String ip) {
		DisputaDocument disputa = obtenerDisputa(idReserva);
		DisputaDocument actualizada = new DisputaDocument(disputa.id(), disputa.idReserva(), disputa.motivo(),
				"CONFIRMACION_COSTOS", disputa.anotacionesPropietario(), disputa.anotacionesArrendatario(),
				disputa.estudioGeometricoJson(), disputa.compressionEvidenceJson(), disputa.curPosParamsJson(),
				disputa.sensoresExternosIds(), resolucion, disputa.fechaCreacion(), Instant.now());
		DisputaDocument guardada = disputaDAO.actualizar(actualizada);
		auditoriaService.registrar(principal.userId(), "RESOLVER_PROPIETARIO", "DISPUTA", idReserva,
				disputa.estado(), guardada.estado(), ip);
		return guardada;
	}

	public DisputaDocument resolvercostos(String idReserva, String resolucion, TokenPrincipal principal, String ip) {
		DisputaDocument disputa = obtenerDisputa(idReserva);
		DisputaDocument actualizada = new DisputaDocument(disputa.id(), disputa.idReserva(), disputa.motivo(),
				"CARGO_DEPOSITO", disputa.anotacionesPropietario(), disputa.anotacionesArrendatario(),
				disputa.estudioGeometricoJson(), disputa.compressionEvidenceJson(), disputa.curPosParamsJson(),
				disputa.sensoresExternosIds(), resolucion, disputa.fechaCreacion(), Instant.now());
		DisputaDocument guardada = disputaDAO.actualizar(actualizada);
		auditoriaService.registrar(principal.userId(), "RESOLVER_COSTOS", "DISPUTA", idReserva,
				disputa.estado(), guardada.estado(), ip);
		return guardada;
	}

	private Map<String, String> placeholder() {
		return new HashMap<>();
	}
}
