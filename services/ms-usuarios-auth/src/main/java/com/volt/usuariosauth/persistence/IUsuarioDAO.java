package com.volt.usuariosauth.persistence;

import java.util.List;
import java.util.Optional;

import com.volt.usuariosauth.domain.EstadoCuenta;
import com.volt.usuariosauth.domain.Usuario;

public interface IUsuarioDAO {
	Usuario guardar(Usuario usuario);

	Usuario actualizar(Usuario usuario);

	Optional<Usuario> buscarPorCorreo(String correo);

	Optional<Usuario> buscarPorId(String id);

	Optional<Usuario> buscarPorDocumento(String documento);

	boolean existeCorreo(String correo);

	boolean existeDocumento(String documento);

	List<Usuario> listar(int pagina, int tamano, EstadoCuenta estado);

	long contar(EstadoCuenta estado);

	void actualizarReputacion(String id, String rol, double promedio, int totalCalificaciones);
}
