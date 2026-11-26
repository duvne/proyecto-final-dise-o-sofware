package com.volt.catalogoflota.business;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.volt.catalogoflota.client.ReservasClient;
import com.volt.catalogoflota.document.CategoriaDocument;
import com.volt.catalogoflota.document.MantenimientoDocument;
import com.volt.catalogoflota.document.PatinetaDocument;
import com.volt.catalogoflota.document.PuntoEntregaDocument;
import com.volt.catalogoflota.document.TarifaDocument;
import com.volt.catalogoflota.document.ZonaDocument;
import com.volt.catalogoflota.persistence.FiltroPatinetas;
import com.volt.catalogoflota.persistence.IAlertaDAO;
import com.volt.catalogoflota.persistence.ICategoriaDAO;
import com.volt.catalogoflota.persistence.IMantenimientoDAO;
import com.volt.catalogoflota.persistence.IPatinetaDAO;
import com.volt.catalogoflota.view.dto.MantenimientoRequest;
import com.volt.catalogoflota.view.dto.MantenimientoResponse;
import com.volt.catalogoflota.view.dto.ModeracionPatinetaRequest;
import com.volt.catalogoflota.view.dto.PatinetaPageResponseDTO;
import com.volt.catalogoflota.view.dto.PatinetaRequestDTO;
import com.volt.catalogoflota.view.dto.PatinetaResponseDTO;
import com.volt.shared.audit.AuditoriaService;
import com.volt.shared.security.TokenPrincipal;

@Service
public class PatinetaService {
	private final IPatinetaDAO patinetaDAO;
	private final ICategoriaDAO categoriaDAO;
	private final IMantenimientoDAO mantenimientoDAO;
	private final ReservasClient reservasClient;
	private final AuditoriaService auditoriaService;

	public PatinetaService(
			IPatinetaDAO patinetaDAO,
			ICategoriaDAO categoriaDAO,
			IMantenimientoDAO mantenimientoDAO,
			ReservasClient reservasClient,
			AuditoriaService auditoriaService) {
		this.patinetaDAO = patinetaDAO;
		this.categoriaDAO = categoriaDAO;
		this.mantenimientoDAO = mantenimientoDAO;
		this.reservasClient = reservasClient;
		this.auditoriaService = auditoriaService;
	}

	public PatinetaResponseDTO crearBorrador(PatinetaRequestDTO request, TokenPrincipal principal, String ip) {
		validarPropietario(principal);
		CategoriaDocument categoria = categoriaDAO.buscarPorNombre(request.categoria())
				.filter(CategoriaDocument::activa)
				.orElseThrow(() -> new IllegalArgumentException("La categoria indicada no existe o esta inactiva."));
		PuntoEntregaDocument punto = punto(request.puntoEntrega(), false);
		PatinetaDocument patineta = new PatinetaDocument(
				UUID.randomUUID().toString(),
				principal.userId(),
				categoria.nombre(),
				request.marca().trim(),
				request.modelo().trim(),
				request.autonomiaKm(),
				request.velocidadMaxima(),
				request.estadoFisico(),
				"BORRADOR",
				false,
				new TarifaDocument(request.tarifa().valorHora(), request.tarifa().valorDia(),
						request.tarifa().valorDeposito()),
				punto,
				request.fotos(),
				request.documentos(),
				null,
				false,
				0.0,
				0,
				Instant.now(),
				Instant.now());
		PatinetaDocument guardada = patinetaDAO.guardar(patineta);
		auditar(principal.userId(), "CREAR_PATINETA_BORRADOR", guardada.id(), null,
				MapaPatineta.de(guardada), ip);
		return PatinetaResponseDTO.desde(guardada);
	}

