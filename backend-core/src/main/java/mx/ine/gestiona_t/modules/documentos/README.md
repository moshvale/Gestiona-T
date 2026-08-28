# Módulo de Validación Documental (documentos)

**Responsabilidad:** FLUJO 3 - Carga y Validación Multinivel de Documentos
**Versión:** 1.0.0
**Fecha:** 09 de julio de 2026

---

## 1. Arquitectura del Módulo

### 1.1 Diagrama de Componentes

```
+------------------------------------------------------------------+
|                   DOCUMENTOS MODULE                                |
+------------------------------------------------------------------+
|                                                                     |
|  +------------------+      +------------------+                   |
|  | DocumentoController|---->| DocumentoService |                   |
|  | (REST API)       |      | (Business Logic) |                   |
|  +------------------+      +--------+---------+                   |
|                                     |                              |
|                    +----------------+----------------+            |
|                    |                |                |            |
|           +--------v------+  +------v-------+  +----v--------+  |
|           | ValidacionTipoA|  | ValidacionTipoB| | ValidacionTipoC|
|           | (APIs directas)|  | (Asistida IA)  | | (Manual)      |
|           +--------+------+  +------+---------+  +----+--------+  |
|                    |                |                |            |
|           +--------v------+  +------v-------+  +----v--------+  |
|           | APIs Externas  |  | Backend-AI   |  | Cola Revision |  |
|           | RNP,RENAPO,SAT |  | OCR+Autenticidad| | (Analistas)   |  |
|           | SFP,IMSS       |  |              |  |               |  |
|           +----------------+  +--------------+  +---------------+  |
|                                                                     |
|  +------------------+      +------------------+                   |
|  | DocumentoRepo    |      | CatalogoService  |                   |
|  | (JPA Repository) |      | (Catálogos Maestros)|                |
|  +------------------+      +------------------+                   |
|                                                                     |
+------------------------------------------------------------------+
                              |
                              v
              +---------------+---------------+
              |                               |
    +---------v---------+          +----------v---------+
    |   PostgreSQL      |          |      MinIO         |
    | (documentos_meta) |          | (archivos físicos) |
    +-------------------+          +--------------------+
```

### 1.2 Responsabilidades

- **Clasificación automática:** Determinar tipo de documento (A, B o C)
- **Validación Tipo A:** APIs directas (RNP, RENAPO, SAT, SFP)
- **Validación Tipo B:** Asistida por IA (OCR + análisis de autenticidad)
- **Validación Tipo C:** Manual con trazabilidad reforzada
- **Gestión de catálogos:** Sincronización con fuentes oficiales
- **Expediente digital:** Consolidación del estatus de todos los documentos
- **Consulta transversal SFP:** Verificación obligatoria de inhabilitaciones

### 1.3 Clasificación de Documentos

| Tipo | Documentos | Método de Validación |
|------|------------|----------------------|
| **A** | Cédula profesional, CURP, RFC, SFP | API directa (100% automatizada) |
| **B** | Bachillerato, certificaciones, constancias laborales, actas de nacimiento, IDs oficiales, comprobantes de domicilio | OCR + IA + cotejo institucional |
| **C** | Documentos históricos, extranjeros, casos excepcionales | Revisión manual con trazabilidad |

---

## 2. Logística de Datos

### 2.1 Entidades Principales

#### Documento
```java
@Entity
@Table(name = "documentos")
public class Documento {
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private UUID aspiranteId;
    
    @Column(nullable = false, length = 36)
    private String folio;
    
    @Column(nullable = false, length = 50)
    private String tipoDocumento;  // CEDULA_PROFESIONAL, BACHILLERATO, etc.
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoValidacion tipoValidacion;  // A, B, C
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstatusDocumento estatus;
    
    @Column(length = 500)
    private String storagePath;  // Ruta en MinIO
    
    @Column(length = 500)
    private String textoExtraido;  // Resultado OCR (cifrado)
    
    @Column
    private Double scoreAutenticidad;  // 0-100
    
    @Column(length = 1000)
    private String motivoRechazo;
    
    @Column
    private UUID analistaId;  // Si fue revisión manual
    
    @Column
    private LocalDateTime fechaValidacion;
    
    @Column(columnDefinition = "jsonb")
    private String metadataValidacion;  // Detalles del proceso de validación
}
```

