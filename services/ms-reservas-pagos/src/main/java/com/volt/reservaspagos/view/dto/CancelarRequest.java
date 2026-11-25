package com.volt.reservaspagos.view.dto;

import jakarta.validation.constraints.Size;

public record CancelarRequest(
		@Size(max = 500) String motivo) {
}
