package com.volt.catalogoflota.persistence;

import java.math.BigDecimal;

public record FiltroPatinetas(
		String categoria,
		String ciudad,
		BigDecimal precioHoraMaximo,
		Double autonomiaMinimaKm,
		int pagina,
		int tamano) {
	public FiltroPatinetas {
		if (pagina < 0 || pagina > 10_000) {
			throw new IllegalArgumentException("La pagina debe estar entre 0 y 10000.");
		}
		if (tamano < 1 || tamano > 50) {
			throw new IllegalArgumentException("El tamano de pagina debe estar entre 1 y 50.");
		}
		if (precioHoraMaximo != null && precioHoraMaximo.signum() < 0) {
			throw new IllegalArgumentException("El precio maximo no puede ser negativo.");
		}
		if (autonomiaMinimaKm != null && autonomiaMinimaKm < 0) {
			throw new IllegalArgumentException("La autonomia minima no puede ser negativa.");
		}
	}
}
