package com.volt.catalogoflota.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.volt.catalogoflota.document.CategoriaDocument;
import com.volt.catalogoflota.document.PatinetaDocument;
import com.volt.catalogoflota.document.PuntoEntregaDocument;
import com.volt.catalogoflota.document.TarifaDocument;
import com.volt.catalogoflota.document.ZonaDocument;
import com.volt.catalogoflota.persistence.ICategoriaDAO;
import com.volt.catalogoflota.persistence.IPatinetaDAO;
import com.volt.catalogoflota.view.dto.PatinetaRequestDTO;
import com.volt.catalogoflota.view.dto.PatinetaResponseDTO;
import com.volt.catalogoflota.view.dto.PuntoEntregaDTO;
import com.volt.catalogoflota.view.dto.TarifaDTO;
import com.volt.catalogoflota.view.dto.ZonaDTO;
import com.volt.shared.security.TokenPrincipal;

@ExtendWith(MockitoExtension.class)
class PatinetaServiceTest {
	@Mock
	private IPatinetaDAO patinetaDAO;
	@Mock
	private ICategoriaDAO categoriaDAO;
	@Mock
	private com.volt.shared.audit.AuditoriaService auditoriaService;

	@InjectMocks
	private PatinetaService patinetaService;

	@Test
	void nuevaPatinetaSeGuardaComoBorradorNoVisibleEnCatalogo() {
		when(categoriaDAO.buscarPorNombre("Urbana")).thenReturn(Optional.of(new CategoriaDocument(
				"cat-1", "Urbana", " urbano ", 20.0, true)));
		when(patinetaDAO.guardar(any(PatinetaDocument.class))).thenAnswer(invocation -> {
			PatinetaDocument document = invocation.getArgument(0);
			return document;
		});

		var created = patinetaService.crearBorrador(request(), principal(), "127.0.0.1");

		assertFalse(created.id().isBlank());
		assertEquals("propietario-1", created.idPropietario());
		assertEquals("BORRADOR", created.estadoPublicacion());
		assertFalse(created.activa());
		verify(patinetaDAO).guardar(any(PatinetaDocument.class));
	}

	@Test
	void obtenerPatinetaActivaInexistenteFallaExplicitamente() {
		when(patinetaDAO.buscarActivaPorId("missing")).thenReturn(Optional.empty());

		assertThrows(PatinetaNoEncontradaException.class, () -> patinetaService.obtenerActiva("missing"));
	}

	private static TokenPrincipal principal() {
		return new TokenPrincipal("propietario-1", "propietario-1@example.com",
				List.of("PROPIETARIO"), true);
	}

	private static PatinetaRequestDTO request() {
		return new PatinetaRequestDTO(
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
						new ZonaDTO(1L, "Norte", "Barranquilla")),
				List.of(),
				List.of());
	}

	@SuppressWarnings("unused")
	private static final class Aux {
		private Aux() {
		}
	}
}
