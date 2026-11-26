package com.volt.reservaspagos.persistence;

import com.volt.reservaspagos.model.domain.PagoInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPagoDAO extends MongoRepository<PagoInfo, String> {
    // Métodos para búsqueda por estado
    List<PagoInfo> findByEstado(String estado);
    
    // Método para encontrar el pago más reciente
    PagoInfo findTopByOrderByFechaPagoDesc();
}