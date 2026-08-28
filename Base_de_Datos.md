# Base de Datos - Gestiona T

**Versión:** 1.1.2  
**Motor:** PostgreSQL 16  
**Esquema:** gestiona_t  
**Fecha:** 10 de agosto de 2026
**Estado:** Documentación alineada con la infraestructura local actual

Actualización: se añadió la tabla cv_cursos_capacitaciones_institucionales para soportar cursos y capacitaciones del flujo moderno del CV institucional, y se documentó la persistencia de resultados de matching.

---

## 1. Visión General

La base de datos de Gestiona T está organizada en módulos funcionales que corresponden a los 7 flujos críticos del sistema:

1. **Autenticación** - Registro, login, OTP
2. **CV Institucional** - Escolaridad, experiencia, cursos, habilidades
3. **Documentos** - Validación multinivel, expedientes digitales
4. **Carta Declaratoria** - 12 bloques declarativos
5. **Firma Electrónica** - 3 niveles de firma, sellos digitales
6. **Auditoría** - Trazabilidad inmutable
7. **Matching** - Resultados de matching curricular

---

## 2. Estructura de Tablas

### 2.1 Módulo de Autenticación

#### Tabla: `aspirantes`
Tabla principal de usuarios del sistema.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | UUID | PK, Generado automáticamente |
| folio | VARCHAR(36) | UNIQUE, Folio único del aspirante |
| nombre_completo | VARCHAR(200) | Nombre completo del aspirante |
| curp | VARCHAR(18) | UNIQUE, CURP del aspirante |
| rfc | VARCHAR(13) | UNIQUE, RFC del aspirante |
| correo_electronico | VARCHAR(100) | UNIQUE, Email de contacto |
| telefono_movil | VARCHAR(20) | Teléfono móvil |
| password_hash | VARCHAR(255) | Hash de contraseña (BCrypt) |
| estatus | VARCHAR(30) | PRE_REGISTRO, ACTIVO, COMPLETO, etc. |
| metodo_identificacion | VARCHAR(30) | CURP o CLAVE_ELECTOR |
| nivel_confianza | INTEGER | Nivel de confianza (0-3) |
| fecha_nacimiento | TIMESTAMP | Fecha de nacimiento |
| entidad_federativa | VARCHAR(2) | Clave de entidad federativa |
| fecha_registro | TIMESTAMP | Fecha de registro |
| fecha_ultimo_acceso | TIMESTAMP | Último acceso |
| activo | BOOLEAN | Estado del registro |
| created_at | TIMESTAMP | Timestamp de creación |
| updated_at | TIMESTAMP | Timestamp de actualización |
| created_by | VARCHAR(100) | Usuario que creó el registro |

**Índices:**
- idx_aspirante_folio (folio)
- idx_aspirante_curp (curp)
- idx_aspirante_correo (correo_electronico)

#### Tabla: `intentos_auth`
Registro de intentos de autenticación para seguridad.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | BIGSERIAL | PK |
| ip_origen | VARCHAR(45) | IP de origen |
| user_agent | VARCHAR(500) | User agent del navegador |
| curp_intentada | VARCHAR(18) | CURP intentada |
| correo_intentado | VARCHAR(100) | Email intentado |
| tipo | VARCHAR(30) | LOGIN, REGISTRO, RECUPERACION |
| resultado | VARCHAR(20) | EXITOSO, FALLIDO |
| motivo_fallo | VARCHAR(500) | Motivo del fallo |
| timestamp | TIMESTAMP | Timestamp del intento |

**Índices:**
- idx_intento_ip (ip_origen)
- idx_intento_timestamp (timestamp)

#### Tabla: `codigos_otp`
Códigos OTP para validación de dos factores.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | BIGSERIAL | PK |
| aspirante_id | UUID | FK → aspirantes(id) |
| codigo_hash | VARCHAR(255) | Hash del código OTP |
| canal | VARCHAR(10) | SMS, EMAIL |
| fecha_expiracion | TIMESTAMP | Fecha de expiración |
| utilizado | BOOLEAN | Si fue utilizado |
| created_at | TIMESTAMP | Timestamp de creación |

