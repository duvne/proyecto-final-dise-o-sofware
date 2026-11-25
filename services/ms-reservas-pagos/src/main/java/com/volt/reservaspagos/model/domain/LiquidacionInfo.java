package com.volt.reservaspagos.model.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record LiquidacionInfo(
		BigDecimal montoBruto,
		BigDecimal comisionDescontada,
		BigDecimal penalizacion,
		BigDecimal montoNeto,
		String estado,
		Instant fechaDesembolso) {
}
