-- ============================================
-- Tabla de Cursos y Capacitaciones Institucionales
-- Flujo moderno (cv_institucionales)
-- ============================================

CREATE TABLE IF NOT EXISTS cv_cursos_capacitaciones_institucionales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cv_id UUID NOT NULL REFERENCES cv_institucionales(id) ON DELETE CASCADE,
    nombre_curso VARCHAR(200) NOT NULL,
    institucion VARCHAR(200) NOT NULL,
    duracion_horas INTEGER NOT NULL,
    fecha_realizacion DATE NOT NULL,
    documento_soporte_path VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_curso_inst_cv ON cv_cursos_capacitaciones_institucionales(cv_id);

-- Asegurar defaults para auditoría
ALTER TABLE cv_cursos_capacitaciones_institucionales
    ALTER COLUMN id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN created_at SET DEFAULT NOW(),
    ALTER COLUMN updated_at SET DEFAULT NOW();

DO $$
BEGIN
    RAISE NOTICE '✅ Tabla cv_cursos_capacitaciones_institucionales creada correctamente';
END $$;
