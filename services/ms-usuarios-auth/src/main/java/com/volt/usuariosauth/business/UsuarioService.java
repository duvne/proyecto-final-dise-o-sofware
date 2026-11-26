package com.volt.usuariosauth.business;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.volt.shared.api.PageResponse;
import com.volt.shared.audit.AuditoriaService;
import com.volt.shared.security.IssuedToken;
import com.volt.shared.security.JwtTokenService;
import com.volt.shared.security.Roles;
import com.volt.usuariosauth.domain.AccesoDenegadoException;
import com.volt.usuariosauth.domain.Consentimiento;
import com.volt.usuariosauth.domain.CredencialesInvalidasException;
import com.volt.usuariosauth.domain.CuentaBloqueadaException;
import com.volt.usuariosauth.domain.EstadoCuenta;
import com.volt.usuariosauth.domain.NoEncontradoException;
import com.volt.usuariosauth.domain.PerfilArrendatario;
import com.volt.usuariosauth.domain.PerfilPropietario;
import com.volt.usuariosauth.domain.RegistroAuditoria;
import com.volt.usuariosauth.domain.Usuario;
import com.volt.usuariosauth.domain.UsuarioYaExisteException;
import com.volt.usuariosauth.persistence.IAuditoriaDAO;
import com.volt.usuariosauth.persistence.IUsuarioDAO;
import com.volt.usuariosauth.view.ActualizarPerfilRequest;
import com.volt.usuariosauth.view.AuthResponse;
import com.volt.usuariosauth.view.ConsentimientoRequest;
import com.volt.usuariosauth.view.LoginRequest;
import com.volt.usuariosauth.view.PerfilPropietarioRequest;
import com.volt.usuariosauth.view.RegistroUsuarioRequest;
import com.volt.usuariosauth.view.UsuarioInternoResponse;
import com.volt.usuariosauth.view.UsuarioResponse;

@Service
public class UsuarioService {
	private final IUsuarioDAO usuarioDAO;
	private final IAuditoriaDAO auditoriaDAO;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenService jwtTokenService;
	private final AuditoriaService auditoriaService;

	public UsuarioService(
			IUsuarioDAO usuarioDAO,
			IAuditoriaDAO auditoriaDAO,
			PasswordEncoder passwordEncoder,
			JwtTokenService jwtTokenService,
			AuditoriaService auditoriaService) {
		this.usuarioDAO = usuarioDAO;
		this.auditoriaDAO = auditoriaDAO;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenService = jwtTokenService;
		this.auditoriaService = auditoriaService;
	}

	public AuthResponse registrar(RegistroUsuarioRequest request, String ip) {
		validarMayoriaDeEdad(request.fechaNacimiento());
		if (!request.aceptaTratamientoDatos()) {
			throw new IllegalArgumentException("Debe aceptar la politica de tratamiento de datos para registrarse.");
		}
		String correo = request.correo().trim().toLowerCase();
		String documento = request.documento().trim();
		if (usuarioDAO.existeCorreo(correo)) {
			throw new UsuarioYaExisteException();
		}
		if (usuarioDAO.existeDocumento(documento)) {
			throw new UsuarioYaExisteException();
		}

		List<String> roles = new ArrayList<>();
		roles.add(Roles.ARRENDATARIO);
		if ("PROPIETARIO".equalsIgnoreCase(request.rol())) {
			roles.add(Roles.PROPIETARIO);
		}

		Usuario usuario = new Usuario(
				UUID.randomUUID().toString(),
				request.nombre().trim(),
				documento,
				correo,
				passwordEncoder.encode(request.contrasena()),
				request.telefono().trim(),
				request.fechaNacimiento(),
				false,
				EstadoCuenta.PENDIENTE_VERIFICACION,
				java.time.Instant.now(),
				roles,
				roles.contains(Roles.PROPIETARIO) ? new PerfilPropietario(null, null, 0, 0.0, 0) : null,
				new PerfilArrendatario(null, 0, 0.0, 0),
				List.of(Consentimiento.de("TRATAMIENTO_DATOS", true, "1.0")),
				null);

		usuarioDAO.guardar(usuario);
		auditar(usuario.id(), "REGISTRO_USUARIO", "USUARIO", usuario.id(), null,
				Map.of("correo", correo, "roles", roles), ip);
		return authResponse(usuario);
	}

