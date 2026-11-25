package com.volt.usuariosauth.domain;

public record PerfilArrendatario(
		String medioPagoToken,
		int totalAlquileres,
		double calificacionPromedio,
		int totalCalificaciones) {

	public PerfilArrendatario conMedioPago(String medioPagoToken) {
		return new PerfilArrendatario(medioPagoToken, totalAlquileres,
				calificacionPromedio, totalCalificaciones);
	}

	public PerfilArrendatario conReputacion(double promedio, int total) {
		return new PerfilArrendatario(medioPagoToken, totalAlquileres, promedio, total);
	}
}
