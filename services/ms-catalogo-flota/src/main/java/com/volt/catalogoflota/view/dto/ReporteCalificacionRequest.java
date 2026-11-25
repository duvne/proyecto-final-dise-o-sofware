package com.volt.catalogoflota.view.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReporteCalificacionRequest(
		@NotBlank @Size(max = 300) String motivo) {
}
