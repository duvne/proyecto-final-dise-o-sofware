package com.volt.reservaspagos.persistence;

import java.util.List;
import java.util.Optional;

import com.volt.reservaspagos.model.domain.CuponDocument;

public interface ICuponDAO {
	CuponDocument guardar(CuponDocument cupon);

	CuponDocument actualizar(CuponDocument cupon);

	Optional<CuponDocument> buscarPorCodigo(String codigo);

	Optional<CuponDocument> buscarPorId(String id);

	List<CuponDocument> listar();

	void eliminar(String id);

	void incrementarUso(String id);
}
