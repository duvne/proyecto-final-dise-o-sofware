package com.volt.reservaspagos.model.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.volt.reservaspagos.client.CatalogoClient;
import com.volt.reservaspagos.client.NotificacionClient;
import com.volt.reservaspagos.client.PatinetaInfo;
import com.volt.reservaspagos.model.domain.AccesoDenegadoException;
import com.volt.reservaspagos.model.domain.ContraCargoDocument;
import com.volt.reservaspagos.model.domain.Cotizacion;
import com.volt.reservaspagos.model.domain.CuponDocument;
import com.volt.reservaspagos.model.domain.DepositoInfo;
import com.volt.reservaspagos.model.domain.EstadoReserva;
import com.volt.reservaspagos.model.domain.EstadoReservaInvalidoException;
import com.volt.reservaspagos.model.domain.LiquidacionInfo;
import com.volt.reservaspagos.model.domain.PagoInfo;
import com.volt.reservaspagos.model.domain.ReservaDocument;
import com.volt.reservaspagos.model.domain.ReservaDocumentBuilder;
import com.volt.reservaspagos.model.domain.ReservaNoEncontradaException;
import com.volt.reservaspagos.model.domain.ResultadoCancelacion;
import com.volt.reservaspagos.model.domain.ServicioNoDisponibleException;
import com.volt.reservaspagos.model.patterns.ConfiguracionComisionSingleton;
import com.volt.reservaspagos.model.patterns.pago.PasarelaPagoFactory;
import com.volt.reservaspagos.model.patterns.pago.RetencionPago;
import com.volt.reservaspagos.persistence.IContraCargoDAO;
import com.volt.reservaspagos.persistence.ICuponDAO;
import com.volt.reservaspagos.persistence.IReservaDAO;
import com.volt.reservaspagos.view.dto.CancelarRequest;
import com.volt.reservaspagos.view.dto.EstadoCuentaResponse;
import com.volt.reservaspagos.view.dto.FinalizarRequest;
import com.volt.reservaspagos.view.dto.FranjaResponse;
import com.volt.reservaspagos.view.dto.ReprogramarRequest;
import com.volt.reservaspagos.view.dto.ReportePagoResponse;
import com.volt.reservaspagos.view.dto.ReservaRequest;
import com.volt.reservaspagos.view.dto.ReservaResponse;
import com.volt.reservaspagos.view.dto.ResolucionDisputaRequest;
import com.volt.shared.api.PageResponse;
import com.volt.shared.audit.AuditoriaService;
import com.volt.shared.security.TokenPrincipal;

@Service
public class GestorReservas {
	private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
	private static final long HORAS_LIBERACION = 48;

	private final IReservaDAO reservaDAO;
	private final ICuponDAO cuponDAO;
	private final IContraCargoDAO contraCargoDAO;
	private final ReservaService calculadora;
	private final CuponService cuponService;
	private final CatalogoClient catalogoClient;
	private final PasarelaPagoFactory pasarelaFactory;
	private final NotificacionClient notificacionClient;
	private final AuditoriaService auditoriaService;

	public GestorReservas(IReservaDAO reservaDAO, ICuponDAO cuponDAO, IContraCargoDAO contraCargoDAO,
			ReservaService calculadora, CuponService cuponService,
			CatalogoClient catalogoClient, PasarelaPagoFactory pasarelaFactory,
			NotificacionClient notificacionClient, AuditoriaService auditoriaService) {
		this.reservaDAO = reservaDAO;
		this.cuponDAO = cuponDAO;
		this.contraCargoDAO = contraCargoDAO;
		this.calculadora = calculadora;
		this.cuponService = cuponService;
		this.catalogoClient = catalogoClient;
		this.pasarelaFactory = pasarelaFactory;
		this.notificacionClient = notificacionClient;
		this.auditoriaService = auditoriaService;
	}

