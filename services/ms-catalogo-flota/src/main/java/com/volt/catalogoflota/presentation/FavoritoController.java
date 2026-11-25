package com.volt.catalogoflota.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.volt.catalogoflota.business.FavoritoService;
import com.volt.catalogoflota.view.dto.FavoritoResponse;
import com.volt.shared.security.TokenPrincipal;

@RestController
@RequestMapping("/api/catalogo/favoritos")
public class FavoritoController {
	private final FavoritoService favoritoService;

	public FavoritoController(FavoritoService favoritoService) {
		this.favoritoService = favoritoService;
	}

	@GetMapping
	public List<FavoritoResponse> listar(@AuthenticationPrincipal TokenPrincipal principal) {
		return favoritoService.listar(principal);
	}

	@PostMapping("/{patinetaId}")
	@ResponseStatus(HttpStatus.CREATED)
	public FavoritoResponse agregar(@PathVariable String patinetaId,
			@AuthenticationPrincipal TokenPrincipal principal) {
		return favoritoService.agregar(patinetaId, principal);
	}

	@DeleteMapping("/{patinetaId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable String patinetaId,
			@AuthenticationPrincipal TokenPrincipal principal) {
		favoritoService.eliminar(patinetaId, principal);
	}
}
