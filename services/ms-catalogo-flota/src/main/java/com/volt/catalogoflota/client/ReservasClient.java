package com.volt.catalogoflota.client;

import java.time.Instant;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.volt.catalogoflota.business.ServicioNoDisponibleException;
import com.volt.shared.security.InternalKey;

@Component
public class ReservasClient {
	private final RestClient restClient;
	private final String internalKey;

	public ReservasClient(
			@Value("${volt.reservas-url:http://localhost:8084}") String baseUrl,
			@Value("${volt.internal-key:volt-internal-dev-key}") String internalKey) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
		this.internalKey = internalKey;
	}

	public Set<String> patinetasOcupadas(Instant inicio, Instant fin) {
		try {
			OcupadasResponse response = restClient.get()
					.uri(uriBuilder -> uriBuilder.path("/api/reservas/disponibilidad/ocupadas")
							.queryParam("inicio", inicio)
							.queryParam("fin", fin)
							.build())
					.retrieve()
					.body(OcupadasResponse.class);
			return response == null ? Set.of() : Set.copyOf(response.patinetasOcupadas());
		} catch (RestClientException exception) {
			throw new ServicioNoDisponibleException("No fue posible consultar la disponibilidad de reservas.");
		}
	}

	public ReservaInfo obtenerReserva(String reservaId) {
		try {
			ReservaInfo response = restClient.get()
					.uri("/api/reservas/interno/{id}", reservaId)
					.header(InternalKey.HEADER, internalKey)
					.retrieve()
					.body(ReservaInfo.class);
			if (response == null) {
				throw new ServicioNoDisponibleException("La reserva no existe.");
			}
			return response;
		} catch (RestClientException exception) {
			throw new ServicioNoDisponibleException("No fue posible consultar la reserva.");
		}
	}
}
