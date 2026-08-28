# Modulo de Firma Electronica (firma)

**Responsabilidad:** FLUJO 6 - Servicio Comun de Firma Electronica en 3 Niveles
**Version:** 1.0.0
**Fecha:** 09 de julio de 2026
**Tipo:** Servicio transversal reutilizable por otros modulos

---

## 1. Arquitectura del Modulo

### 1.1 Diagrama de Componentes

```
+------------------------------------------------------------------+
|                 FIRMA ELECTRONICA MODULE                           |
|                   (Servicio Comun)                                 |
+------------------------------------------------------------------+
|                                                                     |
|  +------------------+      +------------------+                   |
|  | FirmaController  |----->| FirmaService     |                   |
|  | (REST API)       |      | (Orquestador)    |                   |
|  +------------------+      +--------+---------+                   |
|                                     |                              |
|                    +----------------+----------------+            |
|                    |                |                |            |
|           +--------v------+  +------v-------+  +----v--------+  |
|           | FirmaFEA     |  | FirmaBiometrica| | FirmaOTP    |  |
|           | Service      |  | Service        | | Service     |  |
|           | (Nivel Max)  |  | (Nivel Alto)   | | (Nivel Med) |  |
|           +--------+------+  +------+---------+  +----+--------+  |
|                    |                |                |            |
|           +--------v------+  +------v-------+  +----v--------+  |
|           | SatFielClient|  | BiometriaClient| | OtpService  |  |
|           | (API SAT)    |  | (Validacion)   | | (Doble canal)|  |
|           +----------------+  +--------------+  +---------------+  |
|                                                                     |
|  +------------------+      +------------------+                   |
|  | TimestampService |      | PdfSignatureSvc  |                   |
|  | (RFC 3161)       |      | (Sellos PDF)     |                   |
|  +------------------+      +------------------+                   |
|                                                                     |
+------------------------------------------------------------------+
                              |
                              v
              +---------------+---------------+
              |                               |
    +---------v---------+          +----------v---------+
    |   PostgreSQL      |          |      MinIO         |
    | (firmas, sellos)  |          | (PDFs WORM)        |
    +-------------------+          +--------------------+
```

### 1.2 Responsabilidades

- **Servicio comun:** Reutilizable por CartaDeclaratoria, Contratos, etc.
- **3 niveles de firma:** FEA (maxima), Biometrica (alta), OTP (media)
- **Timestamps certificados:** RFC 3161 para validez legal
- **Sellos digitales:** Aplicacion a PDFs con iText
- **Almacenamiento WORM:** MinIO con Write Once Read Many
- **Trazabilidad inmutable:** Hash SHA-256 de cada operacion
- **Validez legal:** Conforme a Ley de Firma Electronica Avanzada

### 1.3 Niveles de Firma

| Nivel | Metodo | Validez Legal | Caso de Uso |
|-------|--------|---------------|-------------|
| **Maximo** | FEA con e.firma/FIEL del SAT | Plena (Ley FEA) | Contratos laborales, documentos criticos |
| **Alto** | Biometrica + OTP | Avanzada (CFPC Art. 191) | Carta declaratoria, declaraciones |
| **Medio** | OTP doble canal (email + SMS) | Simple (suficiente) | Avisos de privacidad, aceptaciones |

---

## 2. Logistica de Datos

### 2.1 Entidades Principales

#### DocumentoFirmado
```java
@Entity
@Table(name = "documentos_firmados")
public class DocumentoFirmado {
    @Id
    private UUID id;
    
    @Column(nullable = false, length = 36)
    private String folioDocumento;  // UUID unico del documento
    
    @Column(nullable = false, length = 50)
    private String tipoDocumento;  // CARTA_DECLARATORIA, CONTRATO, etc.
    
    @Column(nullable = false)
    private UUID aspiranteId;
    
    @Column(nullable = false, length = 36)
    private String folioAspirante;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NivelFirma nivelFirma;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstatusFirma estatus;
    
    @Column(nullable = false, length = 500)
    private String nombreArchivo;
    
    @Column(nullable = false, length = 500)
    private String storagePathOriginal;  // PDF original en MinIO
    
    @Column(length = 500)
    private String storagePathFirmado;  // PDF firmado en MinIO (WORM)
    
    @Column(length = 100)
    private String hashOriginal;  // SHA-256 del PDF original
    
    @Column(length = 100)
    private String hashFirmado;  // SHA-256 del PDF firmado
    
    @Column(columnDefinition = "jsonb")
    private String metadataFirma;  // Detalles de la firma
    
    @Column
    private LocalDateTime fechaFirma;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

#### SelloDigital
```java
@Entity
@Table(name = "sellos_digitales")
public class SelloDigital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID documentoFirmadoId;
    
    @Column(nullable = false, length = 100)
    private String timestampToken;  // Token RFC 3161
    
    @Column(nullable = false)
    private LocalDateTime timestampCertificado;
    
    @Column(length = 200)
    private String autoridadTimestamp;  // TSA (Timestamp Authority)
    
    @Column(length = 100)
    private String hashDocumento;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

