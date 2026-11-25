package com.volt.comunicacion;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

@Service
public class CampaniasService {
	private final Map<String, CampanaDTO> campanas = new ConcurrentHashMap<>();
	private final Map<String, List<String>> receptores = new ConcurrentHashMap<>();

	public CampanaDTO planificar(String organizadorId, String segmento, String mensaje,
			List<String> idsEnvio, Instant fechaInicio) {
		String id = UUID.randomUUID().toString();
		CampanaDTO campana = new CampanaDTO(id, organizadorId, segmento, mensaje, List.copyOf(idsEnvio),
				fechaInicio, "PLANNED", Instant.now(), null);
		campanas.put(id, campana);
		receptores.put(id, new CopyOnWriteArrayList<>(idsEnvio));
		return campana;
	}

	public CampanaDTO enviar(String id, List<String> idsEnvio) {
		CampanaDTO campana = campanas.get(id);
		if (campana == null) {
			throw new IllegalArgumentException("Campana no encontrada.");
		}
		List<String> ids = new CopyOnWriteArrayList<>(idsEnvio);
		List<String> receptores = new java.util.ArrayList<>();
		for (String usuario : ids) {
			receptores.add("RECEIPT-" + System.currentTimeMillis() + "-" + usuario);
		}
		campanas.put(id, new CampanaDTO(campana.id(), campana.organizadorId(), campana.segmento(),
				campana.mensaje(), campana.idsEnvio(), campana.fechaInicio(), "SENT", Instant.now(),
				receptores));
		return campanas.get(id);
	}

	public List<CampanaDTO> listar() {
		return List.copyOf(campanas.values());
	}
}
