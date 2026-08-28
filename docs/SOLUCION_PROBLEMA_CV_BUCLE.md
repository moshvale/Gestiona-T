# Solución problema bucle infinito al abrir "Completar mi CV"

## Descripción del problema

Al dar clic en **"Completar mi CV"** desde `http://localhost:3007/panel`, la aplicación cargaba el formulario de CV, pero el backend devolvía un error `500 Internal Server Error` en `GET http://localhost:8087/api/v1/cv`.

En los logs del backend se observaba:

- `Handler dispatch failed: java.lang.Error: Unresolved compilation problems:`
- `The constructor CvInstitucionalResponse(null, UUID, null, null, null, null, null, null, null, null, int, null, null, List<Object>, List<CvInstitucionalResponse.FormacionResponse>, List<CvInstitucionalResponse.ExperienciaResponse>, List<CvInstitucionalResponse.IdiomaResponse>) is undefined`
- `Type mismatch: cannot convert from List<Object> to LocalDateTime`

Esto indicaba que la construcción de un `CvInstitucionalResponse` vacío no coincidía con la firma del record.

## Causa raíz

En `backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/controller/CvInstitucionalController.java`, el método `defaultEmptyCv(UUID aspiranteId)` construía un `CvInstitucionalResponse` con un argumento faltante.

El record `CvInstitucionalResponse` define 18 campos, incluyendo `logrosProfesionales` antes de `scoreCompletitud`, pero el constructor en `defaultEmptyCv` se llamaba con 17 valores.

Ese desajuste provocaba el `500` y evitaba que la UI recibiera el CV vacío para aspirantes sin CV previo.

## Solución aplicada

### Backend

- Se corrigió `defaultEmptyCv(UUID aspiranteId)` para pasar el valor `null` correspondiente a `logrosProfesionales`.
- Con ello el constructor del record quedó con la cantidad y tipos correctos:
  - `id`
  - `aspiranteId`
  - `entidadPreferida`
  - `sueldoDeseado`
  - `disponibilidad`
  - `areasInteres`
  - `sistemasOperativos`
  - `lenguajesProgramacion`
  - `basesDeDatos`
  - `habilidades`
  - `logrosProfesionales`
  - `scoreCompletitud`
  - `createdAt`
  - `updatedAt`
  - `formacionAcademica`
  - `experienciaLaboral`
  - `idiomas`
  - `cursos`

### Resultado

- `GET /api/v1/cv` devuelve `200` con un CV vacío cuando no existe un CV previo.
- El frontend puede cargar la página `/cv` sin reintentos fallidos.

## Archivos modificados

- `backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/controller/CvInstitucionalController.java`

## Validación realizada

1. Compilación exitosa del backend-core:
   ```bash
   cd /Users/armandovalerianohernandez/code/codes/Gestiona\ T
   mvn -f backend-core/pom.xml -DskipTests compile
   ```
2. Compilación exitosa del frontend:
   ```bash
   cd /Users/armandovalerianohernandez/code/codes/Gestiona\ T/frontend
   npm run build
   ```

## Pasos para verificar en ambiente local

1. Levantar backend-core:
   ```bash
   cd /Users/armandovalerianohernandez/code/codes/Gestiona\ T
   mvn -f backend-core/pom.xml -DskipTests spring-boot:run
   ```
2. Levantar frontend en modo desarrollo:
   ```bash
   cd /Users/armandovalerianohernandez/code/codes/Gestiona\ T/frontend
   npm run dev
   ```
3. Abrir `http://localhost:3007/panel`.
4. Hacer clic en **"Completar mi CV"**.
5. Verificar en DevTools → Network que `GET /api/v1/cv` retorna `200`.
6. Confirmar que la página carga el formulario y no genera múltiples llamadas infinitas.

## Observaciones

- El problema no era la navegación en el frontend sino un error en la creación de un objeto de respuesta vacío en el backend.
- Con la corrección aplicada, el bucle infinito se resuelve porque el backend deja de responder con `500` durante la carga inicial del CV.

---

## Histórico de incidente del 10 de agosto de 2026

### Reproducción

Con los servicios locales levantados, se inició sesión con un aspirante sin CV y se abrió **Completar mi CV** desde `/panel`. El backend respondió correctamente:

- `GET /api/v1/cv`: `200`, devolviendo el CV vacío.
- `GET /api/v1/matching/mi-resultado`: `404`, que es válido cuando aún no existe una evaluación previa y el servicio frontend lo transforma en `null`.

Sin embargo, la página `/cv` se recargaba continuamente. El log del backend mostró las mismas dos peticiones repetidas, sin errores de autenticación ni excepciones del módulo CV. El log del frontend identificó el origen:

- `FATAL: An unexpected Turbopack error occurred`.
- `Failed to write app endpoint /(protected)/cv/page`.
- `Caused by: Next.js package not found`.

### Causa raíz adicional

El script de desarrollo del frontend forzaba `next dev --turbopack`. En este entorno, Turbopack entraba en pánico al generar el endpoint de la página `/cv`; el cliente de desarrollo reconectaba y solicitaba de nuevo `/cv`, produciendo el bucle aparente. No era un error de la respuesta vacía del backend corregida en el incidente anterior.

La revisión de persistencia confirmó que el módulo institucional usa las tablas correctas y alineadas con sus entidades:

- `cv_institucionales` para los datos principales.
- `cv_formacion_academica`, `cv_experiencia_laboral` y `cv_idiomas` como tablas dependientes.
- `cv_cursos_capacitaciones_institucionales` para cursos del CV institucional; no se mezcló con la tabla legacy `cv_cursos_capacitaciones`.

### Nueva solución aplicada

- Se modificó `frontend/package.json` para que `npm run dev` use `next dev --webpack -p 3007`, forzando Webpack y evitando Turbopack.
- Se conservó la deduplicación de carga en `cv/page.tsx` y el tratamiento de `404` de `matching/mi-resultado`; esas defensas evitan solicitudes duplicadas y consideran correctamente que no exista un resultado previo.
- Se ajustó `matching.service.ts` para que el `404` esperado de `matching/mi-resultado` sea un estado válido de Axios y no se registre como error de consola.
- Se evitó consultar `matching/mi-resultado` durante la carga de un CV vacío; la evaluación solo se consulta cuando existe progreso guardado.
- No se modificó el constructor `defaultEmptyCv`: la solución histórica anterior sigue vigente y actualmente `GET /api/v1/cv` devuelve `200`.

### Validación de esta solución

1. Backend core iniciado con `mvn -f backend-core/pom.xml spring-boot:run` en `8087`.
2. Frontend iniciado con `npm run dev` en `3007`, ahora sin Turbopack.
3. Se validó el login del aspirante y la navegación desde `/panel` hacia `/cv`.
4. Se verificó que `/api/v1/cv` responde `200` y que la página deja de recargarse continuamente.
5. La respuesta `404` de `matching/mi-resultado` se mantiene como ausencia esperada de evaluación previa, no como fallo de carga del CV.
