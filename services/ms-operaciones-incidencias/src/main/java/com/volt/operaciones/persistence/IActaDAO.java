package com.volt.operaciones.persistence;

import java.util.List;
import java.util.Optional;

import com.volt.operaciones.domain.ActaDocument;

public interface IActaDAO {
	ActaDocument guardar(ActaDocument acta);
	Optional<ActaDocument> buscarPorId(String id);
	Optional<ActaDocument> buscarPorReserva(String idReserva);
	List<ActaDocument> listar();
}