**Índices:**
- idx_otp_aspirante (aspirante_id)
- idx_otp_expiracion (fecha_expiracion)

---

### 2.2 Módulo de CV Institucional

#### Tabla: `cv_estructurados`
Cabecera del CV estructurado del aspirante.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | UUID | PK |
| aspirante_id | UUID | FK → aspirantes(id) |
| folio | VARCHAR(36) | Folio del aspirante |
| score_completitud | INTEGER | Score de completitud (0-100) |
| fecha_captura | TIMESTAMP | Fecha de captura |
| fecha_ultima_modificacion | TIMESTAMP | Última modificación |
| completo | BOOLEAN | Si está completo |
| metodo_captura | VARCHAR(50) | MANUAL, OCR, IMPORTADO |
| created_at | TIMESTAMP | Timestamp de creación |
| updated_at | TIMESTAMP | Timestamp de actualización |

**Índices:**
- idx_cv_folio (folio)
- idx_cv_aspirante (aspirante_id)

#### Tabla: `cv_escolaridad`
Escolaridad del aspirante.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | UUID | PK |
| cv_id | UUID | FK → cv_estructurados(id) ON DELETE CASCADE |
| nivel | VARCHAR(30) | Nivel educativo |
| institucion | VARCHAR(200) | Institución educativa |
| titulo | VARCHAR(100) | Título obtenido |
| cedula_profesional | VARCHAR(20) | Cédula profesional |
| fecha_inicio | DATE | Fecha de inicio |
| fecha_termino | DATE | Fecha de término |
| status | VARCHAR(20) | EN_CURSO, TERMINADO |
| documento_soporte_path | VARCHAR(500) | Ruta del documento |
| created_at | TIMESTAMP | Timestamp de creación |

#### Tabla: `cv_experiencia_laboral`
Experiencia laboral del aspirante.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | UUID | PK |
| cv_id | UUID | FK → cv_estructurados(id) ON DELETE CASCADE |
| institucion | VARCHAR(200) | Institución |
| rfc_institucion | VARCHAR(13) | RFC de la institución |
| puesto | VARCHAR(100) | Puesto desempeñado |
| funciones | VARCHAR(1000) | Funciones desempeñadas |
| fecha_inicio | DATE | Fecha de inicio |
| fecha_termino | DATE | Fecha de término |
| actualmente_laborando | BOOLEAN | Si labora actualmente |
| nivel_mando | VARCHAR(20) | Nivel de mando |
| documento_soporte_path | VARCHAR(500) | Ruta del documento |
| created_at | TIMESTAMP | Timestamp de creación |

#### Tabla: `cv_cursos_capacitaciones`
Cursos y capacitaciones del aspirante.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | UUID | PK |
| cv_id | UUID | FK → cv_estructurados(id) ON DELETE CASCADE |
| nombre_curso | VARCHAR(200) | Nombre del curso |
| institucion | VARCHAR(200) | Institución |
| duracion_horas | INTEGER | Duración en horas |
| fecha_realizacion | DATE | Fecha de realización |
| documento_soporte_path | VARCHAR(500) | Ruta del documento |
| created_at | TIMESTAMP | Timestamp de creación |

#### Tabla: `cv_habilidades_tecnicas`
Habilidades técnicas del aspirante.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | UUID | PK |
| cv_id | UUID | FK → cv_estructurados(id) ON DELETE CASCADE |
| tipo | VARCHAR(30) | TECNICA, IDIOMA, HERRAMIENTA |
| nombre | VARCHAR(100) | Nombre de la habilidad |
| nivel | VARCHAR(20) | Nivel de dominio |
| fecha_certificacion | DATE | Fecha de certificación |
| fecha_vencimiento | DATE | Fecha de vencimiento |
| created_at | TIMESTAMP | Timestamp de creación |

