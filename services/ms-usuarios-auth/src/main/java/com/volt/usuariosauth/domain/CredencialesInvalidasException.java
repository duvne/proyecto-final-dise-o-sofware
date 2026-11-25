package com.volt.usuariosauth.domain;

public class CredencialesInvalidasException extends RuntimeException {
	public CredencialesInvalidasException() {
		super("Correo o contrasena incorrectos.");
	}
}
