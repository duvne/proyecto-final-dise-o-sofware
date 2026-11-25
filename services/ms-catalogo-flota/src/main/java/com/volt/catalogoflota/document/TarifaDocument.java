package com.volt.catalogoflota.document;

import java.math.BigDecimal;

public record TarifaDocument(
		BigDecimal valorHora,
		BigDecimal valorDia,
		BigDecimal valorDeposito) {
}
