package com.volt.catalogoflota.view.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.volt.catalogoflota.document.MantenimientoDocument;

public record MantenimientoResponse(
		@JsonProperty("_id") String id,
		@JsonProperty("patineta_id") String patinetaId,
		String descripcion,
		@JsonProperty("realizado_por") String realizadoPor,
		@JsonProperty("fecha_registro") Instant fechaRegistro) {

	public static MantenimientoResponse desde(MantenimientoDocument document) {
		return new MantenimientoResponse(document.id(), document.patinetaId(), document.descripcion(),
				document.realizadoPor(), document.fechaRegistro());
	}
}
