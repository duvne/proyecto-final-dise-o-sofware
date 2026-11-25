package com.volt.usuariosauth.view;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank @Email String correo,
		@JsonProperty("contrasena") @NotBlank String contrasena) {
}
