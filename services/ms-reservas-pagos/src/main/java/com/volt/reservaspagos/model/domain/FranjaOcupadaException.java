package com.volt.reservaspagos.model.domain;

public class FranjaOcupadaException extends RuntimeException {
	public FranjaOcupadaException() {
		super("La patineta ya esta reservada en el horario solicitado.");
	}
}