	public PatinetaResponseDTO actualizar(String id, PatinetaRequestDTO request, TokenPrincipal principal, String ip) {
		PatinetaDocument actual = obtenerPropia(id, principal);
		if (!List.of("BORRADOR", "RECHAZADA", "PAUSADA").contains(actual.estadoPublicacion())) {
			throw new IllegalArgumentException("Solo se puede editar una patineta en borrador, rechazada o pausada.");
		}
		CategoriaDocument categoria = categoriaDAO.buscarPorNombre(request.categoria())
				.filter(CategoriaDocument::activa)
				.orElseThrow(() -> new IllegalArgumentException("La categoria indicada no existe o esta inactiva."));
		PatinetaDocument actualizada = new PatinetaDocument(
				actual.id(), actual.idPropietario(), categoria.nombre(), request.marca().trim(),
				request.modelo().trim(), request.autonomiaKm(), request.velocidadMaxima(),
				request.estadoFisico(), actual.estadoPublicacion(), actual.activa(),
				new TarifaDocument(request.tarifa().valorHora(), request.tarifa().valorDia(),
						request.tarifa().valorDeposito()),
				punto(request.puntoEntrega(), actual.puntoEntrega().verificado()),
				request.fotos(), request.documentos(), actual.observacionesModeracion(),
				actual.enMantenimiento(), actual.calificacionPromedio(), actual.totalCalificaciones(),
				actual.fechaRegistro(), Instant.now());
		PatinetaDocument guardada = patinetaDAO.actualizar(actualizada);
		auditar(principal.userId(), "ACTUALIZAR_PATINETA", id, MapaPatineta.de(actual), MapaPatineta.de(guardada), ip);
		return PatinetaResponseDTO.desde(guardada);
	}

	public List<PatinetaResponseDTO> listarActivas(String categoria) {
		return patinetaDAO.listarActivas(categoria).stream().map(PatinetaResponseDTO::desde).toList();
	}

	public PatinetaPageResponseDTO buscarActivas(FiltroPatinetas filtros) {
		return PatinetaPageResponseDTO.desde(patinetaDAO.buscarActivas(filtros));
	}

	public PatinetaPageResponseDTO buscarActivas(FiltroPatinetas filtros, Instant inicio, Instant fin) {
		if (inicio == null && fin == null) {
			return buscarActivas(filtros);
		}
		if (inicio == null || fin == null || !fin.isAfter(inicio)) {
			throw new IllegalArgumentException("El rango de fechas es invalido.");
		}
		FiltroPatinetas conDisponibilidad = new FiltroPatinetas(
				filtros.categoria(), filtros.ciudad(), filtros.precioHoraMaximo(),
				filtros.autonomiaMinimaKm(), filtros.pagina(), filtros.tamano(),
				reservasClient.patinetasOcupadas(inicio, fin));
		return PatinetaPageResponseDTO.desde(patinetaDAO.buscarActivas(conDisponibilidad));
	}

	public PatinetaResponseDTO obtenerActiva(String id) {
		return patinetaDAO.buscarActivaPorId(id)
				.map(PatinetaResponseDTO::desde)
				.orElseThrow(() -> new PatinetaNoEncontradaException(id));
	}

	public PatinetaResponseDTO obtenerParaPropietario(String id, TokenPrincipal principal) {
		return PatinetaResponseDTO.desde(obtenerPropia(id, principal));
	}

	public List<PatinetaResponseDTO> listarPorPropietario(TokenPrincipal principal) {
		return patinetaDAO.listarPorPropietario(principal.userId()).stream()
				.map(PatinetaResponseDTO::desde).toList();
	}

	public PatinetaResponseDTO enviarARevision(String id, TokenPrincipal principal, String ip) {
		PatinetaDocument actual = obtenerPropia(id, principal);
		if (!List.of("BORRADOR", "RECHAZADA", "PAUSADA").contains(actual.estadoPublicacion())) {
			throw new IllegalArgumentException("La patineta no se puede enviar a revision en su estado actual.");
		}
		PatinetaDocument actualizada = copiar(actual, "EN_REVISION", false, actual.enMantenimiento(),
				actual.observacionesModeracion());
		PatinetaDocument guardada = patinetaDAO.actualizar(actualizada);
		auditar(principal.userId(), "ENVIAR_PATINETA_REVISION", id, null,
				MapaPatineta.de(guardada), ip);
		return PatinetaResponseDTO.desde(guardada);
	}

