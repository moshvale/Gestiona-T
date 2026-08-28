# Modulo de Auditoria (auditoria)

**Responsabilidad:** FLUJO 7 - Servicio Transversal de Trazabilidad Inmutable
**Version:** 1.0.0
**Fecha:** 09 de julio de 2026
**Tipo:** Servicio transversal que consume eventos de todos los demas modulos
**Importancia:** CRITICA - Garantiza cumplimiento con Contraloria Interna y OPL

---

## 1. Arquitectura del Modulo

### 1.1 Diagrama de Componentes

```
+------------------------------------------------------------------+
|                   AUDITORIA MODULE                                 |
|              (Servicio Transversal Inmutable)                      |
+------------------------------------------------------------------+
|                                                                     |
|  +------------------+      +------------------+                   |
|  | AuditoriaController|---->| AuditoriaService |                   |
|  | (REST API)       |      | (Orquestador)    |                   |
|  +------------------+      +--------+---------+                   |
|                                     |                              |
|                    +----------------+----------------+            |
|                    |                |                |            |
|           +--------v------+  +------v-------+  +----v--------+  |
|           | EventPublisher |  | CadenaHashSvc|  | RetentionSvc|  |
|           | (Async Events) |  | (Inmutabilidad)| | (Retencion) |  |
|           +--------+------+  +------+---------+  +----+--------+  |
|                    |                |                |            |
|           +--------v------+  +------v-------+  +----v--------+  |
|           | FiltroAudit    |  | BlockchainSvc|  | ReporteSvc  |  |
|           | (Aspect AOP)   |  | (Hyperledger)|  | (Reportes)  |  |
|           +----------------+  +--------------+  +---------------+  |
|                                                                     |
+------------------------------------------------------------------+
                              |
                              v
              +---------------+---------------+
              |                               |
    +---------v---------+          +----------v---------+
    |   PostgreSQL      |          |  Hyperledger       |
    | (solo INSERT)     |          |  (anclaje opcional)|
    | Tabla audit_eventos|         |  Fabric            |
    +-------------------+          +--------------------+
```

### 1.2 Responsabilidades

- **Registro inmutable:** Solo INSERT, bloqueado UPDATE/DELETE a nivel BD
- **Cadena de hashes:** Cada evento incluye hash del evento anterior
- **Publicacion asincrona:** No afecta performance de otros modulos
- **Filtrado AOP:** Intercepta automaticamente operaciones criticas
- **Anclaje blockchain:** Opcional para maxima inmutabilidad
- **Reportes:** Generacion de reportes para Contraloria y OPL
- **Retencion:** Gestion de politicas de retencion (minimo 10 anos)
- **Consulta avanzada:** Filtros por fecha, tipo, modulo, aspirante

### 1.3 Eventos Registrados por Modulo

| Modulo | Eventos Registrados |
|--------|---------------------|
| **auth** | Login exitoso/fallido, registro, OTP, bloqueo de cuenta |
| **cv** | Creacion, modificacion, eliminacion de secciones |
| **documentos** | Carga, clasificacion, validacion, rechazo |
| **matching** | Evaluacion, score, recomendacion |
| **carta-declaratoria** | Aceptacion de bloques, firma, validaciones externas |
| **firma** | Solicitud, validacion identidad, firma, timestamp |
| **seguridad** | Acceso denegado, token invalido, rate limit excedido |

---

## 2. Logistica de Datos

### 2.1 Entidades Principales

#### EventoAuditoria
```java
@Entity
@Table(name = "audit_eventos")
public class EventoAuditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 50)
    private String categoria;  // AUTH, CV, DOCUMENTOS, MATCHING, CARTA, FIRMA, SEGURIDAD
    
    @Column(nullable = false, length = 50)
    private String tipoEvento;  // LOGIN_EXITOSO, CV_CREADO, etc.
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelSeveridad severidad;  // INFO, WARNING, ERROR, CRITICAL
    
    @Column
    private UUID actorId;  // ID del aspirante o usuario interno
    
    @Column(length = 100)
    private String actorTipo;  // ASPIRANTE, ANALISTA, ADMIN, SISTEMA
    
    @Column(nullable = false, length = 45)
    private String ipOrigen;
    
    @Column(length = 500)
    private String userAgent;
    
    @Column(length = 100)
    private String recursoAfectado;  // Folio, ID del documento, etc.
    
    @Column(nullable = false, length = 500)
    private String descripcion;
    
    @Column(columnDefinition = "jsonb")
    private String datosEvento;  // Payload completo del evento (cifrado si sensible)
    
    @Column(length = 100)
    private String hashDatos;  // SHA-256 de datosEvento
    
    @Column(length = 100)
    private String hashAnterior;  // Hash del evento anterior (cadena)
    
    @Column(length = 100)
    private String hashPropio;  // SHA-256 de este evento + hashAnterior
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(length = 100)
    private String correlationId;  // Para tracing distribuido
    
    @Column(length = 50)
    private String moduloOrigen;  // auth, cv, documentos, etc.
    
    @Column(nullable = false)
    private boolean ancladoBlockchain;
    
    @Column(length = 200)
    private String transaccionBlockchain;
}
```

