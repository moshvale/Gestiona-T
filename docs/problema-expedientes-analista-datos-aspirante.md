# Problema y solución: expedientes sin datos para el analista

**Fecha:** 2026-08-19

## Síntoma

Al iniciar sesión como `analista@ine.mx` y abrir:

```text
http://localhost:3007/expedientes
```

la pantalla mostraba al aspirante de prueba `armando.valeriano@ine.mx`, pero al entrar al expediente las consultas de carta declaratoria y perfil devolvían `404 Not Found` para:

```text
fdf16c0e-7f2c-4497-b487-e8e91284ad1b
```

El frontend terminaba mostrando datos genéricos y una lista sin la información cargada por el aspirante.

## Diagnóstico

La autenticación no era el problema:

- El JWT era válido.
- El backend identificaba correctamente a `analista@ine.mx` con rol `ANALISTA_UR`.
- CORS y autorización permitían las solicitudes.

La causa estaba en la lista frontend de expedientes:

- `frontend/src/app/(admin)/expedientes/page.tsx` usaba datos mock hardcodeados.
- El UUID `fdf16c0e-...` se enviaba como si fuera el `aspiranteId` real.
- No existía una consulta al backend para obtener los aspirantes reales.
- Los endpoints `/auth/aspirante/{id}`, `/documentos/por-aspirante/{id}` y `/carta-declaratoria/por-aspirante/{id}` buscan por la columna `aspirantes.id`, no por un UUID inventado ni por el correo mostrado en la tabla.

Por eso el backend respondía correctamente `404`: el identificador recibido no correspondía con un registro existente en `aspirantes`.

## Solución aplicada

### Backend

Se agregó `AspiranteExpedienteResumenDTO`, que expone únicamente información necesaria para el panel:

- `id` real del aspirante.
- `folio`.
- Nombre y correo.
- Estado general.
- Totales y progreso de documentos.

Se amplió `GET /api/v1/admin/aspirantes` en `AdminAspiranteController` para:

1. Consultar aspirantes activos desde `AspiranteRepository`.
2. Obtener el expediente digital asociado por `aspiranteId`.
3. Devolver un resumen seguro sin `passwordHash`.
4. Permitir el acceso a `ROLE_ANALISTA_UR` y `ROLE_ADMIN_SISTEMA`.

El endpoint existente `GET /api/v1/admin/aspirantes/{folio}` se conservó sin cambios.

### Frontend

Se modificó `admin.service.ts` para consultar:

```text
GET /api/v1/admin/aspirantes
```

Se eliminó la lista mock de `expedientes/page.tsx`.

La tabla ahora:

- Carga los aspirantes reales desde el backend.
- Usa `exp.id` como clave de fila.
- Navega a `/expedientes/{id}` usando el UUID real del aspirante.
- Mantiene `folio` como dato visible y de búsqueda.

El detalle puede seguir utilizando sus endpoints actuales por `aspiranteId`, pero ahora recibe un identificador válido.

## Verificación

Se ejecutó compilación del backend con memoria acotada para evitar el agotamiento de memoria del entorno:

```text
mvn -DskipTests -T 1 compile
BUILD SUCCESS
```

También se ejecutó el chequeo TypeScript del frontend:

```text
npx tsc --noEmit
```

Sin errores reportados.

## Pasos para probar

1. Reiniciar `backend-core` para cargar el nuevo endpoint.
2. Recargar `http://localhost:3007/expedientes`.
3. Iniciar sesión como `analista@ine.mx`.
4. Buscar `armando.valeriano@ine.mx`.
5. Seleccionar el expediente.
6. Confirmar en Network que la ruta contiene el `id` real entregado por `GET /api/v1/admin/aspirantes`.

La ausencia de carta declaratoria seguirá siendo un `404` válido si el aspirante todavía no la ha firmado; ese caso se maneja como estado opcional. El perfil y los documentos deben dejar de devolver `404` cuando el aspirante exista y tenga datos asociados.
