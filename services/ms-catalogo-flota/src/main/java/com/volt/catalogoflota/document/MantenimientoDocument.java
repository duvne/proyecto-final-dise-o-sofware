package com.volt.catalogoflota.document;

import java.time.Instant;

public record MantenimientoDocument(
		String id,
		String patinetaId,
		String descripcion,
		String realizadoPor,
		Instant fechaRegistro) {
}
