package com.volt.reservaspagos.model.domain;

import java.math.BigDecimal;

public record ResultadoCancelacion(
		BigDecimal reembolsoAlquiler,
		BigDecimal reembolsoDeposito,
		BigDecimal penalizacionPropietario) {

	public BigDecimal totalReembolsado() {
		return reembolsoAlquiler.add(reembolsoDeposito);
	}
}
