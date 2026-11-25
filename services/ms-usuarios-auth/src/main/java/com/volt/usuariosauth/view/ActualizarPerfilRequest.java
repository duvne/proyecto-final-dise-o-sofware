package com.volt.usuariosauth.view;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarPerfilRequest(
		@NotBlank @Size(max = 120) String nombre,
		@NotBlank @Size(max = 30) String telefono) {
}