#### ExpedienteDigital
```java
@Entity
@Table(name = "expedientes_digitales")
public class ExpedienteDigital {
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private UUID aspiranteId;
    
    @Column(nullable = false, length = 36)
    private String folio;
    
    @Column(nullable = false)
    private int documentosTotales;
    
    @Column(nullable = false)
    private int documentosValidados;
    
    @Column(nullable = false)
    private int documentosRechazados;
    
    @Column(nullable = false)
    private int documentosEnRevision;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstatusExpediente estatusGeneral;
    
    @Column(nullable = false)
    private boolean sfpVerificado;
    
    @Column
    private LocalDateTime fechaUltimaActualizacion;
}
```

#### CatalogoInstitucion
```java
@Entity
@Table(name = "catalogo_instituciones")
public class CatalogoInstitucion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 10)
    private String tipo;  // EMS, ES, CERTIFICADORA
    
    @Column(nullable = false, length = 300)
    private String nombre;
    
    @Column(length = 20)
    private String clave;
    
    @Column(length = 100)
    private String entidadFederativa;
    
    @Column(nullable = false)
    private boolean acreditada;
    
    @Column
    private LocalDate fechaActualizacion;
    
    @Column(length = 50)
    private String fuenteOficial;  // SEP, DGETI, CONOCER, etc.
}
```

#### RevisionManual
```java
@Entity
@Table(name = "revisiones_manuales")
public class RevisionManual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID documentoId;
    
    @Column(nullable = false)
    private UUID analistaId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstatusRevision estatus;
    
    @Column(length = 2000)
    private String dictamen;
    
    @Column(length = 1000)
    private String motivo;
    
    @Column
    private LocalDateTime fechaAsignacion;
    
    @Column
    private LocalDateTime fechaDictamen;
}
```

### 2.2 Relaciones

```
Aspirante (1) ----< (N) Documento
Aspirante (1) ----< (1) ExpedienteDigital
Documento (1) ----< (1) RevisionManual
CatalogoInstitucion (independiente)
```

### 2.3 Flujos de Información

#### Flujo Principal de Validación
```
1. Aspirante carga documento (PDF/imagen)
2. Backend sube archivo a MinIO (cifrado)
3. Backend clasifica tipo de documento (IA)
4. Backend ejecuta OCR para extraer texto
5. SEGÚN tipo de validación:
   - TIPO A: Invoca API externa correspondiente
   - TIPO B: Ejecuta análisis de autenticidad + cotejo catálogos
   - TIPO C: Encola para revisión manual
6. Backend registra resultado en Documento
7. Backend actualiza ExpedienteDigital
8. Backend notifica al aspirante del estatus
9. Consulta transversal SFP (obligatoria)
```

---

## 3. Detalles Técnicos

### 3.1 Dependencias Adicionales

```xml
<!-- Procesamiento de imágenes -->
<dependency>
    <groupId>net.sourceforge.tess4j</groupId>
    <artifactId>tess4j</artifactId>
    <version>5.8.0</version>
</dependency>

<!-- PDF processing -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.1</version>
</dependency>

<!-- Async processing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
```

### 3.2 Variables de Entorno Requeridas

```bash
# APIs Externas
RNP_API_URL=https://api.rnp.sep.gob.mx/v1
RNP_API_KEY=CHANGE_ME

SAT_API_URL=https://api.sat.gob.mx/v1
SAT_API_KEY=CHANGE_ME

SFP_API_URL=https://api.sfp.gob.mx/v1
SFP_API_KEY=CHANGE_ME

IMSS_API_URL=https://api.imss.gob.mx/v1
IMSS_API_KEY=CHANGE_ME

# Backend AI Service
AI_SERVICE_URL=http://localhost:8007

# MinIO Storage
MINIO_ENDPOINT=http://localhost:9007
MINIO_BUCKET_DOCUMENTOS=documentos

# Validación
DOCUMENTO_MAX_SIZE_MB=20
DOCUMENTO_ALLOWED_TYPES=application/pdf,image/jpeg,image/png
OCR_CONFIDENCE_THRESHOLD=0.75
AUTENTICIDAD_THRESHOLD=85
```

