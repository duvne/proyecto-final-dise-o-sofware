package com.volt.catalogoflota.persistence;

import java.util.List;
import java.util.Optional;

import com.volt.catalogoflota.document.CategoriaDocument;

public interface ICategoriaDAO {
	List<CategoriaDocument> listar();

	Optional<CategoriaDocument> buscarPorId(String id);

	Optional<CategoriaDocument> buscarPorNombre(String nombre);

	CategoriaDocument guardar(CategoriaDocument categoria);

	CategoriaDocument actualizar(CategoriaDocument categoria);

	void eliminar(String id);
}
