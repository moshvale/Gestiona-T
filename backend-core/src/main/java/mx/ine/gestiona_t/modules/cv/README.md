# Módulo de CV Institucional (cv)

**Responsabilidad:** FLUJO 2A - Captura del Curriculum Vitae (Formato Institucional)
**Versión:** 1.0.0
**Fecha:** 08 de julio de 2026

---

## 1. Arquitectura del Módulo

### 1.1 Diagrama de Componentes

```
+------------------------------------------------------------------+
|                      CV MODULE                                     |
+------------------------------------------------------------------+
|                                                                     |
|  +------------------+      +------------------+                   |
|  | CvController     |----->| CvService        |                   |
|  | (REST API)       |      | (Business Logic) |                   |
|  +------------------+      +--------+---------+                   |
|                                     |                              |
|                    +----------------+----------------+            |
|                    |                                 |            |
|           +--------v---------+            +----------v---------+  |
|           | CvEstructurado   |            | CvNoEstructurado   |  |
|           | Service          |            | Service            |  |
|           +--------+---------+            +----------+---------+  |
|                    |                                 |            |
|           +--------v---------+            +----------v---------+  |
|           | ValidacionService|            | MapeoService       |  |
|           | (Validaciones)   |            | (Mapeo campos)     |  |
|           +------------------+            +--------------------+  |
|                                                                     |
|  +------------------+      +------------------+                   |
|  | CvRepository     |      | BackendAIClient  |                   |
|  | (JPA Repository) |      | (OCR + NLP)      |                   |
|  +------------------+      +------------------+                   |
|                                                                     |
+------------------------------------------------------------------+
                              |
                              v
                    +------------------+
                    |   PostgreSQL     |
                    |   (Database)     |
                    +------------------+
```

### 1.2 Responsabilidades

- **Captura estructurada:** Formulario basado en Formato Institucional INE
- **Captura no estructurada:** Carga de CV en PDF/Word con OCR + NLP
- **Validaciones automáticas:** Fechas coherentes, instituciones en catálogos
- **Mapeo de campos:** Correspondencia entre CV y Cédula de Puesto
- **Score de completitud:** Porcentaje de campos llenos
- **Integración con IA:** Procesamiento de CVs no estructurados

---

## 2. Logística de Datos

### 2.1 Entidades Principales

#### CvEstructurado
```java
@Entity
@Table(name = "cv_estructurados")
public class CvEstructurado {
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private UUID aspiranteId;
    
    @Column(nullable = false)
    private String folio;
    
    @Column(nullable = false)
    private int scoreCompletitud;  // 0-100
    
    @Column(nullable = false)
    private LocalDateTime fechaCaptura;
    
    @Column(nullable = false)
    private LocalDateTime fechaUltimaModificacion;
    
    @Column(nullable = false)
    private boolean completo;
    
    // Datos estructurados en JSON
    @Column(columnDefinition = "jsonb")
    private String datosEscolaridad;
    
    @Column(columnDefinition = "jsonb")
    private String datosExperiencia;
    
    @Column(columnDefinition = "jsonb")
    private String datosCursos;
    
    @Column(columnDefinition = "jsonb")
    private String datosHabilidades;
    
    @Column(columnDefinition = "jsonb")
    private String datosOtros;
    
    @Column(columnDefinition = "jsonb")
    private String datosReferencias;
}
```

#### Escolaridad
```java
@Entity
@Table(name = "cv_escolaridad")
public class Escolaridad {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID cvId;
    
    @Column(nullable = false, length = 30)
    private String nivel;  // Bachillerato, Licenciatura, Maestría, Doctorado
    
    @Column(nullable = false, length = 200)
    private String institucion;
    
    @Column(length = 100)
    private String titulo;
    
    @Column(length = 20)
    private String cedulaProfesional;
    
    @Column(nullable = false)
    private LocalDate fechaInicio;
    
    @Column
    private LocalDate fechaTermino;
    
    @Column(nullable = false, length = 20)
    private String status;  // Concluido, En curso, Trunco
    
    @Column(length = 500)
    private String documentoSoporte;  // Ruta en MinIO
}
```