---

### 2.3 Módulo de Documentos

#### Tabla: `documentos`
Documentos cargados por aspirantes.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | UUID | PK |
| aspirante_id | UUID | FK → aspirantes(id) |
| folio | VARCHAR(36) | Folio del aspirante |
| tipo_documento | VARCHAR(30) | CV, INE, RFC, etc. |
| tipo_validacion | VARCHAR(10) | API, IA, MANUAL |
| estatus | VARCHAR(30) | PENDIENTE, VALIDADO, RECHAZADO |
| nombre_archivo | VARCHAR(500) | Nombre del archivo |
| storage_path | VARCHAR(500) | Ruta en MinIO |
| content_type | VARCHAR(50) | Tipo MIME |
| tamano_bytes | BIGINT | Tamaño en bytes |
| texto_extraido | TEXT | Texto extraído por OCR |
| score_autenticidad | DOUBLE PRECISION | Score de autenticidad |
| motivo_rechazo | VARCHAR(1000) | Motivo de rechazo |
| analista_id | UUID | ID del analista |
| fecha_carga | TIMESTAMP | Fecha de carga |
| fecha_validacion | TIMESTAMP | Fecha de validación |
| metadata_validacion | JSONB | Metadata de validación |
| created_at | TIMESTAMP | Timestamp de creación |
| updated_at | TIMESTAMP | Timestamp de actualización |

**Índices:**
- idx_doc_folio (folio)
- idx_doc_aspirante (aspirante_id)
- idx_doc_estatus (estatus)

#### Tabla: `expedientes_digitales`
Expediente digital de cada aspirante.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | UUID | PK |
| aspirante_id | UUID | UNIQUE, FK → aspirantes(id) |
| folio | VARCHAR(36) | Folio del aspirante |
| documentos_totales | INTEGER | Total de documentos |
| documentos_validados | INTEGER | Documentos validados |
| documentos_rechazados | INTEGER | Documentos rechazados |
| documentos_en_revision | INTEGER | Documentos en revisión |
| estatus_general | VARCHAR(30) | INCOMPLETO, COMPLETO, REVISADO |
| sfp_verificado | BOOLEAN | Verificado por SFP |
| sfp_habilitado | BOOLEAN | Habilitado por SFP |
| fecha_verificacion_sfp | TIMESTAMP | Fecha de verificación SFP |
| fecha_ultima_actualizacion | TIMESTAMP | Última actualización |
| created_at | TIMESTAMP | Timestamp de creación |
| updated_at | TIMESTAMP | Timestamp de actualización |

#### Tabla: `catalogo_instituciones`
Catálogo de instituciones acreditadas.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | BIGSERIAL | PK |
| tipo | VARCHAR(10) | ESCUELA, EMPRESA, GOBIERNO |
| nombre | VARCHAR(300) | Nombre de la institución |
| clave | VARCHAR(20) | Clave oficial |
| entidad_federativa | VARCHAR(100) | Entidad federativa |
| acreditada | BOOLEAN | Si está acreditada |
| fecha_actualizacion | DATE | Fecha de actualización |
| fuente_oficial | VARCHAR(50) | Fuente oficial |

**Índices:**
- idx_cat_tipo (tipo)
- idx_cat_nombre (nombre)

#### Tabla: `revisiones_manuales`
Revisiones manuales de documentos.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | BIGSERIAL | PK |
| documento_id | UUID | FK → documentos(id) |
| analista_id | UUID | ID del analista |
| estatus | VARCHAR(30) | PENDIENTE, APROBADO, RECHAZADO |
| dictamen | TEXT | Dictamen del analista |
| motivo | VARCHAR(1000) | Motivo del dictamen |
| prioridad | INTEGER | Prioridad de revisión |
| fecha_asignacion | TIMESTAMP | Fecha de asignación |
| fecha_dictamen | TIMESTAMP | Fecha del dictamen |
| created_at | TIMESTAMP | Timestamp de creación |

