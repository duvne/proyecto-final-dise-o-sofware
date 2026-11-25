package com.volt.catalogoflota.persistence;

import java.util.List;

import com.volt.catalogoflota.document.AlertaDisponibilidadDocument;

public interface IAlertaDAO {
	AlertaDisponibilidadDocument guardar(AlertaDisponibilidadDocument alerta);

	List<AlertaDisponibilidadDocument> listarPorUsuario(String usuarioId);

	void eliminar(String id, String usuarioId);
}
