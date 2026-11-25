package com.volt.catalogoflota.view.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PatinetaRequestDTO(
		@NotBlank String categoria,
		@NotBlank String marca,
		@NotBlank String modelo,
		@JsonProperty("autonomia_km") @NotNull @Positive Double autonomiaKm,
		@JsonProperty("velocidad_max") @NotNull @Positive Double velocidadMaxima,
		@JsonProperty("estado_fisico") @NotBlank String estadoFisico,
		@NotNull @Valid TarifaDTO tarifa,
		@JsonProperty("punto_entrega") @NotNull @Valid PuntoEntregaDTO puntoEntrega,
		List<String> fotos,
		List<String> documentos) {

	public PatinetaRequestDTO {
		fotos = fotos == null ? List.of() : List.copyOf(fotos);
		documentos = documentos == null ? List.of() : List.copyOf(documentos);
	}
}
