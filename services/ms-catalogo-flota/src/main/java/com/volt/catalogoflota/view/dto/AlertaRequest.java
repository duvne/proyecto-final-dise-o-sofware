package com.volt.catalogoflota.view.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlertaRequest(
		@JsonProperty("patineta_id") @NotBlank String patinetaId,
		@NotNull Instant inicio,
		@NotNull Instant fin) {
}
