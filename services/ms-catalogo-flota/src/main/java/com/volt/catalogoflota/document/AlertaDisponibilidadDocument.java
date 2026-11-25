package com.volt.catalogoflota.document;

import java.time.Instant;

public record AlertaDisponibilidadDocument(
		String id,
		String usuarioId,
		String patinetaId,
		Instant inicio,
		Instant fin,
		boolean activa,
		Instant fechaRegistro) {
}
