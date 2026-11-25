package com.volt.usuariosauth.view;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record RegistroUsuarioRequest(
		@NotBlank @Size(max = 120) String nombre,
		@NotBlank @Size(max = 40) String documento,
		@NotBlank @Email @Size(max = 254) String correo,
		@NotBlank @Size(min = 12, max = 72) String contrasena,
		@NotBlank @Size(max = 30) String telefono,
		@JsonProperty("fecha_nacimiento") @NotNull @Past LocalDate fechaNacimiento,
		String rol,
		@JsonProperty("acepta_tratamiento_datos") boolean aceptaTratamientoDatos) {
}
