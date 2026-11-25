package com.volt.catalogoflota.view.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record TarifaDTO(
		@JsonProperty("valor_hora") @NotNull @PositiveOrZero BigDecimal valorHora,
		@JsonProperty("valor_dia") @NotNull @PositiveOrZero BigDecimal valorDia,
		@JsonProperty("valor_deposito") @NotNull @PositiveOrZero BigDecimal valorDeposito) {
}
