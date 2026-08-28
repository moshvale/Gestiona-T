# Problema: Backend-AI no inicia por importación de PyTorch

**Fecha:** 26 de agosto de 2026  
**Servicio:** `backend-ai`  
**Entorno:** Windows, Python 3.12.8 AMD64

## Síntoma

Al ejecutar:

```powershell
python -m uvicorn app.main:app --host 0.0.0.0 --port 8007
```

Uvicorn no llegaba a iniciar y mostraba:

```text
ImportError: DLL load failed while importing _C:
Una directiva de Control de aplicaciones bloqueó este archivo.
```

## Causa comprobada

`app.main` importa el router de matching, que carga
`app.services.matching.matching_service`. Ese módulo importa `torch` al
arrancar el proceso. El entorno virtual de `backend-ai` tenía `torch 2.13.0`;
la importación mínima fallaba al cargar el módulo nativo `torch._C`.

Las DLL de PyTorch estaban presentes y `pip check` no reportaba dependencias
rotas. Por tanto, no era un problema de Uvicorn, FastAPI, del código de
matching ni de una DLL ausente. La instalación de esa versión era incompatible
con la política Windows Application Control del equipo.

## Solución aplicada

Se reinstaló el wheel CPU oficial de PyTorch, compatible con Python 3.12 y con
la instalación funcional del proyecto:

```powershell
cd F:\codes\Gestiona T_3\backend-ai
.\.venv\Scripts\Activate.ps1
python -m pip install --force-reinstall --no-deps --index-url https://download.pytorch.org/whl/cpu "torch==2.5.1+cpu"
```

También se fijó `torch==2.5.1+cpu` y el índice CPU oficial en
`backend-ai/requirements.txt`, para evitar que una instalación futura vuelva a
seleccionar `torch 2.13.0`.

## Verificación

La prueba ejecutada después de la reparación fue:

```powershell
python -c "import torch; print(torch.__version__); print(torch.cuda.is_available()); print(torch.tensor([1.0, 2.0]).sum().item())"
```

Resultado comprobado:

```text
2.5.1+cpu
False
3.0
```

`False` es esperado: el backend está configurado para trabajar con CPU.

Después de esta verificación se puede iniciar el servicio con el comando de la
guía y comprobar:

- Swagger: `http://localhost:8007/docs`
- Health: `http://localhost:8007/health`

## Si vuelve a aparecer el bloqueo

Si incluso `torch==2.5.1+cpu` produce el mismo mensaje, Windows Application
Control continúa bloqueando la DLL. En ese caso debe revisarse la política de
seguridad o permitirse el entorno virtual por el administrador del equipo. No
es necesario cambiar el comando de Uvicorn ni modificar el código de
`matching_service.py`.
