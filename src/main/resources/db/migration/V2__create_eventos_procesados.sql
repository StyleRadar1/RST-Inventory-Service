CREATE TABLE IF NOT EXISTS eventos_procesados (
    event_id VARCHAR(100) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    procesado_en TIMESTAMP WITH TIME ZONE NOT NULL
);
