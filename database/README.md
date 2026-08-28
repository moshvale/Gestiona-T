# Base de Datos - Gestiona-T

Este directorio contiene todos los scripts SQL y configuraciones de bases de datos del proyecto.

## Estructura

- **postgres/**: Scripts para PostgreSQL 16
  - `migrations/`: Migraciones versionadas
  - `seeders/`: Datos iniciales (catálogos, roles)
  - `functions/`: Stored procedures y funciones
- **minio/**: Politicas de buckets para almacenamiento de objetos

## Convenciones

- Nombres de tablas en **snake_case**
- Todas las tablas tienen campos: created_at, updated_at, created_by
- Soft delete con deleted_at
- Auditoria obligatoria en tablas criticas
- Migraciones numeradas secuencialmente (001_, 002_, etc.)

## Ejecutar migraciones

Las migraciones se ejecutan automaticamente al iniciar el contenedor de PostgreSQL.
Para ejecutar manualmente:

    docker exec -i gestiona_t_postgres psql -U gestiona_user -d gestiona_t < database/postgres/migrations/001_create_users.sql