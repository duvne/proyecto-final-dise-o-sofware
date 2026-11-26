package com.volt.catalogoflota.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.volt.catalogoflota.document.PatinetaDocument;
import com.volt.catalogoflota.document.PuntoEntregaDocument;
import com.volt.catalogoflota.document.TarifaDocument;
import com.volt.catalogoflota.document.ZonaDocument;

@Repository
public class PatinetaMongoDAOImpl implements IPatinetaDAO {
	private final MongoCollection<Document> collection;

	public PatinetaMongoDAOImpl(
			MongoClient mongoClient,
			@Value("${spring.mongodb.database}") String databaseName) {
		this.collection = mongoClient.getDatabase(databaseName).getCollection("patinetas");
	}

	@Override
	public PatinetaDocument guardar(PatinetaDocument patineta) {
		String id = patineta.id() == null ? java.util.UUID.randomUUID().toString() : patineta.id();
		Instant now = Instant.now();
		PatinetaDocument persisted = new PatinetaDocument(
				id, patineta.idPropietario(), patineta.categoria(), patineta.marca(), patineta.modelo(),
				patineta.autonomiaKm(), patineta.velocidadMaxima(), patineta.estadoFisico(),
				patineta.estadoPublicacion(), patineta.activa(), patineta.tarifa(), patineta.puntoEntrega(),
				patineta.fotos(), patineta.documentos(), patineta.observacionesModeracion(),
				patineta.enMantenimiento(), patineta.calificacionPromedio(), patineta.totalCalificaciones(),
				patineta.fechaRegistro() == null ? now : patineta.fechaRegistro(), now);
		Document document = toDocument(persisted);
		collection.insertOne(document);
		return fromDocument(document);
	}

	@Override
	public PatinetaDocument actualizar(PatinetaDocument patineta) {
		PatinetaDocument persisted = new PatinetaDocument(
				patineta.id(), patineta.idPropietario(), patineta.categoria(), patineta.marca(), patineta.modelo(),
				patineta.autonomiaKm(), patineta.velocidadMaxima(), patineta.estadoFisico(),
				patineta.estadoPublicacion(), patineta.activa(), patineta.tarifa(), patineta.puntoEntrega(),
				patineta.fotos(), patineta.documentos(), patineta.observacionesModeracion(),
				patineta.enMantenimiento(), patineta.calificacionPromedio(), patineta.totalCalificaciones(),
				patineta.fechaRegistro(), Instant.now());
		collection.replaceOne(Filters.eq("_id", persisted.id()), toDocument(persisted));
		return persisted;
	}

	@Override
	public Optional<PatinetaDocument> buscarPorId(String id) {
		return Optional.ofNullable(collection.find(Filters.eq("_id", id)).first())
				.map(PatinetaMongoDAOImpl::fromDocument);
	}

	@Override
	public Optional<PatinetaDocument> buscarActivaPorId(String id) {
		Document document = collection.find(Filters.and(
				Filters.eq("_id", id),
				Filters.eq("activa", true))).first();
		return Optional.ofNullable(document).map(PatinetaMongoDAOImpl::fromDocument);
	}

	@Override
	public List<PatinetaDocument> listarActivas(String categoria) {
		Bson filter = categoria == null || categoria.isBlank()
				? Filters.eq("activa", true)
				: Filters.and(Filters.eq("activa", true), textEquals("categoria", categoria));
		return collection.find(filter)
				.sort(Sorts.ascending("marca", "modelo"))
				.map(PatinetaMongoDAOImpl::fromDocument)
				.into(new ArrayList<>());
	}

	@Override
	public List<PatinetaDocument> listarPorPropietario(String idPropietario) {
		return collection.find(Filters.eq("id_propietario", idPropietario))
				.sort(Sorts.descending("fecha_registro"))
				.map(PatinetaMongoDAOImpl::fromDocument)
				.into(new ArrayList<>());
	}

	@Override
	public PatinetaPage listarPendientes(int pagina, int tamano) {
		Bson filter = Filters.eq("estado_publicacion", "EN_REVISION");
		long total = collection.countDocuments(filter);
		List<PatinetaDocument> elementos = collection.find(filter)
				.sort(Sorts.ascending("fecha_actualizacion"))
				.skip(Math.max(0, pagina) * tamano)
				.limit(tamano)
				.map(PatinetaMongoDAOImpl::fromDocument)
				.into(new ArrayList<>());
		return new PatinetaPage(elementos, total, pagina, tamano);
	}

	@Override
	public PatinetaPage buscarActivas(FiltroPatinetas filtros) {
		List<Bson> criteria = new ArrayList<>();
		criteria.add(Filters.eq("activa", true));
		criteria.add(Filters.eq("en_mantenimiento", false));
		if (filtros.categoria() != null && !filtros.categoria().isBlank()) {
			criteria.add(textEquals("categoria", filtros.categoria()));
		}
		if (filtros.ciudad() != null && !filtros.ciudad().isBlank()) {
			criteria.add(textEquals("punto_entrega.zona.ciudad", filtros.ciudad()));
		}
		if (filtros.precioHoraMaximo() != null) {
			criteria.add(Filters.lte("tarifa.valor_hora", new Decimal128(filtros.precioHoraMaximo())));
		}
		if (filtros.autonomiaMinimaKm() != null) {
			criteria.add(Filters.gte("autonomia_km", filtros.autonomiaMinimaKm()));
		}
		if (!filtros.idsExcluidos().isEmpty()) {
			criteria.add(Filters.nin("_id", filtros.idsExcluidos()));
		}

		Bson filter = Filters.and(criteria);
		long total = collection.countDocuments(filter);
		List<PatinetaDocument> patinetas = collection.find(filter)
				.sort(Sorts.ascending("marca", "modelo"))
				.skip(Math.max(0, filtros.pagina()) * filtros.tamano())
				.limit(filtros.tamano())
				.map(PatinetaMongoDAOImpl::fromDocument)
				.into(new ArrayList<>());
		return new PatinetaPage(patinetas, total, filtros.pagina(), filtros.tamano());
	}

