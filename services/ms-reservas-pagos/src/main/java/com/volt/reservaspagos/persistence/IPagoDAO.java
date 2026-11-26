package com.volt.reservaspagos.persistence;

import java.util.List;
import java.util.Optional;

import com.volt.reservaspagos.model.domain.PagoInfo;

public interface IPagoDAO {
    PagoInfo guardar(PagoInfo pago);
    Optional<PagoInfo> buscarPorId(String id);
    List<PagoInfo> listarPorEstado(String estado);
    long contar();
}