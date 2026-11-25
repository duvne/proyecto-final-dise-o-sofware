package com.volt.catalogoflota.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.volt.catalogoflota.document.CategoriaDocument;

public record CategoriaResponse(
		@JsonProperty("_id") String id,
		String nombre,
		String descripcion,
		@JsonProperty("autonomia_minima_km") Double autonomiaMinimaKm,
		boolean activa) {

	public static CategoriaResponse desde(CategoriaDocument document) {
		return new CategoriaResponse(document.id(), document.nombre(), document.descripcion(),
				document.autonomiaMinimaKm(), document.activa());
	}
}
