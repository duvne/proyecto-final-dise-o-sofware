package com.volt.operaciones.business;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.volt.operaciones.domain.ActaDocument;

@Service
public class AutorizarGastosService {
	private final Map<String, ActaDocument> actas = new HashMap<>();

	public ActaDocument createParaGastos(String idReserva, String propietarioId, String arrendatarioId,
			List<String> itemsDescripcion, List<BigDecimal> itemsPorcentajes, String resoluciones,
			String actorId) {
		BigDecimal total = BigDecimal.ZERO;
		if (itemsDescripcion.size() != itemsPorcentajes.size()) {
			throw new IllegalArgumentException("Los porcentajes deben corresponder a las descripciones.");
		}
		for (BigDecimal porcentaje : itemsPorcentajes) {
			if (porcentaje.compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalArgumentException("Los porcentajes deben ser no negativos.");
			}
			total = total.add(porcentaje);
		}
		if (total.compareTo(new BigDecimal("100")) >= 0) {
			throw new IllegalArgumentException("La suma de gastos a autorizar debe ser inferior al 100%.");
		}
		ActaDocument acta = new ActaDocument(java.util.UUID.randomUUID().toString(), idReserva, List.of(),
				"GASTOS_COMUNES_NO_TARIFADOS", null, List.of(), resoluciones, "GASTOS_PENDIENTES",
				Instant.now(), null);
		actas.put(acta.id(), acta);
		return acta;
	}

	public Map<String, Object> validar(String idReserva, String actorId, List<String> descripciones,
			List<BigDecimal> porcentajes, String estadoExpectativa, String propietarioExpectativo,
			String arrendatarioExpectatario) {
		Map<String, Object> resultado = new HashMap<>();
		resultado.put("actor", actorId);
		resultado.put("gastos_total", porcentajes.stream().mapToDouble(BigDecimal::doubleValue).sum());
		resultado.put("valido", !estadoExpectativa.isBlank());
		resultado.put("propietario_esperado", propietarioExpectativo);
		resultado.put("arrendatario_esperado", arrendatarioExpectatario);
		return resultado;
	}
}
