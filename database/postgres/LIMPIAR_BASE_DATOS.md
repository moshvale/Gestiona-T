# Limpiar Base de Datos - Gestiona-T

## 🚨 Advertencia
Este procedimiento **ELIMINA TODOS LOS DATOS** de usuarios, intentos de autenticación y documentos de la base de datos. Úsalo solo en desarrollo/testing.

---

## 📋 Descripción

Este script limpia la base de datos `gestiona_t` de:
- ✅ Todos los aspirantes registrados
- ✅ Todos los intentos de autenticación (login)
- ✅ Códigos OTP
- ✅ CVs estructurados e institucionales
- ✅ Documentos y expedientes digitales
- ✅ Cartas declaratorias
- ✅ Registros de auditoría
- ✅ Reinicia secuencias (IDs) a valor inicial (1)

---

## 🔧 Requisitos

- Docker Compose ejecutándose con el servicio PostgreSQL
- Contenedor: `gestiona-t-postgres`
- Base de datos: `gestiona_t`
- Usuario: `gestiona_user`

Verificar que el contenedor está activo:
```bash
docker ps | grep postgres
```

---

## 📌 Método 1: Ejecutar Script desde Archivo (Recomendado)

### Paso 1: Copiar el script al contenedor
```bash
docker cp database/postgres/scripts/clear_all_data.sql gestiona-t-postgres:/tmp/clear_all_data.sql
```

### Paso 2: Ejecutar el script
```bash
docker exec -i gestiona-t-postgres psql -U gestiona_user -d gestiona_t -f /tmp/clear_all_data.sql
```

### Verificación
```bash
docker exec -i gestiona-t-postgres psql -U gestiona_user -d gestiona_t << 'EOF'
SELECT 
    (SELECT COUNT(*) FROM aspirantes) as aspirantes,
    (SELECT COUNT(*) FROM intentos_auth) as intentos_auth,
    (SELECT COUNT(*) FROM codigos_otp) as codigos_otp,
    (SELECT COUNT(*) FROM documentos) as documentos;
EOF
```

---

## 📌 Método 2: Ejecutar Script vía stdin (Alternativa)

```bash
docker exec -i gestiona-t-postgres psql -U gestiona_user -d gestiona_t < database/postgres/scripts/clear_all_data.sql
```

---

## 📌 Método 3: Comando Rápido Inline

```bash
docker exec -i gestiona-t-postgres psql -U gestiona_user -d gestiona_t << 'SQL_EOF'
DELETE FROM codigos_otp;
DELETE FROM cv_estructurados;
DELETE FROM cv_institucionales;
DELETE FROM documentos;
DELETE FROM expedientes_digitales;
DELETE FROM cartas_declaratorias;
DELETE FROM documentos_firmados;
DELETE FROM cv_escolaridad;
DELETE FROM cv_cursos_capacitaciones;
DELETE FROM cv_habilidades_tecnicas;
DELETE FROM cv_formacion_academica;
DELETE FROM cv_experiencia_laboral;
DELETE FROM cv_idiomas;
DELETE FROM cv_cursos_capacitaciones_institucionales;
DELETE FROM revisiones_manuales;
DELETE FROM aceptaciones_bloques;
DELETE FROM validaciones_externas_carta;
DELETE FROM sellos_digitales;
DELETE FROM firmas_metadata;
DELETE FROM audit_cadena_hash;
DELETE FROM audit_eventos;
DELETE FROM intentos_auth;
DELETE FROM aspirantes;

DO $$
DECLARE
    seq RECORD;
BEGIN
    FOR seq IN (
        SELECT sequence_schema, sequence_name 
        FROM information_schema.sequences 
        WHERE sequence_schema = 'public'
    ) LOOP
        EXECUTE format('ALTER SEQUENCE %I.%I RESTART WITH 1;', seq.sequence_schema, seq.sequence_name);
    END LOOP;
END $$;

SELECT 'Base de datos limpiada exitosamente ✅' as estado;
SQL_EOF
```

---

## ✅ Flujo Completo de Preparación para Testing

### 1. Limpiar Base de Datos
```bash
docker exec -i gestiona-t-postgres psql -U gestiona_user -d gestiona_t < database/postgres/scripts/clear_all_data.sql
```

### 2. Reiniciar Backend (nueva terminal)
```bash
cd backend-core
mvn -DskipTests spring-boot:run
```

### 3. Reiniciar Frontend (otra terminal)
```bash
cd frontend
npm run dev
```

### 4. Abrir Navegador
```bash
# Acceder a: http://localhost:3007
```

---

## 🧪 Pruebas Manuales Recomendadas

Después de limpiar la BD y reiniciar servicios:

1. **Prueba de Registro**
   - URL: `http://localhost:3007/registro`
   - Registrar usuario: `test@example.com` / `Password123!`
   - Verificar que se crea correctamente

2. **Prueba de Login Exitoso**
   - URL: `http://localhost:3007/login`
   - Usar credenciales registradas
   - Verificar que entra al dashboard

3. **Prueba de Login Fallido (Credenciales Inválidas)**
   - URL: `http://localhost:3007/login`
   - Email: `test@example.com`
   - Contraseña: `WrongPassword123!`
   - **Verificar que devuelve HTTP 401 (no 500)**

4. **Prueba de Recuperación de Contraseña**
   - URL: `http://localhost:3007/recuperar-contrasena`
   - Verificar que la página carga sin errores 404

5. **Prueba de Bloqueo de Cuenta**
   - Intentar login fallido 5 veces seguidas
   - Verificar que se bloquea la cuenta
   - Verificar que devuelve HTTP 423 (Locked)

---

## 📊 Estructura de Tablas Limpiadas

### Tablas Principales
- `aspirantes` - Usuarios del sistema
- `intentos_auth` - Registros de intentos de login

### Tablas Dependientes
- `codigos_otp` - Códigos de un único uso
- `cv_*` - CV estructurado e institucional
- `documentos` - Documentos cargados
- `expedientes_digitales` - Expedientes de users
- `cartas_declaratorias` - Cartas firmadas
- `audit_*` - Registros de auditoría

---

## ⚠️ Solución de Problemas

### Error: "could not connect to server"
Verificar que PostgreSQL está activo:
```bash
docker compose up -d postgres
```

### Error: "TRUNCATE no permitido en audit_eventos"
El script maneja esto automáticamente usando `DELETE` en lugar de `TRUNCATE`.

### Error: "relation does not exist"
Verificar que la base de datos está creada:
```bash
docker exec -i gestiona-t-postgres psql -U gestiona_user -d gestiona_t -c "\dt"
```

---

## 📝 Notas

- El script respeta las relaciones de Foreign Keys eliminando en orden correcto
- Los triggers de auditoría se desactivan para permitir eliminaciones
- Las secuencias (IDs auto-increment) se reinician a 1
- **No requiere apagado de servicios** (la BD acepta conexiones durante limpieza)
- **Cambios aplicables a cualquier entorno** con Docker

---

## 🔗 Archivo del Script
```
database/postgres/scripts/clear_all_data.sql
```