	public PatinetaResponseDTO moderar(String id, ModeracionPatinetaRequest request, String adminId, String ip) {
		PatinetaDocument actual = patinetaDAO.buscarPorId(id)
				.orElseThrow(() -> new PatinetaNoEncontradaException(id));
		if (!"EN_REVISION".equals(actual.estadoPublicacion())) {
			throw new IllegalArgumentException("Solo se pueden moderar patinetas en revision.");
		}
		boolean aprobado = Boolean.TRUE.equals(request.aprobado());
		PatinetaDocument actualizada = copiar(actual,
				aprobado ? "APROBADA" : "RECHAZADA",
				aprobado && !actual.enMantenimiento(),
				actual.enMantenimiento(),
				aprobado ? null : request.observaciones());
		PatinetaDocument guardada = patinetaDAO.actualizar(actualizada);
		auditar(adminId, aprobado ? "APROBAR_PATINETA" : "RECHAZAR_PATINETA", id,
				MapaPatineta.de(actual), MapaPatineta.de(guardada), ip);
		return PatinetaResponseDTO.desde(guardada);
	}

	public PatinetaResponseDTO pausar(String id, TokenPrincipal principal, String ip) {
		PatinetaDocument actual = obtenerPropia(id, principal);
		if (!"APROBADA".equals(actual.estadoPublicacion())) {
			throw new IllegalArgumentException("Solo se puede pausar una patineta aprobada.");
		}
		PatinetaDocument guardada = patinetaDAO.actualizar(copiar(actual, "PAUSADA", false,
				actual.enMantenimiento(), actual.observacionesModeracion()));
		auditar(principal.userId(), "PAUSAR_PATINETA", id, null, MapaPatineta.de(guardada), ip);
		return PatinetaResponseDTO.desde(guardada);
	}

	public PatinetaResponseDTO reactivar(String id, TokenPrincipal principal, String ip) {
		PatinetaDocument actual = obtenerPropia(id, principal);
		if (!"PAUSADA".equals(actual.estadoPublicacion())) {
			throw new IllegalArgumentException("Solo se puede reactivar una patineta pausada.");
		}
		PatinetaDocument guardada = patinetaDAO.actualizar(copiar(actual, "APROBADA",
				!actual.enMantenimiento(), actual.enMantenimiento(), actual.observacionesModeracion()));
		auditar(principal.userId(), "REACTIVAR_PATINETA", id, null, MapaPatineta.de(guardada), ip);
		return PatinetaResponseDTO.desde(guardada);
	}

	public PatinetaResponseDTO retirar(String id, TokenPrincipal principal, String ip) {
		PatinetaDocument actual = obtenerPropia(id, principal);
		PatinetaDocument guardada = patinetaDAO.actualizar(copiar(actual, "RETIRADA", false,
				actual.enMantenimiento(), actual.observacionesModeracion()));
		auditar(principal.userId(), "RETIRAR_PATINETA", id, null, MapaPatineta.de(guardada), ip);
		return PatinetaResponseDTO.desde(guardada);
	}

	public PatinetaResponseDTO marcarMantenimiento(String id, MantenimientoRequest request,
			TokenPrincipal principal, String ip) {
		PatinetaDocument actual = obtenerPropia(id, principal);
		if (!List.of("APROBADA", "PAUSADA").contains(actual.estadoPublicacion())) {
			throw new IllegalArgumentException("Solo se puede enviar a mantenimiento una patineta aprobada o pausada.");
		}
		PatinetaDocument guardada = patinetaDAO.actualizar(copiar(actual, actual.estadoPublicacion(), false,
				true, actual.observacionesModeracion()));
		mantenimientoDAO.guardar(new MantenimientoDocument(UUID.randomUUID().toString(), id,
				request.descripcion(), principal.userId(), Instant.now()));
		auditar(principal.userId(), "INICIAR_MANTENIMIENTO", id, null, Map.of("descripcion", request.descripcion()), ip);
		return PatinetaResponseDTO.desde(guardada);
	}

