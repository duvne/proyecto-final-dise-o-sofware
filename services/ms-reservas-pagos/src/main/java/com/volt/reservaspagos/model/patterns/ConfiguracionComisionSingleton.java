package com.volt.reservaspagos.model.patterns;

import java.math.BigDecimal;

public enum ConfiguracionComisionSingleton {
	INSTANCIA;

	private static final BigDecimal COMISION_BASE = new BigDecimal("0.15");

	public BigDecimal porcentajeComision() {
		return COMISION_BASE;
	}
}