#### CadenaHash
```java
@Entity
@Table(name = "audit_cadena_hash")
public class CadenaHash {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID eventoId;
    
    @Column(nullable = false, length = 100)
    private String hashEvento;
    
    @Column(length = 100)
    private String hashAnterior;
    
    @Column(nullable = false)
    private Long secuencia;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
```

#### ConfiguracionRetencion
```java
@Entity
@Table(name = "audit_configuracion_retencion")
public class ConfiguracionRetencion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String categoria;
    
    @Column(nullable = false)
    private int aniosRetencion;
    
    @Column(nullable = false)
    private boolean activo;
    
    @Column
    private LocalDateTime fechaActualizacion;
}
```

### 2.2 Relaciones

```
EventoAuditoria (1) ----< (1) CadenaHash
EventoAuditoria (N) ----< (1) ConfiguracionRetencion (por categoria)
```

### 2.3 Flujos de Informacion

#### Flujo Principal de Registro
```
1. Modulo origen detecta evento significativo
2. Invoca AuditoriaEventPublisher.publishAsync(evento)
3. Publisher encola evento en cola asincrona (Spring @Async)
4. AuditoriaService procesa evento:
   a. Calcula hash de datos del evento
   b. Obtiene hash del evento anterior
   c. Calcula hash propio (datos + hashAnterior)
   d. Inserta en tabla audit_eventos
   e. Inserta en audit_cadena_hash
5. Si evento es CRITICO, ancla hash en blockchain (opcional)
6. Notifica a sistema de monitoreo (Prometheus)
```

#### Flujo de Consulta
```
1. Usuario (Contraloria, OPL, Admin) solicita consulta
2. AuditoriaController recibe filtros (fecha, tipo, modulo, actor)
3. AuditoriaService ejecuta consulta con paginacion
4. Retorna eventos con verificacion de integridad de cadena
5. Opcionalmente genera reporte PDF/Excel
```

---

## 3. Detalles Tecnicos

### 3.1 Dependencias Adicionales

```xml
<!-- AOP para interceptacion automatica -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>

<!-- Async processing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>

<!-- Exportacion de reportes -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>

<!-- Blockchain (opcional) -->
<dependency>
    <groupId>org.hyperledger.fabric-sdk-java</groupId>
    <artifactId>fabric-sdk-java</artifactId>
    <version>2.2.19</version>
</dependency>
```

### 3.2 Variables de Entorno Requeridas

```bash
# Auditoria
AUDIT_LOG_RETENTION_DAYS=3650
AUDIT_HASH_CHAIN_ENABLED=true
AUDIT_BLOCKCHAIN_ENABLED=false
AUDIT_BLOCKCHAIN_URL=http://localhost:8545
AUDIT_BLOCKCHAIN_CHANNEL=auditoria-channel
AUDIT_ASYNC_POOL_SIZE=10
AUDIT_ASYNC_QUEUE_CAPACITY=1000

# Reportes
AUDIT_REPORT_MAX_ROWS=100000
AUDIT_REPORT_BATCH_SIZE=1000

# Monitoreo
AUDIT_METRICS_ENABLED=true
AUDIT_PROMETHEUS_ENABLED=true
```

### 3.3 Endpoints REST

#### Consulta de Eventos
```
GET    /api/v1/auditoria/eventos
GET    /api/v1/auditoria/eventos/{id}
GET    /api/v1/auditoria/eventos/aspirante/{aspiranteId}
GET    /api/v1/auditoria/eventos/verificar-cadena
POST   /api/v1/auditoria/eventos/buscar
```

