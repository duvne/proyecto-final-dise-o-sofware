package com.volt.operaciones.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public record OponerArrendatarioRequest(
		@JsonProperty("id_reserva") @NotBlank String idReserva,
		@NotBlank String opcion,
		@JsonProperty("anotaciones_arrendatario") String anotaciones) {
}
