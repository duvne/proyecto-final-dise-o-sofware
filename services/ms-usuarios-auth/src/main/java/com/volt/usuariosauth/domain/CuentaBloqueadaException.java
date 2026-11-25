package com.volt.usuariosauth.domain;

public class CuentaBloqueadaException extends RuntimeException {
	public CuentaBloqueadaException(String mensaje) {
		super(mensaje);
	}
}
