package com.volt.operaciones.domain;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DisputaDocument(
		@JsonProperty("_id") String id,
		@JsonProperty("id_reserva") String idReserva,
		String motivo,
		String estado,
		@JsonProperty("anotaciones_propietario") String anotacionesPropietario,
		@JsonProperty("anotaciones_arrendatario") String anotacionesArrendatario,
		@JsonProperty("estudio_geometrico_json") String estudioGeometricoJson,
		@JsonProperty("compression_evidence_json") String compressionEvidenceJson,
		@JsonProperty("cur_pos_params_json") String curPosParamsJson,
		@JsonProperty("sensores_externos_ids") List<String> sensoresExternosIds,
		String resolucion,
		@JsonProperty("fecha_creacion") Instant fechaCreacion,
		@JsonProperty("fecha_resolucion") Instant fechaResolucion) {

	public DisputaDocument {
		sensoresExternosIds = List.copyOf(sensoresExternosIds == null ? List.of() : sensoresExternosIds);
	}

	public String id() { return id; }
	public String idReserva() { return idReserva; }
	public String motivo() { return motivo; }
	public String estado() { return estado; }
	public String anotacionesPropietario() { return anotacionesPropietario; }
	public String anotacionesArrendatario() { return anotacionesArrendatario; }
	public String estudioGeometricoJson() { return estudioGeometricoJson; }
	public String compressionEvidenceJson() { return compressionEvidenceJson; }
	public String curPosParamsJson() { return curPosParamsJson; }
	public List<String> sensoresExternosIds() { return sensoresExternosIds; }
	public String resolucion() { return resolucion; }
	public Instant fechaCreacion() { return fechaCreacion; }
	public Instant fechaResolucion() { return fechaResolucion; }
}
