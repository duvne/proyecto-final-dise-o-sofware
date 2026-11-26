package com.volt.reservaspagos.service;

import com.volt.reservaspagos.model.domain.LiquidacionInfo;
import com.volt.reservaspagos.model.domain.ReservaDocument;
import com.volt.reservaspagos.persistence.IPagoDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;

@Service
public class LiquidacionService {

    @Autowired
    private IPagoDAO pagoDAO;

    /**
     * Calcula y crea una liquidación para una reserva completada
     */
    public LiquidacionInfo calcularLiquidacion(ReservaDocument reserva, BigDecimal montoTotal) {
        BigDecimal comision = montoTotal.multiply(BigDecimal.valueOf(0.15)); // 15% comisión por defecto
        BigDecimal montoNeto = montoTotal.subtract(comision);
        
        LiquidacionInfo liquidacion = LiquidacionInfo.builder()
                .montoBruto(montoTotal)
                .comisionDescontada(comision)
                .penalizacion(BigDecimal.ZERO)
                .montoNeto(montoNeto)
                .estado("PROCESANDO")
                .fechaDesembolso(Instant.now())
                .build();
        
        // Guardar la liquidación asociada al pago
        PagoInfo pago = PagoInfo.pendiente(montoTotal, comision);
        pagoDAO.save(pago);
        
        return liquidacion;
    }

    /**
     * Obtiene el estado actual de un pago
     */
    public String obtenerEstadoPago(String pagoId) {
        return pagoDAO.findById(pagoId)
                .map(PagoInfo::getEstado)
                .orElse("NO_ENCONTRADO");
    }
}