package com.volt.reservaspagos.model.domain;

import java.time.Instant;

public record FranjaDisponible(
		Instant inicio,
		Instant fin,
		String estado) {
}
