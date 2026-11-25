package com.volt.usuariosauth.view;

import jakarta.validation.constraints.NotBlank;

public record ConsentimientoRequest(
		@NotBlank String tipo,
		boolean aceptado,
		@NotBlank String version) {
}
