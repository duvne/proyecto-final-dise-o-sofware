package com.volt.catalogoflota.strategy;

/**
 * Estrategia base para cálculo de tarifas de patinetas.
 * Define el contrato que deben implementar todas las estrategias de tarifa.
 */
public interface TarifaStrategy {
    /**
     * Calcula el costo total de una tarifa dada una duración en minutos.
     *
     * @param minutosDuracion tiempo en minutos de uso
     * @return costo total en el moneda local
     */
    BigDecimal calcularCosto(int minutosDuracion);
}