	public AuthResponse login(LoginRequest request, String ip) {
		Usuario usuario = usuarioDAO.buscarPorCorreo(request.correo().trim().toLowerCase())
				.orElseThrow(CredencialesInvalidasException::new);
		if (usuario.estadoCuenta() == EstadoCuenta.SUSPENDIDA) {
			throw new CuentaBloqueadaException("La cuenta se encuentra suspendida. Contacta al administrador.");
		}
		if (usuario.estadoCuenta() == EstadoCuenta.ELIMINADA) {
			throw new CredencialesInvalidasException();
		}
		if (!passwordEncoder.matches(request.contrasena(), usuario.passwordHash())) {
			throw new CredencialesInvalidasException();
		}
		auditar(usuario.id(), "INICIO_SESION", "USUARIO", usuario.id(), null, null, ip);
		return authResponse(usuario);
	}

	public UsuarioResponse perfil(String userId) {
		return UsuarioResponse.desde(obtenerUsuario(userId));
	}

	public UsuarioResponse actualizarPerfil(String userId, ActualizarPerfilRequest request, String ip) {
		Usuario usuario = obtenerUsuario(userId);
		Usuario actualizado = usuario.conDatos(request.nombre().trim(), request.telefono().trim());
		usuarioDAO.actualizar(actualizado);
		auditar(userId, "ACTUALIZAR_PERFIL", "USUARIO", userId,
				Map.of("nombre", usuario.nombre(), "telefono", String.valueOf(usuario.telefono())),
				Map.of("nombre", actualizado.nombre(), "telefono", actualizado.telefono()), ip);
		return UsuarioResponse.desde(actualizado);
	}

	public UsuarioResponse activarPropietario(String userId, PerfilPropietarioRequest request, String ip) {
		Usuario usuario = obtenerUsuario(userId);
		if (!usuario.verificado()) {
			throw new AccesoDenegadoException("La cuenta debe estar verificada para habilitar el perfil de propietario.");
		}
		List<String> roles = new ArrayList<>(usuario.roles());
		if (!roles.contains(Roles.PROPIETARIO)) {
			roles.add(Roles.PROPIETARIO);
		}
		PerfilPropietario perfil = usuario.perfilPropietario() == null
				? new PerfilPropietario(request.cuentaDesembolso(), request.bancoDestino(), 0, 0.0, 0)
				: usuario.perfilPropietario().conCuenta(request.cuentaDesembolso(), request.bancoDestino());
		Usuario actualizado = usuario.conRoles(roles).conPerfilPropietario(perfil);
		usuarioDAO.actualizar(actualizado);
		auditar(userId, "ACTIVAR_PERFIL_PROPIETARIO", "USUARIO", userId,
				Map.of("roles", usuario.roles()),
				Map.of("roles", actualizado.roles(), "banco", request.bancoDestino()), ip);
		return UsuarioResponse.desde(actualizado);
	}

	public UsuarioResponse registrarConsentimiento(String userId, ConsentimientoRequest request, String ip) {
		Usuario usuario = obtenerUsuario(userId);
		Usuario actualizado = usuario.conConsentimiento(
				Consentimiento.de(request.tipo(), request.aceptado(), request.version()));
		usuarioDAO.actualizar(actualizado);
		auditar(userId, "REGISTRAR_CONSENTIMIENTO", "USUARIO", userId, null,
				Map.of("tipo", request.tipo(), "aceptado", request.aceptado(), "version", request.version()), ip);
		return UsuarioResponse.desde(actualizado);
	}

	public void eliminarCuenta(String userId, String ip) {
		Usuario usuario = obtenerUsuario(userId);
		Usuario anonimizado = usuario.anonimizado();
		usuarioDAO.actualizar(anonimizado);
		auditar(userId, "ELIMINAR_CUENTA", "USUARIO", userId,
				Map.of("correo", usuario.correo()), Map.of("estado", EstadoCuenta.ELIMINADA.name()), ip);
	}

	public UsuarioResponse verificar(String adminId, String userId, boolean aprobado, String motivo, String ip) {
		Usuario usuario = obtenerUsuario(userId);
		EstadoCuenta nuevoEstado = aprobado ? EstadoCuenta.ACTIVA : EstadoCuenta.PENDIENTE_VERIFICACION;
		Usuario actualizado = usuario.conVerificacion(aprobado, nuevoEstado, aprobado ? null : motivo);
		usuarioDAO.actualizar(actualizado);
		auditar(adminId, aprobado ? "VERIFICAR_USUARIO" : "RECHAZAR_VERIFICACION", "USUARIO", userId,
				Map.of("verificado", usuario.verificado(), "estado", usuario.estadoCuenta().name()),
				Map.of("verificado", actualizado.verificado(), "estado", actualizado.estadoCuenta().name(), "motivo", String.valueOf(motivo)), ip);
		return UsuarioResponse.desde(actualizado);
	}

