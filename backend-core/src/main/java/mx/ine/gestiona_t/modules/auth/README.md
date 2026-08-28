# Módulo de Autenticación (auth)

**Responsabilidad:** FLUJO 1 - Registro y Autenticación del Aspirante  
**Versión:** 1.0.0  
**Fecha:** 08 de julio de 2026

---

## 1. Arquitectura del Módulo

### 1.1 Diagrama de Componentes

```
+------------------------------------------------------------------+
|                    AUTH MODULE                                     |
+------------------------------------------------------------------+
|                                                                     |
|  +------------------+      +------------------+                   |
|  | AuthController   |----->| AuthService      |                   |
|  | (REST API)       |      | (Business Logic) |                   |
|  +------------------+      +--------+---------+                   |
|                                     |                              |
|                    +----------------+----------------+            |
|                    |                                 |            |
|           +--------v---------+            +----------v---------+  |
|           | RenapoIntegration|            | ListaNominalService|  |
|           | Service          |            |                    |  |
|           +--------+---------+            +----------+---------+  |
|                    |                                 |            |
|           +--------v---------+            +----------v---------+  |
|           | RenapoClient     |            | ListaNominalClient |  |
|           | (HTTP Client)    |            | (HTTP Client)      |  |
|           +------------------+            +--------------------+  |
|                                                                     |
|  +------------------+      +------------------+                   |
|  | AspiranteRepo    |<---->| JwtService       |                   |
|  | (JPA Repository) |      | (JWT Generation) |                   |
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

- **Registro de aspirantes:** Captura inicial de datos (correo, móvil)
- **Verificación OTP:** Envío y validación de códigos de un solo uso
- **Autenticación por CURP:** Validación contra RENAPO
- **Autenticación por Clave de Elector:** Validación contra Lista Nominal INE
- **Generación de JWT:** Tokens de acceso y refresh
- **Gestión de sesiones:** Control de intentos fallidos y bloqueos

---

## 2. Logística de Datos

### 2.1 Entidades Principales

#### Aspirante
```java
@Entity
@Table(name = "aspirantes")
public class Aspirante {
    @Id
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String folio;  // UUID v7 para orden temporal
    
    @Column(nullable = false)
    private String nombreCompleto;
    
    @Column(unique = true, nullable = false)
    private String curp;
    
    @Column(unique = true)
    private String rfc;
    
    @Column(unique = true, nullable = false)
    private String correoElectronico;
    
    @Column(nullable = false)
    private String telefonoMovil;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    private EstatusAspirante estatus;  // PRE_REGISTRO, REGISTRO_VALIDADO, etc.
    
    @Enumerated(EnumType.STRING)
    private MetodoIdentificacion metodoIdentificacion;  // CURP, CLAVE_ELECTOR
    
    @Column(nullable = false)
    private int nivelConfianza;  // 1-4
    
    @Column(nullable = false)
    private LocalDateTime fechaRegistro;
    
    @Column
    private LocalDateTime fechaUltimoAcceso;
    
    @Column(nullable = false)
    private boolean activo;
    
    // Auditoría
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column
    private String createdBy;
}
```

#### Intento de Autenticación
```java
@Entity
@Table(name = "intentos_auth")
public class IntentoAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String ipOrigen;
    
    @Column
    private String userAgent;
    
    @Column
    private String curpIntentada;
    
    @Column
    private String correoIntentado;
    
    @Enumerated(EnumType.STRING)
    private TipoIntento tipo;  // LOGIN, REGISTRO, OTP, VALIDACION_CURP, VALIDACION_CLAVE_ELECTOR
    
    @Enumerated(EnumType.STRING)
    private ResultadoIntento resultado;  // EXITOSO, FALLIDO, BLOQUEADO
    
    @Column
    private String motivoFallo;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
```

#### Código OTP
```java
@Entity
@Table(name = "codigos_otp")
public class CodigoOTP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID aspiranteId;
    
    @Column(nullable = false)
    private String codigo;  // Hash del código
    
    @Enumerated(EnumType.STRING)
    private CanalOTP canal;  // EMAIL, SMS
    
    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;
    
    @Column(nullable = false)
    private boolean utilizado;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
