package com.volt.usuariosauth.presentation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.volt.shared.security.InternalKey;
import com.volt.usuariosauth.business.UsuarioService;
import com.volt.usuariosauth.domain.AccesoDenegadoException;
import com.volt.usuariosauth.view.ReputacionRequest;
import com.volt.usuariosauth.view.UsuarioInternoResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth/interno/usuarios")
public class InternalUsuarioController {
	private final UsuarioService usuarioService;
	private final String internalKey;

	public InternalUsuarioController(
			UsuarioService usuarioService,
			@Value("${volt.internal-key:volt-internal-dev-key}") String internalKey) {
		this.usuarioService = usuarioService;
		this.internalKey = internalKey;
	}

	@GetMapping("/{id}")
	public UsuarioInternoResponse obtener(@PathVariable String id, HttpServletRequest request) {
		validar(request);
		return usuarioService.obtenerInterno(id);
	}

	@PostMapping("/{id}/reputacion")
	public void actualizarReputacion(
			@PathVariable String id,
			@Valid @RequestBody ReputacionRequest body,
			HttpServletRequest request) {
		validar(request);
		usuarioService.actualizarReputacion(id, body.rol(), body.promedio(), body.totalCalificaciones());
	}

	private void validar(HttpServletRequest request) {
		if (!InternalKey.valida(request, internalKey)) {
			throw new AccesoDenegadoException("Clave interna invalida.");
		}
	}
}
