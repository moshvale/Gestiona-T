-- ============================================================
-- MIGRACION: Tablas del Modulo de Auditoria
-- ============================================================

-- Tabla principal de eventos
CREATE TABLE IF NOT EXISTS audit_eventos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    categoria VARCHAR(30) NOT NULL,
    tipo_evento VARCHAR(40) NOT NULL,
    severidad VARCHAR(20) NOT NULL,
    actor_id UUID,
    actor_tipo VARCHAR(20),
    ip_origen VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    recurso_afectado VARCHAR(100),
    descripcion VARCHAR(1000) NOT NULL,
    datos_evento JSONB,
    hash_datos VARCHAR(100),
    hash_anterior VARCHAR(100),
    hash_propio VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    correlation_id VARCHAR(100),
    modulo_origen VARCHAR(50),
    anclado_blockchain BOOLEAN NOT NULL DEFAULT FALSE,
    transaccion_blockchain VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Tabla de cadena de hashes
CREATE TABLE IF NOT EXISTS audit_cadena_hash (
    id BIGSERIAL PRIMARY KEY,
    evento_id UUID NOT NULL UNIQUE,
    hash_evento VARCHAR(100) NOT NULL,
    hash_anterior VARCHAR(100),
    secuencia BIGINT NOT NULL UNIQUE,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Tabla de configuracion de retencion
CREATE TABLE IF NOT EXISTS audit_configuracion_retencion (
    id BIGSERIAL PRIMARY KEY,
    categoria VARCHAR(50) NOT NULL UNIQUE,
    anios_retencion INTEGER NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    descripcion VARCHAR(500),
    fecha_actualizacion TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indices
CREATE INDEX IF NOT EXISTS idx_audit_eventos_timestamp ON audit_eventos (timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_eventos_categoria ON audit_eventos (categoria);
CREATE INDEX IF NOT EXISTS idx_audit_eventos_tipo ON audit_eventos (tipo_evento);
CREATE INDEX IF NOT EXISTS idx_audit_eventos_actor ON audit_eventos (actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_eventos_severidad ON audit_eventos (severidad);
CREATE INDEX IF NOT EXISTS idx_audit_eventos_recurso ON audit_eventos (recurso_afectado);
CREATE INDEX IF NOT EXISTS idx_audit_eventos_correlation ON audit_eventos (correlation_id);

CREATE INDEX IF NOT EXISTS idx_cadena_secuencia ON audit_cadena_hash (secuencia);
CREATE INDEX IF NOT EXISTS idx_cadena_evento ON audit_cadena_hash (evento_id);