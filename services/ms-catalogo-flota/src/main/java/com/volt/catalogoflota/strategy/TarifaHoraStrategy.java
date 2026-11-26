package com.volt.catalogoflota.strategy;

/**
 * Estrategia de tarifa por hora.
 * Cobra una tarifa fija por cada hora completa o fracción de hora.
 */
public class TarifaHoraStrategy implements TarifaStrategy {

    // Tarifa base por hora (ej: $2.50 por hora)
    private static final BigDecimal TARIFA_HORA = BigDecimal.valueOf(2.50);

    // Tarifa mínima de 30 minutos si la fracción es menor a media hora
    private static final int MINUTOS_MINIMOS = 30;

    @Override
    public BigDecimal calcularCosto(int minutosDuracion) {
        // Calculamos el número de horas (redondeando hacia arriba para fracciones)
        int horasCompletas = (minutosDuracion + 59) / 60; // redondeo hacia arriba

        // Si la fracción es menor a 30 minutos, cobramos media hora
        if (minutosDuracion % 60 < MINUTOS_MINIMOS) {
            horasCompletas--; // quitamos la hora completa
        }

        return TARIFA_HORA.multiply(BigDecimal.valueOf(horasCompletas > 0 ? horasCompletas : 1));
    }

    /**
     * Obtiene la tarifa base por hora.
     */
    public static BigDecimal getTarifaHora() {
        return TARIFA_HORA;
    }
}