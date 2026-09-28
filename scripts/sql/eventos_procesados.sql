-- Tabla de soporte para consumidores idempotentes (entrega at-least-once).
-- Cada consumidor registra el eventoId ya procesado; si el mismo evento llega
-- duplicado (reintento de Kafka), se detecta y se descarta sin repetir el cargo.
-- Se ejecuta manualmente porque la base legada usa ddl-auto: none.
-- Uso: docker exec -i mysql-bank mysql -uroot -proot bank_legacy < scripts/sql/eventos_procesados.sql

CREATE TABLE IF NOT EXISTS eventos_procesados (
    evento_id     VARCHAR(64)  NOT NULL,
    consumidor    VARCHAR(50)  NOT NULL,
    procesado_en  DATETIME     NOT NULL,
    PRIMARY KEY (evento_id, consumidor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
