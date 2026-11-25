package com.volt.catalogoflota.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.volt.catalogoflota.document.CategoriaDocument;
import com.volt.catalogoflota.document.ZonaCatalogoDocument;
import com.volt.catalogoflota.persistence.ICategoriaDAO;
import com.volt.catalogoflota.persistence.IZonaDAO;

@Component
public class CatalogoSeeder implements ApplicationRunner {
	private static final Logger LOG = LoggerFactory.getLogger(CatalogoSeeder.class);

	private final ICategoriaDAO categoriaDAO;
	private final IZonaDAO zonaDAO;

	public CatalogoSeeder(ICategoriaDAO categoriaDAO, IZonaDAO zonaDAO) {
		this.categoriaDAO = categoriaDAO;
		this.zonaDAO = zonaDAO;
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
			if (categoriaDAO.listar().isEmpty()) {
			categoriaDAO.guardar(new CategoriaDocument(null, "Urbana",
					"Patinetas para desplazamientos diarios en ciudad.", 20.0, true));
			categoriaDAO.guardar(new CategoriaDocument(null, "Deportiva",
					"Mayor velocidad y autonomia para trayectos largos.", 30.0, true));
				categoriaDAO.guardar(new CategoriaDocument(null, "Todo terreno",
						"Llantas reforzadas para vias irregulares.", 40.0, true));
				LOG.info("Categorias base de catalogo creadas.");
			}
			if (zonaDAO.listar().isEmpty()) {
				zonaDAO.guardar(new ZonaCatalogoDocument(null, 1L, "Norte", "Barranquilla", true));
				zonaDAO.guardar(new ZonaCatalogoDocument(null, 2L, "Centro", "Barranquilla", true));
				zonaDAO.guardar(new ZonaCatalogoDocument(null, 3L, "El Prado", "Barranquilla", true));
				LOG.info("Zonas base de catalogo creadas.");
			}
		} catch (RuntimeException exception) {
			LOG.debug("El catalogo base no se pudo sembrar durante los tests de contexto: {}", exception.getMessage());
		}
	}
}
