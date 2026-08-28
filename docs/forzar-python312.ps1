# ============================================================
# FORZAR ENTORNO VIRTUAL CON PYTHON 3.12.x
# ============================================================

$ErrorActionPreference = "Stop"
$projectRoot = "F:\codes\Gestiona T"
$backendAiPath = "$projectRoot\backend-ai"

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  FORZAR ENTORNO VIRTUAL CON PYTHON 3.12.x" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# ============================================================
# PASO 1: Desactivar cualquier entorno virtual activo
# ============================================================
Write-Host "[1/8] Desactivando entorno virtual activo..." -ForegroundColor Yellow

if ($env:VIRTUAL_ENV) {
    try {
        deactivate 2>$null
        Write-Host "  [OK] Entorno virtual desactivado" -ForegroundColor Green
    } catch {
        Write-Host "  [--] No habia entorno virtual activo" -ForegroundColor DarkYellow
    }
} else {
    Write-Host "  [--] No habia entorno virtual activo" -ForegroundColor DarkYellow
}

# ============================================================
# PASO 2: Buscar Python 3.12 en ubicaciones comunes
# ============================================================
Write-Host ""
Write-Host "[2/8] Buscando Python 3.12.x en el sistema..." -ForegroundColor Yellow

$python312Paths = @(
    "$env:LOCALAPPDATA\Programs\Python\Python312\python.exe",
    "$env:LOCALAPPDATA\Programs\Python\Python312-x64\python.exe",
    "C:\Program Files\Python312\python.exe",
    "C:\Program Files (x86)\Python312\python.exe",
    "C:\Python312\python.exe",
    "$env:ProgramFiles\Python312\python.exe"
)

$python312 = $null
foreach ($path in $python312Paths) {
    if (Test-Path $path) {
        # Verificar que sea realmente 3.12.x
        $versionOutput = & $path --version 2>&1
        if ($versionOutput -match "Python 3\.12\.") {
            $python312 = $path
            Write-Host "  [OK] Python 3.12 encontrado: $path" -ForegroundColor Green
            Write-Host "       Version: $versionOutput" -ForegroundColor Cyan
            break
        }
    }
}

if (-not $python312) {
    Write-Host ""
    Write-Host "  [ERROR] Python 3.12.x NO fue encontrado en el sistema" -ForegroundColor Red
    Write-Host ""
    Write-Host "  Por favor instala Python 3.12 desde:" -ForegroundColor Yellow
    Write-Host "  https://www.python.org/downloads/release/python-3128/" -ForegroundColor White
    Write-Host ""
    Write-Host "  Durante la instalacion asegurate de:" -ForegroundColor Yellow
    Write-Host "    - Marcar 'Add Python.exe to PATH'" -ForegroundColor White
    Write-Host "    - Marcar 'Install for all users' (recomendado)" -ForegroundColor White
    exit 1
}

# ============================================================
# PASO 3: Navegar al directorio backend-ai
# ============================================================
Write-Host ""
Write-Host "[3/8] Navegando a backend-ai..." -ForegroundColor Yellow

if (-not (Test-Path $backendAiPath)) {
    Write-Host "  [ERROR] Directorio no encontrado: $backendAiPath" -ForegroundColor Red
    exit 1
}

Set-Location $backendAiPath
Write-Host "  [OK] Directorio actual: $(Get-Location)" -ForegroundColor Green

# ============================================================
# PASO 4: Eliminar entorno virtual anterior
# ============================================================
Write-Host ""
Write-Host "[4/8] Eliminando entorno virtual anterior..." -ForegroundColor Yellow

if (Test-Path "$backendAiPath\venv") {
    Write-Host "  Eliminando carpeta venv existente..." -ForegroundColor DarkGray
    try {
        # Intentar eliminar, puede fallar si hay procesos activos
        Remove-Item -Path "$backendAiPath\venv" -Recurse -Force -ErrorAction Stop
        Write-Host "  [OK] Entorno virtual anterior eliminado" -ForegroundColor Green
    } catch {
        Write-Host "  [!] No se pudo eliminar venv. Buscando procesos bloqueantes..." -ForegroundColor Yellow
        
        # Matar procesos Python que puedan estar usando el venv
        $pythonProcesses = Get-Process -Name python -ErrorAction SilentlyContinue | 
            Where-Object { $_.Path -like "*Gestiona T*venv*" }
        
        if ($pythonProcesses) {
            foreach ($proc in $pythonProcesses) {
                Write-Host "  Deteniendo PID $($proc.Id)..." -ForegroundColor DarkGray
                Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
            }
            Start-Sleep -Seconds 2
            Remove-Item -Path "$backendAiPath\venv" -Recurse -Force
            Write-Host "  [OK] Entorno virtual eliminado despues de detener procesos" -ForegroundColor Green
        } else {
            Write-Host "  [ERROR] No se pudo eliminar venv. Cierra VS Code y terminal e intenta de nuevo." -ForegroundColor Red
            exit 1
        }
    }
} else {
    Write-Host "  [--] No existe venv anterior" -ForegroundColor DarkYellow
}

# ============================================================
# PASO 5: Crear nuevo venv con Python 3.12 explícitamente
# ============================================================
Write-Host ""
Write-Host "[5/8] Creando nuevo entorno virtual con Python 3.12.x..." -ForegroundColor Yellow

Write-Host "  Usando ejecutable: $python312" -ForegroundColor DarkGray

& $python312 -m venv venv

if ($LASTEXITCODE -ne 0) {
    Write-Host "  [ERROR] No se pudo crear el entorno virtual" -ForegroundColor Red
    exit 1
}