### 3.3 Endpoints REST

#### Gestión de Documentos
```
POST   /api/v1/documentos/upload
GET    /api/v1/documentos/{folio}
GET    /api/v1/documentos/aspirante/{aspiranteId}
DELETE /api/v1/documentos/{id}
GET    /api/v1/documentos/{id}/download
```

#### Validación
```
POST   /api/v1/documentos/{id}/validar
POST   /api/v1/documentos/validar-todos/{folio}
GET    /api/v1/documentos/{id}/estatus
```

#### Expediente Digital
```
GET    /api/v1/expediente/{folio}
GET    /api/v1/expediente/{folio}/resumen
```

#### Revisiones Manuales (Admin)
```
GET    /api/v1/admin/revisiones/pendientes
POST   /api/v1/admin/revisiones/{id}/asignar
POST   /api/v1/admin/revisiones/{id}/dictaminar
```

#### Catálogos
```
GET    /api/v1/catalogos/instituciones
POST   /api/v1/admin/catalogos/sincronizar
GET    /api/v1/catalogos/tipos-documento
```

### 3.4 Estructura de Paquetes

```
mx.ine.gestiona_t.modules.documentos/
+-- controller/
|   +-- DocumentoController.java
|   +-- ExpedienteController.java
|   +-- RevisionManualController.java
|   +-- CatalogoController.java
+-- service/
|   +-- DocumentoService.java
|   +-- DocumentoServiceImpl.java
|   +-- ValidacionTipoAService.java
|   +-- ValidacionTipoBService.java
|   +-- ValidacionTipoCService.java
|   +-- ClasificadorDocumentoService.java
|   +-- ExpedienteService.java
|   +-- CatalogoService.java
|   +-- MinioDocumentosService.java
+-- repository/
|   +-- DocumentoRepository.java
|   +-- ExpedienteDigitalRepository.java
|   +-- CatalogoInstitucionRepository.java
|   +-- RevisionManualRepository.java
+-- dto/
|   +-- request/
|   |   +-- UploadDocumentoRequest.java
|   |   +-- DictamenRevisionRequest.java
|   +-- response/
|       +-- DocumentoResponse.java
|       +-- ExpedienteResponse.java
|       +-- ValidacionResponse.java
|       +-- RevisionResponse.java
|       +-- CatalogoResponse.java
+-- model/
|   +-- Documento.java
|   +-- ExpedienteDigital.java
|   +-- CatalogoInstitucion.java
|   +-- RevisionManual.java
|   +-- enums/
|       +-- TipoDocumento.java
|       +-- TipoValidacion.java
|       +-- EstatusDocumento.java
|       +-- EstatusExpediente.java
|       +-- EstatusRevision.java
+-- integration/
    +-- RnpClient.java
    +-- SatClient.java
    +-- SfpClient.java
    +-- ImssClient.java
    +-- BackendAIClient.java
    +-- dto/
        +-- RnpResponse.java
        +-- SatResponse.java
        +-- SfpResponse.java
        +-- ImssResponse.java
        +-- OcrResponse.java
        +-- AutenticidadResponse.java
```

---

## 4. Guía de Mantenimiento

### 4.1 Escalabilidad

- **Procesamiento asíncrono:** Usar colas (Quartz o RabbitMQ) para validaciones pesadas
- **Horizontal:** El módulo es stateless, puede escalarse horizontalmente
- **Base de datos:** Índices en folio, aspiranteId, estatus
- **MinIO:** Configurar lifecycle policies para archivos temporales
- **Cache:** Redis para catálogos maestros (actualización cada 24h)

### 4.2 Monitoreo

- **Métricas clave:**
  - Tiempo promedio de validación por tipo (A, B, C)
  - Tasa de éxito de APIs externas
  - Score promedio de autenticidad visual
  - Documentos pendientes de revisión manual
  - Tiempo de respuesta de analistas

- **Alertas:**
  - API externa caída (timeout > 10s)
  - Cola de revisión manual > 100 documentos
  - Score de autenticidad < 60 (posible fraude)
  - SFP reporta inhabilitación

### 4.3 Debugging

