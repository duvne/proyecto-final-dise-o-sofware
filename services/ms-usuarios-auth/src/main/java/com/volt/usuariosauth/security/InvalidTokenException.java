package com.volt.usuariosauth.security;

public class InvalidTokenException extends RuntimeException {
	public InvalidTokenException() {
		super("Token de acceso invalido o vencido.");
	}
}
