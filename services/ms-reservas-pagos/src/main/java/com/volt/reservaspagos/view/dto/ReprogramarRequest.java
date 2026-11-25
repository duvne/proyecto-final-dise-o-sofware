package com.volt.reservaspagos.view.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public record ReprogramarRequest(
		@JsonProperty("fecha_inicio") @NotNull @Future Instant fechaInicio,
		@JsonProperty("fecha_fin") @NotNull Instant fechaFin) {
}