#### Reportes
```
GET    /api/v1/auditoria/reportes/resumen
GET    /api/v1/auditoria/reportes/exportar-excel
GET    /api/v1/auditoria/reportes/exportar-pdf
GET    /api/v1/auditoria/reportes/contraloria
GET    /api/v1/auditoria/reportes/opl
```

#### Estadisticas
```
GET    /api/v1/auditoria/estadisticas/hoy
GET    /api/v1/auditoria/estadisticas/modulo/{modulo}
GET    /api/v1/auditoria/estadisticas/severidad
```

#### Admin
```
GET    /api/v1/admin/auditoria/configuracion
PUT    /api/v1/admin/auditoria/configuracion/retencion
POST   /api/v1/admin/auditoria/anclaje-blockchain/{eventoId}
POST   /api/v1/admin/auditoria/verificar-integridad
```

### 3.4 Estructura de Paquetes

```
mx.ine.gestiona_t.modules.auditoria/
+-- controller/
|   +-- AuditoriaController.java
|   +-- AdminAuditoriaController.java
+-- service/
|   +-- AuditoriaService.java (interfaz)
|   +-- AuditoriaServiceImpl.java
|   +-- AuditoriaEventPublisher.java
|   +-- CadenaHashService.java
|   +-- RetentionService.java
|   +-- ReporteService.java
|   +-- BlockchainService.java (opcional)
+-- repository/
|   +-- EventoAuditoriaRepository.java
|   +-- CadenaHashRepository.java
|   +-- ConfiguracionRetencionRepository.java
+-- dto/
|   +-- request/
|   |   +-- PublicarEventoRequest.java
|   |   +-- BuscarEventosRequest.java
|   +-- response/
|       +-- EventoAuditoriaResponse.java
|       +-- ResumenReporteResponse.java
|       +-- EstadisticasResponse.java
|       +-- VerificacionIntegridadResponse.java
+-- model/
|   +-- EventoAuditoria.java
|   +-- CadenaHash.java
|   +-- ConfiguracionRetencion.java
|   +-- enums/
|       +-- CategoriaEvento.java
|       +-- TipoEvento.java
|       +-- NivelSeveridad.java
|       +-- ActorTipo.java
+-- aspect/
|   +-- AuditoriaAspect.java  (interceptacion AOP)
|   +-- Auditable.java  (anotacion custom)
+-- event/
|   +-- AuditEvent.java
|   +-- AuditEventPublisher.java
+-- config/
    +-- AuditAsyncConfig.java
    +-- AuditSchedulerConfig.java
```

---

## 4. Guia de Mantenimiento

### 4.1 Escalabilidad

- **Particionamiento:** Tabla audit_eventos particionada por mes
- **Indice:** Indices en timestamp, categoria, actorId, tipoEvento
- **Archive:** Eventos antiguos (> 2 anos) mueven a tabla audit_eventos_archive
- **Lectura:** Read replicas para consultas pesadas de Contraloria/OPL
- **Cola:** Usar RabbitMQ/Kafka para alta carga de eventos

### 4.2 Monitoreo

- **Metricas clave:**
  - Eventos por segundo ( throughput )
  - Tiempo promedio de registro de evento
  - Tamano de cola de eventos pendientes
  - Tasa de error en anclaje blockchain
  - Espacio en disco de tabla audit_eventos

- **Alertas:**
  - Cola de eventos > 10,000 pendientes
  - Tiempo de registro > 500ms
  - Error en cadena de hashes (integrity check failed)
  - Espacio en disco > 80%

### 4.3 Debugging

- **Problemas comunes:**
  - **Cadena de hashes rota:** Ejecutar verificacion de integridad
  - **Eventos no se registran:** Verificar cola asincrona, logs de error
  - **Consultas lentas:** Revisar indices, considerar particionamiento
  - **Blockchain falla:** Verificar conectividad, credenciales

### 4.4 Backup y Recuperacion

- **Backup:** Diario completo + incrementales cada hora
- **Replicacion:** Cross-region para disaster recovery
- **Verificacion:** Pruebas trimestrales de restauracion
- **Integridad:** Verificacion diaria de cadena de hashes

---

## 5. Consideraciones de Seguridad

### 5.1 Inmutabilidad

- **Nivel BD:** Trigger de PostgreSQL que bloquea UPDATE/DELETE
- **Nivel aplicacion:** Repositorio con solo metodo save() (INSERT)
- **Cadena de hashes:** Cada evento referencia al anterior
- **Hash propio:** SHA-256(datos + hashAnterior + timestamp)

