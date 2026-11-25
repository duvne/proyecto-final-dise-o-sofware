package com.volt.usuariosauth.view;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UsuarioInternoResponse(
		@JsonProperty("user_id") String userId,
		String correo,
		boolean verificado,
		@JsonProperty("estado_cuenta") String estadoCuenta,
		List<String> roles) {

	public UsuarioInternoResponse {
		roles = List.copyOf(roles);
	}
}
