package com.volt.usuariosauth.domain;

public class RecuperacionInvalidaException extends RuntimeException {
	public RecuperacionInvalidaException() {
		super("El enlace de recuperacion es invalido o ha vencido.");
	}
}
