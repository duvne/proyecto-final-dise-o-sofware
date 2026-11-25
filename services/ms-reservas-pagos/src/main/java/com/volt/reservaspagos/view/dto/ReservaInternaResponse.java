package com.volt.reservaspagos.view.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.volt.reservaspagos.model.domain.ReservaDocument;

public record ReservaInternaResponse(
		@JsonProperty("_id") String id,
		@JsonProperty("id_arrendatario") String idArrendatario,
		@JsonProperty("id_patineta") String idPatineta,
		@JsonProperty("id_propietario") String idPropietario,
		String estado,
		@JsonProperty("fecha_inicio") Instant fechaInicio,
		@JsonProperty("fecha_fin") Instant fechaFin,
		@JsonProperty("fecha_check_in") Instant fechaCheckIn,
		@JsonProperty("fecha_checkout") Instant fechaCheckout) {

	public static ReservaInternaResponse desde(ReservaDocument reserva) {
		return new ReservaInternaResponse(reserva.id(), reserva.idArrendatario(), reserva.idPatineta(),
				reserva.idPropietario(), reserva.estado().name(), reserva.fechaInicio(), reserva.fechaFin(),
				reserva.fechaCheckIn(), reserva.fechaCheckout());
	}
}
