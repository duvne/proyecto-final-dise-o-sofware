package com.volt.catalogoflota.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.volt.catalogoflota.document.PatinetaDocument;

public record PatinetaResponseDTO(
		@JsonProperty("_id") String id,
		@JsonProperty("id_propietario") String idPropietario,
		String categoria,
		String marca,
		String modelo,
		@JsonProperty("autonomia_km") Double autonomiaKm,
		@JsonProperty("velocidad_max") Double velocidadMaxima,
		@JsonProperty("estado_fisico") String estadoFisico,
		@JsonProperty("estado_publicacion") String estadoPublicacion,
		boolean activa,
		TarifaDTO tarifa,
		@JsonProperty("punto_entrega") PuntoEntregaDTO puntoEntrega) {

	public static PatinetaResponseDTO desde(PatinetaDocument document) {
		var tarifa = document.tarifa();
		var punto = document.puntoEntrega();
		var zona = punto.zona();
		return new PatinetaResponseDTO(
				document.id(),
				document.idPropietario(),
				document.categoria(),
				document.marca(),
				document.modelo(),
				document.autonomiaKm(),
				document.velocidadMaxima(),
				document.estadoFisico(),
				document.estadoPublicacion(),
				document.activa(),
				new TarifaDTO(tarifa.valorHora(), tarifa.valorDia(), tarifa.valorDeposito()),
				new PuntoEntregaDTO(
						punto.idPunto(),
						punto.direccion(),
						punto.latitud(),
						punto.longitud(),
						punto.verificado(),
						new ZonaDTO(zona.idZona(), zona.nombre(), zona.ciudad())));
	}
}