- **Problemas comunes:**
  - **OCR falla:** Verificar calidad de imagen, logs de Backend-AI
  - **API externa no responde:** Revisar credenciales, conectividad, rate limits
  - **Documento no se clasifica:** Revisar modelo de clasificación en Backend-AI
  - **Falsos positivos en autenticidad:** Ajustar umbrales, revisar modelo ML

### 4.4 Backup y Recuperación

- **Base de datos:** Backup diario de PostgreSQL
- **MinIO:** Replicación cross-region de documentos
- **Catálogos:** Versionado y rollback capability
- **Recuperación:** En caso de pérdida, restaurar desde backup + re-validar

---

## 5. Consideraciones de Seguridad

### 5.1 Protección de Datos

- **Documentos:** Cifrado AES-256 en MinIO con claves KMS
- **Texto OCR:** Cifrado en base de datos
- **Acceso:** Solo el aspirante propietario y analistas autorizados
- **Descargas:** URLs firmadas con expiración (15 minutos)
- **Auditoría:** Registro de todas las descargas y visualizaciones

### 5.2 Validaciones

- **File upload:** Validar tipo MIME real (no solo extensión), escanear malware
- **Tamaño máximo:** 20MB por documento
- **Rate limiting:** Máximo 10 uploads por minuto por aspirante
- **Inyección:** Sanitizar nombres de archivo, validar metadata

### 5.3 Cumplimiento Normativo

- **LGDPPP:** Datos personales cifrados, aviso de privacidad
- **Catálogo de Disposición Documental:** Retención conforme a normatividad
- **SFP:** Consulta obligatoria antes de cualquier contratación
- **Trazabilidad:** Inmutable, con hash SHA-256 de cada operación

---

## 6. Testing

### 6.1 Tests Unitarios

- **Cobertura mínima:** 80%
- **Casos a cubrir:**
  - Clasificación correcta de documentos
  - Validación Tipo A exitosa y fallida
  - Validación Tipo B con diferentes scores
  - Validación Tipo C con asignación y dictamen
  - Cálculo de estatus de expediente
  - Consulta SFP con y sin inhabilitaciones

### 6.2 Tests de Integración

- **APIs externas:** Mock con WireMock
- **Backend-AI:** Mock con respuestas predefinidas
- **MinIO:** Contenedor de prueba
- **Casos a cubrir:**
  - Flujo completo de carga y validación
  - Escenarios de fallback (API caída -> revisión manual)
  - Sincronización de catálogos

### 6.3 Tests de Seguridad

- **Penetration testing:** OWASP ZAP
- **Casos a cubrir:**
  - Upload de archivos maliciosos
  - Acceso no autorizado a documentos de otros aspirantes
  - Manipulación de metadata
  - Ataques de denegación de servicio

---

## 7. Despliegue

### 7.1 Configuración por Entorno

- **Desarrollo:** APIs externas en sandbox, Backend-AI local
- **Staging:** APIs externas en test, Backend-AI dedicado
- **Producción:** APIs externas oficiales, Backend-AI con HA

### 7.2 Variables de Entorno Críticas

```bash
# NUNCA commitear estos valores
RNP_API_KEY=<credencial_oficial>
SAT_API_KEY=<credencial_oficial>
SFP_API_KEY=<credencial_oficial>
IMSS_API_KEY=<credencial_oficial>
MINIO_SECRET_KEY=<credencial_oficial>
```

---

## 8. Roadmap

### Versión 1.0 (Actual)
- [x] Clasificación automática de documentos
- [x] Validación Tipo A (APIs directas)
- [x] Validación Tipo B (IA asistida)
- [x] Validación Tipo C (manual con trazabilidad)
- [x] Expediente digital consolidado
- [x] Consulta transversal SFP

### Versión 1.1 (Próxima)
- [ ] Procesamiento asíncrono con colas
- [ ] Notificaciones en tiempo real (WebSocket)
- [ ] Dashboard de analistas con métricas
- [ ] Exportación de expediente en PDF

### Versión 2.0 (Futuro)
- [ ] Blockchain para inmutabilidad de documentos críticos
- [ ] Integración con más APIs estatales (Registro Civil)
- [ ] Detección de fraude con ML avanzado
- [ ] Automatización completa de validaciones Tipo B

---
**Fin del documento README.md del Módulo de Validación Documental**