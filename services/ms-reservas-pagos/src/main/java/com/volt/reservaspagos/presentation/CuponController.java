package com.volt.reservaspagos.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.volt.reservaspagos.model.service.CuponService;
import com.volt.reservaspagos.view.dto.CuponRequest;
import com.volt.reservaspagos.view.dto.CuponResponse;
import com.volt.reservaspagos.view.dto.ValidarCuponResponse;
import com.volt.shared.security.TokenPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pagos/cupones")
public class CuponController {
	private final CuponService cuponService;

	public CuponController(CuponService cuponService) {
		this.cuponService = cuponService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public java.util.List<CuponResponse> listar() {
		return cuponService.listar();
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@ResponseStatus(HttpStatus.CREATED)
	public CuponResponse crear(@Valid @RequestBody CuponRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return cuponService.crear(request, principal.userId(), ip(httpRequest));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public CuponResponse actualizar(@PathVariable String id, @Valid @RequestBody CuponRequest request,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		return cuponService.actualizar(id, request, principal.userId(), ip(httpRequest));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable String id,
			@AuthenticationPrincipal TokenPrincipal principal, HttpServletRequest httpRequest) {
		cuponService.eliminar(id, principal.userId(), ip(httpRequest));
	}

	@PostMapping("/validar")
	public ValidarCuponResponse validar(@Valid @RequestBody com.volt.reservaspagos.view.dto.ValidarCuponRequest request) {
		return cuponService.validar(request.codigo());
	}

	private static String ip(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		return forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
	}
}
