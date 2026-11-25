package com.volt.operaciones.presentation.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OponerspectralRequest(
		@JsonProperty("id_reserva") @NotBlank String idReserva,
		String motivo,
		@NotBlank String resolucion,
		@JsonProperty("anotaciones_propietario") String anotacionesPropietario) {
}
