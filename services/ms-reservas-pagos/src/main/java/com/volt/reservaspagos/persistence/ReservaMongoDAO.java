package com.volt.reservaspagos.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.volt.reservaspagos.model.domain.DepositoInfo;
import com.volt.reservaspagos.model.domain.EstadoReserva;
import com.volt.reservaspagos.model.domain.LiquidacionInfo;
import com.volt.reservaspagos.model.domain.PagoInfo;
import com.volt.reservaspagos.model.domain.ReservaDocument;

@Repository
public class ReservaMongoDAO implements IReservaDAO {
	private final MongoCollection<Document> collection;

	public ReservaMongoDAO(MongoClient mongoClient,
			@Value("${spring.mongodb.database}") String databaseName) {
		this.collection = mongoClient.getDatabase(databaseName).getCollection("reservas");
	}

	@Override
	public ReservaDocument guardar(ReservaDocument reserva) {
		collection.insertOne(toDocument(reserva));
		return reserva;
	}

	@Override
	public ReservaDocument actualizar(ReservaDocument reserva) {
		collection.replaceOne(Filters.eq("_id", reserva.id()), toDocument(reserva));
		return reserva;
	}

	@Override
	public Optional<ReservaDocument> buscarPorId(String id) {
		return Optional.ofNullable(collection.find(Filters.eq("_id", id)).first())
				.map(ReservaMongoDAO::fromDocument);
	}

	@Override
	public List<ReservaDocument> listarPorUsuario(String usuarioId, EstadoReserva estado) {
		List<Bson> criteria = new ArrayList<>();
		criteria.add(Filters.or(
				Filters.eq("id_arrendatario", usuarioId),
				Filters.eq("id_propietario", usuarioId)));
		if (estado != null) {
			criteria.add(Filters.eq("estado", estado.name()));
		}
		return collection.find(Filters.and(criteria))
				.sort(Sorts.descending("fecha_creacion"))
				.map(ReservaMongoDAO::fromDocument)
				.into(new ArrayList<>());
	}

	@Override
	public List<ReservaDocument> listarAdmin(EstadoReserva estado, Instant desde, Instant hasta,
			int pagina, int tamano) {
		Bson filter = filtro(estado, desde, hasta);
		return collection.find(filter)
				.sort(Sorts.descending("fecha_creacion"))
				.skip(Math.max(0, pagina) * tamano)
				.limit(tamano)
				.map(ReservaMongoDAO::fromDocument)
				.into(new ArrayList<>());
	}

	@Override
	public long contar(EstadoReserva estado, Instant desde, Instant hasta) {
		return collection.countDocuments(filtro(estado, desde, hasta));
	}

	@Override
	public List<ReservaDocument> buscarParaLiquidar(Instant ahora) {
		return collection.find(Filters.and(
						Filters.eq("estado", EstadoReserva.FINALIZADA.name()),
						Filters.eq("disputa_abierta", false),
						Filters.lte("fecha_liberacion_pago", ahora.toString())))
				.map(ReservaMongoDAO::fromDocument)
				.into(new ArrayList<>());
	}

	@Override
	public List<ReservaDocument> buscarParaNoShow(Instant limite) {
		return collection.find(Filters.and(
						Filters.eq("estado", EstadoReserva.CONFIRMADA.name()),
						Filters.lte("fecha_inicio", limite.toString()),
						Filters.exists("fecha_check_in", false)))
				.map(ReservaMongoDAO::fromDocument)
				.into(new ArrayList<>());
	}

	@Override
	public List<ReservaDocument> listarPorPropietario(String propietarioId) {
		return collection.find(Filters.eq("id_propietario", propietarioId))
				.sort(Sorts.descending("fecha_creacion"))
				.map(ReservaMongoDAO::fromDocument)
				.into(new ArrayList<>());
	}

	private static Bson filtro(EstadoReserva estado, Instant desde, Instant hasta) {
		List<Bson> criteria = new ArrayList<>();
		if (estado != null) {
			criteria.add(Filters.eq("estado", estado.name()));
		}
		if (desde != null) {
			criteria.add(Filters.gte("fecha_inicio", desde.toString()));
		}
		if (hasta != null) {
			criteria.add(Filters.lte("fecha_inicio", hasta.toString()));
		}
		return criteria.isEmpty() ? new Document() : Filters.and(criteria);
	}

