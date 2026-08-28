# Caso de prueba: llenado y firma de la carta declaratoria

**Fecha:** 10 de agosto de 2026  
**Aplicación:** Gestiona-T local  
**Ruta:** `/carta`  
**Resultado final:** Exitoso después de corregir errores de consulta

## Objetivo

Validar el acceso al módulo de carta declaratoria, la aceptación masiva de todos los bloques, la confirmación del consentimiento y la firma/guardado de la carta.

## Datos utilizados

- Aspirante de prueba: `armando.valeriano@ine.mx`.
- La contraseña se manejó únicamente durante el inicio de sesión y no se documenta.
- Folio de carta: `CD-C79B3EF2-1786396232040`.

## Ejecución

1. Se iniciaron PostgreSQL y MinIO mediante Docker Compose.
2. Se inició el backend core en el puerto 8087 y el frontend en el puerto 3007.
3. Se accedió a `http://localhost:3007/carta` y se inició sesión con el aspirante de prueba.
4. Se seleccionó **Aceptar todos** y se confirmó el diálogo de seguridad.
5. El sistema mostró `12 de 12 bloques` y el estado `Lista para firmar`.
6. Se marcó la casilla de confirmación de lectura y aceptación.
7. Se seleccionó **Firmar Carta Declaratoria**.
8. El sistema mostró `¡Carta Firmada Exitosamente!`, método `FEA_FIEL` y habilitó las opciones **Ver PDF** y **Descargar PDF**.

## Prueba de visualización y descarga del PDF

1. Se seleccionó **Ver PDF**.
2. El backend respondió correctamente y generó un PDF de 9,365 bytes; la visualización se completó sin mostrar errores.
3. Se seleccionó **Descargar PDF**.
4. La descarga solicitó correctamente el mismo endpoint PDF y el backend generó nuevamente el archivo de 9,365 bytes.

## Incidencias encontradas y correcciones

### 1. Consulta no única al obtener el estatus

El primer acceso al módulo devolvió HTTP 500 con el mensaje `Query did not return a unique result: 2 results were returned`. El stack trace identificó `CartaDeclaratoriaServiceImpl.obtenerEstatus()` y `findByFolio(folio)`. La base de datos contiene dos registros para el mismo folio, por lo que una consulta JPA que exige un resultado único falla.

**Corrección:** se cambió la consulta a `findFirstByFolioOrderByCreatedAtDesc(folio)` para utilizar la carta más reciente.

### 2. Consulta no única al obtener validaciones

Después de corregir el estatus, la consulta de validaciones externas presentó el mismo problema en `ValidacionExternaService.obtenerValidaciones()`.

**Corrección:** se aplicó la misma selección de la carta más reciente en `ejecutarValidaciones()` y `obtenerValidaciones()`.

### 3. Recompilación

Se ejecutó una recompilación limpia con `mvn clean spring-boot:run` para asegurar que las clases corregidas fueran cargadas por el backend.

### 4. Error al visualizar y descargar el PDF

La primera prueba de **Ver PDF** devolvió HTTP 500 con el mismo mensaje `Query did not return a unique result: 2 results were returned`. El stack trace identificó `CartaDeclaratoriaServiceImpl.obtenerPdf()` y una llamada residual a `findByFolio(folio)`.

**Corrección:** se reemplazó la consulta residual por `findFirstByFolioOrderByCreatedAtDesc(folio)` en el método de generación del PDF. Después de una recompilación limpia, tanto **Ver PDF** como **Descargar PDF** funcionaron correctamente.

## Resultado esperado y obtenido

- Se visualizaron los 12 bloques declaratorios.
- Se aceptaron todos los bloques en una sola operación.
- Se generó el PDF durante la firma.
- La carta quedó firmada correctamente mediante `FEA_FIEL`.
- El módulo mostró el folio y las acciones para ver o descargar el PDF.

## Criterios de aceptación

- [x] El módulo carga sin error 500.
- [x] Se aceptan los 12 bloques declaratorios.
- [x] Se confirma la lectura y aceptación.
- [x] La carta se firma y guarda correctamente.
- [x] Se genera el PDF.
- [x] Se muestran las opciones para ver y descargar el PDF.
- [x] Se corrigen las consultas afectadas por folios duplicados.
- [x] **Ver PDF** genera y muestra correctamente el archivo.
- [x] **Descargar PDF** obtiene correctamente el archivo generado.
