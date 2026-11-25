package com.volt.catalogoflota.document;

import java.time.Instant;

public record CalificacionDocument(
		String id,
		String patinetaId,
		String propietarioId,
		String reservaId,
		String emisorId,
		int puntaje,
		String comentario,
		String replica,
		boolean reportada,
		String motivoReporte,
		boolean oculta,
		Instant fechaRegistro) {
}
