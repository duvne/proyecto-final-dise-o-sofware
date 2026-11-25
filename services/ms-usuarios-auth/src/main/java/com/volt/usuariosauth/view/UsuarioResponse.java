package com.volt.usuariosauth.view;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.volt.usuariosauth.domain.Consentimiento;
import com.volt.usuariosauth.domain.PerfilArrendatario;
import com.volt.usuariosauth.domain.PerfilPropietario;
import com.volt.usuariosauth.domain.Usuario;

public record UsuarioResponse(
		@JsonProperty("_id") String id,
		String nombre,
		String documento,
		String correo,
		String telefono,
		@JsonProperty("fecha_nacimiento") LocalDate fechaNacimiento,
		boolean verificado,
		@JsonProperty("estado_cuenta") String estadoCuenta,
		@JsonProperty("fecha_registro") Instant fechaRegistro,
		List<String> roles,
		@JsonProperty("perfil_propietario") PerfilPropietario perfilPropietario,
		@JsonProperty("perfil_arrendatario") PerfilArrendatario perfilArrendatario,
		List<Consentimiento> consentimientos,
		@JsonProperty("motivo_rechazo") String motivoRechazo) {

	public UsuarioResponse {
		roles = List.copyOf(roles);
		consentimientos = List.copyOf(consentimientos);
	}

	public static UsuarioResponse desde(Usuario usuario) {
		return new UsuarioResponse(
				usuario.id(),
				usuario.nombre(),
				usuario.documento(),
				usuario.correo(),
				usuario.telefono(),
				usuario.fechaNacimiento(),
				usuario.verificado(),
				usuario.estadoCuenta().name(),
				usuario.fechaRegistro(),
				usuario.roles(),
				usuario.perfilPropietario(),
				usuario.perfilArrendatario(),
				usuario.consentimientos(),
				usuario.motivoRechazo());
	}
}