	public PatinetaResponseDTO finalizarMantenimiento(String id, TokenPrincipal principal, String ip) {
		PatinetaDocument actual = obtenerPropia(id, principal);
		if (!actual.enMantenimiento()) {
			throw new IllegalArgumentException("La patineta no esta en mantenimiento.");
		}
		PatinetaDocument guardada = patinetaDAO.actualizar(copiar(actual, actual.estadoPublicacion(),
				"APROBADA".equals(actual.estadoPublicacion()), false, actual.observacionesModeracion()));
		auditar(principal.userId(), "FINALIZAR_MANTENIMIENTO", id, null, null, ip);
		return PatinetaResponseDTO.desde(guardada);
	}

	public PatinetaResponseDTO agregarFotos(String id, List<String> urls, TokenPrincipal principal, String ip) {
		PatinetaDocument actual = obtenerPropia(id, principal);
		List<String> fotos = new ArrayList<>(actual.fotos());
		fotos.addAll(urls);
		PatinetaDocument guardada = new PatinetaDocument(actual.id(), actual.idPropietario(), actual.categoria(),
				actual.marca(), actual.modelo(), actual.autonomiaKm(), actual.velocidadMaxima(),
				actual.estadoFisico(), actual.estadoPublicacion(), actual.activa(), actual.tarifa(),
				actual.puntoEntrega(), fotos, actual.documentos(), actual.observacionesModeracion(),
				actual.enMantenimiento(), actual.calificacionPromedio(), actual.totalCalificaciones(),
				actual.fechaRegistro(), Instant.now());
		patinetaDAO.actualizar(guardada);
		auditar(principal.userId(), "AGREGAR_FOTOS", id, null, Map.of("cantidad", urls.size()), ip);
		return PatinetaResponseDTO.desde(guardada);
	}

	public PatinetaResponseDTO agregarDocumentos(String id, List<String> urls, TokenPrincipal principal, String ip) {
		PatinetaDocument actual = obtenerPropia(id, principal);
		List<String> documentos = new ArrayList<>(actual.documentos());
		documentos.addAll(urls);
		PatinetaDocument guardada = new PatinetaDocument(actual.id(), actual.idPropietario(), actual.categoria(),
				actual.marca(), actual.modelo(), actual.autonomiaKm(), actual.velocidadMaxima(),
				actual.estadoFisico(), actual.estadoPublicacion(), actual.activa(), actual.tarifa(),
				actual.puntoEntrega(), actual.fotos(), documentos, actual.observacionesModeracion(),
				actual.enMantenimiento(), actual.calificacionPromedio(), actual.totalCalificaciones(),
				actual.fechaRegistro(), Instant.now());
		patinetaDAO.actualizar(guardada);
		auditar(principal.userId(), "AGREGAR_DOCUMENTOS", id, null, Map.of("cantidad", urls.size()), ip);
		return PatinetaResponseDTO.desde(guardada);
	}

