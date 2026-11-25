package com.volt.usuariosauth.persistence;

import java.time.Instant;
import java.util.Optional;

import com.volt.usuariosauth.domain.RecuperacionContrasena;

public interface IRecuperacionDAO {
	void guardar(RecuperacionContrasena recuperacion);

	Optional<RecuperacionContrasena> buscarVigente(String tokenHash, Instant ahora);

	void marcarUsado(String id);
}
