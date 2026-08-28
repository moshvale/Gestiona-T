# ============================================
# Script de Pruebas E2E - Gestiona-T
# ============================================

$baseUrl = "http://localhost:8087/api/v1"
$testEmail = "test-ine-e2e@ine.mx"
$testPassword = "Test1234!"
$testCurp = "XAXX900101HDFRRN09"
$testTelefono = "5598765432"

Write-Host "`n🧪 INICIANDO PRUEBAS E2E - GESTIONA-T" -ForegroundColor Cyan
Write-Host "=====================================`n" -ForegroundColor Cyan

# --- TEST 1: Registro ---
Write-Host "📝 TEST 1: Registro de nuevo usuario" -ForegroundColor Yellow
$registroBody = @{
    curp = $testCurp
    correo = $testEmail
    telefono = $testTelefono
    password = $testPassword
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/registro/iniciar" `
        -Method POST -Body $registroBody -ContentType "application/json"
    Write-Host "✅ Registro exitoso: $($response.message)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error en registro: $($_.Exception.Message)" -ForegroundColor Red
}

# --- TEST 2: Login ---
Write-Host "`n🔐 TEST 2: Login" -ForegroundColor Yellow
$loginBody = @{
    correo = $testEmail
    password = $testPassword
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
        -Method POST -Body $loginBody -ContentType "application/json"
    $accessToken = $response.accessToken
    Write-Host "✅ Login exitoso. Token recibido: $($accessToken.Substring(0, 20))..." -ForegroundColor Green
} catch {
    Write-Host "❌ Error en login: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# --- TEST 3: Obtener Perfil (requiere auth) ---
Write-Host "`n👤 TEST 3: Obtener perfil autenticado" -ForegroundColor Yellow
try {
    $headers = @{ "Authorization" = "Bearer $accessToken" }
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/perfil" `
        -Method GET -Headers $headers
    Write-Host "✅ Perfil obtenido: $($response.correoElectronico)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error al obtener perfil: $($_.Exception.Message)" -ForegroundColor Red
}

# --- TEST 4: Registro Duplicado (debe fallar con 409) ---
Write-Host "`n🚫 TEST 4: Registro duplicado (esperado 409)" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/registro/iniciar" `
        -Method POST -Body $registroBody -ContentType "application/json"
    Write-Host "❌ FALLA: Debió retornar 409 pero retornó éxito" -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 409) {
        Write-Host "✅ Correcto: Retornó 409 Conflict" -ForegroundColor Green
    } else {
        Write-Host "❌ Error: Retornó $statusCode en lugar de 409" -ForegroundColor Red
    }
}

# --- TEST 5: Eliminar Perfil ---
Write-Host "`n🗑️ TEST 5: Eliminar perfil" -ForegroundColor Yellow
try {
    $headers = @{ "Authorization" = "Bearer $accessToken" }
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/perfil" `
        -Method DELETE -Headers $headers
    Write-Host "✅ Eliminación exitosa: $($response.message)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error al eliminar: $($_.Exception.Message)" -ForegroundColor Red
}

# --- TEST 6: Login con cuenta eliminada (debe fallar) ---
Write-Host "`n🔒 TEST 6: Login con cuenta eliminada (esperado fallo)" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
        -Method POST -Body $loginBody -ContentType "application/json"
    Write-Host "❌ FALLA: Debió rechazar el login" -ForegroundColor Red
} catch {
    Write-Host "✅ Correcto: Login rechazado como se esperaba" -ForegroundColor Green
}

Write-Host "`n=====================================" -ForegroundColor Cyan
Write-Host "🏁 PRUEBAS E2E COMPLETADAS" -ForegroundColor Cyan