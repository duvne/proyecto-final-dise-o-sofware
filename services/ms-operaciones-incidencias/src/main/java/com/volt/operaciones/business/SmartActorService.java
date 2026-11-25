package com.volt.operaciones.business;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class SmartActorService {
	private final GeneradorDeCertificadosService certificados;

	public SmartActorService(GeneradorDeCertificadosService certificados) {
		this.certificados = certificados;
	}

	public Map<String, Object> process(String idReserva, String intents, boolean signedAction) {
		Map<String, Object> reporte = new HashMap<>();
		reporte.put("id_reserva", idReserva);
		reporte.put("intentos_consecutivos", intents);
		reporte.put("gelo", intents.equals("true") ? true : false);
		reporte.put("accion_firmada", signedAction);
		if (signedAction) {
			reporte.put("INT_PROFUNDAMIENTO_CERTIFICADO", true);
			certificados.generarOprofundamientoCertificado("SMART_ACTOR_" + idReserva, intents);
			reporte.put("firma_id", "SMART_ACTOR_" + idReserva);
		}
		return reporte;
	}
}
