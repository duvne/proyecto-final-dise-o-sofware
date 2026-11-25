package com.volt.catalogoflota.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PuntoEntregaDTO(
		@JsonProperty("id_punto") Long idPunto,
		@NotBlank String direccion,
		@NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitud,
		@NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitud,
		boolean verificado,
		@NotNull @Valid ZonaDTO zona) {
}
