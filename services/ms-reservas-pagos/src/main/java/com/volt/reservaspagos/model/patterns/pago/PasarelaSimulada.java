package com.volt.reservaspagos.model.patterns.pago;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PasarelaSimulada implements PasarelaPago {
	@Override
	public RetencionPago retener(String referencia, BigDecimal monto) {
		return new RetencionPago("SIM-" + UUID.randomUUID(), "APROBADA", Instant.now());
	}
}
