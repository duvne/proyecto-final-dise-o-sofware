package com.volt.reservaspagos.view.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record ResolucionDisputaRequest(
		@JsonProperty("monto_devuelto_deposito") @NotNull @DecimalMin("0.0") BigDecimal montoDevueltoDeposito,
		@JsonProperty("liberar_pago") @NotNull Boolean liberarPago,
		String observaciones) {
}
