package com.volt.usuariosauth.domain;

public class NoEncontradoException extends RuntimeException {
	public NoEncontradoException(String mensaje) {
		super(mensaje);
	}
}
