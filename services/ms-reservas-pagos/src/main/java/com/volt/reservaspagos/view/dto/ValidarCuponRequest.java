package com.volt.reservaspagos.view.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidarCuponRequest(
		@NotBlank String codigo) {
}
