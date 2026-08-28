Write-Host "=== INICIO DEL DIAGNÓSTICO DE BACKEND ===" -ForegroundColor Cyan

$basePath = "F:\codes\Gestiona T\backend-core\src\main\java\mx\ine\gestiona_t\modules\auth"

# 1. Verificar RegistroIniciarRequest.java
Write-Host "`n1. Verificando RegistroIniciarRequest.java:" -ForegroundColor Yellow
$requestFile = Join-Path $basePath "dto\request\RegistroIniciarRequest.java"
if (Test-Path $requestFile) {
    Write-Host "Archivo encontrado. Buscando campo 'curp'..."
    $content = Get-Content $requestFile -Raw
    if ($content -match "curp") {
        Write-Host "✅ El campo 'curp' SÍ está presente en el archivo." -ForegroundColor Green
    } else {
        Write-Host "❌ El campo 'curp' NO está en el archivo." -ForegroundColor Red
    }
} else {
    Write-Host "❌ NO ENCONTRADO: $requestFile" -ForegroundColor Red
}

# 2. Verificar AuthServiceImpl.java (líneas clave)
Write-Host "`n2. Verificando valores por defecto en AuthServiceImpl.java:" -ForegroundColor Yellow
$serviceFile = Join-Path $basePath "service\AuthServiceImpl.java"
if (Test-Path $serviceFile) {
    $found = Get-Content $serviceFile | Select-String -Pattern "ASPIRANTE EN PRE-REGISTRO|PENDIENTE0000000000|XAXX010101000"
    if ($found) {
        Write-Host "✅ CÓDIGO NUEVO ENCONTRADO en el archivo .java" -ForegroundColor Green
        $found | Select-Object LineNumber, Line | Format-Table -AutoSize
    } else {
        Write-Host "❌ CÓDIGO NUEVO NO ENCONTRADO. El archivo .java aún tiene la versión antigua." -ForegroundColor Red
    }
    
    $javaDate = (Get-Item $serviceFile).LastWriteTime
    Write-Host "📅 Fecha de última modificación del .java: $javaDate" -ForegroundColor White
} else {
    Write-Host "❌ NO ENCONTRADO: $serviceFile" -ForegroundColor Red
}

# 3. Verificar archivos .class compilados
Write-Host "`n3. Verificando compilación (.class):" -ForegroundColor Yellow
$classFile = "F:\codes\Gestiona T\backend-core\target\classes\mx\ine\gestiona_t\modules\auth\service\AuthServiceImpl.class"
if (Test-Path $classFile) {
    $classDate = (Get-Item $classFile).LastWriteTime
    Write-Host "📅 Fecha de compilación del .class: $classDate" -ForegroundColor White
    
    if (Test-Path $serviceFile) {
        $javaDate = (Get-Item $serviceFile).LastWriteTime
        if ($javaDate -gt $classDate) {
            Write-Host "⚠️ ADVERTENCIA CRÍTICA: El archivo .java es MÁS RECIENTE que el .class." -ForegroundColor Red
            Write-Host "   ➡️ Esto significa que Spring Boot está ejecutando el código VIEJO compilado, no el nuevo." -ForegroundColor Yellow
            Write-Host "   ➡️ Solución: Detén el backend y ejecuta: mvn clean compile" -ForegroundColor Yellow
        } else {
            Write-Host "✅ El .class es más reciente o igual que el .java (Compilación al día)." -ForegroundColor Green
        }
    }
} else {
    Write-Host "❌ NO ENCONTRADO: El proyecto no se ha compilado o la ruta target/classes es incorrecta." -ForegroundColor Red
}

Write-Host "`n=== FIN DEL DIAGNÓSTICO ===" -ForegroundColor Cyan
Write-Host "Por favor, copia y pega toda esta salida en el chat." -ForegroundColor White