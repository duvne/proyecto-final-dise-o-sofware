package com.volt.catalogoflota.business;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.volt.catalogoflota.document.AlertaDisponibilidadDocument;
import com.volt.catalogoflota.persistence.IAlertaDAO;
import com.volt.catalogoflota.persistence.IPatinetaDAO;
import com.volt.catalogoflota.view.dto.AlertaRequest;
import com.volt.catalogoflota.view.dto.AlertaResponse;
import com.volt.shared.security.TokenPrincipal;

@Service
public class AlertaService {
	private final IAlertaDAO alertaDAO;
	private final IPatinetaDAO patinetaDAO;

	public AlertaService(IAlertaDAO alertaDAO, IPatinetaDAO patinetaDAO) {
		this.alertaDAO = alertaDAO;
		this.patinetaDAO = patinetaDAO;
	}

	public AlertaResponse crear(AlertaRequest request, TokenPrincipal principal) {
		patinetaDAO.buscarPorId(request.patinetaId())
				.orElseThrow(() -> new PatinetaNoEncontradaException(request.patinetaId()));
		if (!request.fin().isAfter(request.inicio())) {
			throw new IllegalArgumentException("El rango de fechas de la alerta es invalido.");
		}
		if (request.inicio().isBefore(Instant.now())) {
			throw new IllegalArgumentException("La alerta debe iniciar en el futuro.");
		}
		AlertaDisponibilidadDocument alerta = alertaDAO.guardar(new AlertaDisponibilidadDocument(
				UUID.randomUUID().toString(), principal.userId(), request.patinetaId(),
				request.inicio(), request.fin(), true, Instant.now()));
		return AlertaResponse.desde(alerta);
	}

	public List<AlertaResponse> listar(TokenPrincipal principal) {
		return alertaDAO.listarPorUsuario(principal.userId()).stream()
				.map(AlertaResponse::desde).toList();
	}

	public void eliminar(String id, TokenPrincipal principal) {
		alertaDAO.eliminar(id, principal.userId());
	}
}
