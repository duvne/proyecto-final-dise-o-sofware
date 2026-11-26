package com.volt.catalogoflota.strategy;


import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
public class TarifaDiaStrategy implements TarifaStrategy {


    private static final BigDecimal TARIFA_DIA = BigDecimal.valueOf(15.00);


    private static final int MINUTOS_DIA_COMPLETO = 24 * 60; // 1440 minutos

    @Override
    public BigDecimal calcularCosto(int minutosDuracion) {

        if (minutosDuracion >= MINUTOS_DIA_COMPLETO) {
            return TARIFA_DIA;
        } else {
            return TARIFA_DIA;
        }
    }

    public static BigDecimal getTarifaDia() {
        return TARIFA_DIA;
    }
}