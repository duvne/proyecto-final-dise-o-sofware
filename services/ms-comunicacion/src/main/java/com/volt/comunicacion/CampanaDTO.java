package com.volt.comunicacion;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CampanaDTO(
		@JsonProperty("_id") String id,
		@JsonProperty("id_organizador") String organizadorId,
		String segmento,
		String mensaje,
		@JsonProperty("ids_envio") List<String> idsEnvio,
		@JsonProperty("fecha_inicio") Instant fechaInicio,
		String estado,
		@JsonProperty("fecha_envio") Instant fechaEnvio,
		@JsonProperty("respuestas") List<String> respuestas) {
}
