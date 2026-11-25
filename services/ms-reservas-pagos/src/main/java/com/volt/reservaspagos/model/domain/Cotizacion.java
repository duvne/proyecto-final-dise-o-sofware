package com.volt.reservaspagos.model.domain;

import java.math.BigDecimal;

public record Cotizacion(
		BigDecimal valorAlquiler,
		BigDecimal depositoGarantia,
		BigDecimal comisionPlataforma,
		BigDecimal totalArrendatario,
		BigDecimal netoPropietario,
		long unidadesTarifarias) {
}
