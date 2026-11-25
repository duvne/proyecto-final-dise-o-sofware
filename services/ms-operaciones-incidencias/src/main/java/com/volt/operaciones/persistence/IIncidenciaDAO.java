package com.volt.operaciones.persistence;

import java.util.List;
import java.util.Optional;

import com.volt.operaciones.domain.IncidenciaDocument;

public interface IIncidenciaDAO {
	IncidenciaDocument guardar(IncidenciaDocument incidencia);
	Optional<IncidenciaDocument> buscarPorId(String id);
	List<IncidenciaDocument> listarPorReserva(String idReserva);
	IncidenciaDocument actualizar(IncidenciaDocument incidencia);
}
