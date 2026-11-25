package com.volt.usuariosauth.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.volt.usuariosauth.domain.Consentimiento;
import com.volt.usuariosauth.domain.EstadoCuenta;
import com.volt.usuariosauth.domain.PerfilArrendatario;
import com.volt.usuariosauth.domain.PerfilPropietario;
import com.volt.usuariosauth.domain.Usuario;
import com.volt.usuariosauth.domain.UsuarioYaExisteException;

@Repository
public class UsuarioMongoDAO implements IUsuarioDAO {
	private final MongoCollection<Document> usuarios;

	public UsuarioMongoDAO(
			MongoClient mongoClient,
			@Value("${spring.mongodb.database}") String databaseName) {
		this.usuarios = mongoClient.getDatabase(databaseName).getCollection("usuarios");
	}

	@Override
	public Usuario guardar(Usuario usuario) {
		try {
			usuarios.insertOne(toDocument(usuario));
		} catch (MongoWriteException exception) {
			if (exception.getError().getCode() == 11000) {
				throw new UsuarioYaExisteException();
			}
			throw exception;
		}
		return usuario;
	}

	@Override
	public Usuario actualizar(Usuario usuario) {
		usuarios.replaceOne(Filters.eq("_id", usuario.id()), toDocument(usuario));
		return usuario;
	}

	@Override
	public Optional<Usuario> buscarPorCorreo(String correo) {
		return buscar(Filters.eq("correo", correo));
	}

	@Override
	public Optional<Usuario> buscarPorId(String id) {
		return buscar(Filters.eq("_id", id));
	}

	@Override
	public Optional<Usuario> buscarPorDocumento(String documento) {
		return buscar(Filters.eq("documento", documento));
	}

	@Override
	public boolean existeCorreo(String correo) {
		return usuarios.countDocuments(Filters.eq("correo", correo)) > 0;
	}

	@Override
	public boolean existeDocumento(String documento) {
		return usuarios.countDocuments(Filters.eq("documento", documento)) > 0;
	}

	@Override
	public List<Usuario> listar(int pagina, int tamano, EstadoCuenta estado) {
		Bson filter = estado == null
				? new Document()
				: Filters.eq("estado_cuenta", estado.name());
		return usuarios.find(filter)
				.sort(Sorts.descending("fecha_registro"))
				.skip(Math.max(0, pagina) * tamano)
				.limit(tamano)
				.map(UsuarioMongoDAO::fromDocument)
				.into(new ArrayList<>());
	}

	@Override
	public long contar(EstadoCuenta estado) {
		Bson filter = estado == null
				? new Document()
				: Filters.eq("estado_cuenta", estado.name());
		return usuarios.countDocuments(filter);
	}

	@Override
	public void actualizarReputacion(String id, String rol, double promedio, int totalCalificaciones) {
		String prefijo = "PROPIETARIO".equalsIgnoreCase(rol)
				? "perfil_propietario"
				: "perfil_arrendatario";
		usuarios.updateOne(Filters.eq("_id", id), Updates.combine(
				Updates.set(prefijo + ".calificacion_promedio", promedio),
				Updates.set(prefijo + ".total_calificaciones", totalCalificaciones)));
	}

	private Optional<Usuario> buscar(Bson filter) {
		Document document = usuarios.find(filter).first();
		return Optional.ofNullable(document).map(UsuarioMongoDAO::fromDocument);
	}

