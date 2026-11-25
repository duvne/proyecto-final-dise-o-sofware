package com.volt.operaciones.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.volt.operaciones.domain.DisputaDocument;

public record DisputaResponse(
		@JsonProperty("_id") String id,
		@JsonProperty("id_reserva") String idReserva,
		String motivo,
		String estado,
		@JsonProperty("anotaciones_propietario") String anotacionesPropietario,
		@JsonProperty("anotaciones_arrendatario") String anotacionesArrendatario,
		String resolucion) {

	public static DisputaResponse desde(DisputaDocument document) {
		return new DisputaResponse(document.id(), document.idReserva(), document.motivo(), document.estado(),
				document.anotacionesPropietario(), document.anotacionesArrendatario(), document.resolucion());
	}
}
