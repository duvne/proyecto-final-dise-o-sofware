package com.volt.catalogoflota.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public record ZonaDTO(
		@JsonProperty("id_zona") Long idZona,
		@NotBlank String nombre,
		@NotBlank String ciudad) {
}