#### ExperienciaLaboral
```java
@Entity
@Table(name = "cv_experiencia_laboral")
public class ExperienciaLaboral {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID cvId;
    
    @Column(nullable = false, length = 200)
    private String institucion;
    
    @Column(length = 13)
    private String rfcInstitucion;
    
    @Column(nullable = false, length = 100)
    private String puesto;
    
    @Column(nullable = false, length = 500)
    private String funciones;
    
    @Column(nullable = false)
    private LocalDate fechaInicio;
    
    @Column
    private LocalDate fechaTermino;
    
    @Column(nullable = false)
    private boolean actualmenteLaborando;
    
    @Column(nullable = false, length = 20)
    private String nivelMando;  // Operativo, Mando medio, Directivo
    
    @Column(length = 500)
    private String documentoSoporte;
}
```

#### CursoCapacitacion
```java
@Entity
@Table(name = "cv_cursos_capacitaciones")
public class CursoCapacitacion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID cvId;
    
    @Column(nullable = false, length = 200)
    private String nombreCurso;
    
    @Column(nullable = false, length = 200)
    private String institucion;
    
    @Column(nullable = false)
    private int duracionHoras;
    
    @Column(nullable = false)
    private LocalDate fechaRealizacion;
    
    @Column(length = 500)
    private String documentoSoporte;
}
```

#### HabilidadTecnica
```java
@Entity
@Table(name = "cv_habilidades_tecnicas")
public class HabilidadTecnica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID cvId;
    
    @Column(nullable = false, length = 100)
    private String tipo;  // Idioma, Software, Conocimiento, Certificacion
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(nullable = false, length = 20)
    private String nivel;  // Basico, Intermedio, Avanzado, Nativo
    
    @Column
    private LocalDate fechaCertificacion;
    
    @Column
    private LocalDate fechaVencimiento;
}
```

### 2.2 Relaciones

```
Aspirante (1) ----< (1) CvEstructurado
CvEstructurado (1) ----< (N) Escolaridad
CvEstructurado (1) ----< (N) ExperienciaLaboral
CvEstructurado (1) ----< (N) CursoCapacitacion
CvEstructurado (1) ----< (N) HabilidadTecnica
```

### 2.3 Flujos de Información

#### Flujo de Captura Estructurada
```
1. Frontend presenta formulario basado en Formato Institucional INE
2. Aspirante llena secciones: Escolaridad, Experiencia, Cursos, Habilidades
3. Backend valida en tiempo real:
   - Fechas coherentes (inicio < término)
   - Instituciones en catálogos oficiales
   - No duplicados de cédula profesional
4. Backend calcula score de completitud
5. Backend guarda en PostgreSQL como JSON estructurado
6. Backend notifica al frontend con score y estatus
```

#### Flujo de Captura No Estructurada
```
1. Frontend permite carga de CV en PDF/Word
2. Backend sube archivo a MinIO
3. Backend invoca Backend-AI para OCR + NLP
4. Backend-AI extrae texto y estructura información
5. Backend mapea información extraída a estructura interna
6. Si mapeo >= 70%, presenta al aspirante para validación
7. Aspirante corrige/completa campos faltantes
8. Backend guarda como CvEstructurado
```

---

## 3. Detalles Técnicos

### 3.1 Dependencias

```xml
<!-- pom.xml (adicional al módulo auth) -->
<dependencies>
    <!-- JSON processing -->
    <dependency>
        <groupId>com.vladmihalcea</groupId>
        <artifactId>hibernate-types-60</artifactId>
        <version>2.21.1</version>
    </dependency>
    
    <!-- File upload -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- MinIO client -->
    <dependency>
        <groupId>io.minio</groupId>
        <artifactId>minio</artifactId>
        <version>8.5.7</version>
    </dependency>
</dependencies>
```

### 3.2 Variables de Entorno Requeridas

```bash
# Backend AI Service
AI_SERVICE_URL=http://localhost:8007

# MinIO Storage
MINIO_ENDPOINT=http://localhost:9007
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET_CV=cv-documentos

# Validaciones
CV_MAX_FILE_SIZE_MB=10
CV_ALLOWED_TYPES=application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document
CV_MIN_COMPLETUD_SCORE=50
```

### 3.3 Endpoints REST

#### CV Estructurado
```
GET /api/v1/cv/{folio}
Response: CvCompletoResponse

POST /api/v1/cv
Body: CvCreateRequest
Response: CvResponse

PUT /api/v1/cv/{folio}
Body: CvUpdateRequest
Response: CvResponse

DELETE /api/v1/cv/{folio}
Response: MensajeResponse
```

