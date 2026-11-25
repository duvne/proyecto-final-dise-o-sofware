package com.volt.reservaspagos.model.patterns.pago;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasarelaPagoFactory {
	private final String proveedor;

	public PasarelaPagoFactory(@Value("${volt.pagos.proveedor:SIMULADA}") String proveedor) {
		this.proveedor = proveedor;
	}

	public PasarelaPago crear() {
		return switch (proveedor.toUpperCase()) {
			case "WOMPI" -> new WompiAdapter();
			case "STRIPE" -> new StripeAdapter();
			default -> new PasarelaSimulada();
		};
	}
}
