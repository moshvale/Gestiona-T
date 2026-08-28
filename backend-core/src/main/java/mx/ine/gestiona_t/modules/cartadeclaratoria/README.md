# Modulo de Carta Declaratoria (carta-declaratoria)

**Responsabilidad:** FLUJO 5A - Carta Declaratoria bajo Protesta de Decir Verdad
**Version:** 1.0.0
**Fecha:** 09 de julio de 2026

---

## 1. Arquitectura del Modulo

### 1.1 Diagrama de Componentes

```
+------------------------------------------------------------------+
|              CARTA-DECLARATORIA MODULE                             |
+------------------------------------------------------------------+
|                                                                     |
|  +------------------+      +------------------+                   |
|  | CartaController  |----->| CartaService     |                   |
|  | (REST API)       |      | (Business Logic) |                   |
|  +------------------+      +--------+---------+                   |
|                                     |                              |
|                    +----------------+----------------+            |
|                    |                |                |            |
|           +--------v------+  +------v-------+  +----v--------+  |
|           | BloquesService |  | PlantillaService| | ValidacionService|
|           | (12 bloques)   |  | (Generacion PDF)| | (RENADEA, etc.)|
|           +----------------+  +--------------+  +---------------+  |
|                    |                |                |            |
|           +--------v------+  +------v-------+  +----v--------+  |
|           | FirmaService   |  | MinioService |  | RenadeaClient|  |
|           | (3 niveles)    |  | (WORM)       |  | (APIs ext.)  |  |
|           +----------------+  +--------------+  +---------------+  |
|                                                                     |
+------------------------------------------------------------------+
                              |
                              v
              +---------------+---------------+
              |                               |
    +---------v---------+          +----------v---------+
    |   PostgreSQL      |          |      MinIO         |
    | (carta, bloques)  |          | (PDF firmado WORM) |
    +-------------------+          +--------------------+
```

### 1.2 Responsabilidades

- **Gestion de los 12 bloques declarativos:** Cada bloque con su fundamento legal
- **Generacion de PDF:** Documento con datos pre-llenados y timestamps
- **Aceptacion expresa:** Checkbox individual por bloque con trazabilidad
- **Validacion externa:** Consulta a RENADEA y sistemas de violencia
- **Firma electronica:** Integracion con el servicio de firma (3 niveles)
- **Almacenamiento inmutable:** PDF firmado en MinIO con WORM
- **Accesibilidad total:** TTS, LSM, lenguas indigenas, lectura facil

### 1.3 Los 12 Bloques Declarativos

| # | Bloque | Fundamento Legal |
|---|--------|------------------|
| 1 | Veracidad Documental | Art. 183 Codigo Penal Federal |
| 2 | No Inhabilitacion Administrativa | LGRA Art. 7, 19, 38 |
| 3 | Antecedentes Penales | Codigo Penal Federal |
| 4 | Obligaciones Fiscales | Codigo Fiscal de la Federacion |
| 5 | Prevencion de Violencia contra Mujeres | Politica de Igualdad INE |
| 6 | Conflicto de Interes | Lineamientos INE |
| 7 | Afiliacion Politica (SPEN) | LGIPE Art. 44 |
| 8 | No Violencia Laboral | Ley Federal del Trabajo |
| 9 | Compromiso Etico | Codigo de Etica Electoral |
| 10 | Proteccion de Datos Personales | LGDPPP |
| 11 | Declaracion Patrimonial | LGRA |
| 12 | Conocimiento de Consecuencias Legales | LGRA + Codigo Penal |

---

## 2. Logistica de Datos

### 2.1 Entidades Principales

#### CartaDeclaratoria
```java
@Entity
@Table(name = "cartas_declaratorias")
public class CartaDeclaratoria {
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private UUID aspiranteId;
    
    @Column(nullable = false, length = 36)
    private String folio;
    
    @Column(nullable = false, length = 36)
    private String folioCarta;  // UUID unico del documento
    
    @Column(nullable = false, length = 20)
    private String version;  // Version de la plantilla
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstatusCarta estatus;
    
    @Column(length = 500)
    private String pdfStoragePath;  // Ruta en MinIO
    
    @Column(length = 100)
    private String pdfHash;  // SHA-256 del PDF
    
    @Column
    private String firmaDigitalHash;  // Hash de la firma
    
    @Column(length = 30)
    private String metodoFirma;  // FEA, BIOMETRICA, OTP_DOBLE
    
    @Column
    private LocalDateTime fechaAceptacion;
    
    @Column
    private LocalDateTime fechaFirma;
    
    @Column(columnDefinition = "jsonb")
    private String metadataSesion;  // IP, user-agent, etc.
}
```

