package com.volt.usuariosauth.domain;

import java.time.Instant;
import java.util.List;

public record Usuario(
		String id,
		String nombre,
		String documento,
		String correo,
		String passwordHash,
		String telefono,
		String fechaNacimiento,
		boolean verificado,
		String estadoCuenta,
		Instant fechaRegistro,
		List<String> roles) {
	public Usuario {
		roles = List.copyOf(roles);
	}
}
