package com.volt.usuariosauth.persistence;

import java.time.Instant;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.volt.usuariosauth.domain.RecuperacionContrasena;

@Repository
public class RecuperacionMongoDAO implements IRecuperacionDAO {
	private final MongoCollection<Document> recuperaciones;

	public RecuperacionMongoDAO(
			MongoClient mongoClient,
			@Value("${spring.mongodb.database}") String databaseName) {
		this.recuperaciones = mongoClient.getDatabase(databaseName).getCollection("recuperaciones");
	}

	@Override
	public void guardar(RecuperacionContrasena recuperacion) {
		recuperaciones.insertOne(new Document("_id", recuperacion.id())
				.append("usuario_id", recuperacion.usuarioId())
				.append("token_hash", recuperacion.tokenHash())
				.append("expira_en", recuperacion.expiraEn().toString())
				.append("usado", recuperacion.usado()));
	}

	@Override
	public Optional<RecuperacionContrasena> buscarVigente(String tokenHash, Instant ahora) {
		Document document = recuperaciones.find(Filters.and(
						Filters.eq("token_hash", tokenHash),
						Filters.eq("usado", false),
						Filters.gt("expira_en", ahora.toString())))
				.first();
		if (document == null) {
			return Optional.empty();
		}
		return Optional.of(new RecuperacionContrasena(
				document.getString("_id"),
				document.getString("usuario_id"),
				document.getString("token_hash"),
				Instant.parse(document.getString("expira_en")),
				Boolean.TRUE.equals(document.getBoolean("usado"))));
	}

	@Override
	public void marcarUsado(String id) {
		recuperaciones.updateOne(Filters.eq("_id", id), Updates.set("usado", true));
	}
}
