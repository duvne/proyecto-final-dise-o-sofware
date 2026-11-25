package com.volt.shared.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;

@Service
public class AuditoriaService {
	private final MongoCollection<Document> collection;

	public AuditoriaService(
			MongoClient mongoClient,
			@Value("${spring.mongodb.database:volt_auditoria}") String databaseName) {
		this.collection = mongoClient.getDatabase(databaseName).getCollection("auditoria");
	}

	public void registrar(
			String actorId,
			String accion,
			String entidad,
			String entidadId,
			Object valoresAnteriores,
			Object valoresNuevos,
			String ip) {
		collection.insertOne(new Document("_id", UUID.randomUUID().toString())
				.append("actor_id", actorId)
				.append("accion", accion)
				.append("entidad", entidad)
				.append("entidad_id", entidadId)
				.append("valores_anteriores", normalizar(valoresAnteriores))
				.append("valores_nuevos", normalizar(valoresNuevos))
				.append("ip", ip)
				.append("timestamp", Instant.now().toString()));
	}

	private static Object normalizar(Object value) {
		if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
			return value;
		}
		if (value instanceof Map<?, ?> map) {
			Document document = new Document();
			map.forEach((key, item) -> document.append(String.valueOf(key), item));
			return document;
		}
		return value.toString();
	}
}
