package com.volt.usuariosauth.domain;

public record PerfilPropietario(
		String cuentaDesembolso,
		String bancoDestino,
		int totalPatinetas,
		double calificacionPromedio,
		int totalCalificaciones) {

	public PerfilPropietario conCuenta(String cuentaDesembolso, String bancoDestino) {
		return new PerfilPropietario(cuentaDesembolso, bancoDestino, totalPatinetas,
				calificacionPromedio, totalCalificaciones);
	}

	public PerfilPropietario conReputacion(double promedio, int total) {
		return new PerfilPropietario(cuentaDesembolso, bancoDestino, totalPatinetas, promedio, total);
	}
}
