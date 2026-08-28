# Caso de prueba: Evaluación de compatibilidad con IA

## Objetivo

Validar que una persona aspirante pueda ejecutar la opción **Evaluar mi Compatibilidad** desde `CV > Idiomas e Info` y recibir el resultado de compatibilidad generado por el Backend AI.

## Precondiciones

- Frontend disponible en `http://localhost:3007`.
- Backend Core disponible en el puerto `8087`.
- Backend AI disponible en el puerto `8007`.
- Sesión autenticada y CV con información capturada.

## Problema detectado

La evaluación devolvía HTTP 500 desde el frontend. El Backend Core intentaba comunicarse con el endpoint `/matching/evaluar` del Backend AI y recibía HTTP 404.

La causa fue una composición inconsistente de prefijos:

- El router de FastAPI declara el prefijo `/matching`.
- La API debe quedar publicada bajo `/api/v1`.
- El archivo local `backend-core/.env` todavía configuraba `AI_SERVICE_URL=http://localhost:8007`, sobrescribiendo el valor corregido de `application.yml`.

## Correcciones aplicadas

1. Se registró el router de matching en FastAPI con el prefijo global `/api/v1`, conservando el prefijo `/matching` del router.
2. Se ajustó la URL predeterminada de Spring a `http://localhost:8007/api/v1`.
3. Se actualizó `backend-core/.env` a `AI_SERVICE_URL=http://localhost:8007/api/v1`.
4. Se reinició el Backend Core para cargar la nueva configuración.

La ruta final utilizada por Spring Core es:

`POST http://localhost:8007/api/v1/matching/evaluar`

## Ejecución

1. Se accedió a `http://localhost:3007/cv`.
2. Se seleccionó la sección **Idiomas e Info**.
3. Se verificaron los datos de informática y habilidades profesionales existentes.
4. Se seleccionó **Evaluar mi Compatibilidad**.
5. El botón mostró temporalmente **Evaluando con IA...**.
6. La interfaz presentó el resultado de la evaluación.

## Resultado observado

- **Porcentaje de compatibilidad:** 23.8%
- **Nivel:** COMPATIBILIDAD BAJA
- **Mensaje:** El perfil muestra una compatibilidad baja con los requisitos del puesto.
- **Ceguera curricular:** Se mostró el aviso de que la evaluación se realizó sin datos personales.
- **Persistencia:** El log del Backend Core confirmó que el resultado fue evaluado y guardado.

## Evidencia técnica

El Backend Core registró una evaluación exitosa con score `23.8%` después de invocar al Backend AI. El error HTTP 404 desapareció al utilizar la ruta completa `/api/v1/matching/evaluar`.

## Conclusión

La prueba fue **exitosa**. La opción **Evaluar mi Compatibilidad** funciona desde el módulo **Idiomas e Info**, el Backend AI procesa la solicitud y el resultado se muestra y guarda correctamente.
