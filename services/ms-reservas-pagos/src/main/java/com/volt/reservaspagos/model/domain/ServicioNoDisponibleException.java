package com.volt.reservaspagos.model.domain;

public class ServicioNoDisponibleException extends RuntimeException {
	public ServicioNoDisponibleException(String mensaje) {
		super(mensaje);
	}
}
