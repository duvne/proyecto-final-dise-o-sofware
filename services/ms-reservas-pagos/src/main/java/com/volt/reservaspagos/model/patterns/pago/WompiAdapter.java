package com.volt.reservaspagos.model.patterns.pago;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class WompiAdapter implements PasarelaPago {
	@Override
	public RetencionPago retener(String referencia, BigDecimal monto) {
		return new RetencionPago("WOMPI-" + UUID.randomUUID(), "APROBADA", Instant.now());
	}
}
