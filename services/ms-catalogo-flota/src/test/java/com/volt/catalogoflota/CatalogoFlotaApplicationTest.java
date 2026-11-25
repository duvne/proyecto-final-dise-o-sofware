package com.volt.catalogoflota;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.volt.catalogoflota.persistence.IPatinetaDAO;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = {
				"spring.mongodb.uri=mongodb://localhost:27017/",
				"spring.mongodb.database=volt_catalogo"
		})
class CatalogoFlotaApplicationTest {
	@Autowired
	private IPatinetaDAO patinetaDAO;

	@Test
	void iniciaLaAplicacionConLaPersistenciaMongoConfigurada() {
		assertNotNull(patinetaDAO);
	}

	@TestConfiguration
	static class MongoClientTestConfiguration {
		@Bean
		MongoClient mongoClient() {
			MongoClient client = mock(MongoClient.class);
			MongoDatabase database = mock(MongoDatabase.class);
			@SuppressWarnings("unchecked")
			MongoCollection<Document> collection = mock(MongoCollection.class);
			when(client.getDatabase("volt_catalogo")).thenReturn(database);
			when(database.getCollection("patinetas")).thenReturn(collection);
			return client;
		}
	}
}
