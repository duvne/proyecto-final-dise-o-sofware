package com.volt.usuariosauth.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.Instant;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
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
		Document document = new Document("_id", usuario.id())
				.append("nombre", usuario.nombre())
				.append("documento", usuario.documento())
				.append("correo", usuario.correo())
				.append("password_hash", usuario.passwordHash())
				.append("telefono", usuario.telefono())
				.append("fecha_nacimiento", usuario.fechaNacimiento())
				.append("verificado", usuario.verificado())
				.append("estado_cuenta", usuario.estadoCuenta())
				.append("fecha_registro", usuario.fechaRegistro().toString())
				.append("roles", usuario.roles());
		try {
			usuarios.insertOne(document);
		} catch (MongoWriteException exception) {
			if (exception.getError().getCode() == 11000) {
				throw new UsuarioYaExisteException();
			}
			throw exception;
		}
		return usuario;
	}

	@Override
	public Optional<Usuario> buscarPorCorreo(String correo) {
		Document document = usuarios.find(Filters.eq("correo", correo)).first();
		if (document == null) {
			return Optional.empty();
		}
		List<String> roles = document.getList("roles", String.class, List.of());
		return Optional.of(new Usuario(
				document.getString("_id"),
				document.getString("nombre"),
				document.getString("documento"),
				document.getString("correo"),
				document.getString("password_hash"),
				document.getString("telefono"),
				document.getString("fecha_nacimiento"),
				Boolean.TRUE.equals(document.getBoolean("verificado")),
				document.getString("estado_cuenta"),
				parseFechaRegistro(document.getString("fecha_registro")),
				new ArrayList<>(roles)));
	}

	private static Instant parseFechaRegistro(String value) {
		if (value == null) {
			throw new IllegalStateException("El usuario no contiene fecha_registro.");
		}
		return Instant.parse(value);
	}
}
