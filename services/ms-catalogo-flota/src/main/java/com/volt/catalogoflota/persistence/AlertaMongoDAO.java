package com.volt.catalogoflota.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.volt.catalogoflota.document.AlertaDisponibilidadDocument;

@Repository
public class AlertaMongoDAO implements IAlertaDAO {
	private final MongoCollection<Document> collection;

	public AlertaMongoDAO(MongoClient mongoClient,
			@Value("${spring.mongodb.database}") String databaseName) {
		this.collection = mongoClient.getDatabase(databaseName).getCollection("alertas_disponibilidad");
	}

	@Override
	public AlertaDisponibilidadDocument guardar(AlertaDisponibilidadDocument alerta) {
		AlertaDisponibilidadDocument persisted = new AlertaDisponibilidadDocument(
				alerta.id() == null ? UUID.randomUUID().toString() : alerta.id(),
				alerta.usuarioId(), alerta.patinetaId(), alerta.inicio(), alerta.fin(), alerta.activa(),
				alerta.fechaRegistro() == null ? Instant.now() : alerta.fechaRegistro());
		collection.insertOne(new Document("_id", persisted.id())
				.append("usuario_id", persisted.usuarioId())
				.append("patineta_id", persisted.patinetaId())
				.append("inicio", persisted.inicio().toString())
				.append("fin", persisted.fin().toString())
				.append("activa", persisted.activa())
				.append("fecha_registro", persisted.fechaRegistro().toString()));
		return persisted;
	}

	@Override
	public List<AlertaDisponibilidadDocument> listarPorUsuario(String usuarioId) {
		return collection.find(Filters.eq("usuario_id", usuarioId))
				.map(document -> new AlertaDisponibilidadDocument(
						document.getString("_id"),
						document.getString("usuario_id"),
						document.getString("patineta_id"),
						Instant.parse(document.getString("inicio")),
						Instant.parse(document.getString("fin")),
						Boolean.TRUE.equals(document.getBoolean("activa")),
						Instant.parse(document.getString("fecha_registro"))))
				.into(new ArrayList<>());
	}

	@Override
	public void eliminar(String id, String usuarioId) {
		collection.deleteOne(Filters.and(Filters.eq("_id", id), Filters.eq("usuario_id", usuarioId)));
	}
}