	public ReservaResponse crear(ReservaRequest request, TokenPrincipal principal, String ip) {
		if (!principal.verificado()) {
			throw new AccesoDenegadoException("Tu cuenta debe estar verificada para reservar.");
		}
		PatinetaInfo patineta = catalogoClient.obtener(request.idPatineta());
		if (!patineta.activa() || !"APROBADA".equals(patineta.estadoPublicacion()) || patineta.enMantenimiento()) {
			throw new EstadoReservaInvalidoException("La patineta no esta disponible para reservar.");
		}
		Cotizacion cotizacion = cotizar(request.fechaInicio(), request.fechaFin(), patineta);
		CuponDocument cupon = null;
		BigDecimal descuento = CERO;
		if (request.cuponCodigo() != null && !request.cuponCodigo().isBlank()) {
			cupon = cuponService.obtenerValido(request.cuponCodigo());
			descuento = cuponService.calcularDescuento(cupon, cotizacion.valorAlquiler());
		}
		BigDecimal totalAlquiler = dinero(cotizacion.valorAlquiler().subtract(descuento));
		BigDecimal comision = dinero(totalAlquiler.multiply(
				ConfiguracionComisionSingleton.INSTANCIA.porcentajeComision()));
		BigDecimal deposito = dinero(cotizacion.depositoGarantia());
		Instant ahora = Instant.now();
		String id = UUID.randomUUID().toString();

		ReservaDocument reserva = new ReservaDocumentBuilder()
				.id(id)
				.idArrendatario(principal.userId())
				.idPatineta(patineta.id())
				.idPropietario(patineta.idPropietario())
				.cuponCodigo(cupon == null ? null : cupon.codigo())
				.fechaInicio(request.fechaInicio())
				.fechaFin(request.fechaFin())
				.estado(EstadoReserva.SOLICITADA)
				.subtotalAlquiler(dinero(cotizacion.valorAlquiler()))
				.descuentoAplicado(descuento)
				.totalAlquiler(totalAlquiler)
				.recargoRetraso(CERO)
				.penalizacionPropietario(CERO)
				.pago(PagoInfo.pendiente(dinero(totalAlquiler.add(deposito)), comision))
				.deposito(DepositoInfo.pendiente(deposito))
				.fechaCreacion(ahora)
				.fechaActualizacion(ahora)
				.disputaAbierta(false)
				.build();
		reservaDAO.guardar(reserva);
		if (cupon != null) {
			cuponDAO.incrementarUso(cupon.id());
		}
		notificacionClient.enviar(reserva.idPropietario(), "RESERVA_SOLICITADA",
				"Nueva solicitud de reserva",
				"Recibiste una solicitud para tu patineta " + patineta.marca() + " " + patineta.modelo(),
				reserva.id());
		notificacionClient.enviar(reserva.idArrendatario(), "RESERVA_CREADA",
				"Reserva creada", "Tu solicitud de reserva quedo pendiente de aprobacion.", reserva.id());
		auditoriaService.registrar(principal.userId(), "CREAR_RESERVA", "RESERVA", id, null,
				Map.of("patineta", patineta.id(), "inicio", request.fechaInicio().toString(),
						"fin", request.fechaFin().toString(), "total", totalAlquiler), ip);
		return ReservaResponse.desde(reserva);
	}

	public ReservaResponse aprobar(String id, TokenPrincipal principal, String ip) {
		ReservaDocument reserva = obtener(id);
		validarPropietario(reserva, principal);
		requerirEstado(reserva, EstadoReserva.SOLICITADA,
				"Solo se pueden aprobar reservas solicitadas.");
		RetencionPago retencion = pasarelaFactory.crear().retener(id, reserva.pago().montoTotal());
		PagoInfo pago = new PagoInfo(retencion.referencia(), reserva.pago().montoTotal(),
				reserva.pago().comisionPlataforma(), CERO, "RETENIDO", retencion.fecha(),
				"COMP-" + id);
		ReservaDocument actualizada = ReservaDocumentBuilder.de(reserva)
				.estado(EstadoReserva.CONFIRMADA)
				.pago(pago)
				.deposito(reserva.deposito().retenido())
				.fechaActualizacion(Instant.now())
				.build();
		reservaDAO.actualizar(actualizada);
		notificacionClient.enviar(reserva.idArrendatario(), "RESERVA_CONFIRMADA",
				"Reserva confirmada", "Tu reserva fue aprobada y el pago quedo retenido.", id);
		auditoriaService.registrar(principal.userId(), "APROBAR_RESERVA", "RESERVA", id,
				Map.of("estado", reserva.estado().name()), Map.of("estado", actualizada.estado().name()), ip);
		return ReservaResponse.desde(actualizada);
	}

