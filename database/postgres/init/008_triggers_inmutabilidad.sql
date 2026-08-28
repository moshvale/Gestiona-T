CREATE OR REPLACE FUNCTION audit_bloquear_update()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'UPDATE no permitido en audit_eventos (tabla inmutable)';
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit_bloquear_delete()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'DELETE no permitido en audit_eventos (tabla inmutable)';
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit_bloquear_truncate()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'TRUNCATE no permitido en audit_eventos';
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_audit_bloquear_update ON audit_eventos;
CREATE TRIGGER trg_audit_bloquear_update
    BEFORE UPDATE ON audit_eventos
    FOR EACH ROW EXECUTE FUNCTION audit_bloquear_update();

DROP TRIGGER IF EXISTS trg_audit_bloquear_delete ON audit_eventos;
CREATE TRIGGER trg_audit_bloquear_delete
    BEFORE DELETE ON audit_eventos
    FOR EACH ROW EXECUTE FUNCTION audit_bloquear_delete();

DROP TRIGGER IF EXISTS trg_audit_bloquear_truncate ON audit_eventos;
CREATE TRIGGER trg_audit_bloquear_truncate
    BEFORE TRUNCATE ON audit_eventos
    FOR EACH STATEMENT EXECUTE FUNCTION audit_bloquear_truncate();

COMMENT ON TABLE audit_eventos IS 'Tabla INMUTABLE - Solo INSERT permitido';

DO $$
BEGIN
    RAISE NOTICE 'Triggers de inmutabilidad creados';
END $$;
