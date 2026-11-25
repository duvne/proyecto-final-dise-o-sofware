package com.volt.reservaspagos.view.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public record FinalizarRequest(
		@JsonProperty("fecha_devolucion") @NotNull Instant fechaDevolucion,
		@JsonProperty("observaciones") String observaciones) {
}
