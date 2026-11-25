package com.volt.reservaspagos.model.domain;

public class ReservaNoEncontradaException extends RuntimeException {
	public ReservaNoEncontradaException(String id) {
		super("No existe la reserva " + id + ".");
	}
}