	public ReservaResponse rechazar(String id, String motivo, TokenPrincipal principal, String ip) {
		ReservaDocument reserva = obtener(id);
		validarPropietario(reserva, principal);
		requerirEstado(reserva, EstadoReserva.SOLICITADA,
				"Solo se pueden rechazar reservas solicitadas.");
		ReservaDocument actualizada = ReservaDocumentBuilder.de(reserva)
				.estado(EstadoReserva.CANCELADA)
				.motivoCancelacion(motivo == null ? "Rechazada por el propietario" : motivo)
				.pago(new PagoInfo(reserva.pago().referenciaPasarela(), reserva.pago().montoTotal(),
						reserva.pago().comisionPlataforma(), CERO, "CANCELADO", null, null))
				.deposito(new DepositoInfo(reserva.deposito().montoRetenido(), CERO, "NO_RETENIDO", Instant.now()))
				.fechaActualizacion(Instant.now())
				.build();
		reservaDAO.actualizar(actualizada);
		notificacionClient.enviar(reserva.idArrendatario(), "RESERVA_RECHAZADA",
				"Reserva rechazada", "El propietario rechazo tu solicitud de reserva.", id);
		auditoriaService.registrar(principal.userId(), "RECHAZAR_RESERVA", "RESERVA", id,
				Map.of("estado", reserva.estado().name()), Map.of("estado", actualizada.estado().name(),
						"motivo", actualizada.motivoCancelacion()), ip);
		return ReservaResponse.desde(actualizada);
	}

	public ReservaResponse cancelar(String id, CancelarRequest request, TokenPrincipal principal, String ip) {
		ReservaDocument reserva = obtener(id);
		validarParticipante(reserva, principal);
		if (reserva.estado() != EstadoReserva.SOLICITADA && reserva.estado() != EstadoReserva.CONFIRMADA) {
			throw new EstadoReservaInvalidoException("La reserva ya no se puede cancelar.");
		}
		boolean arrendatario = principal.userId().equals(reserva.idArrendatario());
		boolean propietario = principal.userId().equals(reserva.idPropietario()) || principal.esAdministrador();
		Instant ahora = Instant.now();
		PagoInfo pago = reserva.pago();
		DepositoInfo deposito = reserva.deposito();
		BigDecimal penalizacion = CERO;

		if (arrendatario) {
			if (reserva.estado() == EstadoReserva.CONFIRMADA) {
				ResultadoCancelacion resultado = calculadora.cancelarPorArrendatario(
						reserva.fechaInicio(), ahora, reserva.totalAlquiler(), reserva.deposito().montoRetenido());
				pago = pago.reembolsado(resultado.totalReembolsado());
				deposito = deposito.resuelto(resultado.reembolsoDeposito());
			} else {
				pago = new PagoInfo(pago.referenciaPasarela(), pago.montoTotal(), pago.comisionPlataforma(),
						CERO, "CANCELADO", null, null);
				deposito = new DepositoInfo(deposito.montoRetenido(), CERO, "NO_RETENIDO", ahora);
			}
		} else if (propietario) {
			if (reserva.estado() == EstadoReserva.CONFIRMADA) {
				ResultadoCancelacion resultado = calculadora.cancelarPorPropietario(
						reserva.fechaInicio(), ahora, reserva.totalAlquiler(), reserva.deposito().montoRetenido());
				pago = pago.reembolsado(resultado.totalReembolsado());
				deposito = deposito.resuelto(resultado.reembolsoDeposito());
				penalizacion = resultado.penalizacionPropietario();
			} else {
				pago = new PagoInfo(pago.referenciaPasarela(), pago.montoTotal(), pago.comisionPlataforma(),
						CERO, "CANCELADO", null, null);
				deposito = new DepositoInfo(deposito.montoRetenido(), CERO, "NO_RETENIDO", ahora);
			}
		}

		ReservaDocument actualizada = ReservaDocumentBuilder.de(reserva)
				.estado(EstadoReserva.CANCELADA)
				.pago(pago)
				.deposito(deposito)
				.penalizacionPropietario(penalizacion)
				.motivoCancelacion(request.motivo() == null ? "Cancelada por el usuario" : request.motivo())
				.fechaActualizacion(ahora)
				.build();
		reservaDAO.actualizar(actualizada);
		notificacionClient.enviar(reserva.idArrendatario(), "RESERVA_CANCELADA", "Reserva cancelada",
				"La reserva fue cancelada. Reembolso: " + pago.montoReembolsado(), id);
		notificacionClient.enviar(reserva.idPropietario(), "RESERVA_CANCELADA", "Reserva cancelada",
				"La reserva fue cancelada.", id);
		auditoriaService.registrar(principal.userId(), "CANCELAR_RESERVA", "RESERVA", id,
				Map.of("estado", reserva.estado().name()), Map.of("estado", actualizada.estado().name(),
						"reembolso", pago.montoReembolsado()), ip);
		return ReservaResponse.desde(actualizada);
	}

