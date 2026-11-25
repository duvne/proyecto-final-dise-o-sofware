package com.volt.reservaspagos.model.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record PagoInfo(
		String referenciaPasarela,
		BigDecimal montoTotal,
		BigDecimal comisionPlataforma,
		BigDecimal montoReembolsado,
		String estado,
		Instant fechaPago,
		String comprobante) {

	public static PagoInfo pendiente(BigDecimal montoTotal, BigDecimal comision) {
		return new PagoInfo(null, montoTotal, comision, BigDecimal.ZERO.setScale(2), "PENDIENTE", null, null);
	}

	public PagoInfo retenido(String referencia, Instant fecha, String comprobante) {
		return new PagoInfo(referencia, montoTotal, comisionPlataforma, montoReembolsado, "RETENIDO", fecha, comprobante);
	}

	public PagoInfo liberado() {
		return new PagoInfo(referenciaPasarela, montoTotal, comisionPlataforma, montoReembolsado, "LIBERADO",
				fechaPago, comprobante);
	}

	public PagoInfo reembolsado(BigDecimal monto) {
		return new PagoInfo(referenciaPasarela, montoTotal, comisionPlataforma, monto, "REEMBOLSADO",
				fechaPago, comprobante);
	}
}