#### FirmaMetadata
```java
@Entity
@Table(name = "firmas_metadata")
public class FirmaMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID documentoFirmadoId;
    
    @Column(nullable = false, length = 45)
    private String ipOrigen;
    
    @Column(length = 500)
    private String userAgent;
    
    @Column(length = 100)
    private String geolocalizacion;
    
    @Column(length = 100)
    private String dispositivoId;
    
    @Column(columnDefinition = "jsonb")
    private String datosBiometricos;  // Si aplica (cifrado)
    
    @Column(length = 100)
    private String otpHash;  // Hash del OTP usado (si aplica)
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

### 2.2 Relaciones

```
DocumentoFirmado (1) ----< (1) SelloDigital
DocumentoFirmado (1) ----< (1) FirmaMetadata
```

### 2.3 Flujos de Informacion

#### Flujo de Firma FEA (Nivel Maximo)
```
1. Recibir PDF + certificado FIEL del aspirante
2. Validar certificado contra API SAT
3. Generar hash SHA-256 del PDF
4. Firmar digitalmente con FIEL
5. Solicitar timestamp certificado (RFC 3161)
6. Aplicar sello digital al PDF
7. Almacenar PDF firmado en MinIO (WORM)
8. Registrar hash y metadata en PostgreSQL
9. Retornar PDF firmado + metadata
```

#### Flujo de Firma Biometrica (Nivel Alto)
```
1. Recibir PDF + selfie con prueba de vida
2. Validar biometria facial contra foto RENAPO/INE
3. Si coincidencia >= 85%, solicitar OTP al movil
4. Validar OTP
5. Generar hash SHA-256 del PDF
6. Generar sello con metadatos biometricos + OTP
7. Solicitar timestamp certificado (RFC 3161)
8. Aplicar sello al PDF
9. Almacenar en MinIO (WORM)
10. Registrar en PostgreSQL
```

#### Flujo de Firma OTP (Nivel Medio)
```
1. Recibir PDF
2. Solicitar OTP a correo Y movil registrados
3. Validar ambos OTP
4. Generar hash SHA-256 del PDF
5. Registrar IP, geolocalizacion, user-agent, timestamp
6. Generar sello con metadatos de sesion
7. Solicitar timestamp certificado (RFC 3161)
8. Aplicar sello al PDF
9. Almacenar en MinIO (WORM)
10. Registrar en PostgreSQL
```

---

## 3. Detalles Tecnicos

### 3.1 Dependencias Adicionales

```xml
<!-- iText para manipulacion de PDFs -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>8.0.2</version>
    <type>pom</type>
</dependency>

<!-- Bouncy Castle para criptografia -->
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
    <version>1.77</version>
</dependency>

<!-- Apache PDFBox para validacion -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.1</version>
</dependency>
```

### 3.2 Variables de Entorno Requeridas

```bash
# SAT FIEL
SAT_FIEL_API_URL=https://api.sat.gob.mx/fiel/v1
SAT_FIEL_API_KEY=CHANGE_ME

# Timestamp Authority (TSA)
TSA_URL=https://tsa.ine.mx/rfc3161
TSA_USERNAME=CHANGE_ME
TSA_PASSWORD=CHANGE_ME

# Biometria
BIOMETRIA_API_URL=https://api.renapo.gob.mx/biometria/v1
BIOMETRIA_API_KEY=CHANGE_ME
BIOMETRIA_THRESHOLD=0.85

# MinIO
MINIO_BUCKET_FIRMAS=documentos-firmados
MINIO_WORM_ENABLED=true

