package com.volt.reservaspagos.view.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.volt.reservaspagos.model.domain.ContraCargoDocument;

public record ContraCargoResponse(
		@JsonProperty("_id") String id,
		@JsonProperty("id_reserva") String idReserva,
		@JsonProperty("id_arrendatario") String idArrendatario,
		BigDecimal monto,
		String motivo,
		String estado,
		@JsonProperty("fecha_registro") Instant fechaRegistro) {

	public static ContraCargoResponse desde(ContraCargoDocument document) {
		return new ContraCargoResponse(document.id(), document.idReserva(), document.idArrendatario(),
				document.monto(), document.motivo(), document.estado(), document.fechaRegistro());
	}
}
