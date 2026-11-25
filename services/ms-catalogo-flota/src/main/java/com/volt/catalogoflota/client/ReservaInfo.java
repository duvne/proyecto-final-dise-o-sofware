package com.volt.catalogoflota.client;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReservaInfo(
		@JsonProperty("_id") String id,
		@JsonProperty("id_arrendatario") String idArrendatario,
		@JsonProperty("id_patineta") String idPatineta,
		@JsonProperty("id_propietario") String idPropietario,
		String estado,
		@JsonProperty("fecha_fin") Instant fechaFin,
		@JsonProperty("fecha_checkout") Instant fechaCheckout) {
}
