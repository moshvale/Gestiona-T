CREATE TABLE IF NOT EXISTS aspirantes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    folio VARCHAR(36) UNIQUE NOT NULL,
    nombre_completo VARCHAR(200) NOT NULL,
    curp VARCHAR(18) UNIQUE,
    rfc VARCHAR(13) UNIQUE,
    correo_electronico VARCHAR(100) UNIQUE NOT NULL,
    telefono_movil VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    estatus VARCHAR(30) NOT NULL DEFAULT 'PRE_REGISTRO',
    metodo_identificacion VARCHAR(30),
    nivel_confianza INTEGER NOT NULL DEFAULT 0,
    fecha_nacimiento TIMESTAMP,
    entidad_federativa VARCHAR(2),
    fecha_registro TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_ultimo_acceso TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_aspirante_folio ON aspirantes(folio);
CREATE INDEX IF NOT EXISTS idx_aspirante_curp ON aspirantes(curp);
CREATE INDEX IF NOT EXISTS idx_aspirante_correo ON aspirantes(correo_electronico);

CREATE TABLE IF NOT EXISTS intentos_auth (
    id BIGSERIAL PRIMARY KEY,
    ip_origen VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    curp_intentada VARCHAR(18),
    correo_intentado VARCHAR(100),
    tipo VARCHAR(30) NOT NULL,
    resultado VARCHAR(20) NOT NULL,
    motivo_fallo VARCHAR(500),
    timestamp TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_intento_ip ON intentos_auth(ip_origen);
CREATE INDEX IF NOT EXISTS idx_intento_timestamp ON intentos_auth(timestamp);

CREATE TABLE IF NOT EXISTS codigos_otp (
    id BIGSERIAL PRIMARY KEY,
    aspirante_id UUID NOT NULL REFERENCES aspirantes(id),
    codigo_hash VARCHAR(255) NOT NULL,
    canal VARCHAR(10) NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    utilizado BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_otp_aspirante ON codigos_otp(aspirante_id);
CREATE INDEX IF NOT EXISTS idx_otp_expiracion ON codigos_otp(fecha_expiracion);

DO $$
BEGIN
    RAISE NOTICE 'Tablas AUTH creadas';
END $$;