	public ReservaResponse reprogramar(String id, ReprogramarRequest request, TokenPrincipal principal, String ip) {
		ReservaDocument reserva = obtener(id);
		if (!principal.userId().equals(reserva.idArrendatario()) && !principal.esAdministrador()) {
			throw new AccesoDenegadoException("Solo el arrendatario puede reprogramar la reserva.");
		}
		if (reserva.estado() != EstadoReserva.SOLICITADA && reserva.estado() != EstadoReserva.CONFIRMADA) {
			throw new EstadoReservaInvalidoException("La reserva no se puede reprogramar en su estado actual.");
		}
		if (Instant.now().isAfter(reserva.fechaInicio())) {
			throw new EstadoReservaInvalidoException("La reserva ya inicio y no se puede reprogramar.");
		}
		PatinetaInfo patineta = catalogoClient.obtener(reserva.idPatineta());
		Cotizacion cotizacion = cotizar(request.fechaInicio(), request.fechaFin(), patineta);
		CuponDocument cupon = reserva.cuponCodigo() == null ? null : cuponService.obtenerValido(reserva.cuponCodigo());
		BigDecimal descuento = cupon == null ? CERO : cuponService.calcularDescuento(cupon, cotizacion.valorAlquiler());
		BigDecimal total = dinero(cotizacion.valorAlquiler().subtract(descuento));
		BigDecimal comision = dinero(total.multiply(
				ConfiguracionComisionSingleton.INSTANCIA.porcentajeComision()));
		PagoInfo pago = new PagoInfo(reserva.pago().referenciaPasarela(),
				dinero(total.add(reserva.deposito().montoRetenido())), comision,
				reserva.pago().montoReembolsado(), reserva.pago().estado(), reserva.pago().fechaPago(),
				reserva.pago().comprobante());
		ReservaDocument actualizada = ReservaDocumentBuilder.de(reserva)
				.fechaInicio(request.fechaInicio())
				.fechaFin(request.fechaFin())
				.subtotalAlquiler(dinero(cotizacion.valorAlquiler()))
				.descuentoAplicado(descuento)
				.totalAlquiler(total)
				.pago(pago)
				.fechaActualizacion(Instant.now())
				.build();
		reservaDAO.actualizar(actualizada);
		notificacionClient.enviar(reserva.idPropietario(), "RESERVA_REPROGRAMADA",
				"Reserva reprogramada", "El arrendatario cambio las fechas de la reserva.", id);
		auditoriaService.registrar(principal.userId(), "REPROGRAMAR_RESERVA", "RESERVA", id,
				Map.of("inicio", reserva.fechaInicio().toString(), "fin", reserva.fechaFin().toString()),
				Map.of("inicio", request.fechaInicio().toString(), "fin", request.fechaFin().toString()), ip);
		return ReservaResponse.desde(actualizada);
	}

	public ReservaResponse extender(String id, Instant nuevaFin, TokenPrincipal principal, String ip) {
		ReservaDocument reserva = obtener(id);
		if (!principal.userId().equals(reserva.idArrendatario()) && !principal.esAdministrador()) {
			throw new AccesoDenegadoException("Solo el arrendatario puede extender la reserva.");
		}
		requerirEstado(reserva, EstadoReserva.EN_CURSO, "Solo se puede extender una reserva en curso.");
		if (nuevaFin == null || !nuevaFin.isAfter(reserva.fechaFin())) {
			throw new IllegalArgumentException("La nueva fecha de fin debe ser posterior a la actual.");
		}
		PatinetaInfo patineta = catalogoClient.obtener(reserva.idPatineta());
		Cotizacion cotizacion = cotizar(reserva.fechaInicio(), nuevaFin, patineta);
		CuponDocument cupon = reserva.cuponCodigo() == null ? null : cuponService.obtenerValido(reserva.cuponCodigo());
		BigDecimal descuento = cupon == null ? CERO : cuponService.calcularDescuento(cupon, cotizacion.valorAlquiler());
		BigDecimal total = dinero(cotizacion.valorAlquiler().subtract(descuento));
		BigDecimal diferencia = dinero(total.subtract(reserva.totalAlquiler()));
		BigDecimal comision = dinero(total.multiply(
				ConfiguracionComisionSingleton.INSTANCIA.porcentajeComision()));
		PagoInfo pago = new PagoInfo(reserva.pago().referenciaPasarela(),
				dinero(reserva.pago().montoTotal().add(diferencia.max(CERO))), comision,
				reserva.pago().montoReembolsado(), reserva.pago().estado(), reserva.pago().fechaPago(),
				reserva.pago().comprobante());
		ReservaDocument actualizada = ReservaDocumentBuilder.de(reserva)
				.fechaFin(nuevaFin)
				.subtotalAlquiler(dinero(cotizacion.valorAlquiler()))
				.descuentoAplicado(descuento)
				.totalAlquiler(total)
				.pago(pago)
				.fechaActualizacion(Instant.now())
				.build();
		reservaDAO.actualizar(actualizada);
		notificacionClient.enviar(reserva.idPropietario(), "RESERVA_EXTENDIDA",
				"Reserva extendida", "El arrendatario extendio la reserva.", id);
		auditoriaService.registrar(principal.userId(), "EXTENDER_RESERVA", "RESERVA", id,
				Map.of("fin", reserva.fechaFin().toString()), Map.of("fin", nuevaFin.toString()), ip);
		return ReservaResponse.desde(actualizada);
	}

