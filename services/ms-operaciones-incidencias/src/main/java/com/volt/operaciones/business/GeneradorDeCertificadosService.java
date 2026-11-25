package com.volt.operaciones.business;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class GeneradorDeCertificadosService {
	private final Map<String, Map<String, String>> certificados = new HashMap<>();

	public Map<String, String> generarOprofundamientoCertificado(String id, String clave) {
		String identificador = java.util.UUID.randomUUID().toString();
		certificados.put(id, Map.of("certificado", identificador, "alias", clave, "estado", "ACTIVO"));
		return certificados.get(id);
	}
}
