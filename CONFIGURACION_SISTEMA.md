# Configuración del Sistema - Gestiona T

**Versión:** 1.3.3  
**Fecha:** 10 de agosto de 2026  
**Estado:** Configuración alineada con el stack real y validado en entorno local

---

## 1. Introducción

Este documento consolida la configuración operativa actual del proyecto. La implementación verificada usa:

- Java 17
- Spring Boot 3.3.2
- Python 3.12
- FastAPI + Uvicorn
- Next.js 16.2.10 + React 19.2.4
- PostgreSQL 16
- MinIO

---

## 2. Puertos y servicios

| Servicio | Puerto | URL / acceso |
|---|---:|---|
| Frontend | 3007 | http://localhost:3007 |
| Backend Core | 8087 | http://localhost:8087 |
| Backend AI | 8007 | http://localhost:8007 |
| PostgreSQL | 5439 | localhost:5439 |
| MinIO API | 9007 | http://localhost:9007 |
| MinIO Console | 9008 | http://localhost:9008 |

---

## 3. Configuración del frontend

**Archivo relevante:** frontend/package.json

```json
"dev": "next dev --turbopack -p 3007"
```

**Variables esperadas:**
```env
NEXT_PUBLIC_API_URL=http://localhost:8087/api/v1
NEXT_PUBLIC_AI_API_URL=http://localhost:8007/api/v1
NEXT_PUBLIC_APP_ENV=development
```

---

## 4. Configuración del backend core

**Archivo relevante:** backend-core/src/main/resources/application.yml

```yaml
server:
  port: ${SERVER_PORT:8087}

spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5439}/${DB_NAME:gestiona_t}
    username: ${DB_USER:gestiona_user}
    password: ${DB_PASSWORD:GestionaT_2026_Secure}

minio:
  endpoint: ${MINIO_ENDPOINT:http://localhost:9007}
  access:
    key: ${MINIO_ACCESS_KEY:minioadmin}
  secret:
    key: ${MINIO_SECRET_KEY:MinioAdmin_2026_Secure}

ai:
  service:
    url: ${AI_SERVICE_URL:http://localhost:8007/api/v1}

cors:
  allowed:
    origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3007,http://localhost:8087,http://127.0.0.1:3007}
```

---

## 5. Configuración del backend AI

**Archivo relevante:** backend-ai/app/core/config.py

```python
class Settings(BaseSettings):
    app_name: str = "Gestiona-T Backend AI"
    app_version: str = "1.0.0"
    nlp_model: str = "es_core_news_lg"
    embeddings_model: str = "paraphrase-multilingual-MiniLM-L12-v2"
    matching_threshold_apto: float = 80.0
    matching_threshold_revision: float = 60.0
    cors_origins: List[str] = ["http://localhost:3007", "http://localhost:8087"]
```

---

## 6. Configuración de infraestructura local

**Archivo relevante:** docker-compose.yml

```yaml
postgres:
  image: postgres:16-alpine
  ports:
    - "5439:5432"

minio:
  image: minio/minio:latest
  ports:
    - "9007:9000"
    - "9008:9001"
```

Credenciales por defecto:

```env
POSTGRES_DB=gestiona_t
POSTGRES_USER=gestiona_user
POSTGRES_PASSWORD=GestionaT_2026_Secure
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=MinioAdmin_2026_Secure
```

---

## 7. Arranque recomendado

### Backend Core
```bash
cd backend-core
mvn spring-boot:run
```

### Backend AI
```bash
cd backend-ai
python3.12 -m venv venv
source venv/bin/activate
python -m pip install --upgrade pip
pip install -r requirements.txt
python -m spacy download es_core_news_lg
uvicorn app.main:app --host 0.0.0.0 --port 8007
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Infraestructura
```bash
docker compose up -d
```

---

## 8. Verificación operativa

- Backend Core health: http://localhost:8087/actuator/health
- Backend AI health: http://localhost:8007/health
- Swagger UI: http://localhost:8087/swagger-ui/index.html
- Docs AI: http://localhost:8007/docs
- MinIO Console: http://localhost:9008

Para matching, el Backend Core usa `AI_SERVICE_URL=http://localhost:8007/api/v1` y consume `POST /api/v1/matching/evaluar`.

---

**Fin del documento CONFIGURACION_SISTEMA.md**