	public ReservaResponse iniciar(String id, String actorId, String ip) {
		ReservaDocument reserva = obtener(id);
		requerirEstado(reserva, EstadoReserva.CONFIRMADA,
				"Solo se puede iniciar una reserva confirmada.");
		Instant ahora = Instant.now();
		if (ahora.isBefore(reserva.fechaInicio().minus(Duration.ofMinutes(30)))) {
			throw new EstadoReservaInvalidoException("Aun no es hora de iniciar la reserva.");
		}
		if (ahora.isAfter(reserva.fechaInicio().plus(Duration.ofHours(2)))) {
			throw new EstadoReservaInvalidoException("La reserva supero el tiempo maximo de espera.");
		}
		ReservaDocument actualizada = ReservaDocumentBuilder.de(reserva)
				.estado(EstadoReserva.EN_CURSO)
				.fechaCheckIn(ahora)
				.fechaActualizacion(ahora)
				.build();
		reservaDAO.actualizar(actualizada);
		notificacionClient.enviar(reserva.idArrendatario(), "RESERVA_INICIADA",
				"Reserva iniciada", "El propietario registro el check-in de la patineta.", id);
		auditoriaService.registrar(actorId, "INICIAR_RESERVA", "RESERVA", id,
				Map.of("estado", reserva.estado().name()), Map.of("estado", actualizada.estado().name()), ip);
		return ReservaResponse.desde(actualizada);
	}

	public ReservaResponse finalizar(String id, FinalizarRequest request, String actorId, String ip) {
		ReservaDocument reserva = obtener(id);
		requerirEstado(reserva, EstadoReserva.EN_CURSO, "Solo se puede finalizar una reserva en curso.");
		if (request.fechaDevolucion().isBefore(reserva.fechaInicio())) {
			throw new IllegalArgumentException("La fecha de devolucion no puede ser anterior al inicio.");
		}
		PatinetaInfo patineta = catalogoClient.obtener(reserva.idPatineta());
		BigDecimal recargo = calculadora.calcularRecargoTardio(reserva.fechaFin(),
				request.fechaDevolucion(), patineta.tarifa().valorHora());
		Instant liberacion = request.fechaDevolucion().plus(Duration.ofHours(HORAS_LIBERACION));
		ReservaDocument actualizada = ReservaDocumentBuilder.de(reserva)
				.estado(EstadoReserva.FINALIZADA)
				.recargoRetraso(recargo)
				.fechaCheckout(request.fechaDevolucion())
				.fechaLiberacionPago(liberacion)
				.observaciones(request.observaciones())
				.fechaActualizacion(Instant.now())
				.build();
		reservaDAO.actualizar(actualizada);
		notificacionClient.enviar(reserva.idArrendatario(), "RESERVA_FINALIZADA",
				"Reserva finalizada", "El check-out fue registrado. El deposito se libera en 48 horas.", id);
		auditoriaService.registrar(actorId, "FINALIZAR_RESERVA", "RESERVA", id,
				Map.of("estado", reserva.estado().name()),
				Map.of("estado", actualizada.estado().name(), "recargo", recargo), ip);
		return ReservaResponse.desde(actualizada);
	}

	public ReservaResponse abrirDisputa(String id, String motivo, String actorId) {
		ReservaDocument reserva = obtener(id);
		if (reserva.estado() != EstadoReserva.FINALIZADA) {
			throw new EstadoReservaInvalidoException("Solo se puede abrir disputa sobre una reserva finalizada.");
		}
		ReservaDocument actualizada = ReservaDocumentBuilder.de(reserva)
				.estado(EstadoReserva.EN_DISPUTA)
				.disputaAbierta(true)
				.observaciones(motivo)
				.fechaActualizacion(Instant.now())
				.build();
		reservaDAO.actualizar(actualizada);
		auditoriaService.registrar(actorId, "ABRIR_DISPUTA", "RESERVA", id,
				Map.of("estado", reserva.estado().name()), Map.of("motivo", motivo), null);
		return ReservaResponse.desde(actualizada);
	}

