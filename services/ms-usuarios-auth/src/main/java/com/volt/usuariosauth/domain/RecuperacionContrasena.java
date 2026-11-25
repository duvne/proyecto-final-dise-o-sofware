package com.volt.usuariosauth.domain;

import java.time.Instant;

public record RecuperacionContrasena(
		String id,
		String usuarioId,
		String tokenHash,
		Instant expiraEn,
		boolean usado) {
}
