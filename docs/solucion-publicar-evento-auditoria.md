# Solución: Error de constructor en `AuditoriaAspect.java`

## Problema

En el archivo `backend-core/src/main/java/mx/ine/gestiona_t/modules/auditoria/aspect/AuditoriaAspect.java` se intentaba crear un objeto `PublicarEventoRequest` con un constructor que no existía.

El error de compilación era:

- `The constructor PublicarEventoRequest(String, String, NivelSeveridad, UUID, ActorTipo, String, UUID, String, Map<String,Object>) is undefined`

Esto ocurría porque el `record` `PublicarEventoRequest` definía 10 parámetros, mientras que el código estaba enviando solamente 9.

## Análisis

La clase `PublicarEventoRequest` se define con estos campos:

- `CategoriaEvento categoria`
- `TipoEvento tipoEvento`
- `NivelSeveridad severidad`
- `UUID actorId`
- `ActorTipo actorTipo`
- `String recursoAfectado`
- `String descripcion`
- `Map<String, Object> datosEvento`
- `String moduloOrigen`
- `String correlationId`

Además, la anotación `@Auditable` en el proyecto entregaba `categoria` y `tipo` como `String`, no como valores de enum.

## Solución aplicada

1. Modifiqué `AuditoriaAspect.java` para construir correctamente el `PublicarEventoRequest` con los 10 parámetros.
2. Agregué el mapeo de las cadenas de `@Auditable` a los enums `CategoriaEvento` y `TipoEvento` mediante métodos auxiliares:
   - `extraerCategoriaEvento(String categoria)`
   - `extraerTipoEvento(String tipo)`
3. Agregué extracción de `moduloOrigen` y `correlationId` desde el `HttpServletRequest` para pasarlos al constructor.
4. Añadí `entidadId` al mapa `datos` para evitar que la variable quedara declarada sin uso.
5. Verifiqué que no quedaran errores de Java en `AuditoriaAspect.java`.

## Archivo modificado

- `backend-core/src/main/java/mx/ine/gestiona_t/modules/auditoria/aspect/AuditoriaAspect.java`

## Resultado

El problema de compilación se resolvió y el código ahora usa el constructor adecuado de `PublicarEventoRequest`.
