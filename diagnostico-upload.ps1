# ==========================================
# SCRIPT DE DIAGNÓSTICO: Falla en Subida de Archivos
# Proyecto: Gestiona T
# ==========================================

Write-Host "🔍 INICIANDO DIAGNÓSTICO DE SUBIDA DE ARCHIVOS..." -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

$projectRoot = "F:\codes\Gestiona T"
$backendModel = "$projectRoot\backend-core\src\main\java\mx\ine\gestiona_t\modules\documentos\model\Documento.java"
$frontendApi = "$projectRoot\frontend\src\lib\api.ts"
$testFile = "$projectRoot\test-diagnostico.pdf"

# 1. Verificar el tipo de dato de metadataValidacion en Documento.java
Write-Host "`n[1/4] Verificando entidad Documento.java (Causa #1 de error 500)..." -ForegroundColor Yellow
$errorJson = Select-String -Path $backendModel -Pattern "private String metadataValidacion" -Quiet
$correctJson = Select-String -Path $backendModel -Pattern "private Map<String, Object> metadataValidacion" -Quiet

if ($errorJson) {
    Write-Host "❌ ERROR CRÍTICO: 'metadataValidacion' sigue siendo de tipo 'String'." -ForegroundColor Red
    Write-Host "   Esto causa el error SQLState 42804 (jsonb vs varchar) y rollback de la transacción (Error 500)." -ForegroundColor Red
} elseif ($correctJson) {
    Write-Host "✅ CORRECTO: 'metadataValidacion' es de tipo 'Map<String, Object>'." -ForegroundColor Green
} else {
    Write-Host "⚠️ ADVERTENCIA: No se encontró la variable 'metadataValidacion' en el archivo." -ForegroundColor Yellow
}

# 2. Verificar límites de multipart en application.properties
Write-Host "`n[2/4] Verificando límites de subida en Spring Boot..." -ForegroundColor Yellow
$propsFile = "$projectRoot\backend-core\src\main\resources\application.properties"
if (Test-Path $propsFile) {
    $maxFileSize = Select-String -Path $propsFile -Pattern "spring.servlet.multipart.max-file-size"
    $maxReqSize = Select-String -Path $propsFile -Pattern "spring.servlet.multipart.max-request-size"
    
    if ($maxFileSize -and $maxReqSize) {
        Write-Host "✅ Límites de multipart configurados:" -ForegroundColor Green
        Write-Host "   $maxFileSize" -ForegroundColor Gray
        Write-Host "   $maxReqSize" -ForegroundColor Gray
    } else {
        Write-Host "⚠️ ADVERTENCIA: No se encontraron límites de multipart. Spring usa 1MB/10MB por defecto." -ForegroundColor Yellow
        Write-Host "   Agrega: spring.servlet.multipart.max-file-size=10MB" -ForegroundColor Yellow
    }
} else {
    Write-Host "⚠️ No se encontró application.properties" -ForegroundColor Yellow
}

# 3. Verificar el interceptor de Axios
Write-Host "`n[3/4] Verificando interceptor de Axios (api.ts)..." -ForegroundColor Yellow
$axiosFormDataCheck = Select-String -Path $frontendApi -Pattern "config\.data instanceof FormData" -Quiet
$axiosDeleteHeader = Select-String -Path $frontendApi -Pattern "delete config\.headers\['Content-Type'\]" -Quiet

if ($axiosFormDataCheck -and $axiosDeleteHeader) {
    Write-Host "✅ CORRECTO: El interceptor detecta FormData y elimina el Content-Type forzado." -ForegroundColor Green
} else {
    Write-Host "❌ ERROR: El interceptor NO está configurado correctamente para FormData." -ForegroundColor Red
    Write-Host "   Asegúrate de agregar: if (config.data instanceof FormData) { delete config.headers['Content-Type']; }" -ForegroundColor Red
}

# 4. Crear archivo de prueba y preparar comando cURL
Write-Host "`n[4/4] Preparando prueba de fuego con cURL..." -ForegroundColor Yellow
if (-not (Test-Path $testFile)) {
    Write-Host "   Creando archivo PDF de prueba dummy (1 byte)..." -ForegroundColor Gray
    "Dummy PDF" | Out-File -FilePath $testFile -Encoding ASCII
}

Write-Host "`n==================================================" -ForegroundColor Cyan
Write-Host "📋 INSTRUCCIONES PARA LA PRUEBA DE FUEGO:" -ForegroundColor Cyan
Write-Host "1. Abre tu navegador, ve al Panel, abre F12 -> Application -> Local Storage."
Write-Host "2. Copia el valor de 'accessToken'."
Write-Host "3. Reemplaza <TU_TOKEN_AQUI> en el siguiente comando y ejecútalo en esta misma terminal:"
Write-Host "==================================================" -ForegroundColor Cyan

$curlCommand = @"
curl.exe -X POST "http://localhost:8087/api/v1/documentos/upload" `
  -H "Authorization: Bearer <TU_TOKEN_AQUI>" `
  -F "file=@`"$testFile`"" `
  -F "tipo=CV" `
  -v
"@

Write-Host $curlCommand -ForegroundColor White
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "💡 Si el cURL funciona (HTTP 200), el problema es 100% del frontend (Next.js/Axios)." -ForegroundColor Magenta
Write-Host "💡 Si el cURL falla (HTTP 500), el problema es 100% del backend (Java/DB)." -ForegroundColor Magenta