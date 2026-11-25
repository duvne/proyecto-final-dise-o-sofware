package com.volt.operaciones.presentation.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.volt.operaciones.domain.IncidenciaDocument;

public record IncidenciaResponse(
		@JsonProperty("_id") String id,
		@JsonProperty("id_reserva") String idReserva,
		String tipo,
		String descripcion,
		String estado,
		String solucion) {

	public static IncidenciaResponse desde(IncidenciaDocument document) {
		return new IncidenciaResponse(document.id(), document.idReserva(), document.tipo(),
				document.descripcion(), document.estado(), document.solucion());
	}
}
