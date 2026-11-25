package com.volt.reservaspagos.model.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record ContraCargoDocument(
		String id,
		String idReserva,
		String idArrendatario,
		BigDecimal monto,
		String motivo,
		String estado,
		Instant fechaRegistro) {
}
