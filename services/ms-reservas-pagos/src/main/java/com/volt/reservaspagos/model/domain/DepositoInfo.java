package com.volt.reservaspagos.model.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record DepositoInfo(
		BigDecimal montoRetenido,
		BigDecimal montoDevuelto,
		String estado,
		Instant fechaResolucion) {

	public static DepositoInfo pendiente(BigDecimal monto) {
		return new DepositoInfo(monto, BigDecimal.ZERO.setScale(2), "PENDIENTE", null);
	}

	public DepositoInfo retenido() {
		return new DepositoInfo(montoRetenido, montoDevuelto, "RETENIDO", null);
	}

	public DepositoInfo liberado() {
		return new DepositoInfo(montoRetenido, montoRetenido, "LIBERADO", Instant.now());
	}

	public DepositoInfo resuelto(BigDecimal devuelto) {
		String estado = devuelto.compareTo(montoRetenido) >= 0
				? "LIBERADO"
				: devuelto.signum() == 0 ? "RETENIDO_TOTAL" : "RETENIDO_PARCIAL";
		return new DepositoInfo(montoRetenido, devuelto, estado, Instant.now());
	}
}
