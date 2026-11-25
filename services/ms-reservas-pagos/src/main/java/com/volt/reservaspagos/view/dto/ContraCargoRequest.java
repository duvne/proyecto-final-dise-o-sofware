package com.volt.reservaspagos.view.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContraCargoRequest(
		@JsonProperty("id_reserva") @NotBlank String idReserva,
		@JsonProperty("id_arrendatario") @NotBlank String idArrendatario,
		@NotNull @DecimalMin("0.01") BigDecimal monto,
		@NotBlank @Size(max = 500) String motivo) {
}
