package com.volt.catalogoflota.view.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModeracionPatinetaRequest(
		@NotNull Boolean aprobado,
		@Size(max = 500) String observaciones) {
}
