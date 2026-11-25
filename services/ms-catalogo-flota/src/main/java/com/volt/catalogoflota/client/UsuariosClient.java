package com.volt.catalogoflota.client;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.volt.shared.security.InternalKey;

@Component
public class UsuariosClient {
	private static final Logger LOG = LoggerFactory.getLogger(UsuariosClient.class);

	private final RestClient restClient;
	private final String internalKey;

	public UsuariosClient(
			@Value("${volt.usuarios-url:http://localhost:8082}") String baseUrl,
			@Value("${volt.internal-key:volt-internal-dev-key}") String internalKey) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
		this.internalKey = internalKey;
	}

	public void actualizarReputacion(String usuarioId, double promedio, int totalCalificaciones) {
		try {
			restClient.post()
					.uri("/api/auth/interno/usuarios/{id}/reputacion", usuarioId)
					.header(InternalKey.HEADER, internalKey)
					.body(Map.of(
							"promedio", promedio,
							"total_calificaciones", totalCalificaciones,
							"rol", "PROPIETARIO"))
					.retrieve()
					.toBodilessEntity();
		} catch (RuntimeException exception) {
			LOG.warn("No se pudo actualizar la reputacion del propietario {}: {}", usuarioId, exception.getMessage());
		}
	}
}
