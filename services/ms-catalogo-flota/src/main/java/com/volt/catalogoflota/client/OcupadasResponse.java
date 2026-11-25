package com.volt.catalogoflota.client;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OcupadasResponse(
		@JsonProperty("patinetas_ocupadas") List<String> patinetasOcupadas) {

	public OcupadasResponse {
		patinetasOcupadas = patinetasOcupadas == null ? List.of() : List.copyOf(patinetasOcupadas);
	}
}
