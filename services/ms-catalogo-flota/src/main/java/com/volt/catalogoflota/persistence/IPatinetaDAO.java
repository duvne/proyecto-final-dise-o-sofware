package com.volt.catalogoflota.persistence;

import java.util.List;
import java.util.Optional;

import com.volt.catalogoflota.document.PatinetaDocument;

public interface IPatinetaDAO {
	PatinetaDocument guardar(PatinetaDocument patineta);

	PatinetaDocument actualizar(PatinetaDocument patineta);

	Optional<PatinetaDocument> buscarPorId(String id);

	Optional<PatinetaDocument> buscarActivaPorId(String id);

	List<PatinetaDocument> listarActivas(String categoria);

	List<PatinetaDocument> listarPorPropietario(String idPropietario);

	PatinetaPage listarPendientes(int pagina, int tamano);

	PatinetaPage buscarActivas(FiltroPatinetas filtros);

	void actualizarCalificacion(String id, double promedio, int totalCalificaciones);
}
