# Instrucciones para limpiar la base de datos de pruebas

Este documento describe cómo ejecutar el script `limpiar_bd_test.sql` para dejar la base de datos vacía antes de nuevas pruebas.

## Objetivo

El script elimina todos los registros de las tablas del esquema `public`, incluyendo:

- usuarios
- documentos
- CV
- expedientes
- auditoría
- registros de intentos de acceso fallidos
- otros datos de prueba

## Requisitos

- Docker instalado y en ejecución
- El contenedor PostgreSQL del proyecto levantado
- Conocimiento del nombre de la base de datos y usuario configurados en el proyecto

En este proyecto, el contenedor suele llamarse:

- `gestiona-t-postgres`
- base de datos: `gestiona_t`
- usuario: `gestiona_user`

## Opción 1: ejecutar desde el contenedor PostgreSQL

Ejecuta este comando desde la raíz del proyecto:

```bash
docker exec -i gestiona-t-postgres psql -U gestiona_user -d gestiona_t -f /tmp/limpiar_bd_test.sql
```

Si quieres copiar primero el script al contenedor:

```bash
docker cp scripts/limpiar_bd_test.sql gestiona-t-postgres:/tmp/limpiar_bd_test.sql
docker exec -i gestiona-t-postgres psql -U gestiona_user -d gestiona_t -f /tmp/limpiar_bd_test.sql
```

## Opción 2: ejecutar desde tu máquina local

Si tienes acceso directo al puerto PostgreSQL local:

```bash
psql -h localhost -p 5439 -U gestiona_user -d gestiona_t -f scripts/limpiar_bd_test.sql
```

## Verificación

Después de ejecutar el script, puedes comprobar que todas las tablas quedaron vacías con:

```sql
SELECT schemaname, relname, n_live_tup
FROM pg_stat_user_tables
WHERE schemaname = 'public'
ORDER BY relname;
```

El resultado esperado es que todas las tablas tengan `0` filas.

## Importante

El script:

- deshabilita los triggers antes del truncado
- ejecuta `TRUNCATE TABLE ... CASCADE`
- vuelve a habilitar los triggers

Esto evita errores por auditoría u otras reglas de base de datos al limpiar los datos.

## Nota

Usa este script solo para entornos de pruebas o desarrollo. No lo ejecutes en una base de datos productiva sin confirmar previamente el impacto.
