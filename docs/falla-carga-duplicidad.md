# Falla: cargas duplicadas de documentos (PDF)

Fecha: 2026-08-11

Resumen
- La carga duplicada se reprodujo en el flujo de `Documentos Oficiales` cuando el mismo documento o un formato equivalente se intentaba subir de nuevo para el mismo aspirante.
- El fallo real no fue por credenciales: el login quedó verificado con `HTTP 200` para `armando.valeriano@ine.mx` con la contraseña proporcionada.
- La causa raíz fue una colisión de inserción en PostgreSQL por la restricción `uq_documentos_aspirante_storage_tipo`, que indica que ya existía un registro con el mismo `aspirante_id`, `storage_path` y `tipo_documento`.
- El efecto visible en frontend era el alert `Error al subir el archivo`, aunque el documento ya quedaba guardado y podía verse tras recargar la página.

Análisis
- La ruta del backend que se usa es `POST /api/v1/documentos/upload`.
- En la prueba real, los logs del backend mostraron esta secuencia:
  - `Documento subido: documentos/.../archivo.pdf`
  - `✅ Validación automática completada exitosamente`
  - inmediatamente después: `ERROR: duplicate key value violates unique constraint "uq_documentos_aspirante_storage_tipo"`
- Eso confirma que el archivo sí se subía a MinIO y el proceso de validación arrancaba, pero el `INSERT` en la tabla `documentos` fallaba por duplicidad.
- El problema no era la sesión ni el login; era la lógica del backend ante un reintento o una carga repetida.

Causa raíz definitiva
- Se estaba intentando guardar un documento que ya existía para ese aspirante y ese tipo en la misma ruta física de almacenamiento.
- El nombre del objeto en MinIO no era completamente estable para evitar colisiones por reintento del mismo archivo, y la lógica no estaba chequeando previamente si el documento ya estaba registrado por `(aspirante_id, tipo_documento)` o por `storage_path`.

Correcciones aplicadas
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/service/MinioDocumentosService.java`
  - Se agregó un sufijo aleatorio al nombre de objeto para garantizar que el mismo archivo no colisione con una ruta ya usada por otra carga y se evita reutilizar el mismo nombre en el almacenamiento.
  - Se mantiene la preservación de un identificador estable para evitar reemplazos accidentales de archivos distintos.

- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/service/DocumentoServiceImpl.java`
  - Antes de insertar un documento principal, se valida si ya existe otro registro del mismo aspirante con el mismo tipo.
  - También se valida si ya existe la misma `storage_path` para ese aspirante.
  - Cuando existe, el backend retorna el documento existente en lugar de fallar con `DataIntegrityViolationException`.
  - Se agregaron manejos defensivos ante `DataIntegrityViolationException` para recuperar el registro existente si la colisión ocurre en el último paso.

- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/repository/DocumentoRepository.java`
  - Se añadió el método `findByAspiranteIdAndTipoDocumento(...)` para detectar duplicados por tipo antes del insert.
  - Se conservó el método `findByAspiranteIdAndStoragePath(...)` para detectar rutas duplicadas.

- `frontend/src/app/(protected)/panel/page.tsx`
  - Se reforzó la lógica de la subida para que, si la petición falla pero el servidor ya registró el documento, no muestre `Error al subir el archivo` y en su lugar muestre el éxito.
  - Se eliminó la redirección automática global por 401/403 que provocaba recargas no deseadas.
  - Se corrigieron los warnings de lint `@typescript-eslint/no-explicit-any` y `@typescript-eslint/no-unused-vars` eliminando `any` y la función no utilizada `handleCvUpload`.

- `frontend/src/lib/api.ts`
  - Se dejó la lógica de sesión controlada por el componente, sin forzar `window.location.href = '/login'` automáticamente en todas las respuestas 401/403.

- `frontend/src/services/auth.service.ts`
  - Se tipó el error de `subirDocumento` con un tipo explícito (`Error & { status?: number; serverMessage?: string }`) para cumplir `@typescript-eslint/no-explicit-any` sin alterar la lógica de negocio.

Verificación ejecutada
- Login verificado con `curl`:

```bash
curl -i -X POST 'http://localhost:8087/api/v1/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"correo":"armando.valeriano@ine.mx","password":"5#2HRk@t"}'
```

Resultado verificado: `HTTP/1.1 200` y respuesta JSON con `accessToken`, `refreshToken` y `folio`.

- Logs del backend verificados durante la carga repetida:
  - `duplicate key value violates unique constraint "uq_documentos_aspirante_storage_tipo"`
  - posteriormente, con la corrección: `Documento ya existe... Retornando existente`

- Validación de lint del frontend ejecutada sobre los archivos afectados:

```bash
cd /Users/armandovalerianohernandez/code/codes/Gestiona\ T/frontend && npx eslint "src/services/auth.service.ts" "src/app/(protected)/panel/page.tsx"
```

Resultado verificado: el comando terminó con exit code `0` y sin errores de ESLint en ambos archivos.

Resultado observado tras la corrección
- Al reintentar la carga del mismo archivo, el backend ya no falla con clave duplicada.
- El frontend ya no muestra el mensaje falso de error cuando el archivo realmente quedó registrado.
- Si el documento ya existe, se responde como `Documento ya registrado` o `Documento ya existente` y la UI puede mostrar el mensaje de éxito en lugar de error.
- Los archivos de frontend quedan libres de los warnings reportados por `@typescript-eslint/no-explicit-any` y `@typescript-eslint/no-unused-vars`.

Archivo de la línea base / documentación auxiliar
- Este documento se mantiene actualizado con la causa real y la corrección técnica aplicada en backend y frontend.

Archivos implicados
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/service/MinioDocumentosService.java`
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/service/DocumentoServiceImpl.java`
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/repository/DocumentoRepository.java`
- `frontend/src/app/(protected)/panel/page.tsx`
- `frontend/src/services/auth.service.ts`
- `frontend/src/lib/api.ts`

Notas finales
- El caso de duplicidad ya no apunta a credenciales ni a sesión.
- La causa era real en la base de datos y ya fue corregida en la capa de servicio/repository.
- El comportamiento del frontend se ajustó para manejar los escenarios de reintento de forma más tolerante y evitar alertas erróneas.
