package com.volt.operaciones.persistence;

import java.util.Optional;

import com.volt.operaciones.domain.DisputaDocument;

public interface IDisputaDAO {
	DisputaDocument guardar(DisputaDocument disputa);
	Optional<DisputaDocument> buscarPorReserva(String idReserva);
	DisputaDocument actualizar(DisputaDocument disputa);
	java.util.List<DisputaDocument> listarTodas();
}