### 5.2 Proteccion de Datos

- **Datos sensibles:** Cifrados con AES-256 en datosEvento
- **Acceso restringido:** Solo roles AUDITOR, CONTRALORIA, OPL, SUPER_ADMIN
- **Consulta auditada:** Toda consulta tambien se registra en auditoria
- **Descargas:** URLs firmadas con expiracion corta

### 5.3 Cumplimiento Normativo

- **LGDPPP:** Retencion conforme a plazos legales
- **Contraloria Interna:** Acceso completo con trazabilidad
- **OPL:** Reportes automaticos periodicos
- **Catalogo de Disposicion Documental:** Retencion minima 10 anos
- **ISO 27001:** Controles de seguridad de informacion

### 5.4 Auditoria de la Auditoria

- **Meta-auditoria:** Las consultas tambien se auditan
- **Accesos registrados:** Quien consulto que y cuando
- **Reportes generados:** Trazabilidad de reportes exportados
- **Configuracion cambiada:** Toda modificacion de config se registra

---

## 6. Testing

### 6.1 Tests Unitarios

- **Cobertura minima:** 90% (modulo critico)
- **Casos a cubrir:**
  - Publicacion de evento exitosa
  - Calculo de cadena de hashes
  - Verificacion de integridad (cadena valida/invalida)
  - Filtros de consulta
  - Generacion de reportes
  - Manejo de eventos criticos

### 6.2 Tests de Integracion

- **Base de datos:** PostgreSQL con triggers de inmutabilidad
- **Blockchain:** Mock de Hyperledger Fabric
- **Casos a cubrir:**
  - Flujo completo de publicacion + registro
  - Verificacion de cadena despues de miles de eventos
  - Consulta con filtros complejos
  - Generacion de reporte grande

### 6.3 Tests de Seguridad

- **Penetration testing:** OWASP ZAP
- **Casos a cubrir:**
  - Intento de UPDATE/DELETE en tabla audit_eventos
  - Acceso no autorizado a eventos
  - Manipulacion de hashes
  - Inyeccion SQL en filtros

---

## 7. Despliegue

### 7.1 Configuracion por Entorno

- **Desarrollo:** Blockchain deshabilitado, retencion corta (30 dias)
- **Staging:** Blockchain simulado, retencion media (1 ano)
- **Produccion:** Blockchain real, retencion completa (10+ anos)

### 7.2 Particionamiento de Tabla

```sql
-- Particion por mes para mejor performance
CREATE TABLE audit_eventos (
    ...
) PARTITION BY RANGE (timestamp);

CREATE TABLE audit_eventos_2026_07 PARTITION OF audit_eventos
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');
```

### 7.3 Triggers de Inmutabilidad

```sql
-- Bloquear UPDATE
CREATE OR REPLACE FUNCTION bloquear_update_audit()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'UPDATE no permitido en tabla audit_eventos';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_bloquear_update
BEFORE UPDATE ON audit_eventos
FOR EACH ROW EXECUTE FUNCTION bloquear_update_audit();

-- Bloquear DELETE
CREATE OR REPLACE FUNCTION bloquear_delete_audit()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'DELETE no permitido en tabla audit_eventos';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_bloquear_delete
BEFORE DELETE ON audit_eventos
FOR EACH ROW EXECUTE FUNCTION bloquear_delete_audit();
```

---

## 8. Roadmap

### Version 1.0 (Actual)
- [x] Registro inmutable de eventos
- [x] Cadena de hashes SHA-256
- [x] Publicacion asincrona
- [x] Consulta con filtros avanzados
- [x] Reportes basicos (Excel, PDF)
- [x] Verificacion de integridad

### Version 1.1 (Proxima)
- [ ] Anclaje en blockchain Hyperledger
- [ ] Particionamiento automatico por mes
- [ ] Dashboard en tiempo real
- [ ] Alertas configurables
- [ ] Integracion con SIEM corporativo

### Version 2.0 (Futuro)
- [ ] Machine learning para deteccion de anomalias
- [ ] Correlacion automatica de eventos
- [ ] Reportes predictivos
- [ ] Integracion con sistemas de otros OPL
- [ ] API GraphQL para consultas complejas

---
**Fin del documento README.md del Modulo de Auditoria**