	private static Document toDocument(Usuario usuario) {
		Document document = new Document("_id", usuario.id())
				.append("nombre", usuario.nombre())
				.append("documento", usuario.documento())
				.append("correo", usuario.correo())
				.append("password_hash", usuario.passwordHash())
				.append("telefono", usuario.telefono())
				.append("fecha_nacimiento", usuario.fechaNacimiento() == null ? null : usuario.fechaNacimiento().toString())
				.append("verificado", usuario.verificado())
				.append("estado_cuenta", usuario.estadoCuenta().name())
				.append("fecha_registro", usuario.fechaRegistro().toString())
				.append("roles", usuario.roles())
				.append("motivo_rechazo", usuario.motivoRechazo());

		if (usuario.perfilPropietario() != null) {
			PerfilPropietario perfil = usuario.perfilPropietario();
			document.append("perfil_propietario", new Document("cuenta_desembolso", perfil.cuentaDesembolso())
					.append("banco_destino", perfil.bancoDestino())
					.append("total_patinetas", perfil.totalPatinetas())
					.append("calificacion_promedio", perfil.calificacionPromedio())
					.append("total_calificaciones", perfil.totalCalificaciones()));
		}
		if (usuario.perfilArrendatario() != null) {
			PerfilArrendatario perfil = usuario.perfilArrendatario();
			document.append("perfil_arrendatario", new Document("medio_pago_token", perfil.medioPagoToken())
					.append("total_alquileres", perfil.totalAlquileres())
					.append("calificacion_promedio", perfil.calificacionPromedio())
					.append("total_calificaciones", perfil.totalCalificaciones()));
		}
		document.append("perfil_administrador", null);

		List<Document> consentimientos = new ArrayList<>();
		for (Consentimiento consentimiento : usuario.consentimientos()) {
			consentimientos.add(new Document("tipo", consentimiento.tipo())
					.append("aceptado", consentimiento.aceptado())
					.append("version", consentimiento.version())
					.append("fecha", consentimiento.fecha().toString()));
		}
		document.append("consentimientos", consentimientos);
		return document;
	}

	private static Usuario fromDocument(Document document) {
		return new Usuario(
				document.getString("_id"),
				document.getString("nombre"),
				document.getString("documento"),
				document.getString("correo"),
				document.getString("password_hash"),
				document.getString("telefono"),
				parseFecha(document.getString("fecha_nacimiento")),
				Boolean.TRUE.equals(document.getBoolean("verificado")),
				parseEstado(document.getString("estado_cuenta")),
				parseInstant(document.getString("fecha_registro")),
				document.getList("roles", String.class, List.of()),
				parsePerfilPropietario(document.get("perfil_propietario", Document.class)),
				parsePerfilArrendatario(document.get("perfil_arrendatario", Document.class)),
				parseConsentimientos(document.getList("consentimientos", Document.class, List.of())),
				document.getString("motivo_rechazo"));
	}

	private static LocalDate parseFecha(String value) {
		return value == null || value.isBlank() ? null : LocalDate.parse(value);
	}

	private static Instant parseInstant(String value) {
		if (value == null) {
			throw new IllegalStateException("El usuario no contiene fecha_registro.");
		}
		return Instant.parse(value);
	}

	private static EstadoCuenta parseEstado(String value) {
		if (value == null) {
			return EstadoCuenta.PENDIENTE_VERIFICACION;
		}
		return EstadoCuenta.valueOf(value);
	}

	private static PerfilPropietario parsePerfilPropietario(Document document) {
		if (document == null) {
			return null;
		}
		return new PerfilPropietario(
				document.getString("cuenta_desembolso"),
				document.getString("banco_destino"),
				document.getInteger("total_patinetas", 0),
				document.getDouble("calificacion_promedio") == null ? 0.0 : document.getDouble("calificacion_promedio"),
				document.getInteger("total_calificaciones", 0));
	}

	private static PerfilArrendatario parsePerfilArrendatario(Document document) {
		if (document == null) {
			return null;
		}
		return new PerfilArrendatario(
				document.getString("medio_pago_token"),
				document.getInteger("total_alquileres", 0),
				document.getDouble("calificacion_promedio") == null ? 0.0 : document.getDouble("calificacion_promedio"),
				document.getInteger("total_calificaciones", 0));
	}

	private static List<Consentimiento> parseConsentimientos(List<Document> documents) {
		List<Consentimiento> consentimientos = new ArrayList<>();
		for (Document document : documents) {
			consentimientos.add(new Consentimiento(
					document.getString("tipo"),
					Boolean.TRUE.equals(document.getBoolean("aceptado")),
					document.getString("version"),
					Instant.parse(document.getString("fecha"))));
		}
		return consentimientos;
	}
}
