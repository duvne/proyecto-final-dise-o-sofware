package com.volt.operaciones.persistence;

import java.time.Instant;
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
import com.volt.operaciones.domain.ActaDocument;

@Repository
public class ActaMongoDAO implements IActaDAO {
	private final MongoCollection<Document> collection;

	public ActaMongoDAO(MongoClient mongoClient, @Value("${spring.mongodb.database}") String databaseName) {
		this.collection = mongoClient.getDatabase(databaseName).getCollection("actas_operaciones");
	}

	@Override
	public ActaDocument guardar(ActaDocument acta) {
		collection.insertOne(new Document("_id", acta.id())
				.append("id_reserva", acta.idReserva())
				.append("incidencias_ids", acta.incidenciasIds())
				.append("descripcion", acta.descripcion())
				.append("notas_mejoras", acta.notasMejoras())
				.append("foto_evidencias", acta.fotoEvidencias())
				.append("observaciones", acta.observaciones())
				.append("estado", acta.estado())
				.append("fecha_creacion", acta.fechaCreacion().toString())
				.append("fecha_resolucion", acta.fechaResolucion() == null ? null : acta.fechaResolucion().toString()));
		return acta;
	}

	@Override
	public Optional<ActaDocument> buscarPorId(String id) {
		return Optional.ofNullable(collection.find(Filters.eq("_id", id)).first()).map(ActaMongoDAO::fromDocument);
	}

	@Override
	public Optional<ActaDocument> buscarPorReserva(String idReserva) {
		return Optional.ofNullable(collection.find(Filters.eq("id_reserva", idReserva)).first()).map(ActaMongoDAO::fromDocument);
	}

	@Override
	public List<ActaDocument> listar() {
		return collection.find().sort(Sorts.descending("fecha_creacion")).map(ActaMongoDAO::fromDocument)
				.into(new ArrayList<>());
	}

	private static ActaDocument fromDocument(Document document) {
		return new ActaDocument(
				document.getString("_id"),
				document.getString("id_reserva"),
				document.getList("incidencias_ids", String.class, List.of()),
				document.getString("descripcion"),
				document.getString("notas_mejoras"),
				document.getList("foto_evidencias", String.class, List.of()),
				document.getString("observaciones"),
				document.getString("estado"),
				Instant.parse(document.getString("fecha_creacion")),
				document.getString("fecha_resolucion") == null ? null : Instant.parse(document.getString("fecha_resolucion")));
	}
}
