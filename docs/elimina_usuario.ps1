# ==========================================
# Script de Limpieza: Eliminar Usuario de Prueba
# ==========================================

# 1. Configuración de la conexión a PostgreSQL (AJUSTA ESTOS VALORES SI ES NECESARIO)
$dbHost = "localhost"       # Cambia a "10.15.0.59" o el nombre de tu contenedor Docker si aplica
$dbPort = "5432"            # Puerto por defecto de PostgreSQL
$dbUser = "postgres"        # Tu usuario de PostgreSQL
$dbName = "postgres"        # Nombre de tu base de datos (ej. "gestiona_t" o "postgres")
$testEmail = "prueba@ine.mx"

Write-Host "🔍 Buscando y eliminando el aspirante con correo: $testEmail" -ForegroundColor Cyan

# 2. Ejecutar el comando de eliminación
# Nota: Te pedirá la contraseña de PostgreSQL. Escríbela y presiona Enter (no se verá mientras escribes).
$deleteQuery = "DELETE FROM aspirantes WHERE correo_electronico = '$testEmail';"

try {
    # Ejecuta psql. Si no está en tu PATH, usa la ruta completa, ej: "C:\Program Files\PostgreSQL\16\bin\psql.exe"
    psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -c $deleteQuery
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ ¡Éxito! El usuario de prueba ha sido eliminado correctamente." -ForegroundColor Green
    } else {
        Write-Host "⚠️ Advertencia: El comando se ejecutó, pero revisa la salida anterior por posibles errores." -ForegroundColor Yellow
    }
}
catch {
    Write-Host "❌ Error al conectar con la base de datos. Verifica tus credenciales y que PostgreSQL esté corriendo." -ForegroundColor Red
    Write-Host "Detalle: $_" -ForegroundColor Red
}

Write-Host "=== FIN DEL SCRIPT ===" -ForegroundColor Cyan