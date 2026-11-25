package com.volt.reservaspagos.persistence;

import java.util.List;

import com.volt.reservaspagos.model.domain.ContraCargoDocument;

public interface IContraCargoDAO {
	ContraCargoDocument guardar(ContraCargoDocument contraCargo);

	List<ContraCargoDocument> listar();
}
