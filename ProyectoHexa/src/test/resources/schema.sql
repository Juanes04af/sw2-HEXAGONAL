CREATE TABLE IF NOT EXISTS dron (
    id          SERIAL PRIMARY KEY,
    serial      VARCHAR(50)  NOT NULL UNIQUE,
    fabricante  VARCHAR(100) NOT NULL,
    modelo      VARCHAR(100),
    peso        NUMERIC(8,2) NOT NULL CHECK (peso > 0),
    tipo        VARCHAR(20)  NOT NULL CHECK (tipo IN ('VIGILANCIA', 'AGRICULTURA'))
);

CREATE TABLE IF NOT EXISTS dron_vigilancia (
    id_dron           INTEGER PRIMARY KEY REFERENCES dron(id) ON DELETE CASCADE,
    deteccion_termica BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS dron_agricultura (
    id_dron          INTEGER PRIMARY KEY REFERENCES dron(id) ON DELETE CASCADE,
    capacidad_tanque NUMERIC(8,2) NOT NULL CHECK (capacidad_tanque > 0)
);
