package com.volt.catalogoflota.business;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.volt.catalogoflota.document.FavoritoDocument;
import com.volt.catalogoflota.persistence.IFavoritoDAO;
import com.volt.catalogoflota.persistence.IPatinetaDAO;
import com.volt.catalogoflota.view.dto.FavoritoResponse;
import com.volt.shared.security.TokenPrincipal;

@Service
public class FavoritoService {
	private final IFavoritoDAO favoritoDAO;
	private final IPatinetaDAO patinetaDAO;

	public FavoritoService(IFavoritoDAO favoritoDAO, IPatinetaDAO patinetaDAO) {
		this.favoritoDAO = favoritoDAO;
		this.patinetaDAO = patinetaDAO;
	}

	public FavoritoResponse agregar(String patinetaId, TokenPrincipal principal) {
		patinetaDAO.buscarActivaPorId(patinetaId)
				.orElseThrow(() -> new PatinetaNoEncontradaException(patinetaId));
		if (favoritoDAO.existe(principal.userId(), patinetaId)) {
			throw new IllegalArgumentException("La patineta ya esta en tus favoritos.");
		}
		FavoritoDocument favorito = favoritoDAO.guardar(new FavoritoDocument(UUID.randomUUID().toString(),
				principal.userId(), patinetaId, Instant.now()));
		return FavoritoResponse.desde(favorito);
	}

	public void eliminar(String patinetaId, TokenPrincipal principal) {
		favoritoDAO.eliminar(principal.userId(), patinetaId);
	}

	public List<FavoritoResponse> listar(TokenPrincipal principal) {
		return favoritoDAO.listarPorUsuario(principal.userId()).stream()
				.map(FavoritoResponse::desde).toList();
	}
}
