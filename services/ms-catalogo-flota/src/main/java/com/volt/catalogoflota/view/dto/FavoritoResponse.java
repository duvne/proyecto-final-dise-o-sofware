package com.volt.catalogoflota.view.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.volt.catalogoflota.document.FavoritoDocument;

public record FavoritoResponse(
		@JsonProperty("_id") String id,
		@JsonProperty("patineta_id") String patinetaId,
		@JsonProperty("fecha_registro") Instant fechaRegistro) {

	public static FavoritoResponse desde(FavoritoDocument document) {
		return new FavoritoResponse(document.id(), document.patinetaId(), document.fechaRegistro());
	}
}
