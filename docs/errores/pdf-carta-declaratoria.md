# Error y solución: PDF de carta declaratoria

## Problema
Al intentar abrir o descargar el PDF de una carta declaratoria desde la interfaz, la petición al endpoint
`/api/v1/carta-declaratoria/{folio}/pdf` devolvía un error 500 y el navegador no podía mostrar el documento.

## Síntoma
- El frontend mostraba un error al intentar visualizar el PDF.
- El backend respondía con un fallo interno al invocar la generación del PDF.

## Causa raíz
El problema no estaba en el frontend ni en la autenticación. La falla se encontraba en la generación del PDF en el backend.
La librería de iText utilizada para convertir HTML a PDF no estaba cargando correctamente varias clases necesarias, lo que provocaba una excepción de tiempo de ejecución durante la conversión.

## Solución aplicada
Se corrigieron las dependencias de iText en el proyecto backend para que quedaran alineadas y se incluyera el módulo necesario para la generación de PDF desde HTML.
Además, se agregó una prueba de regresión para validar que el generador produzca un PDF válido.

## Archivos modificados
- `backend-core/pom.xml`
- `backend-core/src/test/java/mx/ine/gestiona_t/modules/cartadeclaratoria/service/PdfGenerationServiceTest.java`

## Verificación
Se validó el cambio con:

```bash
mvn -Dtest=PdfGenerationServiceTest test
```

Resultado:
- `BUILD SUCCESS`
- `Tests run: 1, Failures: 0, Errors: 0`

También se verificó el endpoint en ejecución con una petición directa al backend, obteniendo:
- `HTTP/1.1 200`
- `Content-Type: application/pdf`

## Nota
Después de aplicar el cambio, fue necesario reiniciar el backend para que tomara la nueva configuración y el endpoint devolviera el PDF correctamente.
