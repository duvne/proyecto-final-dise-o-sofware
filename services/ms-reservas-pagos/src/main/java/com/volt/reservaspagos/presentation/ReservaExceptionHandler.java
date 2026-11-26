package com.volt.reservaspagos.presentation;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.MongoTimeoutException;
import com.volt.reservaspagos.model.domain.AccesoDenegadoException;
import com.volt.reservaspagos.model.domain.CuponInvalidoException;
import com.volt.reservaspagos.model.domain.EstadoReservaInvalidoException;
import com.volt.reservaspagos.model.domain.FranjaOcupadaException;
import com.volt.reservaspagos.model.domain.ReservaNoEncontradaException;
import com.volt.reservaspagos.model.domain.ServicioNoDisponibleException;
import com.volt.shared.api.ApiError;

@RestControllerAdvice
public class ReservaExceptionHandler {
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException exception) {
		Map<String, String> campos = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors()
				.forEach(error -> campos.put(error.getField(), error.getDefaultMessage()));
		return ResponseEntity.badRequest()
				.body(ApiError.de("Solicitud invalida", "Revisa los campos enviados.", campos));
	}

	@ExceptionHandler(ReservaNoEncontradaException.class)
	public ResponseEntity<ApiError> noEncontrada(ReservaNoEncontradaException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ApiError.de("Reserva no encontrada", exception.getMessage()));
	}

	@ExceptionHandler(AccesoDenegadoException.class)
	public ResponseEntity<ApiError> accesoDenegado(AccesoDenegadoException exception) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(ApiError.de("Acceso denegado", exception.getMessage()));
	}

	@ExceptionHandler(FranjaOcupadaException.class)
	public ResponseEntity<ApiError> franjaOcupada(FranjaOcupadaException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ApiError.de("Franja ocupada", exception.getMessage()));
	}

	@ExceptionHandler({ EstadoReservaInvalidoException.class, CuponInvalidoException.class,
			InternoReservaController.ClaveInternaInvalidaException.class })
	public ResponseEntity<ApiError> invalido(RuntimeException exception) {
		return ResponseEntity.badRequest().body(ApiError.de("Solicitud invalida", exception.getMessage()));
	}

	@ExceptionHandler({ MongoExecutionTimeoutException.class, MongoTimeoutException.class,
			ServicioNoDisponibleException.class })
	public ResponseEntity<ApiError> servicioNoDisponible(RuntimeException exception) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(ApiError.de("Servicio no disponible", exception.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> errorInterno(Exception exception) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiError.de("Error interno", exception.getMessage()));
	}
}
