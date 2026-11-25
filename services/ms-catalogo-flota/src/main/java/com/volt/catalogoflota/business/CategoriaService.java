package com.volt.catalogoflota.business;

import java.util.List;

import org.springframework.stereotype.Service;

import com.volt.catalogoflota.document.CategoriaDocument;
import com.volt.catalogoflota.persistence.ICategoriaDAO;
import com.volt.catalogoflota.view.dto.CategoriaRequest;
import com.volt.catalogoflota.view.dto.CategoriaResponse;
import com.volt.shared.audit.AuditoriaService;

@Service
public class CategoriaService {
	private final ICategoriaDAO categoriaDAO;
	private final AuditoriaService auditoriaService;

	public CategoriaService(ICategoriaDAO categoriaDAO, AuditoriaService auditoriaService) {
		this.categoriaDAO = categoriaDAO;
		this.auditoriaService = auditoriaService;
	}

	public List<CategoriaResponse> listar(boolean soloActivas) {
		return categoriaDAO.listar().stream()
				.filter(categoria -> !soloActivas || categoria.activa())
				.map(CategoriaResponse::desde)
				.toList();
	}

	public CategoriaResponse crear(CategoriaRequest request, String adminId, String ip) {
		if (categoriaDAO.buscarPorNombre(request.nombre().trim()).isPresent()) {
			throw new IllegalArgumentException("Ya existe una categoria con ese nombre.");
		}
		CategoriaDocument creada = categoriaDAO.guardar(new CategoriaDocument(null, request.nombre().trim(),
				request.descripcion(), request.autonomiaMinimaKm(), request.activa()));
		auditoriaService.registrar(adminId, "CREAR_CATEGORIA", "CATEGORIA", creada.id(), null,
				request.nombre(), ip);
		return CategoriaResponse.desde(creada);
	}

	public CategoriaResponse actualizar(String id, CategoriaRequest request, String adminId, String ip) {
		CategoriaDocument actual = categoriaDAO.buscarPorId(id)
				.orElseThrow(() -> new IllegalArgumentException("La categoria no existe."));
		categoriaDAO.buscarPorNombre(request.nombre().trim())
				.filter(otra -> !otra.id().equals(id))
				.ifPresent(otra -> {
					throw new IllegalArgumentException("Ya existe una categoria con ese nombre.");
				});
		CategoriaDocument actualizada = categoriaDAO.actualizar(new CategoriaDocument(id, request.nombre().trim(),
				request.descripcion(), request.autonomiaMinimaKm(), request.activa()));
		auditoriaService.registrar(adminId, "ACTUALIZAR_CATEGORIA", "CATEGORIA", id,
				actual.nombre(), actualizada.nombre(), ip);
		return CategoriaResponse.desde(actualizada);
	}

	public void eliminar(String id, String adminId, String ip) {
		CategoriaDocument actual = categoriaDAO.buscarPorId(id)
				.orElseThrow(() -> new IllegalArgumentException("La categoria no existe."));
		categoriaDAO.eliminar(id);
		auditoriaService.registrar(adminId, "ELIMINAR_CATEGORIA", "CATEGORIA", id, actual.nombre(), null, ip);
	}
}
