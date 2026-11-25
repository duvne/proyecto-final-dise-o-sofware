package com.volt.usuariosauth.persistence;

import java.util.List;

import com.volt.usuariosauth.domain.RegistroAuditoria;

public interface IAuditoriaDAO {
	List<RegistroAuditoria> listar(int pagina, int tamano);

	long contar();
}
