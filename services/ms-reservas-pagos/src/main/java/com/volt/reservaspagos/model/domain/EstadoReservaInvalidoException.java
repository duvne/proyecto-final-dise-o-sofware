package com.volt.reservaspagos.model.domain;

public class EstadoReservaInvalidoException extends RuntimeException {
	public EstadoReservaInvalidoException(String mensaje) {
		super(mensaje);
	}
}