#### Secciones del CV
```
POST /api/v1/cv/{folio}/escolaridad
Body: EscolaridadRequest
Response: EscolaridadResponse

PUT /api/v1/cv/{folio}/escolaridad/{id}
Body: EscolaridadRequest
Response: EscolaridadResponse

DELETE /api/v1/cv/{folio}/escolaridad/{id}
Response: MensajeResponse

POST /api/v1/cv/{folio}/experiencia
Body: ExperienciaRequest
Response: ExperienciaResponse

PUT /api/v1/cv/{folio}/experiencia/{id}
Body: ExperienciaRequest
Response: ExperienciaResponse

DELETE /api/v1/cv/{folio}/experiencia/{id}
Response: MensajeResponse

POST /api/v1/cv/{folio}/cursos
Body: CursoRequest
Response: CursoResponse

PUT /api/v1/cv/{folio}/cursos/{id}
Body: CursoRequest
Response: CursoResponse

DELETE /api/v1/cv/{folio}/cursos/{id}
Response: MensajeResponse

POST /api/v1/cv/{folio}/habilidades
Body: HabilidadRequest
Response: HabilidadResponse

PUT /api/v1/cv/{folio}/habilidades/{id}
Body: HabilidadRequest
Response: HabilidadResponse

DELETE /api/v1/cv/{folio}/habilidades/{id}
Response: MensajeResponse
```

#### CV No Estructurado
```
POST /api/v1/cv/upload
Content-Type: multipart/form-data
Body: file (PDF/Word)
Response: CvUploadResponse

POST /api/v1/cv/{folio}/procesar-no-estructurado
Response: CvProcesadoResponse
```

#### Validaciones
```
GET /api/v1/cv/{folio}/validar
Response: ValidacionCvResponse

GET /api/v1/cv/{folio}/score-completitud
Response: ScoreCompletitudResponse
```

### 3.4 Estructura de Paquetes

```
mx.ine.gestiona_t.modules.cv/
+-- controller/
|   +-- CvController.java
|   +-- CvSeccionesController.java
|   +-- CvUploadController.java
+-- service/
|   +-- CvService.java
|   +-- CvServiceImpl.java
|   +-- CvEstructuradoService.java
|   +-- CvNoEstructuradoService.java
|   +-- ValidacionCvService.java
|   +-- MapeoCvService.java
|   +-- MinioService.java
+-- repository/
|   +-- CvEstructuradoRepository.java
|   +-- EscolaridadRepository.java
|   +-- ExperienciaLaboralRepository.java
|   +-- CursoCapacitacionRepository.java
|   +-- HabilidadTecnicaRepository.java
+-- dto/
|   +-- request/
|   |   +-- CvCreateRequest.java
|   |   +-- CvUpdateRequest.java
|   |   +-- EscolaridadRequest.java
|   |   +-- ExperienciaRequest.java
|   |   +-- CursoRequest.java
|   |   +-- HabilidadRequest.java
|   +-- response/
|       +-- CvResponse.java
|       +-- CvCompletoResponse.java
|       +-- EscolaridadResponse.java
|       +-- ExperienciaResponse.java
|       +-- CursoResponse.java
|       +-- HabilidadResponse.java
|       +-- ValidacionCvResponse.java
|       +-- ScoreCompletitudResponse.java
+-- model/
|   +-- CvEstructurado.java
|   +-- Escolaridad.java
|   +-- ExperienciaLaboral.java
|   +-- CursoCapacitacion.java
|   +-- HabilidadTecnica.java
|   +-- enums/
|       +-- NivelEstudio.java
|       +-- StatusEstudio.java
|       +-- NivelMando.java
|       +-- TipoHabilidad.java
|       +-- NivelHabilidad.java
+-- integration/
    +-- BackendAIClient.java
    +-- dto/
        +-- OcrRequest.java
        +-- OcrResponse.java
        +-- MapeoResponse.java
```

---

## 4. Guía de Mantenimiento

### 4.1 Escalabilidad

- **Horizontal:** El módulo es stateless, puede escalarse horizontalmente
- **Base de datos:** Usar índices en campos de búsqueda frecuente (aspiranteId, folio)
- **MinIO:** Configurar lifecycle policies para limpieza de archivos temporales
- **Cache:** Implementar Redis para cache de catálogos de instituciones

