-- Agregar la relación con la vacante
ALTER TABLE expedientes_laborales 
ADD COLUMN IF NOT EXISTS vacante_id UUID REFERENCES vacantes(id) ON DELETE SET NULL;

-- Crear índice para mejorar el rendimiento de las consultas por vacante
CREATE INDEX IF NOT EXISTS idx_expedientes_vacante ON expedientes_laborales(vacante_id);

COMMENT ON COLUMN expedientes_laborales.vacante_id IS 'Vacante de origen si el expediente se generó desde una postulación seleccionada';