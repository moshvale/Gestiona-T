-- ============================================================
-- MIGRACIÓN: Expedientes Laborales y Catálogos
-- Fecha: 26 de agosto de 2026
-- Propósito: Soporte para personal en activo del INE
-- ============================================================

-- 1. Modificar tabla ASPIRANTES: agregar tipo de persona y número de empleado
ALTER TABLE aspirantes
ADD COLUMN IF NOT EXISTS tipo_persona VARCHAR(20) DEFAULT 'EXTERNO',
ADD COLUMN IF NOT EXISTS numero_empleado VARCHAR(20);

-- Restricción: numero_empleado debe ser único cuando no sea nulo
CREATE UNIQUE INDEX IF NOT EXISTS idx_aspirantes_numero_empleado 
ON aspirantes(numero_empleado) 
WHERE numero_empleado IS NOT NULL;

-- Validación: tipo_persona solo acepta EXTERNO o INTERNO
ALTER TABLE aspirantes
ADD CONSTRAINT chk_aspirantes_tipo_persona 
CHECK (tipo_persona IN ('EXTERNO', 'INTERNO'));

COMMENT ON COLUMN aspirantes.tipo_persona IS 'EXTERNO: ciudadano que aplica | INTERNO: empleado activo del INE';
COMMENT ON COLUMN aspirantes.numero_empleado IS 'Número único de empleado (solo para INTERNO). Ej: 15410';

-- 2. Crear tabla JUNTAS_EJECUTIVAS (catálogo para eventual electoral)
CREATE TABLE IF NOT EXISTS juntas_ejecutivas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(200) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    estado VARCHAR(100),
    clave_ine VARCHAR(20) UNIQUE,
    activa BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_juntas_tipo CHECK (tipo IN ('LOCAL', 'DISTRITAL'))
);

CREATE INDEX IF NOT EXISTS idx_juntas_tipo ON juntas_ejecutivas(tipo);
CREATE INDEX IF NOT EXISTS idx_juntas_activa ON juntas_ejecutivas(activa);

COMMENT ON TABLE juntas_ejecutivas IS 'Catálogo de Juntas Locales y Distritales Ejecutivas del INE';

-- 3. Crear tabla VOCALIAS (catálogo para eventual electoral)
CREATE TABLE IF NOT EXISTS vocalias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(200) NOT NULL,
    junta_ejecutiva_id UUID NOT NULL REFERENCES juntas_ejecutivas(id) ON DELETE CASCADE,
    activa BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_vocalia_junta UNIQUE (nombre, junta_ejecutiva_id)
);

CREATE INDEX IF NOT EXISTS idx_vocalias_junta ON vocalias(junta_ejecutiva_id);

COMMENT ON TABLE vocalias IS 'Vocalías de cada Junta Ejecutiva (Organización, Capacitación, RFE, Secretarial)';

-- 4. Crear tabla EXPEDIENTES_LABORALES
CREATE TABLE IF NOT EXISTS expedientes_laborales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aspirante_id UUID NOT NULL REFERENCES aspirantes(id) ON DELETE CASCADE,
    numero_empleado VARCHAR(20) NOT NULL,
    tipo_contratacion VARCHAR(50) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    vigente BOOLEAN NOT NULL DEFAULT true,
    area_adscripcion VARCHAR(200),
    puesto_actual VARCHAR(200),
    nivel_tabular VARCHAR(20),
    junta_ejecutiva_id UUID REFERENCES juntas_ejecutivas(id),
    vocalia_id UUID REFERENCES vocalias(id),
    alta_por_usuario_id UUID,
    observaciones TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_expedientes_tipo_contratacion CHECK (
        tipo_contratacion IN (
            'PRESUPUESTAL',
            'HONORARIO_PERMANENTE',
            'HONORARIO_EVENTUAL',
            'PROCESO_ELECTORAL'
        )
    ),
    CONSTRAINT chk_expedientes_fechas CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);

CREATE INDEX IF NOT EXISTS idx_expedientes_aspirante ON expedientes_laborales(aspirante_id);
CREATE INDEX IF NOT EXISTS idx_expedientes_vigente ON expedientes_laborales(vigente);
CREATE INDEX IF NOT EXISTS idx_expedientes_numero_empleado ON expedientes_laborales(numero_empleado);
CREATE INDEX IF NOT EXISTS idx_expedientes_tipo_contratacion ON expedientes_laborales(tipo_contratacion);
CREATE INDEX IF NOT EXISTS idx_expedientes_junta ON expedientes_laborales(junta_ejecutiva_id);

COMMENT ON TABLE expedientes_laborales IS 'Expedientes laborales del personal en activo del INE. Un aspirante puede tener múltiples expedientes históricos.';

-- 5. Modificar tabla DOCUMENTOS: agregar categoría y vinculación a expediente
ALTER TABLE documentos
ADD COLUMN IF NOT EXISTS categoria VARCHAR(30) DEFAULT 'CONCURSO',
ADD COLUMN IF NOT EXISTS expediente_laboral_id UUID REFERENCES expedientes_laborales(id) ON DELETE SET NULL,
ADD COLUMN IF NOT EXISTS es_documento_base BOOLEAN DEFAULT false;

-- Validación: categoria solo acepta los valores permitidos
ALTER TABLE documentos
ADD CONSTRAINT chk_documentos_categoria 
CHECK (categoria IN ('CONCURSO', 'CONTRATACION', 'EXPEDIENTE_LABORAL'));

CREATE INDEX IF NOT EXISTS idx_documentos_categoria ON documentos(categoria);
CREATE INDEX IF NOT EXISTS idx_documentos_expediente ON documentos(expediente_laboral_id);
CREATE INDEX IF NOT EXISTS idx_documentos_base ON documentos(es_documento_base);

COMMENT ON COLUMN documentos.categoria IS 'CONCURSO: documentos del proceso de selección | CONTRATACION: documentos de contratación | EXPEDIENTE_LABORAL: documentos del expediente del empleado activo';
COMMENT ON COLUMN documentos.es_documento_base IS 'true si es un documento base heredable (acta, CURP, RFC, título)';

-- 6. Poblar vocalías por defecto (se agregarán al crear una Junta Ejecutiva)
-- Las vocalías se crearán dinámicamente cuando se registre una Junta Ejecutiva.

-- 7. Verificación
SELECT 'aspirantes' AS tabla, COUNT(*) AS total FROM aspirantes
UNION ALL
SELECT 'juntas_ejecutivas', COUNT(*) FROM juntas_ejecutivas
UNION ALL
SELECT 'vocalias', COUNT(*) FROM vocalias
UNION ALL
SELECT 'expedientes_laborales', COUNT(*) FROM expedientes_laborales
UNION ALL
SELECT 'documentos', COUNT(*) FROM documentos;