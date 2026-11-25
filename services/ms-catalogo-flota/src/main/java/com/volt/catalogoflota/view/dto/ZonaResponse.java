package com.volt.catalogoflota.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.volt.catalogoflota.document.ZonaCatalogoDocument;

public record ZonaResponse(
		@JsonProperty("_id") String id,
		@JsonProperty("id_zona") Long idZona,
		String nombre,
		String ciudad,
		boolean activa) {

	public static ZonaResponse desde(ZonaCatalogoDocument document) {
		return new ZonaResponse(document.id(), document.idZona(), document.nombre(),
				document.ciudad(), document.activa());
	}
}
