package com.volt.catalogoflota.view.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MantenimientoRequest(
		@NotBlank @Size(max = 500) String descripcion) {
}
