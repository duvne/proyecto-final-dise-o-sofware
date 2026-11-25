package com.volt.reservaspagos.view.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReportePagoResponse(
		Instant desde,
		Instant hasta,
		@JsonProperty("total_reservas") long totalReservas,
		@JsonProperty("total_ingresos") BigDecimal totalIngresos,
		@JsonProperty("total_comisiones") BigDecimal totalComisiones,
		@JsonProperty("total_liquidado") BigDecimal totalLiquidado,
		@JsonProperty("por_estado") Map<String, Long> porEstado) {
}
