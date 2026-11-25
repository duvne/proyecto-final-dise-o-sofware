package com.volt.usuariosauth.view;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuspensionRequest(
		@NotBlank @Size(max = 300) String motivo) {
}
