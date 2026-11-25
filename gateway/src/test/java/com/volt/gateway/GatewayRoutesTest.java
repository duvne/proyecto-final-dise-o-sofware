package com.volt.gateway;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutesTest {
	@Autowired
	private RouteLocator routeLocator;

	@Test
	void routesRequestsToAllFiveMicroservices() {
		Set<String> routeIds = routeLocator.getRoutes()
				.collectList()
				.block(Duration.ofSeconds(2))
				.stream()
				.map(route -> route.getId())
				.collect(Collectors.toSet());

		assertTrue(routeIds.containsAll(Set.of(
				"usuarios-auth",
				"catalogo-flota",
				"reservas-pagos",
				"operaciones-incidencias",
				"comunicacion")));
	}
}
