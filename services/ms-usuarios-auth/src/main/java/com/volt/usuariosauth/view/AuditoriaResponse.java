package com.volt.usuariosauth.view;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.volt.usuariosauth.domain.RegistroAuditoria;

public record AuditoriaResponse(
		@JsonProperty("_id") String id,
		@JsonProperty("actor_id") String actorId,
		String accion,
		String entidad,
		@JsonProperty("entidad_id") String entidadId,
		@JsonProperty("valores_anteriores") Object valoresAnteriores,
		@JsonProperty("valores_nuevos") Object valoresNuevos,
		String ip,
		Instant timestamp) {

	public static AuditoriaResponse desde(RegistroAuditoria registro) {
		return new AuditoriaResponse(
				registro.id(),
				registro.actorId(),
				registro.accion(),
				registro.entidad(),
				registro.entidadId(),
				registro.valoresAnteriores(),
				registro.valoresNuevos(),
				registro.ip(),
				registro.timestamp());
	}
}