**Índices:**
- idx_rev_documento (documento_id)
- idx_rev_estatus (estatus)

---

### 2.4 Módulo de Carta Declaratoria

#### Tabla: `cartas_declaratorias`
Cartas declaratorias de aspirantes.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | UUID | PK |
| aspirante_id | UUID | FK → aspirantes(id) |
| folio | VARCHAR(36) | Folio del aspirante |
| folio_carta | VARCHAR(36) | Folio único de la carta |
| version | VARCHAR(20) | Versión de la carta |
| estatus | VARCHAR(30) | PENDIENTE, ACEPTADA, FIRMADA |
| pdf_storage_path | VARCHAR(500) | Ruta del PDF en MinIO |
| pdf_hash | VARCHAR(100) | Hash del PDF |
| firma_digital_hash | VARCHAR(100) | Hash de la firma digital |
| metodo_firma | VARCHAR(30) | FEA, BIOMETRICA, OTP |
| fecha_aceptacion_completa | TIMESTAMP | Fecha de aceptación completa |
| fecha_firma | TIMESTAMP | Fecha de firma |
| metadata_sesion | JSONB | Metadata de la sesión |
| created_at | TIMESTAMP | Timestamp de creación |
| updated_at | TIMESTAMP | Timestamp de actualización |

**Índices:**
- idx_carta_folio (folio)
- idx_carta_aspirante (aspirante_id)

#### Tabla: `bloques_declaratorios`
Los 12 bloques declarativos del INE.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | INTEGER | PK |
| titulo | VARCHAR(100) | Título del bloque |
| texto | TEXT | Texto del bloque |
| fundamento_legal | VARCHAR(300) | Fundamento legal |
| obligatorio | BOOLEAN | Si es obligatorio |
| orden | INTEGER | Orden del bloque |
| activo | BOOLEAN | Si está activo |

**Bloques (seed data):**
1. VERACIDAD DOCUMENTAL
2. NO INHABILITACION ADMINISTRATIVA
3. ANTECEDENTES PENALES
4. OBLIGACIONES FISCALES
5. PREVENCION DE VIOLENCIA
6. CONFLICTO DE INTERES
7. AFILIACION POLITICA
8. NO VIOLENCIA LABORAL
9. COMPROMISO ETICO
10. PROTECCION DE DATOS
11. DECLARACION PATRIMONIAL
12. CONSECUENCIAS LEGALES

#### Tabla: `aceptaciones_bloques`
Registro de aceptaciones de cada bloque.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | BIGSERIAL | PK |
| carta_id | UUID | FK → cartas_declaratorias(id) |
| bloque_id | INTEGER | FK → bloques_declaratorios(id) |
| aceptado | BOOLEAN | Si fue aceptado |
| timestamp_aceptacion | TIMESTAMP | Timestamp de aceptación |
| ip_origen | VARCHAR(45) | IP de origen |
| user_agent | VARCHAR(500) | User agent |
| hash_texto_bloque | VARCHAR(100) | Hash del texto del bloque |
| created_at | TIMESTAMP | Timestamp de creación |

**Índices:**
- idx_aceptacion_carta (carta_id)

#### Tabla: `validaciones_externas_carta`
Validaciones externas de la carta.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | BIGSERIAL | PK |
| carta_id | UUID | FK → cartas_declaratorias(id) |
| tipo_validacion | VARCHAR(30) | Tipo de validación |
| resultado | BOOLEAN | Resultado de la validación |
| respuesta_api | TEXT | Respuesta de la API |
| mensaje | VARCHAR(1000) | Mensaje de la validación |
| fecha_consulta | TIMESTAMP | Fecha de consulta |
| created_at | TIMESTAMP | Timestamp de creación |

---

### 2.5 Módulo de Firma Electrónica