	@Override
	public void eliminar(String id) {
		collection.deleteOne(Filters.eq("_id", id));
	}

	@Override
	public void actualizarCalificacion(String id, double promedio, int totalCalificaciones) {
		collection.updateOne(Filters.eq("_id", id), Updates.combine(
				Updates.set("calificacion_promedio", promedio),
				Updates.set("total_calificaciones", totalCalificaciones),
				Updates.set("fecha_actualizacion", Instant.now().toString())));
	}

	private static Bson textEquals(String field, String value) {
		return Filters.regex(field, "^" + Pattern.quote(value.trim()) + "$", "i");
	}

	private static Document toDocument(PatinetaDocument patineta) {
		TarifaDocument tarifa = patineta.tarifa();
		PuntoEntregaDocument punto = patineta.puntoEntrega();
		ZonaDocument zona = punto.zona();
		return new Document("_id", patineta.id())
				.append("id_propietario", patineta.idPropietario())
				.append("categoria", patineta.categoria())
				.append("marca", patineta.marca())
				.append("modelo", patineta.modelo())
				.append("autonomia_km", patineta.autonomiaKm())
				.append("velocidad_max", patineta.velocidadMaxima())
				.append("estado_fisico", patineta.estadoFisico())
				.append("estado_publicacion", patineta.estadoPublicacion())
				.append("activa", patineta.activa())
				.append("fotos", patineta.fotos())
				.append("documentos", patineta.documentos())
				.append("observaciones_moderacion", patineta.observacionesModeracion())
				.append("en_mantenimiento", patineta.enMantenimiento())
				.append("calificacion_promedio", patineta.calificacionPromedio())
				.append("total_calificaciones", patineta.totalCalificaciones())
				.append("fecha_registro", patineta.fechaRegistro().toString())
				.append("fecha_actualizacion", patineta.fechaActualizacion().toString())
				.append("tarifa", new Document("valor_hora", new Decimal128(tarifa.valorHora()))
						.append("valor_dia", new Decimal128(tarifa.valorDia()))
						.append("valor_deposito", new Decimal128(tarifa.valorDeposito())))
				.append("punto_entrega", new Document("id_punto", punto.idPunto())
						.append("direccion", punto.direccion())
						.append("latitud", punto.latitud())
						.append("longitud", punto.longitud())
						.append("verificado", punto.verificado())
						.append("zona", new Document("id_zona", zona.idZona())
								.append("nombre", zona.nombre())
								.append("ciudad", zona.ciudad())));
	}

	private static PatinetaDocument fromDocument(Document document) {
		Document tarifa = requireEmbedded(document, "tarifa");
		Document punto = requireEmbedded(document, "punto_entrega");
		Document zona = requireEmbedded(punto, "zona");
		return new PatinetaDocument(
				document.getString("_id"),
				document.getString("id_propietario"),
				document.getString("categoria"),
				document.getString("marca"),
				document.getString("modelo"),
				number(document, "autonomia_km"),
				number(document, "velocidad_max"),
				document.getString("estado_fisico"),
				document.getString("estado_publicacion"),
				Boolean.TRUE.equals(document.getBoolean("activa")),
				new TarifaDocument(
						decimal(tarifa, "valor_hora"),
						decimal(tarifa, "valor_dia"),
						decimal(tarifa, "valor_deposito")),
				new PuntoEntregaDocument(
						integer(punto, "id_punto"),
						punto.getString("direccion"),
						number(punto, "latitud"),
						number(punto, "longitud"),
						Boolean.TRUE.equals(punto.getBoolean("verificado")),
						new ZonaDocument(
								integer(zona, "id_zona"),
								zona.getString("nombre"),
								zona.getString("ciudad"))),
				document.getList("fotos", String.class, List.of()),
				document.getList("documentos", String.class, List.of()),
				document.getString("observaciones_moderacion"),
				Boolean.TRUE.equals(document.getBoolean("en_mantenimiento")),
				document.getDouble("calificacion_promedio") == null ? 0.0 : document.getDouble("calificacion_promedio"),
				document.getInteger("total_calificaciones", 0),
				instant(document.getString("fecha_registro")),
				instant(document.getString("fecha_actualizacion")));
	}

	private static Instant instant(String value) {
		return value == null ? Instant.now() : Instant.parse(value);
	}

	private static Document requireEmbedded(Document document, String field) {
		Document embedded = document.get(field, Document.class);
		if (embedded == null) {
			throw new IllegalStateException("El documento de patineta no contiene el objeto requerido: " + field);
		}
		return embedded;
	}

	private static Double number(Document document, String field) {
		Number value = document.get(field, Number.class);
		return value == null ? null : value.doubleValue();
	}

	private static Long integer(Document document, String field) {
		Number value = document.get(field, Number.class);
		return value == null ? null : value.longValue();
	}

	private static BigDecimal decimal(Document document, String field) {
		Object value = document.get(field);
		if (value == null) {
			return null;
		}
		if (value instanceof Decimal128 decimal128) {
			return decimal128.bigDecimalValue();
		}
		if (value instanceof BigDecimal bigDecimal) {
			return bigDecimal;
		}
		if (value instanceof Number number) {
			return BigDecimal.valueOf(number.doubleValue());
		}
		throw new IllegalStateException("El campo de tarifa contiene un tipo BSON no numerico: " + field);
	}
}
