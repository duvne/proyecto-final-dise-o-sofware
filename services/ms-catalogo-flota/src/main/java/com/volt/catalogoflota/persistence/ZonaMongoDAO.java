package com.volt.catalogoflota.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.volt.catalogoflota.document.ZonaCatalogoDocument;

@Repository
public class ZonaMongoDAO implements IZonaDAO {
	private final MongoCollection<Document> collection;

	public ZonaMongoDAO(MongoClient mongoClient,
			@Value("${spring.mongodb.database}") String databaseName) {
		this.collection = mongoClient.getDatabase(databaseName).getCollection("zonas");
	}

	@Override
	public List<ZonaCatalogoDocument> listar() {
		return collection.find().sort(Sorts.ascending("ciudad", "nombre")).map(ZonaMongoDAO::from)
				.into(new ArrayList<>());
	}

	@Override
	public Optional<ZonaCatalogoDocument> buscarPorId(String id) {
		return Optional.ofNullable(collection.find(Filters.eq("_id", id)).first()).map(ZonaMongoDAO::from);
	}

	@Override
	public ZonaCatalogoDocument guardar(ZonaCatalogoDocument zona) {
		ZonaCatalogoDocument persisted = new ZonaCatalogoDocument(
				zona.id() == null ? UUID.randomUUID().toString() : zona.id(),
				zona.idZona(), zona.nombre(), zona.ciudad(), zona.activa());
		collection.insertOne(toDocument(persisted));
		return persisted;
	}

	@Override
	public ZonaCatalogoDocument actualizar(ZonaCatalogoDocument zona) {
		collection.replaceOne(Filters.eq("_id", zona.id()), toDocument(zona));
		return zona;
	}

	@Override
	public void eliminar(String id) {
		collection.deleteOne(Filters.eq("_id", id));
	}

	private static Document toDocument(ZonaCatalogoDocument zona) {
		return new Document("_id", zona.id())
				.append("id_zona", zona.idZona())
				.append("nombre", zona.nombre())
				.append("ciudad", zona.ciudad())
				.append("activa", zona.activa());
	}

	private static ZonaCatalogoDocument from(Document document) {
		Number idZona = document.get("id_zona", Number.class);
		return new ZonaCatalogoDocument(
				document.getString("_id"),
				idZona == null ? null : idZona.longValue(),
				document.getString("nombre"),
				document.getString("ciudad"),
				Boolean.TRUE.equals(document.getBoolean("activa")));
	}
}
