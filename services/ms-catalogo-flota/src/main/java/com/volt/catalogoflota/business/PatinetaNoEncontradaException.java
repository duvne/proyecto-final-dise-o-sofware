package com.volt.catalogoflota.business;

public class PatinetaNoEncontradaException extends RuntimeException {
	public PatinetaNoEncontradaException(String id) {
		super("No se encontro una patineta activa con id " + id + ".");
	}
}
