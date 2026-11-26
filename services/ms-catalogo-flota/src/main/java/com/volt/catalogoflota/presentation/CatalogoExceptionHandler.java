package com.volt.catalogoflota.presentation;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.volt.catalogoflota.business.AccesoDenegadoException;
import com.volt.catalogoflota.business.PatinetaNoEncontradaException;
import com.volt.catalogoflota.business.ServicioNoDisponibleException;
import com.volt.shared.api.ApiError;

@RestControllerAdvice
public class CatalogoExceptionHandler {
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException exception) {
		Map<String, String> campos = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(error ->
				campos.put(error.getField(), error.getDefaultMessage()));
		String detalle = campos.isEmpty() ? exception.getMessage() : campos.entrySet().stream()
				.map(e -> e.getKey() + ": " + e.getValue())
				.collect(java.util.stream.Collectors.joining("; "));
		return ResponseEntity.badRequest()
				.body(ApiError.de("Solicitud invalida", detalle, campos));
	}

	@ExceptionHandler(PatinetaNoEncontradaException.class)
	public ResponseEntity<ApiError> noEncontrada(PatinetaNoEncontradaException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ApiError.de("Patineta no encontrada", exception.getMessage()));
	}

	@ExceptionHandler(AccesoDenegadoException.class)
	public ResponseEntity<ApiError> accesoDenegado(AccesoDenegadoException exception) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(ApiError.de("Acceso denegado", exception.getMessage()));
	}

	@ExceptionHandler(ServicioNoDisponibleException.class)
	public ResponseEntity<ApiError> servicioNoDisponible(ServicioNoDisponibleException exception) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(ApiError.de("Servicio no disponible", exception.getMessage()));
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
