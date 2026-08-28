# Problema y solucion: acciones de validacion en expedientes

## Problema

Al iniciar sesion con `analista@ine.mx` y abrir el expediente del usuario de
pruebas `armando.valeriano@ine.mx`, no aparecian las acciones para aceptar o
rechazar manualmente los documentos.

## Causas identificadas

### 1. La interfaz trataba los rechazos automaticos como definitivos

La tabla de documentos ocultaba las acciones cuando el estatus era
`RECHAZADO`. En la base local, los documentos de `armando.valeriano@ine.mx`
estaban rechazados por la validacion automatica de IA, por lo que la interfaz
mostraba `Procesado` en lugar de los botones `Validar` y `Rechazar`.

El endpoint del backend si permite una decision manual posterior mediante:

```text
POST /api/v1/documentos/{id}/validar
```

### 2. Habia reglas de seguridad despues de `anyRequest()`

Spring Security no permite registrar `requestMatchers()` despues de
`.anyRequest()`. Las reglas de vacantes agregadas al final de la cadena
dejaban el mismo problema de arranque que ya habia ocurrido con las rutas de
administracion de analistas.

## Solucion aplicada

### Frontend

En `frontend/src/app/(admin)/expedientes/[folio]/page.tsx`, las acciones ahora
se ocultan unicamente cuando el documento esta en `VALIDADO_MANUAL`.

Esto permite al analista:

- corregir un rechazo automatico de IA;
- validar manualmente un documento rechazado;
- rechazar manualmente un documento que requiere correccion;
- revisar nuevamente documentos con validacion automatica.

### Backend

En `backend-core/src/main/java/mx/ine/gestiona_t/config/SecurityConfig.java`,
las reglas `GET`, `POST`, `PUT` y `DELETE` de vacantes fueron movidas antes de
`.anyRequest().authenticated()`. Esta regla queda al final, como exige Spring
Security.

Tambien se conserva la regla especifica de
`/api/v1/admin/analistas/**` antes de la regla general de administracion.

## Validacion

- La consulta de base de datos confirmo que los 9 documentos del usuario de
  pruebas estaban en `RECHAZADO` por la IA.
- La pagina del expediente no presento errores de TypeScript.
- ESLint paso sin errores en la pagina modificada; solo reporto dos warnings
  preexistentes por variables `error` no utilizadas en bloques `catch`.
- `mvn spring-boot:run` desde `backend-core` inicializo correctamente el
  contexto de Spring Security y no volvio a mostrar `Can't configure
  mvcMatchers after anyRequest`.
- La prueba de arranque no pudo abrir otra instancia porque el puerto `8087`
  ya estaba ocupado; esto es un conflicto de proceso local independiente de
  la configuracion de seguridad.