#### BloqueDeclaratorio
```java
@Entity
@Table(name = "bloques_declaratorios")
public class BloqueDeclaratorio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;  // 1-12
    
    @Column(nullable = false, length = 100)
    private String titulo;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;
    
    @Column(nullable = false, length = 200)
    private String fundamentoLegal;
    
    @Column(nullable = false)
    private boolean obligatorio;
    
    @Column
    private Integer orden;
}
```

#### AceptacionBloque
```java
@Entity
@Table(name = "aceptaciones_bloques")
public class AceptacionBloque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID cartaId;
    
    @Column(nullable = false)
    private Integer bloqueId;  // 1-12
    
    @Column(nullable = false)
    private boolean aceptado;
    
    @Column(nullable = false)
    private LocalDateTime timestampAceptacion;
    
    @Column(nullable = false, length = 45)
    private String ipOrigen;
    
    @Column(length = 500)
    private String userAgent;
    
    @Column(length = 100)
    private String hashTextoBloque;  // SHA-256 del texto aceptado
}
```

#### ValidacionExterna
```java
@Entity
@Table(name = "validaciones_externas_carta")
public class ValidacionExterna {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID cartaId;
    
    @Column(nullable = false, length = 30)
    private String tipoValidacion;  // RENADEA, VIOLENCIA, SFP
    
    @Column(nullable = false)
    private boolean resultado;  // true = sin inhabilitacion
    
    @Column(columnDefinition = "TEXT")
    private String respuestaApi;
    
    @Column
    private LocalDateTime fechaConsulta;
}
```

### 2.2 Relaciones

```
Aspirante (1) ----< (N) CartaDeclaratoria
CartaDeclaratoria (1) ----< (12) AceptacionBloque
CartaDeclaratoria (1) ----< (N) ValidacionExterna
BloqueDeclaratorio (independiente, catalogo)
```

### 2.3 Flujos de Informacion

#### Flujo Principal
```
1. Aspirante llega a etapa de carta declaratoria
2. Backend carga plantilla oficial con datos pre-llenados
3. Frontend presenta los 12 bloques con scroll obligatorio
4. Aspirante acepta cada bloque individualmente (checkbox)
5. Backend registra cada aceptacion con timestamp, IP, user-agent
6. Backend ejecuta validaciones externas (RENADEA, violencia)
7. Si validaciones OK, genera PDF con todos los datos
8. Aspirante firma el PDF (FEA, biometrica o OTP)
9. Backend almacena PDF firmado en MinIO (WORM)
10. Backend registra hash en tabla inmutable
11. Backend actualiza estatus a CARTA_FIRMADA
12. Notificacion a UR y RH
```

---

## 3. Detalles Tecnicos

### 3.1 Dependencias Adicionales

```xml
<!-- Generacion de PDF -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>8.0.2</version>
    <type>pom</type>
</dependency>

<!-- Templates -->
<dependency>
    <groupId>org.thymeleaf</groupId>
    <artifactId>thymeleaf</artifactId>
</dependency>
```

### 3.2 Variables de Entorno Requeridas

```bash
# Plantillas
CARTA_VERSION=1.0.0
CARTA_PLANTILLA_PATH=/templates/carta-declaratoria-v1.html

# Validaciones externas
RENADEA_API_URL=https://api.consejojudicial.gob.mx/renadea/v1
RENADEA_API_KEY=CHANGE_ME

VIOLENCIA_API_URL=https://api.segob.gob.mx/violencia/v1
VIOLENCIA_API_KEY=CHANGE_ME

# MinIO
MINIO_BUCKET_CARTAS=cartas-declaratorias
MINIO_WORM_ENABLED=true

# Accesibilidad
TTS_ENABLED=true
LSM_VIDEO_URL=https://ine.mx/videos/lsm/carta-declaratoria.mp4
```

### 3.3 Endpoints REST

#### Gestion de Carta
```
GET    /api/v1/carta-declaratoria/{folio}
POST   /api/v1/carta-declaratoria/{folio}/iniciar
GET    /api/v1/carta-declaratoria/{folio}/bloques
POST   /api/v1/carta-declaratoria/{folio}/aceptar-bloque
POST   /api/v1/carta-declaratoria/{folio}/aceptar-todos
GET    /api/v1/carta-declaratoria/{folio}/estatus
```