```

### 2.2 Relaciones

```
Aspirante (1) ----< (N) IntentoAuth
Aspirante (1) ----< (N) CodigoOTP
```

### 2.3 Flujos de Información

#### Flujo de Registro
```
1. Frontend envía: correo, móvil
2. Backend genera OTP y lo envía a correo + SMS
3. Frontend envía OTP para verificación
4. Backend valida OTP y crea registro temporal (PRE_REGISTRO)
5. Frontend envía CURP + datos filiatorios O Clave de Elector + OCR credencial
6. Backend valida contra RENAPO o Lista Nominal
7. Backend genera folio único y actualiza estatus a REGISTRO_VALIDADO
8. Backend genera JWT y lo devuelve al frontend
```

#### Flujo de Login
```
1. Frontend envía: correo + password
2. Backend valida credenciales
3. Backend verifica que no haya bloqueos por intentos fallidos
4. Backend genera JWT (access token + refresh token)
5. Backend registra intento exitoso en auditoría
6. Backend devuelve tokens al frontend
```

---

## 3. Detalles Técnicos

### 3.1 Dependencias

```xml
<!-- pom.xml -->
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- HTTP Client para APIs externas -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    
    <!-- BCrypt para hashing de passwords -->
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-crypto</artifactId>
    </dependency>
    
    <!-- Lombok (opcional) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### 3.2 Variables de Entorno Requeridas

```bash
# JWT
JWT_SECRET=CHANGE_ME_SUPER_SECRET_KEY_MIN_256_BITS
JWT_EXPIRATION_MS=3600000  # 1 hora
JWT_REFRESH_EXPIRATION_MS=604800000  # 7 días

# APIs Externas
RENAPO_API_URL=https://api.renapo.gob.mx/v1
RENAPO_API_KEY=CHANGE_ME
RENAPO_API_SECRET=CHANGE_ME

INE_LISTA_NOMINAL_URL=https://api.ine.mx/lista-nominal/v1
INE_API_KEY=CHANGE_ME

# Notificaciones
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=notifications@ine.mx
SMTP_PASSWORD=CHANGE_ME

SMS_PROVIDER=twilio
SMS_ACCOUNT_SID=CHANGE_ME
SMS_AUTH_TOKEN=CHANGE_ME
SMS_FROM_NUMBER=+525512345678

# Rate Limiting
RATE_LIMIT_REQUESTS_PER_MINUTE=60
RATE_LIMIT_BURST=10
```

### 3.3 Endpoints REST

#### Registro
```
POST /api/v1/auth/registro/iniciar
Body: { "correo": "usuario@ejemplo.com", "telefono": "5512345678" }
Response: { "mensaje": "OTP enviado", "expiracion": 300 }

POST /api/v1/auth/registro/verificar-otp
Body: { "correo": "usuario@ejemplo.com", "otp": "123456" }
Response: { "tokenTemporal": "eyJhbGc..." }

POST /api/v1/auth/registro/validar-curp
Headers: Authorization: Bearer {tokenTemporal}
Body: { 
  "curp": "GAMA900101HDFRRL09",
  "nombre": "JUAN",
  "apellidoPaterno": "GARCIA",
  "apellidoMaterno": "MARTINEZ",
  "fechaNacimiento": "1990-01-01",
  "entidadFederativa": "DF"
}
Response: { 
  "folio": "01HXYZ...",
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "expiresIn": 3600
}

POST /api/v1/auth/registro/validar-clave-elector
Headers: Authorization: Bearer {tokenTemporal}
Body: { 
  "claveElector": "GARM90010109H200",
  "ocrCredencialFrontal": "base64...",
  "ocrCredencialPosterior": "base64..."
}
Response: { 
  "folio": "01HXYZ...",
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "expiresIn": 3600
}
```

#### Login
```
POST /api/v1/auth/login
Body: { "correo": "usuario@ejemplo.com", "password": "password123" }
Response: { 
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "expiresIn": 3600,
  "folio": "01HXYZ..."
}

POST /api/v1/auth/refresh
Body: { "refreshToken": "eyJhbGc..." }
Response: { 
  "accessToken": "eyJhbGc...",
  "expiresIn": 3600
}

POST /api/v1/auth/logout
Headers: Authorization: Bearer {accessToken}
Response: { "mensaje": "Sesión cerrada exitosamente" }
```

