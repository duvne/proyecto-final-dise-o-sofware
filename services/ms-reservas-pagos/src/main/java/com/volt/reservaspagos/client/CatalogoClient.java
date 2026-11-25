package com.volt.reservaspagos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.volt.reservaspagos.model.domain.ServicioNoDisponibleException;

@Component
public class CatalogoClient {
	private final RestClient restClient;

	public CatalogoClient(@Value("${volt.catalogo-url:http://localhost:8083}") String baseUrl) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
	}

	public PatinetaInfo obtener(String patinetaId) {
		try {
			PatinetaInfo response = restClient.get()
					.uri("/api/catalogo/patinetas/{id}", patinetaId)
					.retrieve()
					.body(PatinetaInfo.class);
			if (response == null) {
				throw new ServicioNoDisponibleException("La patineta no existe o no esta publicada.");
			}
			return response;
		} catch (RestClientException exception) {
			throw new ServicioNoDisponibleException("No fue posible consultar la patineta en el catalogo.");
		}
	}
}
