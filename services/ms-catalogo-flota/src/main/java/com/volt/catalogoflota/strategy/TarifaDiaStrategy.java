package com.volt.catalogoflota.strategy;

/**
 * Estrategia de tarifa por día.
 * Cobra una tarifa plana por uso diario (24 horas).
 */
public class TarifaDiaStrategy implements TarifaStrategy {

    // Tarifa plana por día completo (ej: $15.00 por día)
    private static final BigDecimal TARIFA_DIA = BigDecimal.valueOf(15.00);

    // Tiempo mínimo para considerarse un día completo (en minutos)
    private static final int MINUTOS_DIA_COMPLETO = 24 * 60; // 1440 minutos

    @Override
    public BigDecimal calcularCosto(int minutosDuracion) {
        // Si el uso es menor a un día completo, aún se cobra el día completo
        // (tarifa plana diaria)
        if (minutosDuracion >= MINUTOS_DIA_COMPLETO) {
            return TARIFA_DIA;
        } else {
            // Para uso parcial, también se aplica la tarifa diaria
            // o se podría calcular proporcional, aquí aplicamos el día completo
            return TARIFA_DIA;
        }
    }

    /**
     * Obtiene la tarifa base por día.
     */
    public static BigDecimal getTarifaDia() {
        return TARIFA_DIA;
    }
}