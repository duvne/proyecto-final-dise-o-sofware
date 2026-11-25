package com.volt.catalogoflota.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.volt.catalogoflota.business.AlertaService;
import com.volt.catalogoflota.view.dto.AlertaRequest;
import com.volt.catalogoflota.view.dto.AlertaResponse;
import com.volt.shared.security.TokenPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/catalogo/alertas")
public class AlertaController {
	private final AlertaService alertaService;

	public AlertaController(AlertaService alertaService) {
		this.alertaService = alertaService;
	}

	@GetMapping
	public List<AlertaResponse> listar(@AuthenticationPrincipal TokenPrincipal principal) {
		return alertaService.listar(principal);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AlertaResponse crear(@Valid @RequestBody AlertaRequest request,
			@AuthenticationPrincipal TokenPrincipal principal) {
		return alertaService.crear(request, principal);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable String id, @AuthenticationPrincipal TokenPrincipal principal) {
		alertaService.eliminar(id, principal);
	}
}
