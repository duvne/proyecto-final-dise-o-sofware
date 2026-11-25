package com.volt.reservaspagos.view.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FranjaResponse(
		Instant inicio,
		Instant fin,
		String estado) {
}
