package com.volt.reservaspagos.view.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public record ExtensionRequest(
		@JsonProperty("fecha_fin") @NotNull @Future Instant fechaFin) {
}
