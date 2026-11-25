package com.volt.catalogoflota.persistence;

import java.util.List;

import com.volt.catalogoflota.document.PatinetaDocument;

public record PatinetaPage(List<PatinetaDocument> elementos, long total, int pagina, int tamano) {
	public PatinetaPage {
		elementos = List.copyOf(elementos);
	}
}
