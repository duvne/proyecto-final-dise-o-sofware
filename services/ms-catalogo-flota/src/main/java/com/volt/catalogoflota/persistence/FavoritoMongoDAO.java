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
import com.volt.catalogoflota.document.FavoritoDocument;

@Repository
public class FavoritoMongoDAO implements IFavoritoDAO {
	private final MongoCollection<Document> collection;

	public FavoritoMongoDAO(MongoClient mongoClient,
			@Value("${spring.mongodb.database}") String databaseName) {
		this.collection = mongoClient.getDatabase(databaseName).getCollection("favoritos");
	}

	@Override
	public FavoritoDocument guardar(FavoritoDocument favorito) {
		FavoritoDocument persisted = new FavoritoDocument(
				favorito.id() == null ? UUID.randomUUID().toString() : favorito.id(),
				favorito.usuarioId(), favorito.patinetaId(),
				favorito.fechaRegistro() == null ? Instant.now() : favorito.fechaRegistro());
		collection.insertOne(new Document("_id", persisted.id())
				.append("usuario_id", persisted.usuarioId())
				.append("patineta_id", persisted.patinetaId())
				.append("fecha_registro", persisted.fechaRegistro().toString()));
		return persisted;
	}

	@Override
	public void eliminar(String usuarioId, String patinetaId) {
		collection.deleteOne(Filters.and(
				Filters.eq("usuario_id", usuarioId),
				Filters.eq("patineta_id", patinetaId)));
	}

	@Override
	public List<FavoritoDocument> listarPorUsuario(String usuarioId) {
		return collection.find(Filters.eq("usuario_id", usuarioId))
				.map(document -> new FavoritoDocument(
						document.getString("_id"),
						document.getString("usuario_id"),
						document.getString("patineta_id"),
						Instant.parse(document.getString("fecha_registro"))))
				.into(new ArrayList<>());
	}

	@Override
	public boolean existe(String usuarioId, String patinetaId) {
		return collection.countDocuments(Filters.and(
				Filters.eq("usuario_id", usuarioId),
				Filters.eq("patineta_id", patinetaId))) > 0;
	}
}