#### Firma
```
POST   /api/v1/carta-declaratoria/{folio}/firmar
GET    /api/v1/carta-declaratoria/{folio}/pdf
GET    /api/v1/carta-declaratoria/{folio}/pdf/download
```

#### Validaciones
```
POST   /api/v1/carta-declaratoria/{folio}/validar-externo
GET    /api/v1/carta-declaratoria/{folio}/validaciones
```

#### Admin
```
GET    /api/v1/admin/cartas-declaratorias
GET    /api/v1/admin/cartas-declaratorias/{folio}/detalle
```

### 3.4 Estructura de Paquetes

```
mx.ine.gestiona_t.modules.carta-declaratoria/
+-- controller/
|   +-- CartaDeclaratoriaController.java
|   +-- BloquesController.java
|   +-- AdminCartaController.java
+-- service/
|   +-- CartaDeclaratoriaService.java
|   +-- CartaDeclaratoriaServiceImpl.java
|   +-- BloquesService.java
|   +-- PlantillaService.java
|   +-- ValidacionExternaService.java
|   +-- PdfGenerationService.java
+-- repository/
|   +-- CartaDeclaratoriaRepository.java
|   +-- BloqueDeclaratorioRepository.java
|   +-- AceptacionBloqueRepository.java
|   +-- ValidacionExternaRepository.java
+-- dto/
|   +-- request/
|   |   +-- AceptarBloqueRequest.java
|   |   +-- FirmarCartaRequest.java
|   +-- response/
|       +-- CartaDeclaratoriaResponse.java
|       +-- BloqueResponse.java
|       +-- AceptacionResponse.java
|       +-- EstatusCartaResponse.java
|       +-- ValidacionExternaResponse.java
+-- model/
|   +-- CartaDeclaratoria.java
|   +-- BloqueDeclaratorio.java
|   +-- AceptacionBloque.java
|   +-- ValidacionExterna.java
|   +-- enums/
|       +-- EstatusCarta.java
|       +-- TipoValidacionExterna.java
|       +-- MetodoFirmaCarta.java
+-- integration/
|   +-- RenadeaClient.java
|   +-- ViolenciaClient.java
|   +-- dto/
|       +-- RenadeaResponse.java
|       +-- ViolenciaResponse.java
+-- plantillas/
    +-- carta-declaratoria-v1.html
    +-- carta-declaratoria-v1.tex
```

---

## 4. Guia de Mantenimiento

### 4.1 Escalabilidad

- **Horizontal:** El modulo es stateless, puede escalarse horizontalmente
- **Generacion de PDF:** Usar colas para procesamiento async si hay alta demanda
- **MinIO:** Configurar replicacion cross-region para alta disponibilidad
- **Cache:** Redis para catalogo de bloques (cambio poco frecuente)

### 4.2 Monitoreo

- **Metricas clave:**
  - Tiempo promedio de aceptacion de la carta
  - Tasa de rechazo en validaciones externas
  - Tiempo de generacion de PDF
  - Metodos de firma mas utilizados

- **Alertas:**
  - Validacion RENADEA reporta inhabilitacion
  - Tiempo de generacion de PDF > 10s
  - MinIO storage > 80% capacidad

### 4.3 Debugging

- **Problemas comunes:**
  - **PDF no se genera:** Verificar permisos de escritura, logs de iText
  - **Firma falla:** Revisar credenciales SAT, conectividad
  - **Bloque no se acepta:** Verificar logs de aceptacion, timestamps
  - **Validacion externa falla:** Revisar credenciales API, timeouts

### 4.4 Backup y Recuperacion

- **Base de datos:** Backup diario de PostgreSQL
- **MinIO:** Replicacion de PDFs firmados (WORM, inmutable)
- **Recuperacion:** En caso de perdida, restaurar desde backup + verificar hashes

---

## 5. Consideraciones de Seguridad

### 5.1 Integridad Legal

- **PDF inmutable:** Configuracion WORM en MinIO (Write Once Read Many)
- **Hash SHA-256:** De cada bloque aceptado y del PDF final
- **Timestamps certificados:** RFC 3161 para cada operacion critica
- **Trazabilidad completa:** IP, user-agent, timestamp de cada aceptacion

### 5.2 Proteccion de Datos

- **Datos sensibles:** Cifrados en base de datos (AES-256)
- **PDF firmado:** Almacenado con cifrado en MinIO
- **Acceso restringido:** Solo el aspirante, UR autorizada y Contraloria
- **Descargas:** URLs firmadas con expiracion (15 minutos)

### 5.3 Cumplimiento Normativo

