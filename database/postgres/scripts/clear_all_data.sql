-- ==============================================================================
-- Script para limpiar TODOS LOS DATOS de usuarios de la base de datos gestiona_t
-- ==============================================================================
-- Advertencia: esto ELIMINA TODOS LOS DATOS de usuarios, registros de intentos de
-- autenticación y datos relacionados. Las secuencias se reinician a 1.
-- ==============================================================================

-- Paso 1: Desactivar triggers que bloquean operaciones
-- (La tabla audit_eventos tiene trigger que bloquea TRUNCATE)
DROP TRIGGER IF EXISTS bloquear_truncate_audit ON audit_eventos;

-- Paso 2: Eliminar registros en orden de dependencias (respetando FK)
DELETE FROM codigos_otp;
DELETE FROM cv_estructurados;
DELETE FROM cv_institucionales;
DELETE FROM documentos;
DELETE FROM expedientes_digitales;
DELETE FROM cartas_declaratorias;
DELETE FROM documentos_firmados;
DELETE FROM cv_escolaridad;
DELETE FROM cv_cursos_capacitaciones;
DELETE FROM cv_habilidades_tecnicas;
DELETE FROM cv_formacion_academica;
DELETE FROM cv_experiencia_laboral;
DELETE FROM cv_idiomas;
DELETE FROM cv_cursos_capacitaciones_institucionales;
DELETE FROM revisiones_manuales;
DELETE FROM aceptaciones_bloques;
DELETE FROM validaciones_externas_carta;
DELETE FROM sellos_digitales;
DELETE FROM firmas_metadata;
DELETE FROM audit_cadena_hash;
DELETE FROM audit_eventos;
DELETE FROM intentos_auth;
DELETE FROM aspirantes;

-- Paso 3: Reiniciar todas las secuencias (auto-increment)
DO $$
DECLARE
    seq RECORD;
BEGIN
    FOR seq IN (
        SELECT sequence_schema, sequence_name 
        FROM information_schema.sequences 
        WHERE sequence_schema = 'public'
    ) LOOP
        EXECUTE format('ALTER SEQUENCE %I.%I RESTART WITH 1;', seq.sequence_schema, seq.sequence_name);
    END LOOP;
END $$;

-- Paso 4: Confirmación (descomentar para usar interactivamente)
-- SELECT 'Base de datos limpiada exitosamente ✅' as resultado;
