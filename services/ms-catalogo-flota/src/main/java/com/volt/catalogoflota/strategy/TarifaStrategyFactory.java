package com.volt.catalogoflota.strategy;

import org.springframework.stereotype.Component;

/**
 * Fábrica de estrategias de tarifa.
 * Devuelve la estrategia adecuada según el tipo de tarifa solicitada
 * o el contexto de uso.
 */
@Component
public class TarifaStrategyFactory {

    private final TarifaHoraStrategy tarifaHoraStrategy;
    private final TarifaDiaStrategy tarifaDiaStrategy;

    @Autowired
    public TarifaStrategyFactory(TarifaHoraStrategy tarifaHoraStrategy,
                                 TarifaDiaStrategy tarifaDiaStrategy) {
        this.tarifaHoraStrategy = tarifaHoraStrategy;
        this.tarifaDiaStrategy = tarifaDiaStrategy;
    }

    /**
     * Obtiene la estrategia de tarifa por hora.
     */
    public TarifaStrategy getTarifaHora() {
        return tarifaHoraStrategy;
    }

    /**
     * Obtiene la estrategia de tarifa por día.
     */
    public TarifaStrategy getTarifaDia() {
        return tarifaDiaStrategy;
    }

    /**
     * Obtiene la estrategia apropiada según el tipo especificado.
     *
     * @param tipo "hora" o "dia"
     * @return la estrategia correspondiente
     */
    public TarifaStrategy getEstrategiaPorTipo(String tipo) {
        switch (tipo.toLowerCase()) {
            case "hora":
                return tarifaHoraStrategy;
            case "dia":
            default:
                return tarifaDiaStrategy;
        }
    }
}