#### Tabla: `documentos_firmados`
Documentos firmados electrónicamente.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | UUID | PK |
| folio_documento | VARCHAR(36) | Folio del documento |
| tipo_documento | VARCHAR(30) | Tipo de documento |
| aspirante_id | UUID | FK → aspirantes(id) |
| folio_aspirante | VARCHAR(36) | Folio del aspirante |
| nivel_firma | VARCHAR(30) | FEA, BIOMETRICA, OTP |
| estatus | VARCHAR(30) | PENDIENTE, FIRMADO, RECHAZADO |
| nombre_archivo | VARCHAR(500) | Nombre del archivo |
| storage_path_original | VARCHAR(500) | Ruta del original |
| storage_path_firmado | VARCHAR(500) | Ruta del firmado |
| hash_original | VARCHAR(100) | Hash del original |
| hash_firmado | VARCHAR(100) | Hash del firmado |
| metadata_firma | JSONB | Metadata de la firma |
| motivo_rechazo | VARCHAR(1000) | Motivo de rechazo |
| fecha_solicitud | TIMESTAMP | Fecha de solicitud |
| fecha_firma | TIMESTAMP | Fecha de firma |
| fecha_expiracion | TIMESTAMP | Fecha de expiración |
| created_at | TIMESTAMP | Timestamp de creación |
| updated_at | TIMESTAMP | Timestamp de actualización |

**Índices:**
- idx_doc_firmado_folio (folio_documento)

#### Tabla: `sellos_digitales`
Sellos digitales de documentos firmados.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | BIGSERIAL | PK |
| documento_firmado_id | UUID | UNIQUE, FK → documentos_firmados(id) |
| timestamp_token | TEXT | Token de timestamp |
| timestamp_certificado | TIMESTAMP | Certificado de timestamp |
| autoridad_timestamp | VARCHAR(200) | Autoridad de timestamp |
| hash_documento | VARCHAR(100) | Hash del documento |
| algoritmo_hash | VARCHAR(50) | Algoritmo de hash |
| algoritmo_firma | VARCHAR(50) | Algoritmo de firma |
| certificado_firmante | TEXT | Certificado del firmante |
| created_at | TIMESTAMP | Timestamp de creación |

#### Tabla: `firmas_metadata`
Metadata de firmas electrónicas.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | BIGSERIAL | PK |
| documento_firmado_id | UUID | UNIQUE, FK → documentos_firmados(id) |
| ip_origen | VARCHAR(45) | IP de origen |
| user_agent | VARCHAR(500) | User agent |
| geolocalizacion | VARCHAR(100) | Geolocalización |
| dispositivo_id | VARCHAR(100) | ID del dispositivo |
| datos_biometricos | TEXT | Datos biométricos |
| otp_hash | VARCHAR(100) | Hash del OTP |
| certificado_serial | VARCHAR(100) | Serial del certificado |
| certificado_subject | VARCHAR(200) | Subject del certificado |
| certificado_valido_hasta | TIMESTAMP | Validez del certificado |
| score_coincidencia_biometrica | DOUBLE PRECISION | Score de coincidencia |
| created_at | TIMESTAMP | Timestamp de creación |

---

### 2.6 Módulo de Auditoría

#### Tabla: `auditoria`
Bitácora inmutable de auditoría.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | BIGSERIAL | PK |
| folio | VARCHAR(36) | Folio del aspirante |
| modulo | VARCHAR(50) | Módulo que generó el evento |
| accion | VARCHAR(50) | Acción realizada |
| entidad_afectada | VARCHAR(100) | Entidad afectada |
| id_entidad | UUID | ID de la entidad |
| usuario_ejecutor | VARCHAR(100) | Usuario que ejecutó |
| ip_origen | VARCHAR(45) | IP de origen |
| detalles | JSONB | Detalles del evento |
| hash_operacion | VARCHAR(100) | Hash de la operación |
| created_at | TIMESTAMP | Timestamp de creación (solo INSERT) |

**Índices:**
- idx_auditoria_folio (folio)
- idx_auditoria_modulo (modulo)
- idx_auditoria_fecha (created_at)

---

### 2.7 Módulo de Matching

