package com.volt.catalogoflota.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ZonaRequest(
		@JsonProperty("id_zona") @NotNull Long idZona,
		@NotBlank String nombre,
		@NotBlank String ciudad,
		boolean activa) {
}
