package com.volt.usuariosauth.domain;

import java.time.Instant;

public record RegistroAuditoria(
		String id,
		String actorId,
		String accion,
		String entidad,
		String entidadId,
		Object valoresAnteriores,
		Object valoresNuevos,
		String ip,
		Instant timestamp) {
}
