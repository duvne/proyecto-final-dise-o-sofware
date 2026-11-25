package com.volt.catalogoflota.persistence;

import java.util.List;

import com.volt.catalogoflota.document.MantenimientoDocument;

public interface IMantenimientoDAO {
	MantenimientoDocument guardar(MantenimientoDocument mantenimiento);

	List<MantenimientoDocument> listarPorPatineta(String patinetaId);
}
