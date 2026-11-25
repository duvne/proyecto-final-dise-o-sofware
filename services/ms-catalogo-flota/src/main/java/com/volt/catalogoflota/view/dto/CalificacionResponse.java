package com.volt.catalogoflota.view.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.volt.catalogoflota.document.CalificacionDocument;

public record CalificacionResponse(
		@JsonProperty("_id") String id,
		@JsonProperty("patineta_id") String patinetaId,
		@JsonProperty("propietario_id") String propietarioId,
		@JsonProperty("reserva_id") String reservaId,
		@JsonProperty("emisor_id") String emisorId,
		int puntaje,
		String comentario,
		String replica,
		boolean reportada,
		@JsonProperty("motivo_reporte") String motivoReporte,
		boolean oculta,
		@JsonProperty("fecha_registro") Instant fechaRegistro) {

	public static CalificacionResponse desde(CalificacionDocument document) {
		return new CalificacionResponse(document.id(), document.patinetaId(), document.propietarioId(),
				document.reservaId(), document.emisorId(), document.puntaje(), document.comentario(),
				document.replica(), document.reportada(), document.motivoReporte(), document.oculta(),
				document.fechaRegistro());
	}
}
