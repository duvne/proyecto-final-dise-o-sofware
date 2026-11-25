package com.volt.reservaspagos.view.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CuponRequest(
		@NotBlank String codigo,
		@NotNull @DecimalMin("0.01") @DecimalMax("1.0") BigDecimal porcentaje,
		@JsonProperty("tope_descuento") @NotNull @DecimalMin("0.0") BigDecimal topeDescuento,
		@JsonProperty("fecha_vencimiento") @NotNull @Future Instant fechaVencimiento,
		boolean activo,
		@JsonProperty("limite_usos") @NotNull @Min(1) Integer limiteUsos) {
}
