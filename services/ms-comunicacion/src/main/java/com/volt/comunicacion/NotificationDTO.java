package com.volt.comunicacion;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NotificationDTO(
		@JsonProperty("_id") String id,
		@JsonProperty("usuario_id") String usuarioId,
		String tipo,
		String titulo,
		String mensaje,
		@JsonProperty("reserva_id") String reservaId,
		@JsonProperty("fecha_envio") Instant fechaEnvio) {

	public static NotificationDTO de(String usuarioId, String tipo, String titulo, String mensaje,
			String reservaId) {
		return new NotificationDTO(UUID.randomUUID().toString(), usuarioId, tipo, titulo, mensaje,
				reservaId, Instant.now());
	}
}
