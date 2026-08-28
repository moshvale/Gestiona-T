# Scripts - Gestiona-T

Scripts de automatizacion para el proyecto.

## Scripts disponibles

- setup.sh / setup.ps1: Configuracion inicial del entorno
- deploy.sh: Despliegue a produccion
- backup_db.sh: Backup de base de datos PostgreSQL
- sync_catalogos.sh: Sincronizacion de catalogos maestros

## Mantenimiento

Carpeta maintenance/ contiene scripts para:
- Limpieza de archivos temporales
- Rotacion de logs
- Optimizacion de base de datos

## Uso

### En Linux/Mac

    chmod +x setup.sh
    ./setup.sh

### En Windows (PowerShell)

    .\setup.ps1