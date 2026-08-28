# Problema y solución: postulaciones y expedientes laborales

**Fecha:** 25 de agosto de 2026

## Problema identificado

En `http://localhost:3007/postulaciones` se detectaron las siguientes inconsistencias:

- Una postulación podía conservar el dictamen final `SELECCIONADO`, pero continuar mostrando el estatus operativo `EN_REVISION`.
- Al editar una postulación se cargaban las calificaciones y el dictamen, pero no se consultaban las evidencias previamente cargadas:
  - Examen de Conocimientos.
  - Examen Psicométrico.
  - Formato de Entrevista.
- La pantalla de evaluación esperaba campos planos del CV (`experiencia` y `educacion`), aunque la API devuelve listas de experiencia laboral y formación académica. Como resultado, podía mostrarse únicamente la información de habilidades o presentar los demás apartados como vacíos.

En `http://localhost:3007/expedientes-laborales`:

- Existía un endpoint backend `PUT` para actualizar expedientes, pero la interfaz no ofrecía una acción de edición.
- La acción `Ver detalle` apuntaba a una ruta que no existía en el frontend.
- Al corregir el número de empleado en un expediente, el número almacenado en el aspirante podía quedar desactualizado.
- La edición no permitía limpiar correctamente algunos valores opcionales, como la fecha de fin.
- No existía una relación persistente entre un expediente laboral y `vacanteId`, por lo que no era posible validar automáticamente que el puesto capturado coincidiera con la vacante postulada.
- El alta de un expediente laboral no permitía adjuntar el documento soporte en PDF.
- El detalle del expediente no mostraba el documento soporte ni ofrecía previsualización.
- La edición no permitía sustituir el documento soporte asociado.

## Solución realizada

### Postulaciones

- Se agregó la consulta de documentos mediante:
  - `GET /documentos/por-aspirante/{aspiranteId}`.
- Las evidencias existentes ahora aparecen en la pantalla de edición con nombre, estatus y enlace para visualizarlas.
- Se adaptó la representación del CV a la respuesta real de la API:
  - `experienciaLaboral` muestra puesto, empresa, funciones y fechas.
  - `formacionAcademica` muestra nivel, carrera e institución.
  - `habilidades` continúa mostrándose como campo independiente.
- Al guardar el dictamen final, el backend sincroniza el estatus operativo:
  - `SELECCIONADO` -> `ACEPTADA`.
  - `NO_SELECCIONADO` -> `RECHAZADA`.
  - `PENDIENTE` -> `EN_REVISION`.

### Expedientes laborales

- Se añadió la acción `Editar` en la tabla de expedientes.
- Se creó la pantalla de edición dinámica para consultar y actualizar todos los datos laborales.
- Se agregó un alias compatible para `/expedientes-laborales/{id}/editar`.
- Se mantuvieron las validaciones de número de empleado, tipo de contratación, junta ejecutiva y vocalía.
- Al cambiar el número de empleado, también se actualiza el registro correspondiente del aspirante.
- La fecha de fin puede limpiarse enviando `null`.
- Al cambiar de contratación electoral a otro tipo, se limpian junta ejecutiva y vocalía.
- La relación opcional `vacante_id` se valida al crear y editar el expediente.
- El backend rechaza puestos que no coincidan con la vacante vinculada.
- El backend rechaza niveles tabulares incompatibles cuando la vacante publica uno.
- Los errores de discrepancia se devuelven en JSON para que el frontend muestre el motivo concreto.
- El alta exige seleccionar un archivo PDF como documento soporte.
- El soporte se carga mediante `POST /documentos/expediente-laboral/{expedienteLaboralId}/soporte` y queda asociado al expediente laboral.
- El detalle de edición muestra el nombre del archivo vigente y permite previsualizarlo en una ventana flotante usando `GET /documentos/{id}/view`.
- Al seleccionar un nuevo PDF durante la edición, el soporte anterior se elimina y se sustituye por el nuevo archivo.
- El backend rechaza archivos vacíos o que no tengan tipo de contenido `application/pdf`.
- El expediente devuelve `documentoSoporteId` y `documentoSoporteNombre` para que el frontend pueda identificar el archivo asociado.

## Archivos principales modificados

- `frontend/src/app/(admin)/postulaciones/[id]/page.tsx`
- `frontend/src/app/(admin)/expedientes-laborales/page.tsx`
- `frontend/src/app/(admin)/expedientes-laborales/[id]/page.tsx`
- `frontend/src/app/(admin)/expedientes-laborales/[id]/editar/page.tsx`
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/postulaciones/controller/PostulacionController.java`
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/expedientes/service/ExpedienteLaboralServiceImpl.java`
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/controller/DocumentoController.java`
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/service/DocumentoService.java`
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/service/DocumentoServiceImpl.java`
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/model/enums/TipoDocumento.java`
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/repository/DocumentoRepository.java`
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/dto/response/DocumentoResponse.java`
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/expedientes/dto/response/ExpedienteLaboralResponse.java`
- `frontend/src/components/PdfViewerModal.tsx`

## Validación

- Diagnósticos del editor para los archivos frontend y backend modificados: sin errores.
- `mvn -DskipTests compile` en `backend-core`: exitoso.
- `npm run build` en `frontend`: exitoso, incluyendo typecheck, generación de páginas y optimización de producción.
- `npm run lint` continúa reportando errores preexistentes en archivos auxiliares `._*` y en otras rutas no relacionadas; no se generaron errores en los archivos modificados.

## Estado de la vinculación con la vacante

La migración `migracion_vacante_expediente.sql` agregó `vacante_id` e índice en `expedientes_laborales`. La relación es nullable para conservar expedientes históricos creados sin vacante.

Cuando el expediente se vincula a una vacante, el servicio valida el puesto y, si está definido, el nivel tabular. La advertencia visual se mantiene como ayuda inmediata, pero la regla definitiva se aplica en backend y bloquea el guardado de datos incompatibles.

El área de adscripción y otros requisitos no se bloquean porque la entidad `Vacante` no contiene un campo estructurado equivalente para compararlos de forma confiable.
