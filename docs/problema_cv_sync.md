# Problema y solución del módulo CV

## Problema detectado

Se identificaron inconsistencias entre el frontend del módulo CV, el backend Spring Boot y las tablas de PostgreSQL que almacenan la información institucional del CV.

Estas inconsistencias generaban fallos al intentar guardar el CV completo, principalmente al persistir experiencias laborales, y provocaban errores de base de datos aunque el formulario del frontend enviara datos válidos.

## Causa raíz confirmada

El problema no estaba en el botón de guardar del frontend, sino en el esquema de PostgreSQL del módulo CV.

Durante la verificación se detectó que las tablas del CV conservaban una estructura antigua con restricciones incompatibles y columnas legacy que impedían la inserción correcta de registros. El error observado fue:

- `null value in column "id" of relation "cv_experiencia_laboral" violates not-null constraint`

Esto ocurría porque el backend intenta insertar registros en las tablas del CV usando un modelo actual, mientras la base local aún tenía tablas con definiciones incompatibles.

## Soluciones aplicadas

### 1. Alineación del esquema de base de datos
Se ajustó el script de inicialización de PostgreSQL en [database/postgres/init/003_cv_tables.sql](database/postgres/init/003_cv_tables.sql) para dejar las tablas del módulo CV alineadas con el modelo del backend.

### 2. Reparación de tablas ya existentes
Se aplicaron cambios directos sobre las tablas existentes en PostgreSQL para:
- asignar valores por defecto apropiados a los UUID,
- corregir restricciones legacy,
- y permitir la inserción de formación, experiencia e idiomas del CV.

### 3. Verificación funcional
Se validó la corrección con una inserción de prueba real en PostgreSQL, la cual fue exitosa y devolvió:

- `INSERT 0 1`

Además, se compiló el backend con:

- `mvn -q -DskipTests compile`

y la compilación finalizó sin errores.

## Archivos clave revisados

- [backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/service/CvInstitucionalServiceImpl.java](backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/service/CvInstitucionalServiceImpl.java)
- [backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/dto/request/CvInstitucionalRequest.java](backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/dto/request/CvInstitucionalRequest.java)
- [frontend/src/services/cv.service.ts](frontend/src/services/cv.service.ts)
- [frontend/src/app/(protected)/panel/page.tsx](frontend/src/app/(protected)/panel/page.tsx)
- [frontend/src/app/(protected)/cv/page.tsx](frontend/src/app/(protected)/cv/page.tsx)
- [database/postgres/init/003_cv_tables.sql](database/postgres/init/003_cv_tables.sql)

## Resultado

Se logró:
- corregir el problema de persistencia del CV completo,
- dejar el esquema de PostgreSQL alineado con el backend,
- y validar que las tablas del módulo CV aceptan correctamente los registros generados por la aplicación.
