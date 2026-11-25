package com.volt.usuariosauth.view;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecuperacionConfirmRequest(
		@NotBlank String token,
		@JsonProperty("nueva_contrasena") @NotBlank @Size(min = 12, max = 72) String nuevaContrasena) {
}