	public UsuarioResponse suspender(String adminId, String userId, String motivo, String ip) {
		Usuario usuario = obtenerUsuario(userId);
		Usuario actualizado = usuario.conEstado(EstadoCuenta.SUSPENDIDA, motivo);
		usuarioDAO.actualizar(actualizado);
		auditar(adminId, "SUSPENDER_CUENTA", "USUARIO", userId,
				Map.of("estado", usuario.estadoCuenta().name()),
				Map.of("estado", EstadoCuenta.SUSPENDIDA.name(), "motivo", motivo), ip);
		return UsuarioResponse.desde(actualizado);
	}

	public UsuarioResponse reactivar(String adminId, String userId, String ip) {
		Usuario usuario = obtenerUsuario(userId);
		EstadoCuenta estado = usuario.verificado() ? EstadoCuenta.ACTIVA : EstadoCuenta.PENDIENTE_VERIFICACION;
		Usuario actualizado = usuario.conEstado(estado, null);
		usuarioDAO.actualizar(actualizado);
		auditar(adminId, "REACTIVAR_CUENTA", "USUARIO", userId,
				Map.of("estado", usuario.estadoCuenta().name()),
				Map.of("estado", actualizado.estadoCuenta().name()), ip);
		return UsuarioResponse.desde(actualizado);
	}

	public PageResponse<UsuarioResponse> listar(int pagina, int tamano, EstadoCuenta estado) {
		validarPagina(pagina, tamano);
		List<UsuarioResponse> elementos = usuarioDAO.listar(pagina, tamano, estado).stream()
				.map(UsuarioResponse::desde)
				.toList();
		return PageResponse.de(elementos, usuarioDAO.contar(estado), pagina, tamano);
	}

	public UsuarioResponse obtener(String userId) {
		return UsuarioResponse.desde(obtenerUsuario(userId));
	}

	public PageResponse<RegistroAuditoria> auditoria(int pagina, int tamano) {
		validarPagina(pagina, tamano);
		return PageResponse.de(auditoriaDAO.listar(pagina, tamano), auditoriaDAO.contar(), pagina, tamano);
	}

	public UsuarioInternoResponse obtenerInterno(String userId) {
		Usuario usuario = obtenerUsuario(userId);
		return new UsuarioInternoResponse(
				usuario.id(),
				usuario.correo(),
				usuario.verificado(),
				usuario.estadoCuenta().name(),
				usuario.roles());
	}

	public void actualizarReputacion(String userId, String rol, double promedio, int total) {
		obtenerUsuario(userId);
		usuarioDAO.actualizarReputacion(userId, rol, promedio, total);
	}

	public void registrarAuditoria(String actorId, String accion, String entidad, String entidadId,
			Object antes, Object despues, String ip) {
		auditar(actorId, accion, entidad, entidadId, antes, despues, ip);
	}

	private Usuario obtenerUsuario(String userId) {
		// Primero intentamos buscar por ID (UUID MongoDB)
		return usuarioDAO.buscarPorId(userId)
				.orElseGet(() -> usuarioDAO.buscarPorCorreo(userId.toLowerCase())
						.orElseThrow(() -> new NoEncontradoException("No existe el usuario " + userId + ".")));
	}

	private AuthResponse authResponse(Usuario usuario) {
		IssuedToken token = jwtTokenService.emitir(
				usuario.id(), usuario.correo(), usuario.roles(), usuario.verificado());
		return AuthResponse.desde(token, usuario);
	}

	private void auditar(String actorId, String accion, String entidad, String entidadId,
			Object antes, Object despues, String ip) {
		auditoriaService.registrar(actorId, accion, entidad, entidadId, antes, despues, ip);
	}

	private static void validarMayoriaDeEdad(LocalDate fechaNacimiento) {
		if (fechaNacimiento == null) {
			throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
		}
		if (Period.between(fechaNacimiento, LocalDate.now()).getYears() < 18) {
			throw new IllegalArgumentException("El usuario debe ser mayor de edad (18 anos o mas).");
		}
	}

	private static void validarPagina(int pagina, int tamano) {
		if (pagina < 0 || pagina > 10_000) {
			throw new IllegalArgumentException("La pagina debe estar entre 0 y 10000.");
		}
		if (tamano < 1 || tamano > 100) {
			throw new IllegalArgumentException("El tamano de pagina debe estar entre 1 y 100.");
		}
	}
}
