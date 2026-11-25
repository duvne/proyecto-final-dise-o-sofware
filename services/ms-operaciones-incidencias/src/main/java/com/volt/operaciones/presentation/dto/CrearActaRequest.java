package com.volt.operaciones.presentation.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearActaRequest(
		@JsonProperty("id_reserva") @NotBlank String idReserva,
		@JsonProperty("incidencias_ids") List<String> incidenciasIds,
		@NotBlank String descripcion,
		@JsonProperty("notas_mejoras") String notasMejoras,
		@JsonProperty("foto_evidencias") List<String> fotoEvidencias,
		String observaciones,
		@NotBlank String estado,
		@Size(max = 500) String resolucion) {
}
