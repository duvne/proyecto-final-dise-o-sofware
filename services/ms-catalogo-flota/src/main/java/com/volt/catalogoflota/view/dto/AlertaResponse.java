package com.volt.catalogoflota.view.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.volt.catalogoflota.document.AlertaDisponibilidadDocument;

public record AlertaResponse(
		@JsonProperty("_id") String id,
		@JsonProperty("patineta_id") String patinetaId,
		Instant inicio,
		Instant fin,
		boolean activa,
		@JsonProperty("fecha_registro") Instant fechaRegistro) {

	public static AlertaResponse desde(AlertaDisponibilidadDocument document) {
		return new AlertaResponse(document.id(), document.patinetaId(), document.inicio(), document.fin(),
				document.activa(), document.fechaRegistro());
	}
}
