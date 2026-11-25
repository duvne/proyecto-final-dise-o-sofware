package com.volt.catalogoflota.business;

import java.util.List;

import org.springframework.stereotype.Service;

import com.volt.catalogoflota.document.PatinetaDocument;
import com.volt.catalogoflota.document.PuntoEntregaDocument;
import com.volt.catalogoflota.document.TarifaDocument;
import com.volt.catalogoflota.document.ZonaDocument;
import com.volt.catalogoflota.persistence.FiltroPatinetas;
import com.volt.catalogoflota.persistence.IPatinetaDAO;
import com.volt.catalogoflota.view.dto.PatinetaPageResponseDTO;
import com.volt.catalogoflota.view.dto.PatinetaRequestDTO;
import com.volt.catalogoflota.view.dto.PatinetaResponseDTO;

@Service
public class PatinetaService {
	private final IPatinetaDAO patinetaDAO;

	public PatinetaService(IPatinetaDAO patinetaDAO) {
		this.patinetaDAO = patinetaDAO;
	}

	public PatinetaResponseDTO crearBorrador(PatinetaRequestDTO request) {
		var zona = request.puntoEntrega().zona();
		var tarifa = request.tarifa();
		var puntoEntrega = request.puntoEntrega();
		PatinetaDocument patineta = new PatinetaDocument(
				null,
				request.idPropietario(),
				request.categoria(),
				request.marca(),
				request.modelo(),
				request.autonomiaKm(),
				request.velocidadMaxima(),
				request.estadoFisico(),
				"BORRADOR",
				false,
				new TarifaDocument(tarifa.valorHora(), tarifa.valorDia(), tarifa.valorDeposito()),
				new PuntoEntregaDocument(
						puntoEntrega.idPunto(),
						puntoEntrega.direccion(),
						puntoEntrega.latitud(),
						puntoEntrega.longitud(),
						puntoEntrega.verificado(),
						new ZonaDocument(zona.idZona(), zona.nombre(), zona.ciudad())));
		return PatinetaResponseDTO.desde(patinetaDAO.guardar(patineta));
	}

	public List<PatinetaResponseDTO> listarActivas(String categoria) {
		return patinetaDAO.listarActivas(categoria).stream()
				.map(PatinetaResponseDTO::desde)
				.toList();
	}

	public PatinetaPageResponseDTO buscarActivas(FiltroPatinetas filtros) {
		return PatinetaPageResponseDTO.desde(patinetaDAO.buscarActivas(filtros));
	}

	public PatinetaResponseDTO obtenerActiva(String id) {
		return patinetaDAO.buscarActivaPorId(id)
				.map(PatinetaResponseDTO::desde)
				.orElseThrow(() -> new PatinetaNoEncontradaException(id));
	}
}
