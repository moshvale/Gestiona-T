# Problema y solución: importaciones `PIL` y `pytesseract`

## Síntoma

Pylance mostraba estos diagnósticos en `backend-ai/test_ocr.py`:

- `Import "PIL" could not be resolved` (`reportMissingImports`).
- `Import "pytesseract" could not be resolved` (`reportMissingImports`).

Las importaciones afectadas eran:

```python
from PIL import Image
import pytesseract
```

## Causa

Pylance estaba utilizando el intérprete global de Python 3.13.3 (`/usr/local/bin/python3`), pero los paquetes no estaban instalados en un entorno de proyecto válido para macOS.

Además, el directorio `.venv` existente tenía una estructura de entorno virtual de Windows (`Scripts/` y `Lib/`), por lo que no podía utilizarse correctamente en macOS.

Es importante distinguir los nombres:

- `PIL` es el módulo que proporciona el paquete `Pillow`.
- `pytesseract` es el adaptador Python para el ejecutable externo Tesseract.
- Instalar Tesseract en macOS por sí solo no resuelve los errores de importación de Pylance.

## Solución aplicada

Se creó un entorno virtual nativo de macOS en:

`.venv-macos`

Se instalaron las dependencias OCR:

- `Pillow 12.3.0`
- `pytesseract 0.3.13`

También se actualizó `.vscode/settings.json` para que VS Code utilice:

`.venv-macos/bin/python`

El script se validó ejecutándolo con ese entorno. La prueba confirmó que:

- Pillow se importa correctamente.
- `pytesseract` se importa correctamente.
- Tesseract 5.5.3 está instalado.
- El idioma español `spa` está disponible.

## Procedimiento para reproducir la solución

Desde la raíz del repositorio:

```bash
python3 -m venv .venv-macos
.venv-macos/bin/python -m pip install --upgrade pip
.venv-macos/bin/python -m pip install Pillow pytesseract
```

En VS Code se debe seleccionar el intérprete:

`.venv-macos/bin/python`

Si Pylance conserva el diagnóstico anterior, ejecutar **Developer: Reload Window** desde la paleta de comandos (`Cmd+Shift+P`).

## Consideraciones

El entorno `.venv-macos` contiene las dependencias necesarias para la prueba OCR. Para levantar todo el backend AI también deben instalarse las dependencias de `backend-ai/requirements.txt` en el mismo entorno que se utilice para ejecutar FastAPI.

La versión de Python recomendada por el proyecto sigue siendo Python 3.12, porque las dependencias están documentadas para esa versión. Si se utiliza Python 3.12, se deben instalar `Pillow` y `pytesseract` dentro de ese entorno y seleccionarlo también en VS Code.
