package com.volt.catalogoflota.persistence;

import java.util.List;

import com.volt.catalogoflota.document.FavoritoDocument;

public interface IFavoritoDAO {
	FavoritoDocument guardar(FavoritoDocument favorito);

	void eliminar(String usuarioId, String patinetaId);

	List<FavoritoDocument> listarPorUsuario(String usuarioId);

	boolean existe(String usuarioId, String patinetaId);
}