	public ReservaResponse resolverDisputa(String id, ResolucionDisputaRequest request,
			String adminId, String ip) {
		ReservaDocument reserva = obtener(id);
		if (reserva.estado() != EstadoReserva.EN_DISPUTA) {
			throw new EstadoReservaInvalidoException("La reserva no tiene una disputa abierta.");
		}
		BigDecimal devuelto = request.montoDevueltoDeposito().min(reserva.deposito().montoRetenido());
		DepositoInfo deposito = reserva.deposito().resuelto(devuelto);
		ReservaDocumentBuilder builder = ReservaDocumentBuilder.de(reserva)
				.estado(EstadoReserva.FINALIZADA)
				.deposito(deposito)
				.disputaAbierta(false)
				.observaciones(request.observaciones())
				.fechaActualizacion(Instant.now());
		if (Boolean.TRUE.equals(request.liberarPago())) {
			builder = builder.fechaLiberacionPago(Instant.now());
			ReservaDocument actualizada = liquidarDocumento(builder.build());
			reservaDAO.actualizar(actualizada);
			notificacionClient.enviar(reserva.idPropietario(), "DISPUTA_RESUELTA", "Disputa resuelta",
					"Se libero el pago de la reserva.", id);
			auditoriaService.registrar(adminId, "RESOLVER_DISPUTA", "RESERVA", id,
					Map.of("estado", reserva.estado().name()),
					Map.of("devuelto", devuelto, "liberar_pago", true), ip);
			return ReservaResponse.desde(actualizada);
		}
		ReservaDocument actualizada = builder.fechaLiberacionPago(null).build();
		reservaDAO.actualizar(actualizada);
		auditoriaService.registrar(adminId, "RESOLVER_DISPUTA", "RESERVA", id,
				Map.of("estado", reserva.estado().name()),
				Map.of("devuelto", devuelto, "liberar_pago", false), ip);
		return ReservaResponse.desde(actualizada);
	}

	public ReservaResponse liberarPago(String id, String adminId, String ip) {
		ReservaDocument reserva = obtener(id);
		if (reserva.liquidacion() != null) {
			return ReservaResponse.desde(reserva);
		}
		ReservaDocument actualizada = liquidarDocumento(reserva);
		reservaDAO.actualizar(actualizada);
		auditoriaService.registrar(adminId, "LIBERAR_PAGO", "RESERVA", id, null,
				Map.of("neto", actualizada.liquidacion().montoNeto()), ip);
		return ReservaResponse.desde(actualizada);
	}

	@Scheduled(fixedDelayString = "${volt.reservas.liquidacion-ms:60000}")
	public void liquidarPendientes() {
		for (ReservaDocument reserva : reservaDAO.buscarParaLiquidar(Instant.now())) {
			if (reserva.liquidacion() == null) {
				reservaDAO.actualizar(liquidarDocumento(reserva));
				notificacionClient.enviar(reserva.idPropietario(), "PAGO_LIBERADO",
						"Pago liberado", "El pago de la reserva fue liberado a tu cuenta.", reserva.id());
			}
		}
	}

	@Scheduled(fixedDelayString = "${volt.reservas.noshow-ms:60000}")
	public void vencerNoShows() {
		Instant limite = Instant.now().minus(Duration.ofHours(2));
		for (ReservaDocument reserva : reservaDAO.buscarParaNoShow(limite)) {
			PagoInfo pago = new PagoInfo(reserva.pago().referenciaPasarela(), reserva.pago().montoTotal(),
					reserva.pago().comisionPlataforma(), reserva.deposito().montoRetenido(),
					"REEMBOLSADO", reserva.pago().fechaPago(), reserva.pago().comprobante());
			ReservaDocument actualizada = ReservaDocumentBuilder.de(reserva)
					.estado(EstadoReserva.VENCIDA)
					.pago(pago)
					.deposito(reserva.deposito().liberado())
					.motivoCancelacion("No se presento al check-in")
					.fechaActualizacion(Instant.now())
					.build();
			reservaDAO.actualizar(actualizada);
			notificacionClient.enviar(reserva.idArrendatario(), "RESERVA_VENCIDA",
					"Reserva vencida", "No se registro el check-in y la reserva fue marcada como vencida.", reserva.id());
		}
	}

