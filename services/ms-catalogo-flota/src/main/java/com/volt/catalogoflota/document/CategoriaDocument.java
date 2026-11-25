package com.volt.catalogoflota.document;

public record CategoriaDocument(
		String id,
		String nombre,
		String descripcion,
		Double autonomiaMinimaKm,
		boolean activa) {
}