# OTP
OTP_LENGTH=6
OTP_EXPIRATION_MINUTES=5
```

### 3.3 Endpoints REST

#### Firma de Documentos
```
POST   /api/v1/firmas/firmar-fea
POST   /api/v1/firmas/firmar-biometrica
POST   /api/v1/firmas/firmar-otp
GET    /api/v1/firmas/{folioDocumento}
GET    /api/v1/firmas/{folioDocumento}/pdf
GET    /api/v1/firmas/{folioDocumento}/validar
```

#### Validacion de Firmas
```
POST   /api/v1/firmas/validar-pdf
GET    /api/v1/firmas/{folioDocumento}/metadata
```

#### Admin
```
GET    /api/v1/admin/firmas
GET    /api/v1/admin/firmas/{folioDocumento}/detalle
```

### 3.4 Estructura de Paquetes

```
mx.ine.gestiona_t.modules.firma/
+-- controller/
|   +-- FirmaController.java
|   +-- AdminFirmaController.java
+-- service/
|   +-- FirmaService.java (interfaz comun)
|   +-- FirmaServiceImpl.java (orquestador)
|   +-- FirmaFEAService.java
|   +-- FirmaBiometricaService.java
|   +-- FirmaOTPService.java
|   +-- TimestampService.java
|   +-- PdfSignatureService.java
|   +-- MinioFirmaService.java
+-- repository/
|   +-- DocumentoFirmadoRepository.java
|   +-- SelloDigitalRepository.java
|   +-- FirmaMetadataRepository.java
+-- dto/
|   +-- request/
|   |   +-- FirmarFEARequest.java
|   |   +-- FirmarBiometricaRequest.java
|   |   +-- FirmarOTPRequest.java
|   +-- response/
|       +-- FirmaResponse.java
|       +-- ValidacionFirmaResponse.java
|       +-- MetadataFirmaResponse.java
+-- model/
|   +-- DocumentoFirmado.java
|   +-- SelloDigital.java
|   +-- FirmaMetadata.java
|   +-- enums/
|       +-- NivelFirma.java
|       +-- EstatusFirma.java
|       +-- TipoDocumentoFirma.java
+-- integration/
    +-- SatFielClient.java
    +-- BiometriaClient.java
    +-- TimestampAuthorityClient.java
    +-- dto/
        +-- SatFielResponse.java
        +-- BiometriaResponse.java
        +-- TimestampResponse.java
```

---

## 4. Guia de Mantenimiento

### 4.1 Escalabilidad

- **Horizontal:** Servicio stateless, escalable horizontalmente
- **Procesamiento async:** Usar colas para firmas masivas
- **MinIO:** Replicacion cross-region para alta disponibilidad
- **TSA:** Configurar multiples autoridades de timestamp

### 4.2 Monitoreo

- **Metricas clave:**
  - Tiempo promedio de firma por nivel
  - Tasa de exito de validaciones biometricas
  - Uso de cada metodo de firma
  - Tiempo de respuesta de TSA

- **Alertas:**
  - TSA no responde (timeout > 5s)
  - Tasa de fallo biometrico > 20%
  - MinIO storage > 80%

### 4.3 Debugging

- **Problemas comunes:**
  - **Firma FEA falla:** Verificar certificado FIEL, conectividad SAT
  - **Biometria falla:** Revisar calidad de selfie, umbral de coincidencia
  - **Timestamp falla:** Verificar credenciales TSA, conectividad
  - **PDF no se almacena:** Revisar permisos MinIO, espacio disponible

---

## 5. Consideraciones de Seguridad

### 5.1 Integridad Legal

- **PDF inmutable:** Configuracion WORM en MinIO
- **Hash SHA-256:** De cada documento original y firmado
- **Timestamps RFC 3161:** Certificados por autoridad confiable
- **Sellos digitales:** Aplicados con iText + Bouncy Castle

### 5.2 Proteccion de Datos

- **Datos biometricos:** Cifrados con AES-256 en base de datos
- **Certificados FIEL:** Nunca almacenados, solo validados
- **OTPs:** Hash en base de datos, nunca en texto plano
- **Acceso restringido:** Solo el aspirante y autoridades autorizadas

### 5.3 Cumplimiento Normativo

- **Ley de Firma Electronica Avanzada:** Para nivel FEA
- **CFPC Art. 191:** Para nivel biometrico
- **Firma electronica simple:** Suficiente para nivel OTP
- **LGDPPP:** Datos personales cifrados
- **RFC 3161:** Timestamps certificados

---

## 6. Testing

### 6.1 Tests Unitarios

- **Cobertura minima:** 85%
- **Casos a cubrir:**
  - Firma FEA con certificado valido/invalido
  - Firma biometrica con coincidencia alta/baja
  - Firma OTP con OTP valido/expirado
  - Generacion de timestamps
  - Aplicacion de sellos a PDFs
  - Almacenamiento en MinIO

### 6.2 Tests de Integracion

- **APIs externas:** Mock con WireMock
- **MinIO:** Contenedor de prueba
- **TSA:** Mock de autoridad de timestamp

---

## 7. Roadmap

### Version 1.0 (Actual)
- [x] Firma FEA con FIEL del SAT
- [x] Firma biometrica + OTP
- [x] Firma OTP doble canal
- [x] Timestamps RFC 3161
- [x] Sellos digitales en PDFs
- [x] Almacenamiento WORM en MinIO

### Version 1.1 (Proxima)
- [ ] Firma masiva de documentos
- [ ] Validacion offline de firmas
- [ ] Integracion con blockchain para anclaje
- [ ] Dashboard de firmas con metricas

---
**Fin del documento README.md del Modulo de Firma Electronica**