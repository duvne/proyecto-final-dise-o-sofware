package com.volt.reservaspagos.view.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ValidarCuponResponse(
		String codigo,
		boolean valido,
		BigDecimal porcentaje,
		@JsonProperty("tope_descuento") BigDecimal topeDescuento,
		String mensaje) {
}
