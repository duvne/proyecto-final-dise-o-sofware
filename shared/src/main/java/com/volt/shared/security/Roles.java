package com.volt.shared.security;

import java.util.List;

public final class Roles {
	public static final String ARRENDATARIO = "ARRENDATARIO";
	public static final String PROPIETARIO = "PROPIETARIO";
	public static final String ADMINISTRADOR = "ADMINISTRADOR";

	private Roles() {
	}

	public static List<String> normalizar(List<String> roles) {
		if (roles == null || roles.isEmpty()) {
			return List.of(ARRENDATARIO);
		}
		return roles.stream()
				.filter(rol -> rol != null && !rol.isBlank())
				.map(String::toUpperCase)
				.distinct()
				.toList();
	}
}
