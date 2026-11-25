package com.volt.usuariosauth.view;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReputacionRequest(
		@NotNull @DecimalMin("0.0") @DecimalMax("5.0") Double promedio,
		@JsonProperty("total_calificaciones") @NotNull @Min(0) Integer totalCalificaciones,
		@NotBlank String rol) {
}
