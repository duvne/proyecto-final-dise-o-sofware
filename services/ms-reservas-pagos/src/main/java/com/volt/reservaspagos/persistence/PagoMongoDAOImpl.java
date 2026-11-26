package com.volt.reservaspagos.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.volt.reservaspagos.model.domain.PagoInfo;

import org.springframework.stereotype.Repository;

@Repository
public class PagoMongoDAOImpl implements IPagoDAO {
    private final Map<String, PagoInfo> store = new ConcurrentHashMap<>();

    @Override
    public PagoInfo guardar(PagoInfo pago) {
        store.put(pago.referenciaPasarela() != null ? pago.referenciaPasarela() : java.util.UUID.randomUUID().toString(), pago);
        return pago;
    }

    @Override
    public Optional<PagoInfo> buscarPorId(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<PagoInfo> listarPorEstado(String estado) {
        List<PagoInfo> result = new ArrayList<>();
        for (PagoInfo p : store.values()) {
            if (estado.equals(p.estado())) {
                result.add(p);
            }
        }
        return result;
    }

    @Override
    public long contar() {
        return store.size();
    }
}