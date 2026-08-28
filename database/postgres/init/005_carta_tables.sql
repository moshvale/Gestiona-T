CREATE TABLE IF NOT EXISTS cartas_declaratorias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aspirante_id UUID NOT NULL REFERENCES aspirantes(id),
    folio VARCHAR(36) NOT NULL,
    folio_carta VARCHAR(36) NOT NULL,
    version VARCHAR(20) NOT NULL,
    estatus VARCHAR(30) NOT NULL,
    pdf_storage_path VARCHAR(500),
    pdf_hash VARCHAR(100),
    firma_digital_hash VARCHAR(100),
    metodo_firma VARCHAR(30),
    fecha_aceptacion_completa TIMESTAMP,
    fecha_firma TIMESTAMP,
    metadata_sesion JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_carta_folio ON cartas_declaratorias(folio);
CREATE INDEX IF NOT EXISTS idx_carta_aspirante ON cartas_declaratorias(aspirante_id);

CREATE TABLE IF NOT EXISTS bloques_declaratorios (
    id INTEGER PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    texto TEXT NOT NULL,
    fundamento_legal VARCHAR(300) NOT NULL,
    obligatorio BOOLEAN NOT NULL DEFAULT TRUE,
    orden INTEGER NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS aceptaciones_bloques (
    id BIGSERIAL PRIMARY KEY,
    carta_id UUID NOT NULL REFERENCES cartas_declaratorias(id),
    bloque_id INTEGER NOT NULL REFERENCES bloques_declaratorios(id),
    aceptado BOOLEAN NOT NULL,
    timestamp_aceptacion TIMESTAMP NOT NULL,
    ip_origen VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    hash_texto_bloque VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_aceptacion_carta ON aceptaciones_bloques(carta_id);

CREATE TABLE IF NOT EXISTS validaciones_externas_carta (
    id BIGSERIAL PRIMARY KEY,
    carta_id UUID NOT NULL REFERENCES cartas_declaratorias(id),
    tipo_validacion VARCHAR(30) NOT NULL,
    resultado BOOLEAN NOT NULL,
    respuesta_api TEXT,
    mensaje VARCHAR(1000),
    fecha_consulta TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Seed de los 12 bloques
INSERT INTO bloques_declaratorios (id, titulo, texto, fundamento_legal, obligatorio, orden, activo) VALUES
(1, 'VERACIDAD DOCUMENTAL', 'Declaro que toda la informacion y documentos proporcionados son autenticos y verificables.', 'Art. 183 Codigo Penal Federal', true, 1, true),
(2, 'NO INHABILITACION ADMINISTRATIVA', 'Declaro que NO me encuentro inhabilitado para desempenar empleo publico.', 'LGRA Arts. 7, 19, 38', true, 2, true),
(3, 'ANTECEDENTES PENALES', 'Declaro que NO he sido condenado por delito doloso.', 'Codigo Penal Federal', true, 3, true),
(4, 'OBLIGACIONES FISCALES', 'Declaro que cumplo con mis obligaciones fiscales.', 'Codigo Fiscal de la Federacion', true, 4, true),
(5, 'PREVENCION DE VIOLENCIA', 'Declaro NO haber ejercido violencia contra las mujeres ni ser deudor alimentario moroso.', 'Politica de Igualdad INE', true, 5, true),
(6, 'CONFLICTO DE INTERES', 'Declaro que NO tengo conflicto de interes para el cargo.', 'Lineamientos INE', true, 6, true),
(7, 'AFILIACION POLITICA', 'Declaro que NO estoy afiliado a partido politico alguno.', 'LGIPE Art. 44', true, 7, true),
(8, 'NO VIOLENCIA LABORAL', 'Declaro que NO he sido sancionado por violencia laboral.', 'Ley Federal del Trabajo', true, 8, true),
(9, 'COMPROMISO ETICO', 'Declaro conocer y aceptar el Codigo de Etica del INE.', 'Codigo de Etica Electoral', true, 9, true),
(10, 'PROTECCION DE DATOS', 'Autorizo al INE el tratamiento de mis datos personales.', 'LGDPPP', true, 10, true),
(11, 'DECLARACION PATRIMONIAL', 'Declaro que presentare mi Declaracion Patrimonial si soy contratado.', 'LGRA', true, 11, true),
(12, 'CONSECUENCIAS LEGALES', 'Declaro conocer las consecuencias legales de la falsedad en declaraciones.', 'LGRA + Codigo Penal', true, 12, true)
ON CONFLICT (id) DO NOTHING;

DO $$
BEGIN
    RAISE NOTICE 'Tablas CARTA_DECLARATORIA creadas';
END $$;
