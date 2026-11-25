package com.volt.catalogoflota.persistence;

import java.util.List;
import java.util.Optional;

import com.volt.catalogoflota.document.ZonaCatalogoDocument;

public interface IZonaDAO {
	List<ZonaCatalogoDocument> listar();

	Optional<ZonaCatalogoDocument> buscarPorId(String id);

	ZonaCatalogoDocument guardar(ZonaCatalogoDocument zona);

	ZonaCatalogoDocument actualizar(ZonaCatalogoDocument zona);

	void eliminar(String id);
}
