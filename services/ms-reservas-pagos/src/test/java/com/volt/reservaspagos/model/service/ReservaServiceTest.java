package com.volt.reservaspagos.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.volt.reservaspagos.model.domain.Cotizacion;
import com.volt.reservaspagos.model.domain.ResultadoCancelacion;

class ReservaServiceTest {
	private final ReservaService service = new ReservaService();
	private final Instant ahora = Instant.parse("2026-11-24T12:00:00Z");

	@Test
	void aplicaTarifaDiariaDesdeOchoHorasYNoCalculaComisionSobreDeposito() {
		Cotizacion cotizacion = service.cotizar(
				ahora,
				ahora.plusSeconds(8 * 60 * 60),
				new BigDecimal("5000"),
				new BigDecimal("30000"),
				new BigDecimal("70000"));

		assertEquals(new BigDecimal("30000.00"), cotizacion.valorAlquiler());
		assertEquals(new BigDecimal("4500.00"), cotizacion.comisionPlataforma());
		assertEquals(new BigDecimal("70000.00"), cotizacion.depositoGarantia());
		assertEquals(new BigDecimal("100000.00"), cotizacion.totalArrendatario());
		assertEquals(new BigDecimal("25500.00"), cotizacion.netoPropietario());
	}

	@Test
	void rechazaReservasDeMenosDeUnaHora() {
		assertThrows(IllegalArgumentException.class, () -> service.cotizar(
				ahora,
				ahora.plusSeconds(59 * 60),
				new BigDecimal("5000"),
				new BigDecimal("30000"),
				new BigDecimal("70000")));
	}

	@Test
	void reservaPorHorasRedondeaCadaFraccionAlSiguienteBloque() {
		Cotizacion cotizacion = service.cotizar(
				ahora,
				ahora.plusSeconds(60 * 60 + 1),
				new BigDecimal("5000"),
				new BigDecimal("30000"),
				BigDecimal.ZERO);

		assertEquals(2, cotizacion.unidadesTarifarias());
		assertEquals(new BigDecimal("10000.00"), cotizacion.valorAlquiler());
	}

	@Test
	void permiteFranjasContiguasPeroDetectaCruces() {
		assertFalse(service.seSuperponen(
				ahora, ahora.plusSeconds(3600),
				ahora.plusSeconds(3600), ahora.plusSeconds(7200)));
		assertTrue(service.seSuperponen(
				ahora, ahora.plusSeconds(7200),
				ahora.plusSeconds(3600), ahora.plusSeconds(10800)));
	}

	@Test
	void cancelacionDelArrendatarioAplicaUmbralesYDevuelveDepositoSeparadamente() {
		ResultadoCancelacion reembolso24h = service.cancelarPorArrendatario(
				ahora.plusSeconds(24 * 3600), ahora,
				new BigDecimal("40000"), new BigDecimal("80000"));
		ResultadoCancelacion reembolso2h = service.cancelarPorArrendatario(
				ahora.plusSeconds(2 * 3600), ahora,
				new BigDecimal("40000"), new BigDecimal("80000"));
		ResultadoCancelacion reembolsoMenos2h = service.cancelarPorArrendatario(
				ahora.plusSeconds(2 * 3600 - 1), ahora,
				new BigDecimal("40000"), new BigDecimal("80000"));

		assertEquals(new BigDecimal("40000.00"), reembolso24h.reembolsoAlquiler());
		assertEquals(new BigDecimal("80000.00"), reembolso24h.reembolsoDeposito());
		assertEquals(new BigDecimal("20000.00"), reembolso2h.reembolsoAlquiler());
		assertEquals(new BigDecimal("80000.00"), reembolso2h.reembolsoDeposito());
		assertEquals(new BigDecimal("0.00"), reembolsoMenos2h.reembolsoAlquiler());
	}

	@Test
	void inasistenciaNoReembolsaAlquilerPeroLiberaElDeposito() {
		ResultadoCancelacion resultado = service.calcularReembolsoPorInasistencia(
				new BigDecimal("40000"), new BigDecimal("80000"));

		assertEquals(new BigDecimal("0.00"), resultado.reembolsoAlquiler());
		assertEquals(new BigDecimal("80000.00"), resultado.reembolsoDeposito());
	}

	@Test
	void recargoTardioCobraCientoCincuentaPorCientoPorHoraOFraccion() {
		BigDecimal recargo = service.calcularRecargoTardio(
				ahora,
				ahora.plusSeconds(3600 + 1),
				new BigDecimal("5000"));

		assertEquals(new BigDecimal("15000.00"), recargo);
		assertEquals(new BigDecimal("0.00"), service.calcularRecargoTardio(
				ahora, ahora, new BigDecimal("5000")));
	}

	@Test
	void cancelacionDelPropietarioReembolsaCompletoYPenalizaSoloAntesDe24Horas() {
		ResultadoCancelacion conPenalizacion = service.cancelarPorPropietario(
				ahora.plusSeconds(23 * 3600), ahora,
				new BigDecimal("50000"), new BigDecimal("90000"));
		ResultadoCancelacion sinPenalizacion = service.cancelarPorPropietario(
				ahora.plusSeconds(24 * 3600), ahora,
				new BigDecimal("50000"), new BigDecimal("90000"));

		assertEquals(new BigDecimal("50000.00"), conPenalizacion.reembolsoAlquiler());
		assertEquals(new BigDecimal("90000.00"), conPenalizacion.reembolsoDeposito());
		assertEquals(new BigDecimal("10000.00"), conPenalizacion.penalizacionPropietario());
		assertEquals(new BigDecimal("0.00"), sinPenalizacion.penalizacionPropietario());
	}
}