#### Tabla: `resultados_matching`
Resultado de la evaluación de compatibilidad curricular con IA.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | UUID | PK |
| aspirante_id | UUID | FK lógica hacia `aspirantes(id)` |
| score | DOUBLE PRECISION | Porcentaje de compatibilidad |
| nivel_compatibilidad | VARCHAR(50) | Nivel calculado por el servicio AI |
| mensaje | TEXT | Explicación breve del resultado |
| fecha_evaluacion | TIMESTAMP | Fecha y hora de evaluación |

El resultado se genera mediante `POST /api/v1/matching/evaluar` y se persiste después de una respuesta exitosa del Backend AI.

---

## 3. Relaciones Entre Tablas

### Diagrama de Relaciones

```
aspirantes (1) ----< (N) cv_estructurados
aspirantes (1) ----< (N) documentos
aspirantes (1) ----< (1) expedientes_digitales
aspirantes (1) ----< (N) cartas_declaratorias
aspirantes (1) ----< (N) documentos_firmados
aspirantes (1) ----< (N) codigos_otp

cv_estructurados (1) ----< (N) cv_escolaridad
cv_estructurados (1) ----< (N) cv_experiencia_laboral
cv_estructurados (1) ----< (N) cv_cursos_capacitaciones
cv_estructurados (1) ----< (N) cv_habilidades_tecnicas

documentos (1) ----< (N) revisiones_manuales

cartas_declaratorias (1) ----< (N) aceptaciones_bloques
cartas_declaratorias (1) ----< (N) validaciones_externas_carta

bloques_declaratorios (1) ----< (N) aceptaciones_bloques

documentos_firmados (1) ----< (1) sellos_digitales
documentos_firmados (1) ----< (1) firmas_metadata
```

---

## 4. Comandos Útiles

### Conexión a PostgreSQL

```bash
# Inspeccionar variables de entorno del contenedor
docker inspect gestiona-t-postgres --format '{{json .Config.Env}}' | ConvertFrom-Json

# Conectarse a la base de datos
docker exec -it gestiona-t-postgres psql -U gestiona_user -d gestiona_t
```

### Consultas Comunes

```sql
-- Ver aspirantes
SELECT * FROM aspirantes WHERE correo_electronico = 'prueba@ine.mx';

-- Limpiar OTP de un aspirante
DELETE FROM codigos_otp WHERE aspirante_id = (SELECT id FROM aspirantes WHERE correo_electronico = 'prueba@ine.mx');

-- Limpiar intentos de autenticación
DELETE FROM intento_auth WHERE correo_intentado = 'prueba@ine.mx';

-- Eliminar aspirante (cascada)
DELETE FROM aspirantes WHERE correo_electronico = 'prueba@ine.mx';

-- Verificar eliminación
SELECT COUNT(*) FROM aspirantes WHERE correo_electronico = 'prueba@ine.mx';
```

---

## 5. Migraciones y Seeders

### Migraciones
- `009_audit_tables.sql` - Tablas de auditoría
- `010_audit_triggers_inmutabilidad.sql` - Triggers de inmutabilidad

### Seeders
- `001_bloques_declaratorios.sql` - 12 bloques declarativos
- `002_audit_configuracion_retencion.sql` - Configuración de retención

---

## 6. Consideraciones de Diseño

### Auditoría Inmutable
- La tabla `auditoria` es de solo INSERT (nunca UPDATE/DELETE)
- Todas las operaciones críticas generan un registro de auditoría
- Hash SHA-256 de operaciones para integridad

### Soft Delete
- Las tablas críticas usan `deleted_at` en lugar de DELETE físico
- Permite recuperación de datos y trazabilidad histórica

### Índices
- Índices en campos de búsqueda frecuente (folio, correo, curp)
- Índices compuestos para consultas complejas

### Foreign Keys
- ON DELETE CASCADE en tablas dependientes
- ON DELETE RESTRICT en tablas críticas

---

**Fin del documento Base_de_Datos.md**