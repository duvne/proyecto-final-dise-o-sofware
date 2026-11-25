package com.volt.catalogoflota.presentation;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volt.catalogoflota.business.PatinetaService;
import com.volt.catalogoflota.persistence.FiltroPatinetas;
import com.volt.catalogoflota.view.dto.PatinetaPageResponseDTO;
import com.volt.catalogoflota.view.dto.PatinetaRequestDTO;
import com.volt.catalogoflota.view.dto.PatinetaResponseDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/catalogo/patinetas")
public class PatinetaRestController {
	private final PatinetaService patinetaService;

	public PatinetaRestController(PatinetaService patinetaService) {
		this.patinetaService = patinetaService;
	}

	@PostMapping
	public ResponseEntity<PatinetaResponseDTO> crear(@Valid @RequestBody PatinetaRequestDTO request) {
		PatinetaResponseDTO created = patinetaService.crearBorrador(request);
		return ResponseEntity.created(URI.create("/api/catalogo/patinetas/" + created.id())).body(created);
	}

	@GetMapping
	public List<PatinetaResponseDTO> listarActivas(
			@RequestParam(required = false) String categoria) {
		return patinetaService.listarActivas(categoria);
	}

	@GetMapping("/busqueda")
	public PatinetaPageResponseDTO buscarActivas(
			@RequestParam(required = false) String categoria,
			@RequestParam(required = false) String ciudad,
			@RequestParam(required = false) BigDecimal precioHoraMaximo,
			@RequestParam(required = false) Double autonomiaMinimaKm,
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamano) {
		return patinetaService.buscarActivas(new FiltroPatinetas(
				categoria,
				ciudad,
				precioHoraMaximo,
				autonomiaMinimaKm,
				pagina,
				tamano));
	}

	@GetMapping("/{id}")
	public PatinetaResponseDTO obtenerActiva(@PathVariable String id) {
		return patinetaService.obtenerActiva(id);
	}
}
