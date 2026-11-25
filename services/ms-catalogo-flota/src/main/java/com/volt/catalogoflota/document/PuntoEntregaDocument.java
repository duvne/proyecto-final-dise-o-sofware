package com.volt.catalogoflota.document;

public record PuntoEntregaDocument(
		Long idPunto,
		String direccion,
		Double latitud,
		Double longitud,
		boolean verificado,
		ZonaDocument zona) {
}
