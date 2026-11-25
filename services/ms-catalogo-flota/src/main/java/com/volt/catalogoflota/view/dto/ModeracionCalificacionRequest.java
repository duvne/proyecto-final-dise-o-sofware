package com.volt.catalogoflota.view.dto;

import jakarta.validation.constraints.NotNull;

public record ModeracionCalificacionRequest(
		@NotNull Boolean oculta,
		String motivo) {
}
