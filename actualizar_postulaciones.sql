-- Agregar columnas de evaluación y dictamen final a la tabla postulaciones
ALTER TABLE postulaciones 
ADD COLUMN IF NOT EXISTS calificacion_conocimientos DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS calificacion_psicometrica DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS calificacion_entrevista DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS estatus_final_seleccion VARCHAR(50) DEFAULT 'PENDIENTE',
ADD COLUMN IF NOT EXISTS dictamen_final TEXT;

-- Verificar que se agregaron correctamente
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'postulaciones' 
  AND column_name IN (
    'calificacion_conocimientos', 
    'calificacion_psicometrica', 
    'calificacion_entrevista', 
    'estatus_final_seleccion', 
    'dictamen_final'
  )
ORDER BY column_name;