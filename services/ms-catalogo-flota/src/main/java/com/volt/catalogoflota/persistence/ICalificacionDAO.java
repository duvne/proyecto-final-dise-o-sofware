package com.volt.catalogoflota.persistence;

import java.util.List;
import java.util.Optional;

import com.volt.catalogoflota.document.CalificacionDocument;

public interface ICalificacionDAO {
	CalificacionDocument guardar(CalificacionDocument calificacion);

	CalificacionDocument actualizar(CalificacionDocument calificacion);

	Optional<CalificacionDocument> buscarPorId(String id);

	List<CalificacionDocument> listarPorPatineta(String patinetaId, boolean incluirOcultas);
}
