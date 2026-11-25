package com.volt.catalogoflota.document;

import java.time.Instant;

public record FavoritoDocument(
		String id,
		String usuarioId,
		String patinetaId,
		Instant fechaRegistro) {
}
