package com.volt.catalogoflota.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.bson.Document;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.volt.catalogoflota.document.PatinetaDocument;
import com.volt.catalogoflota.document.PuntoEntregaDocument;
import com.volt.catalogoflota.document.TarifaDocument;
import com.volt.catalogoflota.document.ZonaDocument;

class PatinetaMongoDAOImplTest {
	@Test
	void guardaEsquemaCompatibleConColeccionYPreciosDecimalesBson() {
		MongoClient client = mock(MongoClient.class);
		MongoDatabase database = mock(MongoDatabase.class);
		@SuppressWarnings("unchecked")
		MongoCollection<Document> collection = mock(MongoCollection.class);
		when(client.getDatabase("volt_catalogo")).thenReturn(database);
		when(database.getCollection("patinetas")).thenReturn(collection);

		PatinetaMongoDAOImpl dao = new PatinetaMongoDAOImpl(client, "volt_catalogo");
		dao.guardar(new PatinetaDocument(
				null,
				"propietario-1",
				"Urbana",
				"VOLT",
				"City",
				35.0,
				25.0,
				"BUENO",
				"BORRADOR",
				false,
				new TarifaDocument(
						new BigDecimal("5000.25"),
						new BigDecimal("30000.50"),
						new BigDecimal("50000.75")),
				new PuntoEntregaDocument(
						1L,
						"Calle 1 # 2-3",
						10.98,
						-74.80,
						true,
						new ZonaDocument(1L, "Norte", "Barranquilla"))));

		ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
		verify(collection).insertOne(documentCaptor.capture());
		Document document = documentCaptor.getValue();
		Document tarifa = document.get("tarifa", Document.class);
		Document puntoEntrega = document.get("punto_entrega", Document.class);
		Document zona = puntoEntrega.get("zona", Document.class);

		assertNotNull(document.getString("_id"));
		assertEquals("propietario-1", document.getString("id_propietario"));
		assertEquals(new Decimal128(new BigDecimal("5000.25")), tarifa.get("valor_hora"));
		assertEquals(1L, puntoEntrega.get("id_punto"));
		assertEquals("Barranquilla", zona.getString("ciudad"));
	}
}
