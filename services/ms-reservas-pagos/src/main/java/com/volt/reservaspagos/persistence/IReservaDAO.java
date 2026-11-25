package com.volt.reservaspagos.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.volt.reservaspagos.model.domain.EstadoReserva;
import com.volt.reservaspagos.model.domain.ReservaDocument;

public interface IReservaDAO {
	ReservaDocument guardar(ReservaDocument reserva);

	ReservaDocument actualizar(ReservaDocument reserva);

	Optional<ReservaDocument> buscarPorId(String id);

	List<ReservaDocument> listarPorUsuario(String usuarioId, EstadoReserva estado);

	List<ReservaDocument> listarAdmin(EstadoReserva estado, Instant desde, Instant hasta, int pagina, int tamano);

	long contar(EstadoReserva estado, Instant desde, Instant hasta);

	List<ReservaDocument> buscarParaLiquidar(Instant ahora);

	List<ReservaDocument> buscarParaNoShow(Instant limite);

	List<ReservaDocument> listarPorPropietario(String propietarioId);
}