- **Validez legal:** Conforme a Ley de Firma Electronica Avanzada
- **LGDPPP:** Aviso de privacidad aceptado, derechos ARCO
- **LGRA:** Declaraciones conforme a articulos 7, 19, 38
- **Codigo Penal Federal:** Art. 183 (falsedad de declaraciones)
- **Catalogo de Disposicion Documental:** Retencion conforme a normatividad

### 5.4 Auditoria

- **Tabla aceptaciones_bloques:** Solo INSERT (nunca UPDATE/DELETE)
- **Hash de cada bloque:** Para detectar alteraciones
- **Retencion minima:** 10 anos conforme a normatividad archivistica
- **Anclaje blockchain:** Opcional para maxima inmutabilidad

---

## 6. Testing

### 6.1 Tests Unitarios

- **Cobertura minima:** 85%
- **Casos a cubrir:**
  - Aceptacion de cada uno de los 12 bloques
  - Validacion de completitud (los 12 bloques aceptados)
  - Generacion de PDF con datos pre-llenados
  - Firma con los 3 metodos (FEA, biometrica, OTP)
  - Validaciones externas (RENADEA, violencia)
  - Casos edge (aspirante inhabilitado, PDF corrupto)

### 6.2 Tests de Integracion

- **APIs externas:** Mock con WireMock
- **MinIO:** Contenedor de prueba
- **Casos a cubrir:**
  - Flujo completo de carta declaratoria
  - Generacion y firma de PDF
  - Validaciones externas con respuestas simuladas

### 6.3 Tests de Seguridad

- **Penetration testing:** OWASP ZAP
- **Casos a cubrir:**
  - Manipulacion de PDF firmado
  - Falsificacion de timestamps
  - Acceso no autorizado a cartas de otros aspirantes
  - Alteracion de hashes

---

## 7. Despliegue

### 7.1 Configuracion por Entorno

- **Desarrollo:** APIs externas en sandbox, MinIO local
- **Staging:** APIs externas en test, MinIO con replicacion
- **Produccion:** APIs externas oficiales, MinIO distribuido con WORM

### 7.2 Variables de Entorno Criticas

```bash
# NUNCA commitear estos valores
RENADEA_API_KEY=<credencial_oficial>
VIOLENCIA_API_KEY=<credencial_oficial>
MINIO_SECRET_KEY=<credencial_oficial>
```

### 7.3 Health Checks

```
GET /actuator/health
Response: {
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "minio": { "status": "UP" },
    "renadea": { "status": "UP" },
    "violencia": { "status": "UP" }
  }
}
```

---

## 8. Accesibilidad

### 8.1 Lectores de Pantalla

- Compatible con NVDA, JAWS, VoiceOver
- Etiquetas ARIA en todos los bloques
- Navegacion completa por teclado

### 8.2 Texto a Voz (TTS)

- Cada bloque puede ser leido en voz alta
- Proveedor: Azure Cognitive Services
- Voces en espanol estandar y latinoamericano

### 8.3 Lenguas Indigenas

- Traduccion oficial de los 12 bloques a 68 lenguas indigenas
- Prioridad: nahua, maya, zapoteco, mixteco, otomi, totonaco
- Carga bajo demanda para optimizar performance

### 8.4 Lengua de Senas Mexicana (LSM)

- Video explicativo con interprete para cada bloque
- URL: https://ine.mx/videos/lsm/carta-declaratoria.mp4
- Subtitulos en espanol

### 8.5 Lectura Facil

- Version simplificada con pictogramas
- Para personas con discapacidad intelectual
- Mantiene validez legal (version complementaria)

---

## 9. Roadmap

### Version 1.0 (Actual)
- [x] 12 bloques declarativos con fundamento legal
- [x] Aceptacion individual con trazabilidad
- [x] Generacion de PDF con datos pre-llenados
- [x] Firma electronica en 3 niveles
- [x] Validaciones externas (RENADEA, violencia)
- [x] Almacenamiento inmutable en MinIO

### Version 1.1 (Proxima)
- [ ] Procesamiento async de generacion de PDF
- [ ] Notificaciones en tiempo real (WebSocket)
- [ ] Dashboard de analistas con metricas
- [ ] Exportacion de expediente completo en PDF

### Version 2.0 (Futuro)
- [ ] Blockchain para anclaje de hashes
- [ ] Soporte para lenguas indigenas con TTS
- [ ] Video explicativo en LSM interactivo
- [ ] Integracion con SSO institucional del INE

---
**Fin del documento README.md del Modulo de Carta Declaratoria**