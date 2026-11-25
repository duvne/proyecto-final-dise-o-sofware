package com.volt.reservaspagos.model.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record ReservaDocument(
		String id,
		String idArrendatario,
		String idPatineta,
		String idPropietario,
		String cuponCodigo,
		Instant fechaInicio,
		Instant fechaFin,
		EstadoReserva estado,
		BigDecimal subtotalAlquiler,
		BigDecimal descuentoAplicado,
		BigDecimal totalAlquiler,
		BigDecimal recargoRetraso,
		BigDecimal penalizacionPropietario,
		PagoInfo pago,
		DepositoInfo deposito,
		LiquidacionInfo liquidacion,
		Instant fechaCreacion,
		Instant fechaActualizacion,
		Instant fechaCheckIn,
		Instant fechaCheckout,
		Instant fechaLiberacionPago,
		boolean disputaAbierta,
		String motivoCancelacion,
		String observaciones) {
}
