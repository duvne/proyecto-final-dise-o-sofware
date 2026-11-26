package com.volt.reservaspagos.persistence;

import com.volt.reservaspagos.model.domain.PagoInfo;
import com.volt.reservaspagos.persistence.IPagoDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PagoMongoDAOImpl implements IPagoDAO {

    @Autowired
    private IPagoDAO pagoRepository;

    @Override
    public PagoInfo save(PagoInfo pago) {
        return pagoRepository.save(pago);
    }

    @Override
    public Optional<PagoInfo> findById(String id) {
        return pagoRepository.findById(id);
    }

    @Override
    public List<PagoInfo> findByEstado(String estado) {
        return pagoRepository.findByEstado(estado);
    }

    @Override
    public List<PagoInfo> findAll() {
        return pagoRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        pagoRepository.deleteById(id);
    }
}