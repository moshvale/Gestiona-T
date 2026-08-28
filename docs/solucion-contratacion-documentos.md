# Solución: documentos de contratación presupuestal

**Fecha:** 25 de agosto de 2026

## Resumen

Al ingresar como analista a una postulación con `estatusFinalSeleccion = SELECCIONADO`, la pantalla de contratación debía permitir elegir el tipo `Presupuestal`, mostrar siete documentos requeridos y cargar archivos.

El flujo presentó dos errores consecutivos:

1. La pantalla no podía cargar los documentos del aspirante y recibía `403 Forbidden`.
2. Después de corregir la consulta, la carga de `FUM` recibía `500 Internal Server Error`.

## Causa raíz

### Error 403 al cargar documentos

La pantalla de contratación utilizaba:

```text
GET /api/v1/documentos?aspiranteId={id}
```

Ese endpoint está restringido al rol `ROLE_ASPIRANTE`. Para consultas realizadas por analistas existe el endpoint autorizado:

```text
GET /api/v1/documentos/por-aspirante/{aspiranteId}
```

### Error 500 al cargar FUM

El frontend enviaba el texto visible `FUM` en el campo multipart `tipo`. El backend intentaba convertirlo directamente con:

```java
TipoDocumento.valueOf(tipoDocumentoStr)
```

El enum `TipoDocumento` no contenía los tipos de contratación, por lo que se producía:

```text
No enum constant mx.ine.gestiona_t.modules.documentos.model.enums.TipoDocumento.FUM
```

Los tipos mostrados por la interfaz tampoco coincidían directamente con nombres válidos de enum, por ejemplo `3 Cartas de recomendación`.

## Solución aplicada

### Frontend

En `contratacion/page.tsx` se sustituyeron las dos consultas de documentos por el endpoint autorizado para analistas:

```text
GET /api/v1/documentos/por-aspirante/{aspiranteId}
```

### Backend

Se agregaron al enum `TipoDocumento` las constantes:

```text
FUM
TRES_CARTAS_RECOMENDACION
ALTA_ISSSTE
FOTOGRAFIA
FORMATO_INSCRIPCION_FONAC
FORMATO_GASTOS_FUNERARIOS
FORMATO_SEGURO_VIDA_INSTITUCIONAL
```

También se agregó un conversor en `DocumentoServiceImpl` para traducir las etiquetas de la interfaz a los valores internos del enum, incluyendo:

- `3 Cartas de recomendación` -> `TRES_CARTAS_RECOMENDACION`
- `Alta del ISSSTE` -> `ALTA_ISSSTE`
- `Formato de inscripción al FONAC` -> `FORMATO_INSCRIPCION_FONAC`
- `Formato de gastos funerarios` -> `FORMATO_GASTOS_FUNERARIOS`
- `Formato de seguro de vida institucional` -> `FORMATO_SEGURO_VIDA_INSTITUCIONAL`

## Resultado funcional

El caso se validó con la cuenta de analista de desarrollo y una postulación existente:

1. Se abrió `/postulaciones` como analista.
2. Se evaluó al aspirante y se guardó el dictamen `SELECCIONADO`.
3. Se abrió la gestión de contratación.
4. Se seleccionó `Presupuestal`.
5. Se verificaron los siete documentos requeridos:
   - FUM
   - 3 Cartas de recomendación
   - Alta del ISSSTE
   - Fotografía
   - Formato de inscripción al FONAC
   - Formato de gastos funerarios
   - Formato de seguro de vida institucional
6. Se cargó `prueba-contratacion.pdf` para `FUM`.
7. La interfaz mostró `Subido: prueba-contratacion.pdf` y las opciones `Ver` y `Reemplazar`.

## Archivos modificados

- `frontend/src/app/(admin)/postulaciones/[id]/contratacion/page.tsx`
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/model/enums/TipoDocumento.java`
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/documentos/service/DocumentoServiceImpl.java`

## Validación técnica

Se ejecutó correctamente:

```text
cd backend-core
mvn clean compile
```

Resultado: `BUILD SUCCESS`.

La validación de diagnósticos tampoco reportó errores en los archivos modificados.
