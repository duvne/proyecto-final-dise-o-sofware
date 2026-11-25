package com.volt.reservaspagos.view.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EstadoCuentaResponse(
		@JsonProperty("total_reservas") long totalReservas,
		@JsonProperty("total_ingresos") BigDecimal totalIngresos,
		@JsonProperty("total_comisiones") BigDecimal totalComisiones,
		@JsonProperty("total_liquidado") BigDecimal totalLiquidado,
		@JsonProperty("pendiente_liquidar") BigDecimal pendienteLiquidar,
		List<ReservaResponse> reservas) {
}
