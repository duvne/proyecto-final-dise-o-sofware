package com.volt.usuariosauth.persistence;

import java.util.Optional;

import com.volt.usuariosauth.domain.Usuario;

public interface IUsuarioDAO {
	Usuario guardar(Usuario usuario);

	Optional<Usuario> buscarPorCorreo(String correo);
}
