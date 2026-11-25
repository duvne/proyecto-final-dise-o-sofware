package com.volt.reservaspagos.client;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.volt.shared.security.InternalKey;

@Component
public class NotificacionClient {
	private static final Logger LOG = LoggerFactory.getLogger(NotificacionClient.class);

	private final RestClient restClient;
	private final String internalKey;

	public NotificacionClient(
			@Value("${volt.comunicacion-url:http://localhost:8085}") String baseUrl,
			@Value("${volt.internal-key:volt-internal-dev-key}") String internalKey) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
		this.internalKey = internalKey;
	}

	public void enviar(String usuarioId, String tipo, String titulo, String mensaje, String reservaId) {
		try {
			restClient.post()
					.uri("/api/comunicacion/interno/notificaciones")
					.header(InternalKey.HEADER, internalKey)
					.body(Map.of(
							"usuario_id", usuarioId,
							"tipo", tipo,
							"titulo", titulo,
							"mensaje", mensaje,
							"reserva_id", reservaId == null ? "" : reservaId))
					.retrieve()
					.toBodilessEntity();
		} catch (RuntimeException exception) {
			LOG.warn("No se pudo enviar la notificacion a {}: {}", usuarioId, exception.getMessage());
		}
	}
}