#### Validación de Token
```
GET /api/v1/auth/validate
Headers: Authorization: Bearer {accessToken}
Response: { 
  "valid": true,
  "folio": "01HXYZ...",
  "correo": "usuario@ejemplo.com",
  "nivelConfianza": 2
}
```

### 3.4 Estructura de Paquetes

```
mx.ine.gestiona_t.modules.auth/
+-- controller/
|   +-- AuthController.java
|   +-- AspiranteController.java
+-- service/
|   +-- AuthService.java
|   +-- AuthServiceImpl.java
|   +-- RenapoIntegrationService.java
|   +-- ListaNominalService.java
|   +-- JwtService.java
|   +-- OtpService.java
+-- repository/
|   +-- AspiranteRepository.java
|   +-- IntentoAuthRepository.java
|   +-- CodigoOTPRepository.java
+-- dto/
|   +-- request/
|   |   +-- RegistroIniciarRequest.java
|   |   +-- VerificarOtpRequest.java
|   |   +-- ValidarCurpRequest.java
|   |   +-- ValidarClaveElectorRequest.java
|   |   +-- LoginRequest.java
|   +-- response/
|       +-- RegistroResponse.java
|       +-- LoginResponse.java
|       +-- TokenResponse.java
|       +-- ValidacionResponse.java
+-- model/
|   +-- Aspirante.java
|   +-- IntentoAuth.java
|   +-- CodigoOTP.java
|   +-- enums/
|       +-- EstatusAspirante.java
|       +-- MetodoIdentificacion.java
|       +-- TipoIntento.java
|       +-- ResultadoIntento.java
|       +-- CanalOTP.java
+-- integration/
    +-- RenapoClient.java
    +-- ListaNominalClient.java
    +-- dto/
        +-- RenapoResponse.java
        +-- ListaNominalResponse.java
```

---

## 4. Guía de Mantenimiento

### 4.1 Escalabilidad

- **Horizontal:** El módulo es stateless (no guarda sesión en memoria), por lo que puede escalarse horizontalmente agregando más instancias detrás de un load balancer.
- **Base de datos:** Usar connection pooling (HikariCP) y considerar read replicas para consultas de validación de tokens.
- **Cache:** Implementar Redis para cache de tokens válidos y rate limiting distribuido.

### 4.2 Monitoreo

- **Métricas clave:**
  - Tiempo de respuesta de endpoints de autenticación
  - Tasa de éxito/fallo de validaciones contra RENAPO y Lista Nominal
  - Número de intentos fallidos por IP
  - Tiempo de expiración de OTPs no utilizados

- **Alertas:**
  - Más de 100 intentos fallidos desde la misma IP en 1 hora
  - Tiempo de respuesta de APIs externas > 5 segundos
  - Tasa de error de APIs externas > 5%

- **Logs:**
  - Registrar todos los intentos de autenticación (exitosos y fallidos)
  - No registrar passwords ni datos sensibles en logs
  - Incluir correlation ID para tracing distribuido

### 4.3 Debugging

- **Problemas comunes:**
  - **Token inválido:** Verificar que JWT_SECRET sea el mismo en todas las instancias
  - **OTP no llega:** Revisar configuración SMTP/SMS y logs de envío
  - **Validación RENAPO falla:** Verificar credenciales API y conectividad
  - **Bloqueo de cuenta:** Revisar tabla intentos_auth y liberar manualmente si es necesario

- **Herramientas:**
  - Spring Boot Actuator para health checks y métricas
  - Micrometer + Prometheus para métricas detalladas
  - Jaeger para tracing distribuido
  - Kibana para análisis de logs

### 4.4 Backup y Recuperación

- **Base de datos:** Backup diario de PostgreSQL con retención de 30 días
- **Secrets:** JWT_SECRET debe estar respaldado en HashiCorp Vault
- **Recuperación:** En caso de pérdida de JWT_SECRET, todos los tokens existentes se invalidan y los usuarios deben hacer login nuevamente

---

## 5. Consideraciones de Seguridad

### 5.1 Autenticación

- **Passwords:** Hash con BCrypt (cost factor 12)
- **JWT:** Algoritmo HS512 con clave mínima de 256 bits
- **OTP:** Códigos de 6 dígitos, expiración de 5 minutos, máximo 3 intentos
- **Rate limiting:** Máximo 5 intentos de login por IP/hora, bloqueo de 24 horas

