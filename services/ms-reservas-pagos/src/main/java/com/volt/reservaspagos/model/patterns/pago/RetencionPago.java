package com.volt.reservaspagos.model.patterns.pago;

import java.time.Instant;

public record RetencionPago(String referencia, String estado, Instant fecha) {
}
