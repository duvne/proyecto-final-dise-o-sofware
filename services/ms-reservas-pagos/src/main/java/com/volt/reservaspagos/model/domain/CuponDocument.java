package com.volt.reservaspagos.model.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record CuponDocument(
		String id,
		String codigo,
		BigDecimal porcentaje,
		BigDecimal topeDescuento,
		Instant fechaVencimiento,
		boolean activo,
		int usos,
		int limiteUsos) {
}
