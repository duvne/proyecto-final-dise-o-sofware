package com.volt.comunicacion.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.volt.comunicacion.NotificationDTO;
import com.volt.comunicacion.NotificationsService;
import com.volt.shared.security.InternalKey;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/comunicacion")
public class NotificationsController {
	private final NotificationsService notificationsService;

	public NotificationsController(NotificationsService notificationsService) {
		this.notificationsService = notificationsService;
	}

	@PostMapping("/interno/notificaciones")
	public ResponseEntity<NotificationDTO> enviarInterno(@Valid @RequestBody Request request,
			HttpServletRequest httpRequest, HttpServletResponse response) {
		if (!InternalKey.valida(httpRequest, notificationsService.getInternalKey())) {
			return ResponseEntity.status(401).build();
		}
		return ResponseEntity.status(202)
				.body(notificationsService.send(request.usuarioId(), request.tipo(), request.titulo(),
						request.mensaje(), request.reservaId()));
	}

	@GetMapping("/cola")
	public List<NotificationDTO> cola() {
		return notificationsService.list();
	}

	public record Request(String usuarioId, String tipo, String titulo, String mensaje, String reservaId) {
	}
}
