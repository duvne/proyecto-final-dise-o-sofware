package com.volt.reservaspagos.persistence;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.volt.reservaspagos.model.domain.FranjaDisponible;
import com.volt.reservaspagos.model.domain.FranjaOcupadaException;

@Repository
public class FranjaReservaDAO {
	private static final List<String> ESTADOS_ACTIVOS = List.of("SOLICITADA", "CONFIRMADA", "EN_CURSO");

	private final JdbcTemplate jdbcTemplate;
	private volatile boolean esquemaInicializado;

	public FranjaReservaDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void bloquear(String idReserva, String idPatineta, Instant inicio, Instant fin) {
		asegurarEsquema();
		bloquearPatineta(idPatineta);
		if (haySolapamiento(idPatineta, inicio, fin, null)) {
			throw new FranjaOcupadaException();
		}
		jdbcTemplate.update("""
				INSERT INTO franjas_reserva (id_reserva, id_patineta, inicio, fin, estado)
				VALUES (?, ?, ?, ?, 'SOLICITADA')
				""", idReserva, idPatineta, Timestamp.from(inicio), Timestamp.from(fin));
	}

	public void reprogramar(String idReserva, String idPatineta, Instant inicio, Instant fin) {
		asegurarEsquema();
		bloquearPatineta(idPatineta);
		if (haySolapamiento(idPatineta, inicio, fin, idReserva)) {
			throw new FranjaOcupadaException();
		}
		int actualizadas = jdbcTemplate.update("""
				UPDATE franjas_reserva SET inicio = ?, fin = ?
				WHERE id_reserva = ? AND id_patineta = ?
				""", Timestamp.from(inicio), Timestamp.from(fin), idReserva, idPatineta);
		if (actualizadas == 0) {
			jdbcTemplate.update("""
					INSERT INTO franjas_reserva (id_reserva, id_patineta, inicio, fin, estado)
					VALUES (?, ?, ?, ?, 'CONFIRMADA')
					""", idReserva, idPatineta, Timestamp.from(inicio), Timestamp.from(fin));
		}
	}

	public void actualizarEstado(String idReserva, String estado) {
		asegurarEsquema();
		jdbcTemplate.update("UPDATE franjas_reserva SET estado = ? WHERE id_reserva = ?", estado, idReserva);
	}

	public void liberar(String idReserva) {
		asegurarEsquema();
		jdbcTemplate.update("DELETE FROM franjas_reserva WHERE id_reserva = ?", idReserva);
	}

	public List<String> patinetasOcupadas(Instant inicio, Instant fin) {
		asegurarEsquema();
		return jdbcTemplate.queryForList("""
				SELECT DISTINCT id_patineta FROM franjas_reserva
				WHERE estado IN ('SOLICITADA', 'CONFIRMADA', 'EN_CURSO')
				  AND inicio < ? AND fin > ?
				""", String.class, Timestamp.from(fin), Timestamp.from(inicio));
	}

	public List<FranjaDisponible> franjas(String idPatineta, Instant desde, Instant hasta) {
		asegurarEsquema();
		Instant inicioFiltro = desde == null ? Instant.EPOCH : desde;
		Instant finFiltro = hasta == null ? Instant.parse("2100-01-01T00:00:00Z") : hasta;
		return jdbcTemplate.query("""
				SELECT inicio, fin, estado FROM franjas_reserva
				WHERE id_patineta = ? AND inicio < ? AND fin > ?
				ORDER BY inicio
				""", (resultSet, rowNum) -> new FranjaDisponible(
						resultSet.getTimestamp("inicio").toInstant(),
						resultSet.getTimestamp("fin").toInstant(),
						resultSet.getString("estado")),
				idPatineta, Timestamp.from(finFiltro), Timestamp.from(inicioFiltro));
	}

	private void bloquearPatineta(String idPatineta) {
		jdbcTemplate.update("INSERT INTO reserva_lock (id_patineta) VALUES (?) ON CONFLICT (id_patineta) DO NOTHING",
				idPatineta);
		jdbcTemplate.queryForList("SELECT id_patineta FROM reserva_lock WHERE id_patineta = ? FOR UPDATE",
				String.class, idPatineta);
	}

	private boolean haySolapamiento(String idPatineta, Instant inicio, Instant fin, String idReservaExcluida) {
		String sql = """
				SELECT COUNT(*) FROM franjas_reserva
				WHERE id_patineta = ?
				  AND estado IN ('SOLICITADA', 'CONFIRMADA', 'EN_CURSO')
				  AND inicio < ? AND fin > ?
				""" + (idReservaExcluida == null ? "" : " AND id_reserva <> ?");
		Integer count = idReservaExcluida == null
				? jdbcTemplate.queryForObject(sql, Integer.class, idPatineta,
						Timestamp.from(fin), Timestamp.from(inicio))
				: jdbcTemplate.queryForObject(sql, Integer.class, idPatineta,
						Timestamp.from(fin), Timestamp.from(inicio), idReservaExcluida);
		return count != null && count > 0;
	}

	private void asegurarEsquema() {
		if (esquemaInicializado) {
			return;
		}
		synchronized (this) {
			if (esquemaInicializado) {
				return;
			}
			jdbcTemplate.execute("""
					CREATE TABLE IF NOT EXISTS reserva_lock (
						id_patineta VARCHAR(64) PRIMARY KEY
					)
					""");
			jdbcTemplate.execute("""
					CREATE TABLE IF NOT EXISTS franjas_reserva (
						id_reserva VARCHAR(64) PRIMARY KEY,
						id_patineta VARCHAR(64) NOT NULL,
						inicio TIMESTAMPTZ NOT NULL,
						fin TIMESTAMPTZ NOT NULL,
						estado VARCHAR(20) NOT NULL
					)
					""");
			jdbcTemplate.execute("""
					CREATE INDEX IF NOT EXISTS idx_franjas_patineta
					ON franjas_reserva (id_patineta, inicio, fin)
					""");
			esquemaInicializado = true;
		}
	}
}