	private static Document toDocument(ReservaDocument reserva) {
		Document pago = new Document("referencia_pasarela", reserva.pago().referenciaPasarela())
				.append("monto_total", decimal(reserva.pago().montoTotal()))
				.append("comision_plataforma", decimal(reserva.pago().comisionPlataforma()))
				.append("monto_reembolsado", decimal(reserva.pago().montoReembolsado()))
				.append("estado", reserva.pago().estado())
				.append("fecha_pago", reserva.pago().fechaPago() == null ? null : reserva.pago().fechaPago().toString())
				.append("comprobante", reserva.pago().comprobante());
		Document deposito = new Document("monto_retenido", decimal(reserva.deposito().montoRetenido()))
				.append("monto_devuelto", decimal(reserva.deposito().montoDevuelto()))
				.append("estado", reserva.deposito().estado())
				.append("fecha_resolucion", reserva.deposito().fechaResolucion() == null
						? null : reserva.deposito().fechaResolucion().toString());
		Document liquidacion = reserva.liquidacion() == null ? null : new Document(
				"monto_bruto", decimal(reserva.liquidacion().montoBruto()))
				.append("comision_descontada", decimal(reserva.liquidacion().comisionDescontada()))
				.append("penalizacion", decimal(reserva.liquidacion().penalizacion()))
				.append("monto_neto", decimal(reserva.liquidacion().montoNeto()))
				.append("estado", reserva.liquidacion().estado())
				.append("fecha_desembolso", reserva.liquidacion().fechaDesembolso() == null
						? null : reserva.liquidacion().fechaDesembolso().toString());

		return new Document("_id", reserva.id())
				.append("id_arrendatario", reserva.idArrendatario())
				.append("id_patineta", reserva.idPatineta())
				.append("id_propietario", reserva.idPropietario())
				.append("id_cupon", reserva.cuponCodigo())
				.append("fecha_inicio", reserva.fechaInicio().toString())
				.append("fecha_fin", reserva.fechaFin().toString())
				.append("estado", reserva.estado().name())
				.append("subtotal_alquiler", decimal(reserva.subtotalAlquiler()))
				.append("descuento_aplicado", decimal(reserva.descuentoAplicado()))
				.append("total_alquiler", decimal(reserva.totalAlquiler()))
				.append("recargo_retraso", decimal(reserva.recargoRetraso()))
				.append("penalizacion_propietario", decimal(reserva.penalizacionPropietario()))
				.append("pago", pago)
				.append("deposito", deposito)
				.append("liquidacion", liquidacion)
				.append("fecha_creacion", reserva.fechaCreacion().toString())
				.append("fecha_actualizacion", reserva.fechaActualizacion().toString())
				.append("fecha_check_in", reserva.fechaCheckIn() == null ? null : reserva.fechaCheckIn().toString())
				.append("fecha_checkout", reserva.fechaCheckout() == null ? null : reserva.fechaCheckout().toString())
				.append("fecha_liberacion_pago", reserva.fechaLiberacionPago() == null
						? null : reserva.fechaLiberacionPago().toString())
				.append("disputa_abierta", reserva.disputaAbierta())
				.append("motivo_cancelacion", reserva.motivoCancelacion())
				.append("observaciones", reserva.observaciones());
	}

	private static ReservaDocument fromDocument(Document document) {
		Document pago = document.get("pago", Document.class);
		Document deposito = document.get("deposito", Document.class);
		Document liquidacion = document.get("liquidacion", Document.class);
		return new ReservaDocument(
				document.getString("_id"),
				document.getString("id_arrendatario"),
				document.getString("id_patineta"),
				document.getString("id_propietario"),
				document.getString("id_cupon"),
				Instant.parse(document.getString("fecha_inicio")),
				Instant.parse(document.getString("fecha_fin")),
				EstadoReserva.valueOf(document.getString("estado")),
				money(document, "subtotal_alquiler"),
				money(document, "descuento_aplicado"),
				money(document, "total_alquiler"),
				money(document, "recargo_retraso"),
				money(document, "penalizacion_propietario"),
				pago == null ? PagoInfo.pendiente(BigDecimal.ZERO, BigDecimal.ZERO)
						: new PagoInfo(
								pago.getString("referencia_pasarela"),
								money(pago, "monto_total"),
								money(pago, "comision_plataforma"),
								money(pago, "monto_reembolsado"),
								pago.getString("estado"),
								instant(pago.getString("fecha_pago")),
								pago.getString("comprobante")),
				deposito == null ? DepositoInfo.pendiente(BigDecimal.ZERO)
						: new DepositoInfo(
								money(deposito, "monto_retenido"),
								money(deposito, "monto_devuelto"),
								deposito.getString("estado"),
								instant(deposito.getString("fecha_resolucion"))),
				liquidacion == null ? null : new LiquidacionInfo(
						money(liquidacion, "monto_bruto"),
						money(liquidacion, "comision_descontada"),
						money(liquidacion, "penalizacion"),
						money(liquidacion, "monto_neto"),
						liquidacion.getString("estado"),
						instant(liquidacion.getString("fecha_desembolso"))),
				Instant.parse(document.getString("fecha_creacion")),
				Instant.parse(document.getString("fecha_actualizacion")),
				instant(document.getString("fecha_check_in")),
				instant(document.getString("fecha_checkout")),
				instant(document.getString("fecha_liberacion_pago")),
				Boolean.TRUE.equals(document.getBoolean("disputa_abierta")),
				document.getString("motivo_cancelacion"),
				document.getString("observaciones"));
	}

	private static Decimal128 decimal(BigDecimal value) {
		return value == null ? null : new Decimal128(value);
	}

	private static BigDecimal money(Document document, String field) {
		Object value = document.get(field);
		if (value == null) {
			return BigDecimal.ZERO.setScale(2);
		}
		if (value instanceof Decimal128 decimal) {
			return decimal.bigDecimalValue();
		}
		if (value instanceof BigDecimal bigDecimal) {
			return bigDecimal;
		}
		if (value instanceof Number number) {
			return BigDecimal.valueOf(number.doubleValue());
		}
		throw new IllegalStateException("Campo monetario invalido: " + field);
	}

	private static Instant instant(String value) {
		return value == null ? null : Instant.parse(value);
	}
}