Write-Host "  [OK] Entorno virtual creado" -ForegroundColor Green

# ============================================================
# PASO 6: Activar y verificar versión del venv
# ============================================================
Write-Host ""
Write-Host "[6/8] Activando entorno virtual y verificando version..." -ForegroundColor Yellow

$activateScript = "$backendAiPath\venv\Scripts\Activate.ps1"
if (-not (Test-Path $activateScript)) {
    Write-Host "  [ERROR] Script de activacion no encontrado" -ForegroundColor Red
    exit 1
}

# Activar el entorno
& $activateScript

# Verificar la versión del Python en el venv
$venvPythonPath = "$backendAiPath\venv\Scripts\python.exe"
$venvVersion = & $venvPythonPath --version 2>&1

Write-Host "  Version detectada en venv: $venvVersion" -ForegroundColor Cyan

if ($venvVersion -notmatch "Python 3\.12\.") {
    Write-Host ""
    Write-Host "  [ERROR CRITICO] El venv NO esta usando Python 3.12.x" -ForegroundColor Red
    Write-Host "  Version detectada: $venvVersion" -ForegroundColor Red
    Write-Host ""
    Write-Host "  Esto indica un problema con el PATH del sistema." -ForegroundColor Yellow
    Write-Host "  El PATH esta sobrescribiendo el Python del venv." -ForegroundColor Yellow
    exit 1
}

Write-Host "  [OK] Venv usando Python 3.12.x correctamente" -ForegroundColor Green

# ============================================================
# PASO 7: Instalar dependencias
# ============================================================
Write-Host ""
Write-Host "[7/8] Instalando dependencias (esto puede tardar 10-15 minutos)..." -ForegroundColor Yellow

# Usar siempre el python.exe del venv directamente para evitar problemas de PATH
$venvPip = "$backendAiPath\venv\Scripts\pip.exe"
$venvPython = "$backendAiPath\venv\Scripts\python.exe"

Write-Host "  Actualizando pip..." -ForegroundColor DarkGray
& $venvPython -m pip install --upgrade pip --quiet

Write-Host "  Instalando PyTorch CPU-only (version ligera)..." -ForegroundColor DarkGray
& $venvPip install torch==2.5.1+cpu --index-url https://download.pytorch.org/whl/cpu --quiet

if ($LASTEXITCODE -ne 0) {
    Write-Host "  [!] PyTorch no se instalo, intentando sin version especifica..." -ForegroundColor Yellow
    & $venvPip install torch --quiet
}

Write-Host "  Instalando dependencias de requirements.txt..." -ForegroundColor DarkGray
if (Test-Path "$backendAiPath\requirements.txt") {
    & $venvPip install -r "$backendAiPath\requirements.txt" --quiet
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  [!] Algunas dependencias fallaron, instalando una por una..." -ForegroundColor Yellow
        $packages = Get-Content "$backendAiPath\requirements.txt" | Where-Object { $_ -and $_ -notmatch "^#" }
        foreach ($pkg in $packages) {
            & $venvPip install $pkg --quiet 2>$null
        }
    }
} else {
    Write-Host "  [!] requirements.txt no encontrado, instalando paquetes basicos..." -ForegroundColor Yellow
    & $venvPip install fastapi uvicorn[standard] pydantic spacy transformers sentence-transformers scikit-learn numpy pandas python-dotenv loguru httpx --quiet
}

Write-Host "  [OK] Dependencias instaladas" -ForegroundColor Green

# ============================================================
# PASO 8: Descargar modelo spaCy
# ============================================================
Write-Host ""
Write-Host "[8/8] Descargando modelo spaCy es_core_news_lg..." -ForegroundColor Yellow

& $venvPython -m spacy download es_core_news_lg

if ($LASTEXITCODE -eq 0) {
    Write-Host "  [OK] Modelo spaCy descargado" -ForegroundColor Green
} else {
    Write-Host "  [!] Error con modelo grande, intentando modelo pequeno..." -ForegroundColor Yellow
    & $venvPython -m spacy download es_core_news_sm
}

# ============================================================
# VERIFICACION FINAL
# ============================================================
Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  VERIFICACION FINAL" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""

Write-Host "Paquetes criticos instalados:" -ForegroundColor Cyan
& $venvPython -c "import sys; print(f'  Python: {sys.version}')"
& $venvPython -c "import numpy; print(f'  NumPy: {numpy.__version__}')" 2>$null
& $venvPython -c "import torch; print(f'  PyTorch: {torch.__version__}')" 2>$null
& $venvPython -c "import spacy; print(f'  spaCy: {spacy.__version__}')" 2>$null
& $venvPython -c "import fastapi; print(f'  FastAPI: {fastapi.__version__}')" 2>$null

Write-Host ""
Write-Host "Modelos spaCy disponibles:" -ForegroundColor Cyan
& $venvPython -m spacy validate 2>$null

# ============================================================
# INSTRUCCIONES FINALES
# ============================================================
Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  ENTORNO VIRTUAL LISTO CON PYTHON 3.12.x" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Para iniciar el Backend-AI:" -ForegroundColor Yellow
Write-Host "  cd 'F:\codes\Gestiona T\backend-ai'" -ForegroundColor White
Write-Host "  .\venv\Scripts\Activate.ps1" -ForegroundColor White
Write-Host "  uvicorn app.main:app --reload --port 8007" -ForegroundColor White
Write-Host ""
Write-Host "IMPORTANTE: Siempre activa el venv ANTES de ejecutar uvicorn" -ForegroundColor Yellow
Write-Host "Si ves '(venv)' al inicio de tu prompt, estas en el venv correcto" -ForegroundColor Yellow
Write-Host ""