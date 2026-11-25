package com.volt.reservaspagos.view.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservaRequest(
		@JsonProperty("id_patineta") @NotBlank String idPatineta,
		@JsonProperty("fecha_inicio") @NotNull @Future Instant fechaInicio,
		@JsonProperty("fecha_fin") @NotNull Instant fechaFin,
		@JsonProperty("cupon_codigo") String cuponCodigo) {
}
