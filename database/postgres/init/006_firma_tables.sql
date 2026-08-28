CREATE TABLE IF NOT EXISTS documentos_firmados (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    folio_documento VARCHAR(36) NOT NULL,
    tipo_documento VARCHAR(30) NOT NULL,
    aspirante_id UUID NOT NULL REFERENCES aspirantes(id),
    folio_aspirante VARCHAR(36) NOT NULL,
    nivel_firma VARCHAR(30) NOT NULL,
    estatus VARCHAR(30) NOT NULL,
    nombre_archivo VARCHAR(500) NOT NULL,
    storage_path_original VARCHAR(500) NOT NULL,
    storage_path_firmado VARCHAR(500),
    hash_original VARCHAR(100),
    hash_firmado VARCHAR(100),
    metadata_firma JSONB,
    motivo_rechazo VARCHAR(1000),
    fecha_solicitud TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_firma TIMESTAMP,
    fecha_expiracion TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_doc_firmado_folio ON documentos_firmados(folio_documento);

CREATE TABLE IF NOT EXISTS sellos_digitales (
    id BIGSERIAL PRIMARY KEY,
    documento_firmado_id UUID NOT NULL UNIQUE REFERENCES documentos_firmados(id),
    timestamp_token TEXT NOT NULL,
    timestamp_certificado TIMESTAMP NOT NULL,
    autoridad_timestamp VARCHAR(200) NOT NULL,
    hash_documento VARCHAR(100) NOT NULL,
    algoritmo_hash VARCHAR(50),
    algoritmo_firma VARCHAR(50),
    certificado_firmante TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS firmas_metadata (
    id BIGSERIAL PRIMARY KEY,
    documento_firmado_id UUID NOT NULL UNIQUE REFERENCES documentos_firmados(id),
    ip_origen VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    geolocalizacion VARCHAR(100),
    dispositivo_id VARCHAR(100),
    datos_biometricos TEXT,
    otp_hash VARCHAR(100),
    certificado_serial VARCHAR(100),
    certificado_subject VARCHAR(200),
    certificado_valido_hasta TIMESTAMP,
    score_coincidencia_biometrica DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

DO $$
BEGIN
    RAISE NOTICE 'Tablas FIRMA creadas';
END $$;
