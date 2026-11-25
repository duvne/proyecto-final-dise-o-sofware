package com.volt.reservaspagos.view.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.volt.reservaspagos.model.domain.CuponDocument;

public record CuponResponse(
		@JsonProperty("_id") String id,
		String codigo,
		BigDecimal porcentaje,
		@JsonProperty("tope_descuento") BigDecimal topeDescuento,
		@JsonProperty("fecha_vencimiento") Instant fechaVencimiento,
		boolean activo,
		int usos,
		@JsonProperty("limite_usos") int limiteUsos) {

	public static CuponResponse desde(CuponDocument cupon) {
		return new CuponResponse(cupon.id(), cupon.codigo(), cupon.porcentaje(),
				cupon.topeDescuento(), cupon.fechaVencimiento(), cupon.activo(), cupon.usos(),
				cupon.limiteUsos());
	}
}
