package com.volt.catalogoflota.business;

public class ServicioNoDisponibleException extends RuntimeException {
	public ServicioNoDisponibleException(String mensaje) {
		super(mensaje);
	}
}
