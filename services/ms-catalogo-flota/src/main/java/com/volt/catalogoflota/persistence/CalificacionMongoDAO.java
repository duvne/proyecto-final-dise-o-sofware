package com.volt.catalogoflota.persistence;

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
import com.volt.catalogoflota.document.CalificacionDocument;

@Repository
public class CalificacionMongoDAO implements ICalificacionDAO {
	private final MongoCollection<Document> collection;

	public CalificacionMongoDAO(MongoClient mongoClient,
			@Value("${spring.mongodb.database}") String databaseName) {
		this.collection = mongoClient.getDatabase(databaseName).getCollection("calificaciones");
	}

	@Override
	public CalificacionDocument guardar(CalificacionDocument calificacion) {
		CalificacionDocument persisted = new CalificacionDocument(
				calificacion.id() == null ? UUID.randomUUID().toString() : calificacion.id(),
				calificacion.patinetaId(), calificacion.propietarioId(), calificacion.reservaId(),
				calificacion.emisorId(), calificacion.puntaje(), calificacion.comentario(),
				calificacion.replica(), calificacion.reportada(), calificacion.motivoReporte(),
				calificacion.oculta(), calificacion.fechaRegistro() == null ? Instant.now() : calificacion.fechaRegistro());
		collection.insertOne(toDocument(persisted));
		return persisted;
	}

	@Override
	public CalificacionDocument actualizar(CalificacionDocument calificacion) {
		collection.replaceOne(Filters.eq("_id", calificacion.id()), toDocument(calificacion));
		return calificacion;
	}

	@Override
	public Optional<CalificacionDocument> buscarPorId(String id) {
		return Optional.ofNullable(collection.find(Filters.eq("_id", id)).first()).map(CalificacionMongoDAO::from);
	}

	@Override
	public List<CalificacionDocument> listarPorPatineta(String patinetaId, boolean incluirOcultas) {
		var filter = incluirOcultas
				? Filters.eq("patineta_id", patinetaId)
				: Filters.and(Filters.eq("patineta_id", patinetaId), Filters.eq("oculta", false));
		return collection.find(filter).sort(Sorts.descending("fecha_registro"))
				.map(CalificacionMongoDAO::from).into(new ArrayList<>());
	}

	private static Document toDocument(CalificacionDocument calificacion) {
		return new Document("_id", calificacion.id())
				.append("patineta_id", calificacion.patinetaId())
				.append("propietario_id", calificacion.propietarioId())
				.append("reserva_id", calificacion.reservaId())
				.append("emisor_id", calificacion.emisorId())
				.append("puntaje", calificacion.puntaje())
				.append("comentario", calificacion.comentario())
				.append("replica", calificacion.replica())
				.append("reportada", calificacion.reportada())
				.append("motivo_reporte", calificacion.motivoReporte())
				.append("oculta", calificacion.oculta())
				.append("fecha_registro", calificacion.fechaRegistro().toString());
	}

	private static CalificacionDocument from(Document document) {
		return new CalificacionDocument(
				document.getString("_id"),
				document.getString("patineta_id"),
				document.getString("propietario_id"),
				document.getString("reserva_id"),
				document.getString("emisor_id"),
				document.getInteger("puntaje", 0),
				document.getString("comentario"),
				document.getString("replica"),
				Boolean.TRUE.equals(document.getBoolean("reportada")),
				document.getString("motivo_reporte"),
				Boolean.TRUE.equals(document.getBoolean("oculta")),
				Instant.parse(document.getString("fecha_registro")));
	}
}
