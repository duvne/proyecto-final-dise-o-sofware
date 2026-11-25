package com.volt.usuariosauth.security;

import java.util.List;

public record TokenPrincipal(String userId, List<String> roles) {
	public TokenPrincipal {
		roles = List.copyOf(roles);
	}
}
