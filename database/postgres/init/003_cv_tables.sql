CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS cv_estructurados (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aspirante_id UUID NOT NULL REFERENCES aspirantes(id),
    folio VARCHAR(36) NOT NULL,
    score_completitud INTEGER NOT NULL DEFAULT 0,
    fecha_captura TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_ultima_modificacion TIMESTAMP NOT NULL DEFAULT NOW(),
    completo BOOLEAN NOT NULL DEFAULT FALSE,
    metodo_captura VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_cv_folio ON cv_estructurados(folio);
CREATE INDEX IF NOT EXISTS idx_cv_aspirante ON cv_estructurados(aspirante_id);

CREATE TABLE IF NOT EXISTS cv_escolaridad (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cv_id UUID NOT NULL REFERENCES cv_estructurados(id) ON DELETE CASCADE,
    nivel VARCHAR(30) NOT NULL,
    institucion VARCHAR(200) NOT NULL,
    titulo VARCHAR(100),
    cedula_profesional VARCHAR(20),
    fecha_inicio DATE NOT NULL,
    fecha_termino DATE,
    status VARCHAR(20) NOT NULL,
    documento_soporte_path VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS cv_cursos_capacitaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cv_id UUID NOT NULL REFERENCES cv_estructurados(id) ON DELETE CASCADE,
    nombre_curso VARCHAR(200) NOT NULL,
    institucion VARCHAR(200) NOT NULL,
    duracion_horas INTEGER NOT NULL,
    fecha_realizacion DATE NOT NULL,
    documento_soporte_path VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS cv_habilidades_tecnicas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cv_id UUID NOT NULL REFERENCES cv_estructurados(id) ON DELETE CASCADE,
    tipo VARCHAR(30) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    nivel VARCHAR(20) NOT NULL,
    fecha_certificacion DATE,
    fecha_vencimiento DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS cv_institucionales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aspirante_id UUID NOT NULL UNIQUE REFERENCES aspirantes(id),
    entidad_preferida VARCHAR(100),
    sueldo_deseado NUMERIC(10,2),
    disponibilidad VARCHAR(50),
    areas_interes TEXT,
    sistemas_operativos VARCHAR(255),
    lenguajes_programacion VARCHAR(255),
    bases_de_datos VARCHAR(255),
    habilidades TEXT,
    logros_profesionales TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS cv_formacion_academica (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cv_id UUID NOT NULL REFERENCES cv_institucionales(id) ON DELETE CASCADE,
    nivel VARCHAR(100) NOT NULL,
    institucion VARCHAR(200) NOT NULL,
    carrera VARCHAR(200) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    cedula_profesional VARCHAR(50),
    estatus VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS cv_experiencia_laboral (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cv_id UUID NOT NULL REFERENCES cv_institucionales(id) ON DELETE CASCADE,
    tipo_experiencia VARCHAR(50) NOT NULL,
    empresa VARCHAR(150) NOT NULL,
    puesto VARCHAR(150) NOT NULL,
    funciones TEXT,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    sueldo NUMERIC(10,2),
    nivel_mando VARCHAR(20) NOT NULL DEFAULT 'OPERATIVO',
    actualmente_laborando BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS cv_idiomas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cv_id UUID NOT NULL REFERENCES cv_institucionales(id) ON DELETE CASCADE,
    idioma VARCHAR(100) NOT NULL,
    nivel_escritura VARCHAR(50),
    nivel_lectura VARCHAR(50),
    nivel_conversacion VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

DO $$
BEGIN
    RAISE NOTICE 'Tablas CV creadas y alineadas';
END $$;
