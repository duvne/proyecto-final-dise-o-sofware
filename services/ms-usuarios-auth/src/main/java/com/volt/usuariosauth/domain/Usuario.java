package com.volt.usuariosauth.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record Usuario(
		String id,
		String nombre,
		String documento,
		String correo,
		String passwordHash,
		String telefono,
		LocalDate fechaNacimiento,
		boolean verificado,
		EstadoCuenta estadoCuenta,
		Instant fechaRegistro,
		List<String> roles,
		PerfilPropietario perfilPropietario,
		PerfilArrendatario perfilArrendatario,
		List<Consentimiento> consentimientos,
		String motivoRechazo) {

	public Usuario {
		roles = List.copyOf(roles);
		consentimientos = List.copyOf(consentimientos);
	}

	public Usuario conPasswordHash(String nuevoHash) {
		return new Usuario(id, nombre, documento, correo, nuevoHash, telefono, fechaNacimiento,
				verificado, estadoCuenta, fechaRegistro, roles, perfilPropietario,
				perfilArrendatario, consentimientos, motivoRechazo);
	}

	public Usuario conDatos(String nuevoNombre, String nuevoTelefono) {
		return new Usuario(id, nuevoNombre, documento, correo, passwordHash, nuevoTelefono,
				fechaNacimiento, verificado, estadoCuenta, fechaRegistro, roles, perfilPropietario,
				perfilArrendatario, consentimientos, motivoRechazo);
	}

	public Usuario conPerfilPropietario(PerfilPropietario nuevoPerfil) {
		return new Usuario(id, nombre, documento, correo, passwordHash, telefono, fechaNacimiento,
				verificado, estadoCuenta, fechaRegistro, roles, nuevoPerfil,
				perfilArrendatario, consentimientos, motivoRechazo);
	}

	public Usuario conPerfilArrendatario(PerfilArrendatario nuevoPerfil) {
		return new Usuario(id, nombre, documento, correo, passwordHash, telefono, fechaNacimiento,
				verificado, estadoCuenta, fechaRegistro, roles, perfilPropietario,
				nuevoPerfil, consentimientos, motivoRechazo);
	}

	public Usuario conVerificacion(boolean nuevoVerificado, EstadoCuenta nuevoEstado, String motivo) {
		return new Usuario(id, nombre, documento, correo, passwordHash, telefono, fechaNacimiento,
				nuevoVerificado, nuevoEstado, fechaRegistro, roles, perfilPropietario,
				perfilArrendatario, consentimientos, motivo);
	}

	public Usuario conEstado(EstadoCuenta nuevoEstado, String motivo) {
		return new Usuario(id, nombre, documento, correo, passwordHash, telefono, fechaNacimiento,
				verificado, nuevoEstado, fechaRegistro, roles, perfilPropietario,
				perfilArrendatario, consentimientos, motivo);
	}

	public Usuario conRoles(List<String> nuevosRoles) {
		return new Usuario(id, nombre, documento, correo, passwordHash, telefono, fechaNacimiento,
				verificado, estadoCuenta, fechaRegistro, List.copyOf(nuevosRoles), perfilPropietario,
				perfilArrendatario, consentimientos, motivoRechazo);
	}

	public Usuario conConsentimiento(Consentimiento consentimiento) {
		List<Consentimiento> nuevos = new ArrayList<>(consentimientos);
		nuevos.add(consentimiento);
		return new Usuario(id, nombre, documento, correo, passwordHash, telefono, fechaNacimiento,
				verificado, estadoCuenta, fechaRegistro, roles, perfilPropietario,
				perfilArrendatario, nuevos, motivoRechazo);
	}

	public Usuario anonimizado() {
		return new Usuario(
				id,
				"Usuario eliminado",
				"ANON-" + id,
				"anon-" + id + "@volt.local",
				"!",
				null,
				null,
				false,
				EstadoCuenta.ELIMINADA,
				fechaRegistro,
				List.of(),
				null,
				null,
				List.of(),
				"Cuenta eliminada por solicitud del titular");
	}
}
