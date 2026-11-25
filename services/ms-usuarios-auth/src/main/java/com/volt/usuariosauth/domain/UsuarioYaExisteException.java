package com.volt.usuariosauth.domain;

public class UsuarioYaExisteException extends RuntimeException {
	public UsuarioYaExisteException() {
		super("Ya existe una cuenta con ese correo o documento.");
	}
}
