package com.volt.usuariosauth.view;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VerificacionRequest(
		@NotNull Boolean aprobado,
		@Size(max = 300) String motivo) {
}
