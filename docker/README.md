# Docker - Gestiona-T

Configuración de contenedores para desarrollo local y despliegue.

## Servicios incluidos

- **PostgreSQL 16**: base de datos transaccional
- **MinIO**: almacenamiento de objetos (documentos)
- **Backend-Core**: Java Spring Boot
- **Backend-AI**: Python FastAPI
- **Frontend**: Next.js
- **Nginx**: reverse proxy opcional

## Comandos útiles

### Levantar todos los servicios

```bash
docker compose up -d
```

### Ver logs

```bash
docker compose logs -f postgres
```

### Detener servicios

```bash
docker compose down
```

### Acceder a PostgreSQL

```bash
docker exec -it gestiona-t-postgres psql -U gestiona_user -d gestiona_t
```

### Acceder a MinIO Console

URL: http://localhost:9008  
Usuario: minioadmin  
Contraseña: MinioAdmin_2026_Secure