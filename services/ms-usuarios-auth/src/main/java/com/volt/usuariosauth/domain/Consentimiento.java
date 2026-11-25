package com.volt.usuariosauth.domain;

import java.time.Instant;

public record Consentimiento(
		String tipo,
		boolean aceptado,
		String version,
		Instant fecha) {

	public static Consentimiento de(String tipo, boolean aceptado, String version) {
		return new Consentimiento(tipo, aceptado, version, Instant.now());
	}
}