	public PatinetaResponseDTO verificarPunto(String id, String adminId, String ip) {
		PatinetaDocument actual = patinetaDAO.buscarPorId(id)
				.orElseThrow(() -> new PatinetaNoEncontradaException(id));
		PuntoEntregaDocument punto = new PuntoEntregaDocument(actual.puntoEntrega().idPunto(),
				actual.puntoEntrega().direccion(), actual.puntoEntrega().latitud(),
				actual.puntoEntrega().longitud(), true, actual.puntoEntrega().zona());
		PatinetaDocument guardada = new PatinetaDocument(actual.id(), actual.idPropietario(), actual.categoria(),
				actual.marca(), actual.modelo(), actual.autonomiaKm(), actual.velocidadMaxima(),
				actual.estadoFisico(), actual.estadoPublicacion(), actual.activa(), actual.tarifa(),
				punto, actual.fotos(), actual.documentos(), actual.observacionesModeracion(),
				actual.enMantenimiento(), actual.calificacionPromedio(), actual.totalCalificaciones(),
				actual.fechaRegistro(), Instant.now());
		patinetaDAO.actualizar(guardada);
		auditar(adminId, "VERIFICAR_PUNTO_ENTREGA", id, null, null, ip);
		return PatinetaResponseDTO.desde(guardada);
	}

	public PatinetaPageResponseDTO listarPendientes(int pagina, int tamano) {
		return PatinetaPageResponseDTO.desde(patinetaDAO.listarPendientes(pagina, tamano));
	}

	public void eliminar(String id, TokenPrincipal principal, String ip) {
		obtenerPropia(id, principal);
		patinetaDAO.eliminar(id);
		auditar(principal.userId(), "ELIMINAR_PATINETA", id, null, Map.of("id", id), ip);
	}

	public List<MantenimientoResponse> historialMantenimiento(String id, TokenPrincipal principal) {
		obtenerPropia(id, principal);
		return mantenimientoDAO.listarPorPatineta(id).stream().map(MantenimientoResponse::desde).toList();
	}

	private PatinetaDocument obtenerPropia(String id, TokenPrincipal principal) {
		PatinetaDocument actual = patinetaDAO.buscarPorId(id)
				.orElseThrow(() -> new PatinetaNoEncontradaException(id));
		if (!principal.esAdministrador() && !principal.userId().equals(actual.idPropietario())) {
			throw new AccesoDenegadoException("No puedes administrar una patineta que no te pertenece.");
		}
		return actual;
	}

	private static void validarPropietario(TokenPrincipal principal) {
		if (!principal.esAdministrador() && !principal.tieneRol("PROPIETARIO")) {
			throw new AccesoDenegadoException("Debes activar el perfil de propietario para publicar patinetas.");
		}
		if (!principal.verificado()) {
			throw new AccesoDenegadoException("Tu cuenta debe estar verificada para publicar patinetas.");
		}
	}

	private static PuntoEntregaDocument punto(com.volt.catalogoflota.view.dto.PuntoEntregaDTO dto, boolean verificado) {
		return new PuntoEntregaDocument(
				dto.idPunto(),
				dto.direccion(),
				dto.latitud(),
				dto.longitud(),
				verificado,
				new ZonaDocument(dto.zona().idZona(), dto.zona().nombre(), dto.zona().ciudad()));
	}

	private static PatinetaDocument copiar(PatinetaDocument actual, String estado, boolean activa,
			boolean enMantenimiento, String observaciones) {
		return new PatinetaDocument(actual.id(), actual.idPropietario(), actual.categoria(), actual.marca(),
				actual.modelo(), actual.autonomiaKm(), actual.velocidadMaxima(), actual.estadoFisico(),
				estado, activa, actual.tarifa(), actual.puntoEntrega(), actual.fotos(), actual.documentos(),
				observaciones, enMantenimiento, actual.calificacionPromedio(), actual.totalCalificaciones(),
				actual.fechaRegistro(), Instant.now());
	}

	private void auditar(String actorId, String accion, String entidadId, Object antes, Object despues, String ip) {
		auditoriaService.registrar(actorId, accion, "PATINETA", entidadId, antes, despues, ip);
	}

	private static final class MapaPatineta {
		private MapaPatineta() {
		}

		static java.util.Map<String, Object> de(PatinetaDocument document) {
			return java.util.Map.of(
					"categoria", document.categoria(),
					"marca", document.marca(),
					"modelo", document.modelo(),
					"estado_publicacion", document.estadoPublicacion(),
					"activa", document.activa());
		}
	}
}
