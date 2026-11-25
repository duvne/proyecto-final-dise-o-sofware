package com.volt.catalogoflota.document;

public record ZonaCatalogoDocument(
		String id,
		Long idZona,
		String nombre,
		String ciudad,
		boolean activa) {
}
