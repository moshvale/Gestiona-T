# Observaciones de navegación para aspirante

## 1. Alcance revisado

Se revisó la estructura funcional del frontend para el flujo de aspirante y las rutas públicas/protegidas asociadas al registro, acceso, postulación, carga documental, CV, carta declaratoria y seguimiento.

Se evidencian estas rutas principales:

- /
- /registro
- /login
- /recuperar-contrasena
- /convocatorias
- /convocatorias/[id]
- /convocatorias/[id]/postulacion/[postulacionId]
- /panel
- /cv
- /carta
- /seguimiento
- /perfil

## 2. Flujo lógico de navegación del aspirante

### A. Entrada pública

1. /
   - Es la landing o página inicial del sistema.
   - Presenta la opción de consultar convocatorias y de seguir el proceso.
   - No es la ruta de autenticación propiamente, pero funciona como punto de entrada.

2. /convocatorias
   - Es la ruta pública de consulta de vacantes disponibles.
   - Es la entrada natural para alguien que quiere revisar ofertas antes de autenticarse o después de hacerlo.

3. /registro
   - Ruta específica para alta de aspirante.
   - Es el punto de inicio si el usuario aún no tiene cuenta.
   - Tras completarlo, la aplicación redirige al login con datos de registro y una marca de “registrado”.

4. /login
   - Ruta de acceso al sistema.
   - Es el punto de entrada recurrente para aspirantes ya registrados.
   - Una vez credenciales válidas, el sistema redirige al aspirante a /panel.

5. /recuperar-contrasena
   - Ruta auxiliar necesaria para usuarios que olvidaron la contraseña.
   - Es un acceso complementario, no central dentro del flujo principal.

### B. Flujo de postulación

6. /convocatorias/[id]
   - Después de elegir una vacante, el aspirante entra al detalle de la convocatoria.
   - Aquí se puede revisar la plaza, requisitos, documentación y condiciones.
   - Es el puente lógico entre la lista de vacantes y la acción de postularse.

7. /convocatorias/[id]/postulacion/[postulacionId]
   - Esta ruta es la continuación de la postulación y marca la fase de cumplimiento del proceso.
   - Aquí se completan o validan los pasos requeridos: CV, documentos, carta declaratoria.
   - La lógica del sistema muestra claramente que esta página es un punto de continuidad del proceso de postulación.

### C. Panel del aspirante

8. /panel
   - Es la ruta central de la experiencia ya autenticada.
   - Es el punto donde se consulta la información general del aspirante, documentos cargados, estado del CV y acceso a tareas del proceso.
   - El login redirige a esta ruta de forma explícita.

9. /cv
   - Ruta de elaboración o edición del CV institucional.
   - Tiene sentido después del acceso al panel y antes de cerrar la postulación.
   - Se observa que el CV se guarda y luego se devuelve al panel.

10. /carta
    - Ruta para revisar, aceptar y firmar la carta declaratoria.
    - Tiene sentido después de haber completado la fase de documentos del aspirante.
    - Es un paso clave del cierre del proceso y aparece como una tarea final dentro del flujo del aspirante.

11. /seguimiento
    - Ruta lógica para consultar el estado del proceso una vez que ya se inició la postulación o cuando el aspirante quiere revisar avance.
    - Tiene sentido después de que la interacción con convocatoria, documentos y carta ya está en curso.

12. /perfil
    - Es una ruta de administración de cuenta / perfil de usuario.
    - No forma parte del “orden principal” del proceso de selección, pero sí es un acceso complementario dentro del panel o menú del aspirante.

## 3. Orden de navegación más lógico

El orden más consistente con la implementación actual es el siguiente:

1. /
2. /registro (si no tiene cuenta)
3. /login
4. /convocatorias
5. /convocatorias/[id]
6. /convocatorias/[id]/postulacion/[postulacionId]
7. /panel
8. /cv
9. /carta
10. /seguimiento
11. /perfil (acceso lateral o complementario)

Este orden encaja con la lógica de negocio del proceso:

- El aspirante entra al sistema
- Se registra o inicia sesión
- Explora y selecciona una convocatoria
- Completa la postulación
- Revisa y carga su CV
- Completa documentos y acepta la carta
- Consulta seguimiento del proceso
- Accede a perfil o ajustes si lo requiere

## 4. Qué está bien definido

Los accesos principales del aspirante están bien cubiertos:

- Registro público
- Login público
- Recuperación de contraseña
- Listado de convocatorias
- Detalle de convocatoria
- Proceso de postulación
- Panel del aspirante
- CV institucional
- Carta declaratoria
- Seguimiento

## 5. ¿Hace falta alguna URL?

### Sí, como mejora de experiencia de usuario, podría considerarse una ruta explícita para:

- Mis documentos
- Mi perfil
- Confirmación de postulación exitosa

Sin embargo, en la implementación actual no hay una brecha funcional crítica para completar el flujo principal. El sistema usa /panel como consolidación general, y así resuelve la parte de documentos y estado del aspirante sin necesidad de una ruta independiente.

### Lo que sí se recomienda como ausencia de navegación directa

- No hay una página dedicada exclusivamente a “documentos del aspirante” como recurso standalone.
- No hay una vista de “postulación completada” separada y clara antes de entrar a /panel o /seguimiento.
- No hay un punto de entrada claro “mi proceso” en la raíz autenticada, aparte de /panel.

## 6. Conclusión

El flujo principal del aspirante está coherente y sigue una secuencia natural:

registro -> login -> convocatoria -> detalle -> postulación -> panel -> CV -> carta -> seguimiento.

La mayor carencia no es una ruta obligatoria para el negocio, sino una orquestación más explícita de la experiencia de navegación, especialmente si se desea separar claramente “documentos”, “perfil” y “seguimiento” en rutas dedicadas. En términos de lógica funcional, la navegación actual es suficiente y ordenada.
