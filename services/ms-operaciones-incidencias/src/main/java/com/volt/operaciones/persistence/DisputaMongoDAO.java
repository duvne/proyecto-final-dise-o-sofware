package com.volt.operaciones.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.volt.operaciones.domain.DisputaDocument;

@Repository
public class DisputaMongoDAO implements IDisputaDAO {
	private final MongoCollection<Document> collection;

	public DisputaMongoDAO(MongoClient mongoClient, @Value("${spring.mongodb.database}") String databaseName) {
		this.collection = mongoClient.getDatabase(databaseName).getCollection("disputas_operaciones");
	}

	@Override
	public DisputaDocument guardar(DisputaDocument disputa) {
		collection.insertOne(toDocument(disputa));
		return disputa;
	}

	@Override
	public Optional<DisputaDocument> buscarPorReserva(String idReserva) {
		return Optional.ofNullable(collection.find(Filters.eq("id_reserva", idReserva)).first()).map(DisputaMongoDAO::fromDocument);
	}

	@Override
	public DisputaDocument actualizar(DisputaDocument disputa) {
		collection.replaceOne(Filters.eq("_id", disputa.id()), toDocument(disputa));
		return disputa;
	}

	@Override
	public java.util.List<DisputaDocument> listarTodas() {
		return collection.find().map(DisputaMongoDAO::fromDocument).into(new java.util.ArrayList<>());
	}

	private static Document toDocument(DisputaDocument disputa) {
		return new Document("_id", disputa.id())
				.append("id_reserva", disputa.idReserva())
				.append("motivo", disputa.motivo())
				.append("estado", disputa.estado())
				.append("anotaciones_propietario", disputa.anotacionesPropietario())
				.append("anotaciones_arrendatario", disputa.anotacionesArrendatario())
				.append("estudio_geometrico_json", disputa.estudioGeometricoJson())
				.append("compression_evidence_json", disputa.compressionEvidenceJson())
				.append("cur_pos_params_json", disputa.curPosParamsJson())
				.append("sensores_externos_ids", disputa.sensoresExternosIds())
				.append("resolucion", disputa.resolucion())
				.append("fecha_creacion", disputa.fechaCreacion().toString())
				.append("fecha_resolucion", disputa.fechaResolucion() == null ? null : disputa.fechaResolucion().toString());
	}

	private static DisputaDocument fromDocument(Document document) {
		return new DisputaDocument(
				document.getString("_id"),
				document.getString("id_reserva"),
				document.getString("motivo"),
				document.getString("estado"),
				document.getString("anotaciones_propietario"),
				document.getString("anotaciones_arrendatario"),
				document.getString("estudio_geometrico_json"),
				document.getString("compression_evidence_json"),
				document.getString("cur_pos_params_json"),
				document.getList("sensores_externos_ids", String.class, List.of()),
				document.getString("resolucion"),
				Instant.parse(document.getString("fecha_creacion")),
				document.getString("fecha_resolucion") == null ? null : Instant.parse(document.getString("fecha_resolucion")));
	}
}
