-- ============================================================
-- TRIGGERS DE INMUTABILIDAD PARA TABLA audit_eventos
-- Garantiza que NADIE pueda modificar o eliminar registros
-- ============================================================

-- Funcion para bloquear UPDATE
CREATE OR REPLACE FUNCTION audit_bloquear_update()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'VIOLACION DE SEGURIDAD: UPDATE no permitido en tabla audit_eventos. '
                    'Esta tabla es inmutable por diseño. Intento de modificacion del registro ID: %',
                    OLD.id;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger para bloquear UPDATE
DROP TRIGGER IF EXISTS trg_audit_bloquear_update ON audit_eventos;
CREATE TRIGGER trg_audit_bloquear_update
    BEFORE UPDATE ON audit_eventos
    FOR EACH ROW
    EXECUTE FUNCTION audit_bloquear_update();

-- Funcion para bloquear DELETE
CREATE OR REPLACE FUNCTION audit_bloquear_delete()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'VIOLACION DE SEGURIDAD: DELETE no permitido en tabla audit_eventos. '
                    'Esta tabla es inmutable por diseño. Intento de eliminacion del registro ID: %',
                    OLD.id;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger para bloquear DELETE
DROP TRIGGER IF EXISTS trg_audit_bloquear_delete ON audit_eventos;
CREATE TRIGGER trg_audit_bloquear_delete
    BEFORE DELETE ON audit_eventos
    FOR EACH ROW
    EXECUTE FUNCTION audit_bloquear_delete();

-- Funcion para bloquear TRUNCATE
CREATE OR REPLACE FUNCTION audit_bloquear_truncate()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'VIOLACION DE SEGURIDAD: TRUNCATE no permitido en tabla audit_eventos. '
                    'Esta tabla es inmutable por diseño.';
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger para bloquear TRUNCATE
DROP TRIGGER IF EXISTS trg_audit_bloquear_truncate ON audit_eventos;
CREATE TRIGGER trg_audit_bloquear_truncate
    BEFORE TRUNCATE ON audit_eventos
    FOR EACH STATEMENT
    EXECUTE FUNCTION audit_bloquear_truncate();

-- ============================================================
-- INDICES ADICIONALES PARA PERFORMANCE
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_audit_eventos_timestamp_desc 
    ON audit_eventos (timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_audit_eventos_categoria_timestamp 
    ON audit_eventos (categoria, timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_audit_eventos_actor_timestamp 
    ON audit_eventos (actor_id, timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_audit_eventos_modulo_timestamp 
    ON audit_eventos (modulo_origen, timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_audit_eventos_recurso 
    ON audit_eventos (recurso_afectado);

CREATE INDEX IF NOT EXISTS idx_audit_eventos_correlation 
    ON audit_eventos (correlation_id);

-- ============================================================
-- COMENTARIOS DE DOCUMENTACION
-- ============================================================

COMMENT ON TABLE audit_eventos IS 
    'Tabla inmutable de eventos de auditoria. Solo permite INSERT. '
    'Triggers bloquean UPDATE, DELETE y TRUNCATE. '
    'Cada evento incluye hash del anterior para formar cadena de integridad.';

COMMENT ON TABLE audit_cadena_hash IS 
    'Cadena de hashes que garantiza la integridad de los eventos. '
    'Cada registro referencia al hash del evento anterior.';

COMMENT ON TABLE audit_configuracion_retencion IS 
    'Configuracion de politicas de retencion por categoria de evento. '
    'Minimo 10 anos conforme a normatividad archivistica del INE.';