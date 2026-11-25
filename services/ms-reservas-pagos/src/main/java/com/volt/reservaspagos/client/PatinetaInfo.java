package com.volt.reservaspagos.client;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PatinetaInfo(
		@JsonProperty("_id") String id,
		@JsonProperty("id_propietario") String idPropietario,
		String categoria,
		String marca,
		String modelo,
		@JsonProperty("estado_publicacion") String estadoPublicacion,
		boolean activa,
		@JsonProperty("en_mantenimiento") boolean enMantenimiento,
		TarifaInfo tarifa) {

	public record TarifaInfo(
			@JsonProperty("valor_hora") BigDecimal valorHora,
			@JsonProperty("valor_dia") BigDecimal valorDia,
			@JsonProperty("valor_deposito") BigDecimal valorDeposito) {
	}
}
