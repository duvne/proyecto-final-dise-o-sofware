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
import com.volt.catalogoflota.document.CategoriaDocument;

@Repository
public class CategoriaMongoDAO implements ICategoriaDAO {
	private final MongoCollection<Document> collection;

	public CategoriaMongoDAO(MongoClient mongoClient,
			@Value("${spring.mongodb.database}") String databaseName) {
		this.collection = mongoClient.getDatabase(databaseName).getCollection("categorias");
	}

	@Override
	public List<CategoriaDocument> listar() {
		return collection.find().sort(Sorts.ascending("nombre")).map(CategoriaMongoDAO::from)
				.into(new ArrayList<>());
	}

	@Override
	public Optional<CategoriaDocument> buscarPorId(String id) {
		return Optional.ofNullable(collection.find(Filters.eq("_id", id)).first()).map(CategoriaMongoDAO::from);
	}

	@Override
	public Optional<CategoriaDocument> buscarPorNombre(String nombre) {
		return Optional.ofNullable(collection.find(Filters.regex("nombre", "^" + java.util.regex.Pattern.quote(nombre) + "$", "i")).first())
				.map(CategoriaMongoDAO::from);
	}

	@Override
	public CategoriaDocument guardar(CategoriaDocument categoria) {
		CategoriaDocument persisted = new CategoriaDocument(
				categoria.id() == null ? UUID.randomUUID().toString() : categoria.id(),
				categoria.nombre(), categoria.descripcion(), categoria.autonomiaMinimaKm(), categoria.activa());
		collection.insertOne(toDocument(persisted));
		return persisted;
	}

	@Override
	public CategoriaDocument actualizar(CategoriaDocument categoria) {
		collection.replaceOne(Filters.eq("_id", categoria.id()), toDocument(categoria));
		return categoria;
	}

	@Override
	public void eliminar(String id) {
		collection.deleteOne(Filters.eq("_id", id));
	}

	private static Document toDocument(CategoriaDocument categoria) {
		return new Document("_id", categoria.id())
				.append("nombre", categoria.nombre())
				.append("descripcion", categoria.descripcion())
				.append("autonomia_minima_km", categoria.autonomiaMinimaKm())
				.append("activa", categoria.activa());
	}

	private static CategoriaDocument from(Document document) {
		return new CategoriaDocument(
				document.getString("_id"),
				document.getString("nombre"),
				document.getString("descripcion"),
				document.getDouble("autonomia_minima_km"),
				Boolean.TRUE.equals(document.getBoolean("activa")));
	}
}
