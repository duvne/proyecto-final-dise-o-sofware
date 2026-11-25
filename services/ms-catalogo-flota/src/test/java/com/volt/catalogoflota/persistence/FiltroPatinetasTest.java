package com.volt.catalogoflota.persistence;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class FiltroPatinetasTest {
	@Test
	void rechazaPaginasFueraDelLimite() {
		assertThrows(IllegalArgumentException.class, () -> new FiltroPatinetas(
				null, null, null, null, -1, 20));
		assertThrows(IllegalArgumentException.class, () -> new FiltroPatinetas(
				null, null, null, null, 0, 51));
	}

	@Test
	void rechazaCriteriosNumericosNegativos() {
		assertThrows(IllegalArgumentException.class, () -> new FiltroPatinetas(
				null, null, new BigDecimal("-0.01"), null, 0, 20));
		assertThrows(IllegalArgumentException.class, () -> new FiltroPatinetas(
				null, null, null, -1.0, 0, 20));
	}
}
