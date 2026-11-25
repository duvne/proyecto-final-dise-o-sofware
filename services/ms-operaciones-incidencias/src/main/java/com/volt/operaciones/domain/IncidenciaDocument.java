package com.volt.operaciones.domain;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IncidenciaDocument(
		@JsonProperty("_id") String id,
		@JsonProperty("id_reserva") String idReserva,
		String tipo,
		String descripcion,
		String estado,
		String solucion,
		@JsonProperty("estudio_geometrico_json") String estudioGeometricoJson,
		@JsonProperty("compression_evidence_json") String compressionEvidenceJson,
		@JsonProperty("cur_pos_params_json") String curPosParamsJson,
		@JsonProperty("sensores_externos_ids") List<String> sensoresExternosIds,
		@JsonProperty("fecha_creacion") Instant fechaCreacion,
		@JsonProperty("fecha_resolucion") Instant fechaResolucion) {

	public IncidenciaDocument {
		sensoresExternosIds = List.copyOf(sensoresExternosIds == null ? List.of() : sensoresExternosIds);
	}
}
