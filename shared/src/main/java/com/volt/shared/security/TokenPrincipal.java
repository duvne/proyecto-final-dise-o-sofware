package com.volt.shared.security;

import java.util.List;

public record TokenPrincipal(String userId, String correo, List<String> roles, boolean verificado) {
	public TokenPrincipal {
		roles = List.copyOf(roles);
	}

	public boolean tieneRol(String rol) {
		return roles.contains(rol);
	}

	public boolean esAdministrador() {
		return tieneRol(Roles.ADMINISTRADOR);
	}
}
