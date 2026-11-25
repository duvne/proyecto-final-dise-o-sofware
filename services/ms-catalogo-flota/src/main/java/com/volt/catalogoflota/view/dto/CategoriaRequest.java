package com.volt.catalogoflota.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(
		@NotBlank String nombre,
		String descripcion,
		@JsonProperty("autonomia_minima_km") Double autonomiaMinimaKm,
		boolean activa) {
}
