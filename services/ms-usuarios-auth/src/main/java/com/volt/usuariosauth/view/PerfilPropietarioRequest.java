package com.volt.usuariosauth.view;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerfilPropietarioRequest(
		@JsonProperty("cuenta_desembolso") @NotBlank @Size(max = 40) String cuentaDesembolso,
		@JsonProperty("banco_destino") @NotBlank @Size(max = 80) String bancoDestino) {
}