### 4.2 Monitoreo

- **Métricas clave:**
  - Tiempo de procesamiento de CVs no estructurados
  - Score promedio de completitud
  - Tasa de éxito de OCR
  - Número de CVs capturados por día

- **Alertas:**
  - Backend-AI no responde (timeout > 30s)
  - MinIO storage > 80% capacidad
  - Tasa de error de OCR > 20%

### 4.3 Debugging

- **Problemas comunes:**
  - **CV no se guarda:** Verificar permisos de escritura en PostgreSQL
  - **OCR falla:** Revisar logs de Backend-AI y calidad del archivo
  - **Validación falla:** Verificar catálogos de instituciones actualizados
  - **Score bajo:** Revisar campos obligatorios no llenos

### 4.4 Backup y Recuperación

- **Base de datos:** Backup diario de PostgreSQL
- **MinIO:** Replicación de buckets a storage secundario
- **Recuperación:** En caso de pérdida, restaurar desde backup más reciente

---

## 5. Consideraciones de Seguridad

### 5.1 Protección de Datos

- **Datos sensibles:** Cédulas profesionales, RFCs cifrados en base de datos
- **Documentos:** Cifrado en MinIO con claves KMS
- **Acceso:** Solo el aspirante propietario puede ver/editar su CV
- **Auditoría:** Registro de todas las modificaciones al CV

### 5.2 Validaciones

- **Inyección SQL:** Usar JPA con parámetros vinculados
- **XSS:** Sanitizar entradas de texto en frontend y backend
- **File upload:** Validar tipo MIME, tamaño máximo, escanear malware
- **Autorización:** Verificar que el aspirante solo acceda a su propio CV

### 5.3 Cumplimiento Normativo

- **LGDPPP:** Datos personales cifrados, aviso de privacidad aceptado
- **Formato Institucional:** Alineado con lineamientos del INE y SFP
- **Retención:** Conforme al Catálogo de Disposición Documental del INE

---

## 6. Testing

### 6.1 Tests Unitarios

- **Cobertura mínima:** 80%
- **Casos a cubrir:**
  - Creación de CV estructurado completo
  - Creación de CV estructurado incompleto
  - Actualización de secciones del CV
  - Validación de fechas coherentes
  - Validación de instituciones en catálogos
  - Cálculo de score de completitud
  - Procesamiento de CV no estructurado (mock Backend-AI)

### 6.2 Tests de Integración

- **Base de datos:** H2 en memoria
- **Backend-AI:** Mock con WireMock
- **MinIO:** Mock o contenedor de prueba
- **Casos a cubrir:**
  - Flujo completo de captura estructurada
  - Flujo completo de carga y procesamiento de CV no estructurado
  - Validaciones automáticas
  - Integración con MinIO

---

## 7. Despliegue

### 7.1 Configuración por Entorno

- **Desarrollo:** Backend-AI local, MinIO local
- **Staging:** Backend-AI dedicado, MinIO con replicación
- **Producción:** Backend-AI con alta disponibilidad, MinIO distribuido

### 7.2 Variables de Entorno Críticas

```bash
AI_SERVICE_URL=<url_produccion_backend_ai>
MINIO_ENDPOINT=<url_produccion_minio>
MINIO_ACCESS_KEY=<credencial_oficial>
MINIO_SECRET_KEY=<credencial_oficial>
```

---

## 8. Roadmap

### Versión 1.0 (Actual)
- [x] Captura estructurada con Formato Institucional
- [x] Captura no estructurada con OCR + NLP
- [x] Validaciones automáticas
- [x] Score de completitud
- [x] CRUD completo de secciones

### Versión 1.1 (Próxima)
- [ ] Importación de CV desde LinkedIn
- [ ] Sugerencias automáticas basadas en IA
- [ ] Plantillas de CV por tipo de puesto
- [ ] Exportación a PDF con formato INE

### Versión 2.0 (Futuro)
- [ ] Bolsa de trabajo automática
- [ ] Matching proactivo con convocatorias
- [ ] Análisis de brechas de competencias
- [ ] Recomendaciones de capacitación

---
**Fin del documento README.md del Módulo de CV Institucional**