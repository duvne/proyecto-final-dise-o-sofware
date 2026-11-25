package com.volt.catalogoflota.business;

import java.util.List;

import org.springframework.stereotype.Service;

import com.volt.catalogoflota.document.ZonaCatalogoDocument;
import com.volt.catalogoflota.persistence.IZonaDAO;
import com.volt.catalogoflota.view.dto.ZonaRequest;
import com.volt.catalogoflota.view.dto.ZonaResponse;
import com.volt.shared.audit.AuditoriaService;

@Service
public class ZonaService {
	private final IZonaDAO zonaDAO;
	private final AuditoriaService auditoriaService;

	public ZonaService(IZonaDAO zonaDAO, AuditoriaService auditoriaService) {
		this.zonaDAO = zonaDAO;
		this.auditoriaService = auditoriaService;
	}

	public List<ZonaResponse> listar(boolean soloActivas) {
		return zonaDAO.listar().stream()
				.filter(zona -> !soloActivas || zona.activa())
				.map(ZonaResponse::desde)
				.toList();
	}

	public ZonaResponse crear(ZonaRequest request, String adminId, String ip) {
		ZonaCatalogoDocument creada = zonaDAO.guardar(new ZonaCatalogoDocument(null, request.idZona(),
				request.nombre().trim(), request.ciudad().trim(), request.activa()));
		auditoriaService.registrar(adminId, "CREAR_ZONA", "ZONA", creada.id(), null,
				request.nombre(), ip);
		return ZonaResponse.desde(creada);
	}

	public ZonaResponse actualizar(String id, ZonaRequest request, String adminId, String ip) {
		ZonaCatalogoDocument actual = zonaDAO.buscarPorId(id)
				.orElseThrow(() -> new IllegalArgumentException("La zona no existe."));
		ZonaCatalogoDocument actualizada = zonaDAO.actualizar(new ZonaCatalogoDocument(id, request.idZona(),
				request.nombre().trim(), request.ciudad().trim(), request.activa()));
		auditoriaService.registrar(adminId, "ACTUALIZAR_ZONA", "ZONA", id, actual.nombre(),
				actualizada.nombre(), ip);
		return ZonaResponse.desde(actualizada);
	}

	public void eliminar(String id, String adminId, String ip) {
		ZonaCatalogoDocument actual = zonaDAO.buscarPorId(id)
				.orElseThrow(() -> new IllegalArgumentException("La zona no existe."));
		zonaDAO.eliminar(id);
		auditoriaService.registrar(adminId, "ELIMINAR_ZONA", "ZONA", id, actual.nombre(), null, ip);
	}
}
