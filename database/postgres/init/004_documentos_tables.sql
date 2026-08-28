CREATE TABLE IF NOT EXISTS documentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aspirante_id UUID NOT NULL REFERENCES aspirantes(id),
    folio VARCHAR(36) NOT NULL,
    tipo_documento VARCHAR(30) NOT NULL,
    tipo_validacion VARCHAR(10) NOT NULL,
    estatus VARCHAR(30) NOT NULL,
    nombre_archivo VARCHAR(500) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(50),
    tamano_bytes BIGINT,
    texto_extraido TEXT,
    score_autenticidad DOUBLE PRECISION,
    motivo_rechazo VARCHAR(1000),
    analista_id UUID,
    fecha_carga TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_validacion TIMESTAMP,
    metadata_validacion JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_doc_folio ON documentos(folio);
CREATE INDEX IF NOT EXISTS idx_doc_aspirante ON documentos(aspirante_id);
CREATE INDEX IF NOT EXISTS idx_doc_estatus ON documentos(estatus);

CREATE TABLE IF NOT EXISTS expedientes_digitales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aspirante_id UUID UNIQUE NOT NULL REFERENCES aspirantes(id),
    folio VARCHAR(36) NOT NULL,
    documentos_totales INTEGER NOT NULL DEFAULT 0,
    documentos_validados INTEGER NOT NULL DEFAULT 0,
    documentos_rechazados INTEGER NOT NULL DEFAULT 0,
    documentos_en_revision INTEGER NOT NULL DEFAULT 0,
    estatus_general VARCHAR(30) NOT NULL DEFAULT 'INCOMPLETO',
    sfp_verificado BOOLEAN NOT NULL DEFAULT FALSE,
    sfp_habilitado BOOLEAN,
    fecha_verificacion_sfp TIMESTAMP,
    fecha_ultima_actualizacion TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS catalogo_instituciones (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(10) NOT NULL,
    nombre VARCHAR(300) NOT NULL,
    clave VARCHAR(20),
    entidad_federativa VARCHAR(100),
    acreditada BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_actualizacion DATE,
    fuente_oficial VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_cat_tipo ON catalogo_instituciones(tipo);
CREATE INDEX IF NOT EXISTS idx_cat_nombre ON catalogo_instituciones(nombre);

CREATE TABLE IF NOT EXISTS revisiones_manuales (
    id BIGSERIAL PRIMARY KEY,
    documento_id UUID NOT NULL REFERENCES documentos(id),
    analista_id UUID,
    estatus VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    dictamen TEXT,
    motivo VARCHAR(1000),
    prioridad INTEGER,
    fecha_asignacion TIMESTAMP,
    fecha_dictamen TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rev_documento ON revisiones_manuales(documento_id);
CREATE INDEX IF NOT EXISTS idx_rev_estatus ON revisiones_manuales(estatus);

DO $$
BEGIN
    RAISE NOTICE 'Tablas DOCUMENTOS creadas';
END $$;
