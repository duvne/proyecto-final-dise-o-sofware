package com.volt.catalogoflota.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.volt.catalogoflota.document.PatinetaDocument;
import com.volt.catalogoflota.document.PuntoEntregaDocument;
import com.volt.catalogoflota.document.TarifaDocument;
import com.volt.catalogoflota.document.ZonaDocument;
import com.volt.catalogoflota.persistence.IPatinetaDAO;
import com.volt.catalogoflota.view.dto.PatinetaRequestDTO;
import com.volt.catalogoflota.view.dto.PuntoEntregaDTO;
import com.volt.catalogoflota.view.dto.TarifaDTO;
import com.volt.catalogoflota.view.dto.ZonaDTO;

@ExtendWith(MockitoExtension.class)
class PatinetaServiceTest {
	@Mock
	private IPatinetaDAO patinetaDAO;

	@InjectMocks
	private PatinetaService patinetaService;

	@Test
	void nuevaPatinetaSeGuardaComoBorradorNoVisibleEnCatalogo() {
		when(patinetaDAO.guardar(any(PatinetaDocument.class))).thenAnswer(invocation -> {
			PatinetaDocument document = invocation.getArgument(0);
			return new PatinetaDocument(
					"patineta-1",
					document.idPropietario(),
					document.categoria(),
					document.marca(),
					document.modelo(),
					document.autonomiaKm(),
					document.velocidadMaxima(),
					document.estadoFisico(),
					document.estadoPublicacion(),
					document.activa(),
					document.tarifa(),
					document.puntoEntrega());
		});

		var created = patinetaService.crearBorrador(request());

		assertEquals("patineta-1", created.id());
		assertEquals("BORRADOR", created.estadoPublicacion());
		assertFalse(created.activa());
		verify(patinetaDAO).guardar(any(PatinetaDocument.class));
	}

	@Test
	void obtenerPatinetaActivaInexistenteFallaExplicitamente() {
		when(patinetaDAO.buscarActivaPorId("missing")).thenReturn(Optional.empty());

		assertThrows(PatinetaNoEncontradaException.class, () -> patinetaService.obtenerActiva("missing"));
	}

	private static PatinetaRequestDTO request() {
		return new PatinetaRequestDTO(
				"propietario-1",
				"Urbana",
				"VOLT",
				"City",
				35.0,
				25.0,
				"BUENO",
				new TarifaDTO(new BigDecimal("5000"), new BigDecimal("30000"), new BigDecimal("50000")),
				new PuntoEntregaDTO(
						1L,
						"Calle 1 # 2-3, Barranquilla",
						10.98,
						-74.80,
						true,
						new ZonaDTO(1L, "Norte", "Barranquilla")));
	}
}
