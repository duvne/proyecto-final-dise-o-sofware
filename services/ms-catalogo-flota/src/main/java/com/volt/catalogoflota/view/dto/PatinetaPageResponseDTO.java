package com.volt.catalogoflota.view.dto;

import java.util.List;

import com.volt.catalogoflota.persistence.PatinetaPage;

public record PatinetaPageResponseDTO(
		List<PatinetaResponseDTO> elementos,
		long total,
		int pagina,
		int tamano) {
	public PatinetaPageResponseDTO {
		elementos = List.copyOf(elementos);
	}

	public static PatinetaPageResponseDTO desde(PatinetaPage page) {
		return new PatinetaPageResponseDTO(
				page.elementos().stream().map(PatinetaResponseDTO::desde).toList(),
				page.total(),
				page.pagina(),
				page.tamano());
	}
}
