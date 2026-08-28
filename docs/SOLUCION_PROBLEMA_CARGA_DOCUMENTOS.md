# Solución al Problema de Carga de Documentos

## Problema actual: error 500 por folio nulo

El 19 de agosto de 2026 la carga fallaba con `500 Internal Server Error`. El
frontend solo mostraba `No se pudo subir el documento`, pero la causa real se
encontraba en PostgreSQL:

```text
null value in column "folio" of relation "expedientes_digitales" violates not-null constraint
```

El flujo de carga iniciaba la validación automática en segundo plano antes de
crear el expediente. Esa validación llamaba a
`ExpedienteService.actualizarExpediente(aspiranteId)`, que usaba
`obtenerOCrear(aspiranteId, null)`. Cuando la tarea en segundo plano ganaba la
condición de carrera, Hibernate intentaba insertar el expediente con `folio`
nulo. El mensaje `current transaction is aborted` y el `UnexpectedRollbackException`
eran consecuencias de ese primer error, no problemas de autenticación.

### Solución aplicada

`ExpedienteService.obtenerOCrear` ahora resuelve el folio desde
`AspiranteRepository` cuando recibe un valor nulo o vacío. Así, tanto la carga
normal como la validación automática concurrente pueden crear el expediente
con el folio real del aspirante. La columna continúa siendo `NOT NULL` y no se
introduce un folio sintético que pudiera quedar desincronizado.

La validación recomendada es compilar `backend-core`, reiniciar el servicio y
subir nuevamente un documento desde `http://localhost:3007/panel`. Debe
responder `200`, crear el expediente con el folio del aspirante y no generar
el error de transacción abortada.

## Problema anterior: 403 Forbidden

## Problema Original

Al intentar subir un documento (CV) desde el frontend (`http://localhost:3007/panel`) al backend (`http://localhost:8087/api/v1/documentos/upload`), se recibía un error **403 Forbidden** aunque el backend procesaba correctamente el documento en MinIO.

### Síntomas
- Frontend: Error "El backend rechazó la solicitud. Verifica los permisos o intenta de nuevo."
- Browser Console: `POST http://10.15.0.59:8087/api/v1/documentos/upload 403 ()`
- Backend: El documento se cargaba exitosamente en MinIO pero la respuesta era 403

## Causas Raíz Identificadas

### 1. Contradicción en SecurityConfig.java
**Problema:** Se creaba un bean `requestCache()` pero luego se deshabilitaba en la cadena de seguridad, causando conflictos.

```java
// ❌ INCORRECTO - Contradicción
@Bean
public RequestCache requestCache() {
    HttpSessionRequestCache cache = new HttpSessionRequestCache();
    cache.setMatchingRequestParameterName(null);
    return cache;
}

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // ... otras configuraciones
        .requestCache(cache -> cache.disable()) // ❌ Deshabilita el bean que acabamos de crear
        // ...
}
```

### 2. Uso de Mono<> en Controlador Spring MVC
**Problema:** El controlador usaba respuestas reactivas (`Mono<>`) en un entorno Spring MVC tradicional, no WebFlux.

```java
// ❌ INCORRECTO - Mezcla de paradigmas
@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public Mono<UploadResponse> cargarDocumento(...) {
    return documentoService.cargarDocumento(...);
}
```

Esto causaba problemas de serialización porque:
- Spring MVC no maneja correctamente respuestas reactivas
- El contenido multipart se consumía antes de serializar la respuesta
- La respuesta no se enviaba correctamente al cliente

### 3. Problema Conocido de Spring Security con multipart/form-data
Spring Security tiene un problema documentado donde las solicitudes multipart se consumen completamente antes de validar la autenticación, causando errores 403 incluso con tokens válidos.

