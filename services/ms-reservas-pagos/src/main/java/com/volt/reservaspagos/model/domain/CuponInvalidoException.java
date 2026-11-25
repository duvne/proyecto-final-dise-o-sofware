package com.volt.reservaspagos.model.domain;

public class CuponInvalidoException extends RuntimeException {
	public CuponInvalidoException(String mensaje) {
		super(mensaje);
	}
}
