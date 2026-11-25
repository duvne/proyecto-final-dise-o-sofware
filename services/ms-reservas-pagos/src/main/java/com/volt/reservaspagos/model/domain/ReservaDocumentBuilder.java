package com.volt.reservaspagos.model.domain;

import java.math.BigDecimal;
import java.time.Instant;

public final class ReservaDocumentBuilder {
	private String id;
	private String idArrendatario;
	private String idPatineta;
	private String idPropietario;
	private String cuponCodigo;
	private Instant fechaInicio;
	private Instant fechaFin;
	private EstadoReserva estado;
	private BigDecimal subtotalAlquiler;
	private BigDecimal descuentoAplicado;
	private BigDecimal totalAlquiler;
	private BigDecimal recargoRetraso;
	private BigDecimal penalizacionPropietario;
	private PagoInfo pago;
	private DepositoInfo deposito;
	private LiquidacionInfo liquidacion;
	private Instant fechaCreacion;
	private Instant fechaActualizacion;
	private Instant fechaCheckIn;
	private Instant fechaCheckout;
	private Instant fechaLiberacionPago;
	private boolean disputaAbierta;
	private String motivoCancelacion;
	private String observaciones;

	public ReservaDocumentBuilder() {
	}

	public static ReservaDocumentBuilder de(ReservaDocument reserva) {
		ReservaDocumentBuilder builder = new ReservaDocumentBuilder();
		builder.id = reserva.id();
		builder.idArrendatario = reserva.idArrendatario();
		builder.idPatineta = reserva.idPatineta();
		builder.idPropietario = reserva.idPropietario();
		builder.cuponCodigo = reserva.cuponCodigo();
		builder.fechaInicio = reserva.fechaInicio();
		builder.fechaFin = reserva.fechaFin();
		builder.estado = reserva.estado();
		builder.subtotalAlquiler = reserva.subtotalAlquiler();
		builder.descuentoAplicado = reserva.descuentoAplicado();
		builder.totalAlquiler = reserva.totalAlquiler();
		builder.recargoRetraso = reserva.recargoRetraso();
		builder.penalizacionPropietario = reserva.penalizacionPropietario();
		builder.pago = reserva.pago();
		builder.deposito = reserva.deposito();
		builder.liquidacion = reserva.liquidacion();
		builder.fechaCreacion = reserva.fechaCreacion();
		builder.fechaActualizacion = reserva.fechaActualizacion();
		builder.fechaCheckIn = reserva.fechaCheckIn();
		builder.fechaCheckout = reserva.fechaCheckout();
		builder.fechaLiberacionPago = reserva.fechaLiberacionPago();
		builder.disputaAbierta = reserva.disputaAbierta();
		builder.motivoCancelacion = reserva.motivoCancelacion();
		builder.observaciones = reserva.observaciones();
		return builder;
	}

	public ReservaDocumentBuilder id(String value) { this.id = value; return this; }
	public ReservaDocumentBuilder idArrendatario(String value) { this.idArrendatario = value; return this; }
	public ReservaDocumentBuilder idPatineta(String value) { this.idPatineta = value; return this; }
	public ReservaDocumentBuilder idPropietario(String value) { this.idPropietario = value; return this; }
	public ReservaDocumentBuilder cuponCodigo(String value) { this.cuponCodigo = value; return this; }
	public ReservaDocumentBuilder fechaInicio(Instant value) { this.fechaInicio = value; return this; }
	public ReservaDocumentBuilder fechaFin(Instant value) { this.fechaFin = value; return this; }
	public ReservaDocumentBuilder estado(EstadoReserva value) { this.estado = value; return this; }
	public ReservaDocumentBuilder subtotalAlquiler(BigDecimal value) { this.subtotalAlquiler = value; return this; }
	public ReservaDocumentBuilder descuentoAplicado(BigDecimal value) { this.descuentoAplicado = value; return this; }
	public ReservaDocumentBuilder totalAlquiler(BigDecimal value) { this.totalAlquiler = value; return this; }
	public ReservaDocumentBuilder recargoRetraso(BigDecimal value) { this.recargoRetraso = value; return this; }
	public ReservaDocumentBuilder penalizacionPropietario(BigDecimal value) { this.penalizacionPropietario = value; return this; }
	public ReservaDocumentBuilder pago(PagoInfo value) { this.pago = value; return this; }
	public ReservaDocumentBuilder deposito(DepositoInfo value) { this.deposito = value; return this; }
	public ReservaDocumentBuilder liquidacion(LiquidacionInfo value) { this.liquidacion = value; return this; }
	public ReservaDocumentBuilder fechaCreacion(Instant value) { this.fechaCreacion = value; return this; }
	public ReservaDocumentBuilder fechaActualizacion(Instant value) { this.fechaActualizacion = value; return this; }
	public ReservaDocumentBuilder fechaCheckIn(Instant value) { this.fechaCheckIn = value; return this; }
	public ReservaDocumentBuilder fechaCheckout(Instant value) { this.fechaCheckout = value; return this; }
	public ReservaDocumentBuilder fechaLiberacionPago(Instant value) { this.fechaLiberacionPago = value; return this; }
	public ReservaDocumentBuilder disputaAbierta(boolean value) { this.disputaAbierta = value; return this; }
	public ReservaDocumentBuilder motivoCancelacion(String value) { this.motivoCancelacion = value; return this; }
	public ReservaDocumentBuilder observaciones(String value) { this.observaciones = value; return this; }

	public ReservaDocument build() {
		return new ReservaDocument(id, idArrendatario, idPatineta, idPropietario, cuponCodigo,
				fechaInicio, fechaFin, estado, subtotalAlquiler, descuentoAplicado, totalAlquiler,
				recargoRetraso, penalizacionPropietario, pago, deposito, liquidacion, fechaCreacion,
				fechaActualizacion, fechaCheckIn, fechaCheckout, fechaLiberacionPago, disputaAbierta,
				motivoCancelacion, observaciones);
	}
}