	public PageResponse<ReservaResponse> listarMias(TokenPrincipal principal, EstadoReserva estado, int pagina, int tamano) {
		List<ReservaDocument> reservas = reservaDAO.listarPorUsuario(principal.userId(), estado);
		int inicio = Math.min(reservas.size(), pagina * tamano);
		int fin = Math.min(reservas.size(), inicio + tamano);
		return PageResponse.de(reservas.subList(inicio, fin).stream().map(ReservaResponse::desde).toList(),
				reservas.size(), pagina, tamano);
	}

	public PageResponse<ReservaResponse> listarAdmin(EstadoReserva estado, Instant desde, Instant hasta,
			int pagina, int tamano) {
		List<ReservaResponse> elementos = reservaDAO.listarAdmin(estado, desde, hasta, pagina, tamano).stream()
				.map(ReservaResponse::desde).toList();
		return PageResponse.de(elementos, reservaDAO.contar(estado, desde, hasta), pagina, tamano);
	}

	public ReservaResponse obtenerParaUsuario(String id, TokenPrincipal principal) {
		ReservaDocument reserva = obtener(id);
		if (!principal.esAdministrador()
				&& !principal.userId().equals(reserva.idArrendatario())
				&& !principal.userId().equals(reserva.idPropietario())) {
			throw new AccesoDenegadoException("No tienes acceso a esta reserva.");
		}
		return ReservaResponse.desde(reserva);
	}

	public ReservaResponse obtenerInterno(String id) {
		return ReservaResponse.desde(obtener(id));
	}

	public List<FranjaResponse> calendario(String patinetaId, Instant desde, Instant hasta) {
		return reservaDAO.listarAdmin(null, desde, hasta, 0, 10000).stream()
				.filter(reserva -> patinetaId.equals(reserva.idPatineta()))
				.map(reserva -> new FranjaResponse(reserva.fechaInicio(), reserva.fechaFin(), reserva.estado().name()))
				.toList();
	}

	public List<String> patinetasOcupadas(Instant inicio, Instant fin) {
		if (inicio == null || fin == null || !fin.isAfter(inicio)) {
			throw new IllegalArgumentException("El rango de fechas es invalido.");
		}
		return reservaDAO.listarAdmin(null, null, null, 0, 10000).stream()
				.filter(reserva -> EstadoReserva.SOLICITADA == reserva.estado()
						|| EstadoReserva.CONFIRMADA == reserva.estado()
						|| EstadoReserva.EN_CURSO == reserva.estado())
				.filter(reserva -> !reserva.fechaInicio().isAfter(fin)
						&& !reserva.fechaFin().isBefore(inicio))
				.map(ReservaDocument::idPatineta)
				.distinct()
				.toList();
	}

	public EstadoCuentaResponse estadoCuenta(String propietarioId) {
		List<ReservaDocument> reservas = reservaDAO.listarPorPropietario(propietarioId);
		BigDecimal ingresos = CERO;
		BigDecimal comisiones = CERO;
		BigDecimal liquidado = CERO;
		BigDecimal pendiente = CERO;
		for (ReservaDocument reserva : reservas) {
			if (reserva.estado() == EstadoReserva.CANCELADA || reserva.estado() == EstadoReserva.VENCIDA) {
				continue;
			}
			BigDecimal bruto = dinero(reserva.totalAlquiler().add(reserva.recargoRetraso()));
			ingresos = dinero(ingresos.add(bruto));
			comisiones = dinero(comisiones.add(reserva.pago().comisionPlataforma()));
			if (reserva.liquidacion() != null) {
				liquidado = dinero(liquidado.add(reserva.liquidacion().montoNeto()));
			} else if (reserva.estado() == EstadoReserva.FINALIZADA
					|| reserva.estado() == EstadoReserva.EN_DISPUTA) {
				BigDecimal neto = bruto.subtract(reserva.pago().comisionPlataforma())
						.subtract(reserva.penalizacionPropietario());
				pendiente = dinero(pendiente.add(neto));
			}
		}
		return new EstadoCuentaResponse(reservas.size(), ingresos, comisiones, liquidado, pendiente,
				reservas.stream().map(ReservaResponse::desde).toList());
	}

