package com.volt.usuariosauth.view;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(
		@JsonProperty("access_token") String accessToken,
		@JsonProperty("token_type") String tokenType,
		@JsonProperty("expires_in") long expiresInSeconds,
		@JsonProperty("user_id") String userId,
		String correo,
		List<String> roles) {
	public AuthResponse {
		roles = List.copyOf(roles);
	}
}
