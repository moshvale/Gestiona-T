# Solución al error de carga de documentos del analista

## Resumen del problema reproducido

Al acceder como **Analista** a una postulación, fallaba la carga de:

- Examen de Conocimientos
- Examen Psicométrico
- Formato de Entrevista

La petición llegaba autenticada a:

```text
POST http://localhost:8087/api/v1/documentos/upload
```

El backend respondía `500 Internal Server Error`. El JWT era válido, el analista estaba autenticado y el archivo se subía a MinIO.

## Causa raíz

La pantalla de evaluación enviaba estos valores en el campo multipart `tipo`:

```text
EVALUACION_CONOCIMIENTOS
EVALUACION_PSICOMETRICA
EVALUACION_ENTREVISTA
```

Sin embargo, `TipoDocumento` no tenía esas constantes. En `DocumentoServiceImpl.cargarDocumento`, esta línea intentaba convertir el texto recibido:

```java
TipoDocumento.valueOf(tipoDocumentoStr)
```

Al recibir `EVALUACION_ENTREVISTA`, Java lanzaba:

```text
No enum constant mx.ine.gestiona_t.modules.documentos.model.enums.TipoDocumento.EVALUACION_ENTREVISTA
```

Por eso el error no era de CORS, JWT, permisos del analista ni del archivo: era un desajuste entre el contrato del frontend y el enum persistido por backend.

## Solución aplicada

Se agregaron al enum `TipoDocumento` las tres constantes que ya utiliza la pantalla:

```java
EVALUACION_CONOCIMIENTOS,
EVALUACION_PSICOMETRICA,
EVALUACION_ENTREVISTA
```

También se registraron en `ClasificadorDocumentoService` como `TipoValidacion.TIPO_C`. Esto permite guardar las evidencias y enviarlas a revisión manual, sin ejecutar validaciones automáticas diseñadas para documentos oficiales como INE, RFC o cédula profesional.

La columna se persiste con `EnumType.STRING` y longitud 30; los nuevos nombres caben en esa columna, por lo que no se requiere migración de base de datos.

## Flujo corregido

1. El analista obtiene un JWT válido y accede a la postulación.
2. La pantalla envía `file`, `tipo` y `aspiranteId`.
3. `DocumentoController` valida el analista y obtiene el aspirante correcto.
4. `DocumentoServiceImpl` convierte el tipo recibido a una constante existente.
5. El archivo se guarda en MinIO y el registro se guarda con `TIPO_C`.
6. La evidencia queda disponible para revisión manual.

## Diferencia con el diagnóstico anterior

Este documento describía originalmente un `403` causado por autorización y asociación incorrecta del aspirante. Esas correcciones siguen siendo necesarias y ya están reflejadas en `SecurityConfig`, `DocumentoController` y la pantalla de evaluación. El incidente actual es posterior: el `500` ocurre después de autorizar la petición, al convertir el tipo de documento.

## Archivos involucrados

- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/model/enums/TipoDocumento.java`
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/service/ClasificadorDocumentoService.java`
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/service/DocumentoServiceImpl.java`
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/controller/DocumentoController.java`
- `frontend/src/app/(admin)/postulaciones/[id]/page.tsx`

## Validación

Se ejecutó correctamente:

```text
cd backend-core
mvn -q -DskipTests compile
```

## Prueba funcional recomendada

Después de reiniciar `backend-core`, iniciar sesión como analista, abrir una postulación y cargar un archivo para cada evidencia. Cada petición debe responder `200 OK`; los documentos deben aparecer asociados al aspirante evaluado y con tipo `TIPO_C` para revisión manual.
