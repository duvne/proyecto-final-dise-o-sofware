package com.volt.operaciones.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.volt.operaciones.domain.ActaDocument;

public record ActaResponse(
		@JsonProperty("_id") String id,
		@JsonProperty("id_reserva") String idReserva,
		String descripcion,
		String observaciones,
		String estado) {

	public static ActaResponse desde(ActaDocument document) {
		return new ActaResponse(document.id(), document.idReserva(), document.descripcion(),
				document.observaciones(), document.estado());
	}
}
