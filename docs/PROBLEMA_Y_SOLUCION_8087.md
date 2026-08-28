# Problema detectado: error 400 al guardar CV institucional (validación de payload)

## Descripción del problema
Al completar el formulario de CV institucional y presionar `Guardar CV Completo`, el frontend mostraba:

- `❌ Error al guardar el CV. Verifica los datos e intenta de nuevo.`

En DevTools se observó que la petición `POST http://10.15.0.59:8087/api/v1/cv` devolvía **400 Bad Request** y la consola del frontend no mostraba inicialmente detalles legibles.

## Causa raíz
El backend valida el payload con DTOs que usan restricciones (`@NotBlank`, `@NotNull`) y tipos fuertemente tipados (`LocalDate`). El frontend enviaba campos en formatos no compatibles con esas validaciones, por ejemplo:

- Campos de fecha vacíos enviados como cadena `""` en lugar de `null`.
- Filas vacías o parcialmente completas (formación/experiencia/idiomas) que llegaban sin filtrado.
- Algunos campos requeridos llegaban vacíos, provocando que Spring devolviera 400 con detalles de validación.

## Solución aplicada
Se aplicaron cambios en el frontend para evitar el envío de payloads inválidos y mejorar la retroalimentación al usuario:

1. `frontend/src/app/(protected)/cv/page.tsx` (modificado)
   - Implementación de validaciones inline por campo (`errors` state).
   - Validación en tiempo real en `handleChange` y `handleArrayChange`.
   - `runFullValidation` antes de enviar para construir un mapa de errores por campo y mostrarlos inline.
   - Manejo mejorado del `catch` para mostrar los `data.message` o `data.details` que devuelve el backend.

2. Presentación en UI
   - Mensajes de error mostrados bajo los inputs obligatorios en `formacionAcademica`, `experienciaLaboral` e `idiomas`.
   - Se mantiene la deshabilitación de `fechaFin` cuando la experiencia es `Actual`.

3. Backend (observado)
   - El backend ya cuenta con DTOs que validan campos: `backend-core/.../CvInstitucionalRequest.java`.
   - El manejador global de excepciones `GlobalExceptionHandler` devuelve ahora mensajes/descripciones claras para `HttpMessageNotReadableException` y `MethodArgumentNotValidException` (si corresponde). Si tu versión del backend no incluye estas mejoras, revisa `backend-core/src/main/java/mx/ine/gestiona_t/common/exceptions/GlobalExceptionHandler.java`.

Nota: los cambios aplicados en este repo fueron principalmente en el frontend para evitar el 400; si prefieres que el backend relaje o transforme input (p. ej. aceptar cadenas vacías como null), puedo proponer un parche servidor.

## Archivos modificados
- `frontend/src/app/(protected)/cv/page.tsx` — validaciones inline y manejo de errores.
- (previo análisis) `frontend/src/lib/api.ts` — interceptor ya registra y muestra tokens y errores en consola.

## Verificación y pasos para reproducir
1. Levanta servicios (ver GUIA_LEVANTAR_SERVICIOS.md):

```powershell
cd "F:\codes\Gestiona T"
docker-compose up -d
cd "F:\codes\Gestiona T\frontend"
npm run dev
cd "F:\codes\Gestiona T\backend-core"
mvn spring-boot:run
```

2. Abrir UI: `http://10.15.0.59:3007/cv`.
3. Rellenar los campos obligatorios (Formación: `nivel`, `carrera`, `institución`, `fechaInicio`; Experiencia: `tipoExperiencia`, `empresa`, `puesto`, `fechaInicio` salvo cuando `tipoExperiencia` = `Actual`; Idiomas: `idioma`).
4. Hacer click en `Guardar CV Completo`.

Resultados esperados:
- Si faltan campos obligatorios: se muestran mensajes inline y el envío se bloquea.
- Si el servidor responde `400`, ahora el frontend mostrará el `message` o los `details` de la respuesta en un alert para facilitar corrección.

## Siguientes pasos recomendados
- Si deseas, implemento validación inline adicional (fechas lógicas: `fechaInicio <= fechaFin`) y estilos para los mensajes.
- Si prefieres que el backend convierta automáticamente cadenas vacías en `null` para fechas, puedo proponer el cambio en los DTOs o en un `@ControllerAdvice` de deserialización.

---
Documentado y actualizado para reflejar la solución actual (validaciones cliente y mejor manejo de errores). Si quieres, aplico los cambios backend sugeridos o añado validación adicional en la UI.
