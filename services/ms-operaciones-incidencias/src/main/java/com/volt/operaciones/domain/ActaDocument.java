package com.volt.operaciones.domain;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ActaDocument(
		@JsonProperty("_id") String id,
		@JsonProperty("id_reserva") String idReserva,
		List<String> incidenciasIds,
		String descripcion,
		@JsonProperty("notas_mejoras") String notasMejoras,
		@JsonProperty("foto_evidencias") List<String> fotoEvidencias,
		String observaciones,
		String estado,
		@JsonProperty("fecha_creacion") Instant fechaCreacion,
		@JsonProperty("fecha_resolucion") Instant fechaResolucion) {

	public ActaDocument {
		incidenciasIds = List.copyOf(incidenciasIds == null ? List.of() : incidenciasIds);
		fotoEvidencias = List.copyOf(fotoEvidencias == null ? List.of() : fotoEvidencias);
	}
}
