# 🚀 Guía para levantar los servicios de Gestiona-T

**Versión:** 1.3.1  
**Fecha:** 04 de agosto de 2026  
**Estado:** Guía alineada con la configuración verificada y la ejecución local real

---

## 1. Prerrequisitos

- Docker Desktop o Docker Engine funcionando.
- Java 17 + Maven.
- Python 3.12.
- Node.js 20+ y npm.
- Git.

---

## 2. Infraestructura local

Desde la raíz del repositorio:

```bash
cd /ruta/al/repositorio/Gestiona\ T
docker compose up -d
docker ps
```

### Verificación esperada
- PostgreSQL en localhost:5439
- MinIO API en http://localhost:9007
- MinIO Console en http://localhost:9008

---

Verificar que los puertos estèn desactivados:
lsof -iTCP:8087 -sTCP:LISTEN -n -P
lsof -iTCP:3007 -sTCP:LISTEN -n -P
En caso de estar activos:
kill -9 $(lsof -t -i :8087)
kill -9 $(lsof -t -i :3007)

## 3. Backend AI

Desde PowerShell en Windows:

```bash
cd backend-ai
py -3.12 -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m uvicorn app.main:app --host 0.0.0.0 --port 8007
```

Desde bash en macOS o Linux:

```bash
cd backend-ai
python3.12 -m venv .venv
source .venv/bin/activate
```

Con el entorno virtual activado, continúa con:

```bash
python -m pip install --upgrade pip
python -m pip install -r requirements.txt
python -m spacy download es_core_news_lg
python -c "import torch; print(torch.__version__, torch.cuda.is_available())"
python -m uvicorn app.main:app --host 0.0.0.0 --port 8007
```

En Windows, `requirements.txt` fija PyTorch a la variante CPU oficial
`2.5.1+cpu`. Esta versión evita el bloqueo de DLL observado con versiones más
nuevas en equipos donde Windows Application Control impide cargar `torch._C`.
Si la instalación anterior quedó incompleta, ejecuta dentro de `backend-ai`:

```powershell
python -m pip install --force-reinstall --no-deps --index-url https://download.pytorch.org/whl/cpu "torch==2.5.1+cpu"
python -c "import torch; print(torch.__version__, torch.cuda.is_available())"
```

La verificación debe imprimir `2.5.1+cpu False` (el backend usa CPU). Si aun
así aparece `Una directiva de Control de aplicaciones bloqueó este archivo`,
la política de seguridad del equipo sigue bloqueando la DLL y debe revisarla
el administrador de Windows; no se soluciona cambiando el comando de Uvicorn.

> No uses `source venv/bin/activate` en PowerShell. Ese comando pertenece a
> bash; en Windows debes usar `.\venv\Scripts\Activate.ps1`.
>
> Se recomienda ejecutar Uvicorn como `python -m uvicorn` para garantizar que
> se utilice el ejecutable instalado en el entorno virtual activo.

### Nota sobre el entorno de Python y Pylance en macOS

El intérprete seleccionado en VS Code debe ser el mismo entorno donde se
instalaron las dependencias de `backend-ai/requirements.txt`. En particular:

- `PIL` proviene del paquete `Pillow`.
- `pytesseract` debe estar instalado en el entorno activo.
- Tesseract es un ejecutable externo; instalarlo no sustituye la instalación
	del paquete Python `pytesseract`.

Para comprobar o reparar las importaciones OCR dentro del entorno activado:

```bash
python -m pip install Pillow pytesseract
python test_ocr.py
```

En VS Code, selecciona el mismo intérprete mediante **Python: Select
Interpreter**. Si Pylance mantiene un diagnóstico anterior después de la
instalación, ejecuta **Developer: Reload Window** (`Cmd+Shift+P`).

No utilices en macOS un entorno virtual creado en Windows. Si el directorio
`.venv` contiene `Scripts/` y `Lib/`, créalo nuevamente en macOS con:

```bash
python3.12 -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip
python -m pip install -r requirements.txt
```

### Verificación
- Docs: http://localhost:8007/docs
- Health: http://localhost:8007/health

---

## 4. Backend Core

```bash
cd backend-core
mvn spring-boot:run
```

### Verificación
- Swagger: http://localhost:8087/swagger-ui/index.html
- Health: http://localhost:8087/actuator/health

---

## 5. Frontend

```bash
cd frontend
npm install
npm run dev
```

### Verificación
- Aplicación: http://localhost:3007

---

## 6. Resumen de puertos y servicios

| Servicio | Tecnología | Puerto | URL de verificación |
|---|---|---:|---|
| PostgreSQL | Docker | 5439 | localhost:5439 |
| MinIO API | Docker | 9007 | http://localhost:9007 |
| MinIO Console | Docker | 9008 | http://localhost:9008 |
| Backend AI | FastAPI | 8007 | http://localhost:8007/docs |
| Backend Core | Spring Boot | 8087 | http://localhost:8087/swagger-ui/index.html |
| Frontend | Next.js | 3007 | http://localhost:3007 |

---

## 7. Detener los servicios

```bash
docker compose down
```

En cada terminal de backend, frontend y AI, presione Ctrl + C para finalizar el proceso activo.

---

**Fin de la guía de levantamiento**