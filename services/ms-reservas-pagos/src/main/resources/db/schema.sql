-- Franjas transaccionales usadas por MS-ReservasPagos.
CREATE TABLE IF NOT EXISTS reserva_lock (
    id_patineta VARCHAR(64) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS franjas_reserva (
    id_reserva VARCHAR(64) PRIMARY KEY,
    id_patineta VARCHAR(64) NOT NULL,
    inicio TIMESTAMPTZ NOT NULL,
    fin TIMESTAMPTZ NOT NULL,
    estado VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_franjas_patineta
    ON franjas_reserva (id_patineta, inicio, fin);
