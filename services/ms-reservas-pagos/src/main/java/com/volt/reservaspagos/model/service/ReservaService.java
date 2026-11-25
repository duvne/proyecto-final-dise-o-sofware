package com.volt.reservaspagos.model.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.volt.reservaspagos.model.domain.Cotizacion;
import com.volt.reservaspagos.model.domain.ResultadoCancelacion;
import com.volt.reservaspagos.model.patterns.ConfiguracionComisionSingleton;

@Service
public class ReservaService {
	private static final BigDecimal UNO = BigDecimal.ONE;
	private static final BigDecimal DOS = new BigDecimal("2");
	private static final BigDecimal VEINTE_POR_CIENTO = new BigDecimal("0.20");
	private static final BigDecimal CIENTO_CINCUENTA_POR_CIENTO = new BigDecimal("1.50");
	private static final long HORAS_PARA_TARIFA_DIARIA = 8;
	private static final long HORAS_POR_DIA = 24;
	private static final long MINUTOS_MINIMOS = 60;

	public Cotizacion cotizar(
			Instant inicio,
			Instant fin,
			BigDecimal tarifaHora,
			BigDecimal tarifaDia,
			BigDecimal deposito) {
		validarPeriodo(inicio, fin);
		validarMonto(tarifaHora, "La tarifa por hora");
		validarMonto(tarifaDia, "La tarifa diaria");
		validarMonto(deposito, "El deposito");

		long segundos = Duration.between(inicio, fin).getSeconds();
		if (segundos < MINUTOS_MINIMOS * 60) {
			throw new IllegalArgumentException("La reserva debe durar al menos una hora.");
		}

		long horasFacturables = unidadesIniciadas(segundos, 60 * 60);
		long unidadesTarifarias;
		BigDecimal alquiler;
		if (segundos >= HORAS_PARA_TARIFA_DIARIA * 60 * 60) {
			unidadesTarifarias = unidadesIniciadas(segundos, HORAS_POR_DIA * 60 * 60);
			alquiler = tarifaDia.multiply(BigDecimal.valueOf(unidadesTarifarias));
		} else {
			unidadesTarifarias = horasFacturables;
			alquiler = tarifaHora.multiply(BigDecimal.valueOf(horasFacturables));
		}

		alquiler = dinero(alquiler);
		BigDecimal comision = dinero(alquiler.multiply(
				ConfiguracionComisionSingleton.INSTANCIA.porcentajeComision()));
		BigDecimal garantia = dinero(deposito);
		return new Cotizacion(
				alquiler,
				garantia,
				comision,
				dinero(alquiler.add(garantia)),
				dinero(alquiler.subtract(comision)),
				unidadesTarifarias);
	}

	public ResultadoCancelacion cancelarPorArrendatario(
			Instant inicioReserva,
			Instant momentoCancelacion,
			BigDecimal valorAlquiler,
			BigDecimal deposito) {
		validarCancelacion(inicioReserva, momentoCancelacion);
		validarMonto(valorAlquiler, "El valor del alquiler");
		validarMonto(deposito, "El deposito");

		long segundosRestantes = Duration.between(momentoCancelacion, inicioReserva).getSeconds();
		BigDecimal porcentajeReembolso;
		if (segundosRestantes < 2 * 60 * 60) {
			porcentajeReembolso = BigDecimal.ZERO;
		} else if (segundosRestantes >= 24 * 60 * 60) {
			porcentajeReembolso = UNO;
		} else {
			porcentajeReembolso = new BigDecimal("0.50");
		}

		return new ResultadoCancelacion(
				dinero(valorAlquiler.multiply(porcentajeReembolso)),
				dinero(deposito),
				BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
	}

	public ResultadoCancelacion calcularReembolsoPorInasistencia(
			BigDecimal valorAlquiler,
			BigDecimal deposito) {
		validarMonto(valorAlquiler, "El valor del alquiler");
		validarMonto(deposito, "El deposito");
		return new ResultadoCancelacion(
				BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
				dinero(deposito),
				BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
	}

	public ResultadoCancelacion cancelarPorPropietario(
			Instant inicioReserva,
			Instant momentoCancelacion,
			BigDecimal valorAlquiler,
			BigDecimal deposito) {
		validarCancelacion(inicioReserva, momentoCancelacion);
		validarMonto(valorAlquiler, "El valor del alquiler");
		validarMonto(deposito, "El deposito");

		long segundosRestantes = Duration.between(momentoCancelacion, inicioReserva).getSeconds();
		BigDecimal penalizacion = segundosRestantes < 24 * 60 * 60
				? dinero(valorAlquiler.multiply(VEINTE_POR_CIENTO))
				: BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

		return new ResultadoCancelacion(
				dinero(valorAlquiler),
				dinero(deposito),
				penalizacion);
	}

	public BigDecimal calcularRecargoTardio(
			Instant finPactado,
			Instant devolucionReal,
			BigDecimal tarifaHora) {
		Objects.requireNonNull(finPactado, "La fecha de fin pactada es obligatoria.");
		Objects.requireNonNull(devolucionReal, "La fecha de devolucion es obligatoria.");
		validarMonto(tarifaHora, "La tarifa por hora");

		long segundosTarde = Duration.between(finPactado, devolucionReal).getSeconds();
		if (segundosTarde <= 0) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		long horasOCualquierFraccion = unidadesIniciadas(segundosTarde, 60 * 60);
		return dinero(tarifaHora
				.multiply(CIENTO_CINCUENTA_POR_CIENTO)
				.multiply(BigDecimal.valueOf(horasOCualquierFraccion)));
	}

	public boolean seSuperponen(Instant inicioA, Instant finA, Instant inicioB, Instant finB) {
		validarPeriodo(inicioA, finA);
		validarPeriodo(inicioB, finB);
		return inicioA.isBefore(finB) && inicioB.isBefore(finA);
	}

	private static long unidadesIniciadas(long segundos, long segundosPorUnidad) {
		return (segundos + segundosPorUnidad - 1) / segundosPorUnidad;
	}

	private static BigDecimal dinero(BigDecimal valor) {
		return valor.setScale(2, RoundingMode.HALF_UP);
	}

	private static void validarPeriodo(Instant inicio, Instant fin) {
		Objects.requireNonNull(inicio, "La fecha de inicio es obligatoria.");
		Objects.requireNonNull(fin, "La fecha de fin es obligatoria.");
		if (!inicio.isBefore(fin)) {
			throw new IllegalArgumentException("La fecha de fin debe ser posterior al inicio.");
		}
	}

	private static void validarCancelacion(Instant inicioReserva, Instant momentoCancelacion) {
		Objects.requireNonNull(inicioReserva, "La fecha de inicio de la reserva es obligatoria.");
		Objects.requireNonNull(momentoCancelacion, "La fecha de cancelacion es obligatoria.");
		if (!momentoCancelacion.isBefore(inicioReserva)) {
			throw new IllegalArgumentException("La cancelacion debe ocurrir antes del inicio de la reserva.");
		}
	}

	private static void validarMonto(BigDecimal monto, String nombre) {
		Objects.requireNonNull(monto, nombre + " es obligatorio.");
		if (monto.signum() < 0) {
			throw new IllegalArgumentException(nombre + " no puede ser negativo.");
		}
	}
}
