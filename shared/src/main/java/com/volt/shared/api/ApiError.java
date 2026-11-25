package com.volt.shared.api;

import java.time.Instant;
import java.util.Map;

public record ApiError(
		String error,
		String detalle,
		Map<String, String> campos,
		Instant timestamp) {

	public ApiError {
		campos = campos == null ? Map.of() : Map.copyOf(campos);
	}

	public static ApiError de(String error, String detalle) {
		return new ApiError(error, detalle, Map.of(), Instant.now());
	}

	public static ApiError de(String error, String detalle, Map<String, String> campos) {
		return new ApiError(error, detalle, campos, Instant.now());
	}
}
