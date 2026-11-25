package com.volt.catalogoflota.persistence;

import java.util.List;
import java.util.Optional;

import com.volt.catalogoflota.document.PatinetaDocument;

public interface IPatinetaDAO {
	PatinetaDocument guardar(PatinetaDocument patineta);

	Optional<PatinetaDocument> buscarActivaPorId(String id);

	List<PatinetaDocument> listarActivas(String categoria);

	PatinetaPage buscarActivas(FiltroPatinetas filtros);
}