	public ReportePagoResponse reporte(Instant desde, Instant hasta) {
		List<ReservaDocument> reservas = reservaDAO.listarAdmin(null, desde, hasta, 0, 10000);
		BigDecimal ingresos = CERO;
		BigDecimal comisiones = CERO;
		BigDecimal liquidado = CERO;
		Map<String, Long> porEstado = new LinkedHashMap<>();
		for (ReservaDocument reserva : reservas) {
			ingresos = dinero(ingresos.add(reserva.totalAlquiler()));
			comisiones = dinero(comisiones.add(reserva.pago().comisionPlataforma()));
			if (reserva.liquidacion() != null) {
				liquidado = dinero(liquidado.add(reserva.liquidacion().montoNeto()));
			}
			porEstado.merge(reserva.estado().name(), 1L, Long::sum);
		}
		return new ReportePagoResponse(desde, hasta, reservas.size(), ingresos, comisiones, liquidado,
				porEstado);
	}

	public List<ReservaResponse> liquidaciones(String propietarioId) {
		return reservaDAO.listarPorPropietario(propietarioId).stream()
				.filter(reserva -> reserva.liquidacion() != null)
				.sorted(Comparator.comparing(ReservaDocument::fechaActualizacion).reversed())
				.map(ReservaResponse::desde)
				.toList();
	}

	public ContraCargoDocument registrarContraCargo(String idReserva, String idArrendatario,
			BigDecimal monto, String motivo, String adminId, String ip) {
		ContraCargoDocument contraCargo = new ContraCargoDocument(UUID.randomUUID().toString(), idReserva,
				idArrendatario, dinero(monto), motivo, "REGISTRADO", Instant.now());
		contraCargoDAO.guardar(contraCargo);
		auditoriaService.registrar(adminId, "REGISTRAR_CONTRACARGO", "RESERVA", idReserva,
				null, Map.of("monto", monto, "motivo", motivo), ip);
		return contraCargo;
	}

	public List<ContraCargoDocument> listarContraCargos() {
		return contraCargoDAO.listar();
	}

	private ReservaDocument liquidarDocumento(ReservaDocument reserva) {
		BigDecimal bruto = dinero(reserva.totalAlquiler().add(reserva.recargoRetraso()));
		BigDecimal comision = reserva.pago().comisionPlataforma() == null
				? dinero(reserva.totalAlquiler().multiply(ConfiguracionComisionSingleton.INSTANCIA.porcentajeComision()))
				: reserva.pago().comisionPlataforma();
		BigDecimal penalizacion = reserva.penalizacionPropietario() == null ? CERO : reserva.penalizacionPropietario();
		BigDecimal neto = dinero(bruto.subtract(comision).subtract(penalizacion));
		LiquidacionInfo liquidacion = new LiquidacionInfo(bruto, comision, penalizacion, neto,
				"PAGADA", Instant.now());
		DepositoInfo deposito = reserva.deposito().estado().startsWith("RETENIDO")
				? reserva.deposito()
				: reserva.deposito().liberado();
		return ReservaDocumentBuilder.de(reserva)
				.liquidacion(liquidacion)
				.deposito(deposito)
				.pago(reserva.pago().liberado())
				.fechaActualizacion(Instant.now())
				.build();
	}

	private ReservaDocument obtener(String id) {
		return reservaDAO.buscarPorId(id)
				.orElseThrow(() -> new ReservaNoEncontradaException(id));
	}

	private static void validarPropietario(ReservaDocument reserva, TokenPrincipal principal) {
		if (!principal.esAdministrador() && !principal.userId().equals(reserva.idPropietario())) {
			throw new AccesoDenegadoException("Solo el propietario puede realizar esta operacion.");
		}
	}

	private static void validarParticipante(ReservaDocument reserva, TokenPrincipal principal) {
		if (!principal.esAdministrador()
				&& !principal.userId().equals(reserva.idArrendatario())
				&& !principal.userId().equals(reserva.idPropietario())) {
			throw new AccesoDenegadoException("No tienes acceso a esta reserva.");
		}
	}

	private static void requerirEstado(ReservaDocument reserva, EstadoReserva esperado, String mensaje) {
		if (reserva.estado() != esperado) {
			throw new EstadoReservaInvalidoException(mensaje);
		}
	}

	private static Cotizacion cotizar(Instant inicio, Instant fin, PatinetaInfo patineta) {
		if (patineta.tarifa() == null) {
			throw new ServicioNoDisponibleException("La patineta no tiene tarifas configuradas.");
		}
		return new ReservaService().cotizar(inicio, fin, patineta.tarifa().valorHora(),
				patineta.tarifa().valorDia(), patineta.tarifa().valorDeposito());
	}

	private static BigDecimal dinero(BigDecimal valor) {
		return valor.setScale(2, RoundingMode.HALF_UP);
	}
}
