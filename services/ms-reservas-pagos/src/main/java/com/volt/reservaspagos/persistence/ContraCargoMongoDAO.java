package com.volt.reservaspagos.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import com.volt.reservaspagos.model.domain.ContraCargoDocument;

@Repository
public class ContraCargoMongoDAO implements IContraCargoDAO {
	private final MongoCollection<Document> collection;

	public ContraCargoMongoDAO(MongoClient mongoClient,
			@Value("${spring.mongodb.database}") String databaseName) {
		this.collection = mongoClient.getDatabase(databaseName).getCollection("contracargos");
	}

	@Override
	public ContraCargoDocument guardar(ContraCargoDocument contraCargo) {
		collection.insertOne(new Document("_id", contraCargo.id())
				.append("id_reserva", contraCargo.idReserva())
				.append("id_arrendatario", contraCargo.idArrendatario())
				.append("monto", new Decimal128(contraCargo.monto()))
				.append("motivo", contraCargo.motivo())
				.append("estado", contraCargo.estado())
				.append("fecha_registro", contraCargo.fechaRegistro().toString()));
		return contraCargo;
	}

	@Override
	public List<ContraCargoDocument> listar() {
		return collection.find().sort(Sorts.descending("fecha_registro"))
				.map(document -> new ContraCargoDocument(
						document.getString("_id"),
						document.getString("id_reserva"),
						document.getString("id_arrendatario"),
						money(document),
						document.getString("motivo"),
						document.getString("estado"),
						Instant.parse(document.getString("fecha_registro"))))
				.into(new ArrayList<>());
	}

	private static BigDecimal money(Document document) {
		Object value = document.get("monto");
		if (value instanceof Decimal128 decimal) {
			return decimal.bigDecimalValue();
		}
		return BigDecimal.ZERO;
	}
}
