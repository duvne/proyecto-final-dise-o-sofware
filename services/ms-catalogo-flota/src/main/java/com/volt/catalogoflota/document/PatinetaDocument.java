package com.volt.catalogoflota.document;

import java.time.Instant;
import java.util.List;

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
		PuntoEntregaDocument puntoEntrega,
		List<String> fotos,
		List<String> documentos,
		String observacionesModeracion,
		boolean enMantenimiento,
		double calificacionPromedio,
		int totalCalificaciones,
		Instant fechaRegistro,
		Instant fechaActualizacion) {

	public PatinetaDocument {
		fotos = fotos == null ? List.of() : List.copyOf(fotos);
		documentos = documentos == null ? List.of() : List.copyOf(documentos);
	}

	public PatinetaDocument(
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
		this(id, idPropietario, categoria, marca, modelo, autonomiaKm, velocidadMaxima,
				estadoFisico, estadoPublicacion, activa, tarifa, puntoEntrega,
				List.of(), List.of(), null, false, 0.0, 0, Instant.now(), Instant.now());
	}
}
