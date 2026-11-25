package com.volt.reservaspagos.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.volt.reservaspagos.model.domain.CuponDocument;

@Repository
public class CuponMongoDAO implements ICuponDAO {
	private final MongoCollection<Document> collection;

	public CuponMongoDAO(MongoClient mongoClient,
			@Value("${spring.mongodb.database}") String databaseName) {
		this.collection = mongoClient.getDatabase(databaseName).getCollection("cupones");
	}

	@Override
	public CuponDocument guardar(CuponDocument cupon) {
		CuponDocument persisted = new CuponDocument(
				cupon.id() == null ? UUID.randomUUID().toString() : cupon.id(),
				cupon.codigo().trim().toUpperCase(), cupon.porcentaje(), cupon.topeDescuento(),
				cupon.fechaVencimiento(), cupon.activo(), cupon.usos(), cupon.limiteUsos());
		collection.insertOne(toDocument(persisted));
		return persisted;
	}

	@Override
	public CuponDocument actualizar(CuponDocument cupon) {
		CuponDocument persisted = new CuponDocument(cupon.id(), cupon.codigo().trim().toUpperCase(),
				cupon.porcentaje(), cupon.topeDescuento(), cupon.fechaVencimiento(), cupon.activo(),
				cupon.usos(), cupon.limiteUsos());
		collection.replaceOne(Filters.eq("_id", cupon.id()), toDocument(persisted));
		return persisted;
	}

	@Override
	public Optional<CuponDocument> buscarPorCodigo(String codigo) {
		return Optional.ofNullable(collection.find(Filters.eq("codigo", codigo.trim().toUpperCase())).first())
				.map(CuponMongoDAO::fromDocument);
	}

	@Override
	public Optional<CuponDocument> buscarPorId(String id) {
		return Optional.ofNullable(collection.find(Filters.eq("_id", id)).first())
				.map(CuponMongoDAO::fromDocument);
	}

	@Override
	public List<CuponDocument> listar() {
		return collection.find().sort(Sorts.descending("fecha_vencimiento"))
				.map(CuponMongoDAO::fromDocument).into(new ArrayList<>());
	}

	@Override
	public void eliminar(String id) {
		collection.deleteOne(Filters.eq("_id", id));
	}

	@Override
	public void incrementarUso(String id) {
		collection.updateOne(Filters.eq("_id", id), Updates.inc("usos", 1));
	}

	private static Document toDocument(CuponDocument cupon) {
		return new Document("_id", cupon.id())
				.append("codigo", cupon.codigo())
				.append("porcentaje", new Decimal128(cupon.porcentaje()))
				.append("tope_descuento", new Decimal128(cupon.topeDescuento()))
				.append("fecha_vencimiento", cupon.fechaVencimiento().toString())
				.append("activo", cupon.activo())
				.append("usos", cupon.usos())
				.append("limite_usos", cupon.limiteUsos());
	}

	private static CuponDocument fromDocument(Document document) {
		return new CuponDocument(
				document.getString("_id"),
				document.getString("codigo"),
				money(document, "porcentaje"),
				money(document, "tope_descuento"),
				Instant.parse(document.getString("fecha_vencimiento")),
				Boolean.TRUE.equals(document.getBoolean("activo")),
				document.getInteger("usos", 0),
				document.getInteger("limite_usos", 0));
	}

	private static BigDecimal money(Document document, String field) {
		Object value = document.get(field);
		if (value instanceof Decimal128 decimal) {
			return decimal.bigDecimalValue();
		}
		if (value instanceof BigDecimal bigDecimal) {
			return bigDecimal;
		}
		if (value instanceof Number number) {
			return BigDecimal.valueOf(number.doubleValue());
		}
		return BigDecimal.ZERO;
	}
}
