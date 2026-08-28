-- Script reutilizable para dejar la base de datos de pruebas vacía
-- Elimina todos los registros de todas las tablas del esquema public.
-- Incluye usuarios, documentos, CV, auditoría y registros de intentos de acceso fallidos.
--
-- Uso:
--   docker exec -i gestiona-t-postgres psql -U gestiona_user -d gestiona_t -f /tmp/limpiar_bd_test.sql
-- o desde un cliente local conectado a localhost:5439
--
-- Nota:
--   Se deshabilitan los triggers para evitar que funciones de auditoría bloqueen el TRUNCATE.

BEGIN;

DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT table_schema, table_name
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_type = 'BASE TABLE'
        ORDER BY table_name
    LOOP
        EXECUTE format('ALTER TABLE %I.%I DISABLE TRIGGER ALL', r.table_schema, r.table_name);
    END LOOP;
END $$;

DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT table_schema, table_name
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_type = 'BASE TABLE'
        ORDER BY table_name
    LOOP
        EXECUTE format('TRUNCATE TABLE %I.%I CASCADE', r.table_schema, r.table_name);
    END LOOP;
END $$;

DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT table_schema, table_name
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_type = 'BASE TABLE'
        ORDER BY table_name
    LOOP
        EXECUTE format('ALTER TABLE %I.%I ENABLE TRIGGER ALL', r.table_schema, r.table_name);
    END LOOP;
END $$;

COMMIT;

SELECT table_name, n_live_tup
FROM pg_stat_user_tables
WHERE schemaname = 'public'
ORDER BY table_name;
