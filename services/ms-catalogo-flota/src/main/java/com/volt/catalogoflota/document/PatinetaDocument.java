package com.volt.catalogoflota.document;

public record PatinetaDocument(
		String id,
		String idPropietario,
		String categoria,
		String marca,
		String modelo,
		Double autonomiaKm,
		Double velocidadMaxima,
		String estadoFisico,
		String estadoPublicacion,
		boolean activa,
		TarifaDocument tarifa,
		PuntoEntregaDocument puntoEntrega) {
}
