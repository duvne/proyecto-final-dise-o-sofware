package com.volt.usuariosauth.presentation;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.volt.shared.api.ApiError;
import com.volt.usuariosauth.domain.AccesoDenegadoException;
import com.volt.usuariosauth.domain.CredencialesInvalidasException;
import com.volt.usuariosauth.domain.CuentaBloqueadaException;
import com.volt.usuariosauth.domain.NoEncontradoException;
import com.volt.usuariosauth.domain.RecuperacionInvalidaException;
import com.volt.usuariosauth.domain.UsuarioYaExisteException;

@RestControllerAdvice
public class AuthExceptionHandler {
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException exception) {
		Map<String, String> campos = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors()
				.forEach(error -> campos.put(error.getField(), error.getDefaultMessage()));
		return ResponseEntity.badRequest()
				.body(ApiError.de("Solicitud invalida", "Revisa los campos enviados.", campos));
	}

	@ExceptionHandler(UsuarioYaExisteException.class)
	public ResponseEntity<ApiError> usuarioExistente(UsuarioYaExisteException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ApiError.de("Usuario duplicado", exception.getMessage()));
	}

	@ExceptionHandler(CredencialesInvalidasException.class)
	public ResponseEntity<ApiError> credenciales(CredencialesInvalidasException exception) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiError.de("Credenciales invalidas", exception.getMessage()));
	}

	@ExceptionHandler(CuentaBloqueadaException.class)
	public ResponseEntity<ApiError> cuentaBloqueada(CuentaBloqueadaException exception) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(ApiError.de("Cuenta bloqueada", exception.getMessage()));
	}

	@ExceptionHandler(AccesoDenegadoException.class)
	public ResponseEntity<ApiError> accesoDenegado(AccesoDenegadoException exception) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(ApiError.de("Acceso denegado", exception.getMessage()));
	}

	@ExceptionHandler(NoEncontradoException.class)
	public ResponseEntity<ApiError> noEncontrado(NoEncontradoException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ApiError.de("No encontrado", exception.getMessage()));
	}

	@ExceptionHandler(RecuperacionInvalidaException.class)
	public ResponseEntity<ApiError> recuperacion(RecuperacionInvalidaException exception) {
		return ResponseEntity.badRequest()
				.body(ApiError.de("Recuperacion invalida", exception.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> solicitudInvalida(IllegalArgumentException exception) {
		return ResponseEntity.badRequest()
				.body(ApiError.de("Solicitud invalida", exception.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> errorInterno(Exception exception) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiError.de("Error interno", exception.getMessage()));
	}
}