### 5.2 Autorización

- **Roles:** ASPIRANTE, ADMIN_UR, ADMIN_RH, SUPER_ADMIN
- **Permisos:** Basados en roles con Spring Security
- **Endpoints públicos:** Solo registro y login
- **Endpoints protegidos:** Todos los demás requieren JWT válido

### 5.3 Protección de Datos

- **Datos sensibles:** CURP, RFC, correo, teléfono cifrados en base de datos (AES-256)
- **Logs:** Nunca registrar passwords, OTPs completos o datos sensibles
- **HTTPS:** TLS 1.3 obligatorio en todos los endpoints
- **CORS:** Configurar orígenes permitidos explícitamente

### 5.4 Auditoría

- **Tabla intentos_auth:** Registro inmutable de todos los intentos de autenticación
- **Campos auditados:** IP origen, user agent, timestamp, resultado, motivo de fallo
- **Retención:** Mínimo 10 años conforme a normatividad archivistica
- **Integridad:** Hash SHA-256 de cada registro para detectar alteraciones

### 5.5 Cumplimiento Normativo

- **LGDPPP:** Aviso de privacidad aceptado antes de cualquier captura de datos
- **Derechos ARCO:** Implementar endpoints para acceso, rectificación, cancelación y oposición
- **Consentimiento:** Registro explícito de aceptación de términos y condiciones
- **Retención de datos:** Conforme al Catálogo de Disposición Documental del INE

---

## 6. Testing

### 6.1 Tests Unitarios

- **Cobertura mínima:** 80%
- **Frameworks:** JUnit 5 + Mockito
- **Casos a cubrir:**
  - Registro exitoso con CURP válida
  - Registro exitoso con Clave de Elector válida
  - Login exitoso
  - Login fallido (credenciales incorrectas)
  - Login fallido (cuenta bloqueada)
  - OTP válido
  - OTP expirado
  - OTP inválido
  - Token JWT válido
  - Token JWT expirado
  - Token JWT mal formado

### 6.2 Tests de Integración

- **Base de datos:** H2 en memoria para tests
- **APIs externas:** Mock con WireMock
- **Casos a cubrir:**
  - Flujo completo de registro
  - Flujo completo de login
  - Refresh de token
  - Validación contra RENAPO (mock)
  - Validación contra Lista Nominal (mock)

### 6.3 Tests de Seguridad

- **Herramientas:** OWASP ZAP, Burp Suite
- **Casos a cubrir:**
  - Inyección SQL
  - XSS
  - CSRF
  - Brute force
  - Session hijacking
  - Token manipulation

---

## 7. Despliegue

### 7.1 Configuración por Entorno

- **Desarrollo:** Base de datos local, APIs externas en modo sandbox
- **Staging:** Base de datos dedicada, APIs externas en modo test
- **Producción:** Base de datos con alta disponibilidad, APIs externas en modo producción

### 7.2 Variables de Entorno Críticas

```bash
# NUNCA commitear estos valores
JWT_SECRET=<generar_con_openssl_rand_hex_64>
RENAPO_API_KEY=<credencial_oficial>
RENAPO_API_SECRET=<credencial_oficial>
INE_API_KEY=<credencial_oficial>
SMTP_PASSWORD=<password_real>
SMS_AUTH_TOKEN=<token_real>
```

### 7.3 Health Checks

```
GET /actuator/health
Response: {
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "renapo": { "status": "UP" },
    "listaNominal": { "status": "UP" }
  }
}
```

---

## 8. Roadmap

### Versión 1.0 (Actual)
- [x] Registro por CURP
- [x] Registro por Clave de Elector
- [x] Login con password
- [x] JWT con refresh token
- [x] OTP por email y SMS

### Versión 1.1 (Próxima)
- [ ] Autenticación biométrica (selfie + prueba de vida)
- [ ] Integración con e.firma (FIEL) como opción adicional
- [ ] Autenticación de dos factores (2FA) con TOTP
- [ ] Soporte para lenguas indígenas en mensajes OTP

### Versión 2.0 (Futuro)
- [ ] Integración con SSO institucional del INE
- [ ] Autenticación con certificado digital
- [ ] Soporte para WebAuthn/FIDO2

---
**Fin del documento README.md del Módulo de Autenticación**