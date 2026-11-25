package com.volt.reservaspagos.view.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.volt.reservaspagos.model.domain.DepositoInfo;
import com.volt.reservaspagos.model.domain.LiquidacionInfo;
import com.volt.reservaspagos.model.domain.PagoInfo;
import com.volt.reservaspagos.model.domain.ReservaDocument;

public record ReservaResponse(
		@JsonProperty("_id") String id,
		@JsonProperty("id_arrendatario") String idArrendatario,
		@JsonProperty("id_patineta") String idPatineta,
		@JsonProperty("id_propietario") String idPropietario,
		@JsonProperty("cupon_codigo") String cuponCodigo,
		@JsonProperty("fecha_inicio") Instant fechaInicio,
		@JsonProperty("fecha_fin") Instant fechaFin,
		String estado,
		@JsonProperty("subtotal_alquiler") BigDecimal subtotalAlquiler,
		@JsonProperty("descuento_aplicado") BigDecimal descuentoAplicado,
		@JsonProperty("total_alquiler") BigDecimal totalAlquiler,
		@JsonProperty("recargo_retraso") BigDecimal recargoRetraso,
		@JsonProperty("penalizacion_propietario") BigDecimal penalizacionPropietario,
		PagoDTO pago,
		DepositoDTO deposito,
		LiquidacionDTO liquidacion,
		@JsonProperty("fecha_creacion") Instant fechaCreacion,
		@JsonProperty("fecha_actualizacion") Instant fechaActualizacion,
		@JsonProperty("fecha_check_in") Instant fechaCheckIn,
		@JsonProperty("fecha_checkout") Instant fechaCheckout,
		@JsonProperty("fecha_liberacion_pago") Instant fechaLiberacionPago,
		@JsonProperty("disputa_abierta") boolean disputaAbierta,
		@JsonProperty("motivo_cancelacion") String motivoCancelacion,
		String observaciones) {

	public record PagoDTO(
			@JsonProperty("referencia_pasarela") String referenciaPasarela,
			@JsonProperty("monto_total") BigDecimal montoTotal,
			@JsonProperty("comision_plataforma") BigDecimal comisionPlataforma,
			@JsonProperty("monto_reembolsado") BigDecimal montoReembolsado,
			String estado,
			@JsonProperty("fecha_pago") Instant fechaPago,
			String comprobante) {
		public static PagoDTO desde(PagoInfo pago) {
			return new PagoDTO(pago.referenciaPasarela(), pago.montoTotal(), pago.comisionPlataforma(),
					pago.montoReembolsado(), pago.estado(), pago.fechaPago(), pago.comprobante());
		}
	}

	public record DepositoDTO(
			@JsonProperty("monto_retenido") BigDecimal montoRetenido,
			@JsonProperty("monto_devuelto") BigDecimal montoDevuelto,
			String estado,
			@JsonProperty("fecha_resolucion") Instant fechaResolucion) {
		public static DepositoDTO desde(DepositoInfo deposito) {
			return new DepositoDTO(deposito.montoRetenido(), deposito.montoDevuelto(),
					deposito.estado(), deposito.fechaResolucion());
		}
	}

	public record LiquidacionDTO(
			@JsonProperty("monto_bruto") BigDecimal montoBruto,
			@JsonProperty("comision_descontada") BigDecimal comisionDescontada,
			BigDecimal penalizacion,
			@JsonProperty("monto_neto") BigDecimal montoNeto,
			String estado,
			@JsonProperty("fecha_desembolso") Instant fechaDesembolso) {
		public static LiquidacionDTO desde(LiquidacionInfo liquidacion) {
			return liquidacion == null ? null : new LiquidacionDTO(liquidacion.montoBruto(),
					liquidacion.comisionDescontada(), liquidacion.penalizacion(),
					liquidacion.montoNeto(), liquidacion.estado(), liquidacion.fechaDesembolso());
		}
	}

	public static ReservaResponse desde(ReservaDocument reserva) {
		return new ReservaResponse(
				reserva.id(), reserva.idArrendatario(), reserva.idPatineta(), reserva.idPropietario(),
				reserva.cuponCodigo(), reserva.fechaInicio(), reserva.fechaFin(), reserva.estado().name(),
				reserva.subtotalAlquiler(), reserva.descuentoAplicado(), reserva.totalAlquiler(),
				reserva.recargoRetraso(), reserva.penalizacionPropietario(),
				PagoDTO.desde(reserva.pago()), DepositoDTO.desde(reserva.deposito()),
				LiquidacionDTO.desde(reserva.liquidacion()), reserva.fechaCreacion(),
				reserva.fechaActualizacion(), reserva.fechaCheckIn(), reserva.fechaCheckout(),
				reserva.fechaLiberacionPago(), reserva.disputaAbierta(), reserva.motivoCancelacion(),
				reserva.observaciones());
	}
}
