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
import com.mongodb.client.model.Sorts;
import com.volt.catalogoflota.document.MantenimientoDocument;

@Repository
public class MantenimientoMongoDAO implements IMantenimientoDAO {
	private final MongoCollection<Document> collection;

	public MantenimientoMongoDAO(MongoClient mongoClient,
			@Value("${spring.mongodb.database}") String databaseName) {
		this.collection = mongoClient.getDatabase(databaseName).getCollection("mantenimientos");
	}

	@Override
	public MantenimientoDocument guardar(MantenimientoDocument mantenimiento) {
		MantenimientoDocument persisted = new MantenimientoDocument(
				mantenimiento.id() == null ? UUID.randomUUID().toString() : mantenimiento.id(),
				mantenimiento.patinetaId(), mantenimiento.descripcion(), mantenimiento.realizadoPor(),
				mantenimiento.fechaRegistro() == null ? Instant.now() : mantenimiento.fechaRegistro());
		collection.insertOne(new Document("_id", persisted.id())
				.append("patineta_id", persisted.patinetaId())
				.append("descripcion", persisted.descripcion())
				.append("realizado_por", persisted.realizadoPor())
				.append("fecha_registro", persisted.fechaRegistro().toString()));
		return persisted;
	}

	@Override
	public List<MantenimientoDocument> listarPorPatineta(String patinetaId) {
		return collection.find(Filters.eq("patineta_id", patinetaId))
				.sort(Sorts.descending("fecha_registro"))
				.map(document -> new MantenimientoDocument(
						document.getString("_id"),
						document.getString("patineta_id"),
						document.getString("descripcion"),
						document.getString("realizado_por"),
						Instant.parse(document.getString("fecha_registro"))))
				.into(new ArrayList<>());
	}
}
