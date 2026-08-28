# Caso de prueba: llenado y validación del CV institucional

**Fecha:** 10 de agosto de 2026  
**Aplicación:** Gestiona-T local  
**Ruta:** `/cv`  
**Resultado final:** Exitoso

## Objetivo

Validar el llenado de los módulos del CV con valores válidos, el guardado completo, la eliminación de módulos opcionales y los mensajes de validación de campos obligatorios.

## Datos utilizados

Se utilizaron valores de prueba válidos y no sensibles:

- Expectativas: Jalisco, sueldo mensual de 28,750, disponibilidad de 15 días y áreas de interés relacionadas con tecnología y organización electoral.
- Formación: Licenciatura en Ingeniería en Sistemas, Universidad de Guadalajara, fechas 2010-08-15 a 2014-06-30.
- Experiencia: Servicios Digitales del Centro, Analista de Sistemas, fechas 2018-01-10 a 2023-12-15, sueldo mensual de 23,500.
- Curso: Gestión de Proyectos Ágiles, Instituto Tecnológico de Monterrey, 40 horas, fecha 2024-05-20.
- Idioma: Inglés, escritura avanzada, lectura avanzada y conversación intermedia.
- Informática: Windows/Linux/macOS, Java/TypeScript/Python y PostgreSQL/MySQL.
- Habilidades y logros: análisis de datos, comunicación, liderazgo y automatización de procesos.

## Ejecución

1. Se inició el frontend en el puerto 3007, el backend core en el puerto 8087 y PostgreSQL mediante Docker.
2. Se inició sesión con el aspirante de prueba.
3. Se llenaron todos los módulos con datos válidos.
4. Se ejecutó **Guardar CV Completo**.
5. Se editaron expectativas, habilidades, logros y conocimientos informáticos.
6. Se eliminaron los módulos opcionales de cursos e idiomas.
7. Se ejecutó nuevamente **Guardar CV Completo**.
8. Se eliminó temporalmente el valor de **Carrera / Profesión** y se guardó para probar la validación.
9. El sistema mostró el mensaje `Hay campos obligatorios incompletos` y el campo mostró `Obligatorio`.
10. Se restauró el valor de carrera y se ejecutó **Guardar CV Completo** nuevamente.

## Incidencias encontradas y correcciones

### 1. Columna histórica `institucion`

El primer guardado devolvió HTTP 500 porque la tabla `cv_experiencia_laboral` tenía la columna histórica `institucion` como `NOT NULL`, mientras que la entidad institucional solo persistía `empresa`.

**Corrección:** se agregó el campo histórico al mapeo de `CvExperienciaLaboral` y se sincroniza con `empresa` al guardar.

### 2. Columna histórica `nivel_mando`

Después de corregir `institucion`, el esquema local exigió también `nivel_mando`, campo perteneciente al modelo estructurado anterior y no visible en el formulario institucional.

**Corrección local:** se configuró el valor predeterminado `OPERATIVO` en la columna para que las inserciones del CV institucional sean compatibles con el esquema existente. Se evitó mapearlo nuevamente en la entidad para no generar un mapeo duplicado entre entidades que comparten la tabla.

## Resultado esperado y obtenido

- El primer intento falló por incompatibilidad entre el esquema de base de datos y el modelo institucional.
- Tras la corrección, el backend registró `CV guardado con score de completitud: 100%`.
- El segundo guardado, con cursos e idiomas eliminados, fue exitoso.
- La prueba de campo obligatorio mostró correctamente el mensaje `Obligatorio` en **Carrera / Profesión**.
- El valor se restauró y el guardado final fue exitoso.
- El panel mostró el CV como **Completado** y listo para evaluación.

## Criterios de aceptación

- [x] Se llenaron los módulos con valores válidos.
- [x] Se guardó el CV completo sin error después de la corrección.
- [x] Se editaron módulos existentes.
- [x] Se eliminaron cursos e idiomas, que son opcionales.
- [x] Se validó un campo obligatorio vacío.
- [x] Se restauró el campo obligatorio y se confirmó el guardado final.
