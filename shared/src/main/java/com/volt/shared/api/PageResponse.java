package com.volt.shared.api;

import java.util.List;

public record PageResponse<T>(List<T> elementos, long total, int pagina, int tamano) {
	public PageResponse {
		elementos = List.copyOf(elementos);
	}

	public static <T> PageResponse<T> de(List<T> elementos, long total, int pagina, int tamano) {
		return new PageResponse<>(elementos, total, pagina, tamano);
	}
}
