package com.volt.reservaspagos.model.patterns.pago;

import java.math.BigDecimal;

public interface PasarelaPago {
	RetencionPago retener(String referencia, BigDecimal monto);
}
