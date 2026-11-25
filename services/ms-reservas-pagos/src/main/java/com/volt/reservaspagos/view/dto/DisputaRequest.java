package com.volt.reservaspagos.view.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DisputaRequest(
		@NotBlank @Size(max = 500) String motivo) {
}
