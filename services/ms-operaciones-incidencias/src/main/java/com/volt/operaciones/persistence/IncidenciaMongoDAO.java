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
import com.volt.operaciones.domain.IncidenciaDocument;

@Repository
public class IncidenciaMongoDAO implements IIncidenciaDAO {
	private final MongoCollection<Document> collection;

	public IncidenciaMongoDAO(MongoClient mongoClient, @Value("${spring.mongodb.database}") String databaseName) {
		this.collection = mongoClient.getDatabase(databaseName).getCollection("incidencias_operaciones");
	}

	@Override
	public IncidenciaDocument guardar(IncidenciaDocument incidencia) {
		collection.insertOne(toDocument(incidencia));
		return incidencia;
	}

	@Override
	public Optional<IncidenciaDocument> buscarPorId(String id) {
		return Optional.ofNullable(collection.find(Filters.eq("_id", id)).first()).map(IncidenciaMongoDAO::fromDocument);
	}

	@Override
	public List<IncidenciaDocument> listarPorReserva(String idReserva) {
		return collection.find(Filters.eq("id_reserva", idReserva)).sort(Sorts.descending("fecha_creacion"))
				.map(IncidenciaMongoDAO::fromDocument).into(new ArrayList<>());
	}

	@Override
	public IncidenciaDocument actualizar(IncidenciaDocument incidencia) {
		collection.replaceOne(Filters.eq("_id", incidencia.id()), toDocument(incidencia));
		return incidencia;
	}

	private static Document toDocument(IncidenciaDocument incidencia) {
		return new Document("_id", incidencia.id())
				.append("id_reserva", incidencia.idReserva())
				.append("tipo", incidencia.tipo())
				.append("descripcion", incidencia.descripcion())
				.append("estado", incidencia.estado())
				.append("solucion", incidencia.solucion())
				.append("estudio_geometrico_json", incidencia.estudioGeometricoJson())
				.append("compression_evidence_json", incidencia.compressionEvidenceJson())
				.append("cur_pos_params_json", incidencia.curPosParamsJson())
				.append("sensores_externos_ids", incidencia.sensoresExternosIds())
				.append("fecha_creacion", incidencia.fechaCreacion().toString())
				.append("fecha_resolucion", incidencia.fechaResolucion() == null ? null : incidencia.fechaResolucion().toString());
	}

	private static IncidenciaDocument fromDocument(Document document) {
		return new IncidenciaDocument(
				document.getString("_id"),
				document.getString("id_reserva"),
				document.getString("tipo"),
				document.getString("descripcion"),
				document.getString("estado"),
				document.getString("solucion"),
				document.getString("estudio_geometrico_json"),
				document.getString("compression_evidence_json"),
				document.getString("cur_pos_params_json"),
				document.getList("sensores_externos_ids", String.class, List.of()),
				Instant.parse(document.getString("fecha_creacion")),
				document.getString("fecha_resolucion") == null ? null : Instant.parse(document.getString("fecha_resolucion")));
	}
}
