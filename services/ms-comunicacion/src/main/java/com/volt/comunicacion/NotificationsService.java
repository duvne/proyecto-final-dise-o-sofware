package com.volt.comunicacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class NotificationsService {
	private static final Logger LOG = LoggerFactory.getLogger(NotificationsService.class);
	private final ConcurrentLinkedQueue<NotificationDTO> cola = new ConcurrentLinkedQueue<>();
	private final String internalKey;

	public NotificationsService(@Value("${volt.internal-key:volt-internal-dev-key}") String internalKey) {
		this.internalKey = internalKey;
	}

	public NotificationDTO send(String usuarioId, String tipo, String titulo, String mensaje, String reservaId) {
		NotificationDTO notificacion = NotificationDTO.de(usuarioId, tipo, titulo, mensaje, reservaId);
		cola.add(notificacion);
		return notificacion;
	}

	@Scheduled(fixedDelayString = "${volt.comunicacion.dispatch-ms:500}")
	public void dispatch() {
		while (!cola.isEmpty()) {
			NotificationDTO notificacion = cola.poll();
			if (notificacion != null) {
				LOG.info("Notificacion enviada id={} usuario={} tipo={}", notificacion.id(),
						notificacion.usuarioId(), notificacion.tipo());
			}
		}
	}

	public String getInternalKey() {
		return internalKey;
	}

	public List<NotificationDTO> list() {
		return cola.stream().toList();
	}
}
