package com.volt.usuariosauth.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import com.volt.usuariosauth.domain.RegistroAuditoria;

@Repository
public class AuditoriaMongoDAO implements IAuditoriaDAO {
	private final MongoCollection<Document> auditoria;

	public AuditoriaMongoDAO(
			MongoClient mongoClient,
			@Value("${spring.mongodb.database}") String databaseName) {
		this.auditoria = mongoClient.getDatabase(databaseName).getCollection("auditoria");
	}

	@Override
	public List<RegistroAuditoria> listar(int pagina, int tamano) {
		return auditoria.find()
				.sort(Sorts.descending("timestamp"))
				.skip(Math.max(0, pagina) * tamano)
				.limit(tamano)
				.map(AuditoriaMongoDAO::fromDocument)
				.into(new ArrayList<>());
	}

	@Override
	public long contar() {
		return auditoria.countDocuments();
	}

	private static RegistroAuditoria fromDocument(Document document) {
		return new RegistroAuditoria(
				document.getString("_id"),
				document.getString("actor_id"),
				document.getString("accion"),
				document.getString("entidad"),
				document.getString("entidad_id"),
				document.get("valores_anteriores"),
				document.get("valores_nuevos"),
				document.getString("ip"),
				Instant.parse(document.getString("timestamp")));
	}
}