Referencias:
- [Spring Security Issue #7060](https://github.com/spring-projects/spring-security/issues/7060)
- [Spring Security Issue #10326](https://github.com/spring-projects/spring-security/issues/10326)

## Soluciones Aplicadas

### 1. Corrección de SecurityConfig.java
**Solución:** Eliminar el bean contradictorio y deshabilitar directamente el request cache.

```java
// ✅ CORRECTO
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .requestCache(cache -> cache.disable()) // ✅ Deshabilitado directamente
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/**").permitAll()
            .requestMatchers("/api/v1/auth/**").permitAll()
            .requestMatchers("/api/v1/bloques-declaratorios").permitAll()
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

### 2. Corrección de DocumentoController.java
**Solución:** Cambiar de respuestas reactivas a síncronas usando `.block()`.

```java
// ✅ CORRECTO - Respuestas síncronas
@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<UploadResponse> cargarDocumento(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "tipo", required = false, defaultValue = "CV") String tipo,
        @RequestHeader(value = "Authorization", required = false) String authHeader) {
    
    try {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            log.error("❌ No hay autenticación en SecurityContext");
            return ResponseEntity.status(403).build();
        }
        
        UUID aspiranteId = UUID.fromString(authentication.getPrincipal().toString());
        String folio = jwtService.extractFolio(authHeader.substring(7));
        
        log.info("📤 POST /api/v1/documentos/upload - Aspirante: {} | Tipo: {} | Archivo: {}", 
                 aspiranteId, tipo, file.getOriginalFilename());
        
        // ✅ Bloquear el Mono para obtener el resultado de forma síncrona
        UploadResponse response = documentoService.cargarDocumento(file, aspiranteId, folio, tipo).block();
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        log.error("❌ Error al procesar upload: {}", e.getMessage(), e);
        return ResponseEntity.status(500).build();
    }
}
```

**Cambios aplicados a todos los endpoints:**
- `Mono<UploadResponse>` → `ResponseEntity<UploadResponse>`
- `Mono<List<DocumentoResponse>>` → `ResponseEntity<List<DocumentoResponse>>`
- `Mono<DocumentoResponse>` → `ResponseEntity<DocumentoResponse>`
- `Mono<Void>` → `ResponseEntity<Void>`
- `Mono<byte[]>` → `ResponseEntity<byte[]>`
- Agregar `.block()` a todas las llamadas al servicio

### 3. Uso de SecurityContextHolder
**Solución:** Obtener el `aspiranteId` del `SecurityContextHolder` en lugar de extraerlo manualmente del token.

```java
// ✅ CORRECTO - Usar SecurityContext
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
UUID aspiranteId = UUID.fromString(authentication.getPrincipal().toString());
```

Esto asegura que:
- La autenticación ya fue validada por el `JwtAuthenticationFilter`
- No hay duplicación de lógica de extracción de token
- El controlador usa el contexto de seguridad estándar de Spring

## Recomendaciones para Futuros Módulos de Carga

### 1. Configuración de Seguridad
Siempre deshabilitar el `requestCache` en endpoints que manejan archivos:

```java
.requestCache(cache -> cache.disable())
```

### 2. Paradigma de Programación
**NO mezclar** Spring MVC con WebFlux:
- Si el proyecto usa Spring MVC → Usar respuestas síncronas (`ResponseEntity<>`)
- Si el proyecto usa WebFlux → Usar respuestas reactivas (`Mono<>`, `Flux<>`)
- Nunca mezclar ambos paradigmas en el mismo controlador

### 3. Manejo de Autenticación
Siempre obtener la información del usuario del `SecurityContextHolder`:

```java
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
String userId = authentication.getPrincipal().toString();
```

### 4. Logging Detallado
Agregar logging en el filtro JWT para diagnosticar problemas de autenticación:

```java
log.debug("🔍 [JWT FILTER] {} {} - Authorization header: {}", method, requestUri, 
          authHeader != null ? (authHeader.substring(0, Math.min(20, authHeader.length())) + "...") : "null");
```

### 5. Manejo de Errores
Siempre manejar excepciones en endpoints de carga:

```java
try {
    // Lógica de carga
    return ResponseEntity.ok(response);
} catch (Exception e) {
    log.error("❌ Error al procesar upload: {}", e.getMessage(), e);
    return ResponseEntity.status(500).build();
}
```

### 6. Validación de Autenticación
Verificar que el usuario esté autenticado antes de procesar:

```java
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
if (authentication == null || authentication.getPrincipal() == null) {
    log.error("❌ No hay autenticación en SecurityContext");
    return ResponseEntity.status(403).build();
}
```

## Archivos Modificados

1. **f:\codes\Gestiona T\backend-core\src\main\java\mx\ine\gestiona_t\config\SecurityConfig.java**
   - Eliminado bean `requestCache()` contradictorio
   - Movido `.requestCache(cache -> cache.disable())` antes de `.authorizeHttpRequests()`

2. **f:\codes\Gestiona T\backend-core\src\main\java\mx\ine\gestiona_t\modules\documentos\controller\DocumentoController.java**
   - Cambiado todos los métodos de `Mono<>` a `ResponseEntity<>`
   - Agregado `.block()` a todas las llamadas al servicio
   - Eliminado import `reactor.core.publisher.Mono`
   - Usar `SecurityContextHolder` para obtener autenticación

3. **f:\codes\Gestiona T\backend-core\src\main\java\mx\ine\gestiona_t\modules\auth\service\JwtAuthenticationFilter.java**
   - Agregado logging detallado para diagnóstico

## Verificación

Para verificar que la solución funciona correctamente:

1. Iniciar el backend-core:
   ```powershell
   cd "f:\codes\Gestiona T\backend-core"
   mvn spring-boot:run
   ```

2. Iniciar el frontend:
   ```powershell
   cd "f:\codes\Gestiona T\frontend"
   npm run dev
   ```

3. Iniciar sesión en `http://localhost:3007/login`

4. Navegar a `http://localhost:3007/panel`

5. Intentar subir un archivo PDF (máximo 10MB)

6. Verificar que:
   - No aparezca error 403
   - El archivo se cargue correctamente
   - No se cierre la sesión

## Referencias

- [Spring Security Documentation - Multipart](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#multipart)
- [Spring Security Issue #7060 - Multipart file request with no authentication](https://github.com/spring-projects/spring-security/issues/7060)
- [Spring Security Issue #10326 - DefaultBearerTokenResolver triggers processing of multipart content](https://github.com/spring-projects/spring-security/issues/10326)
- [Spring Boot - Multipart File Upload](https://spring.io/guides/gs/uploading-files/)

---
**Fecha:** 24 de julio de 2026  
**Proyecto:** Gestiona T  
**Módulo:** Carga de Documentos  
**Estado:** Resuelto ✅
