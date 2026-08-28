# Problema y solución: bloques declaratorios en `/carta`

## Síntoma

En `http://localhost:3007/carta` no aparecían los 12 bloques declaratorios que el aspirante debe aceptar antes de firmar la carta.

## Causa raíz

El frontend solicita los bloques mediante:

`GET /api/v1/carta-declaratoria/{folio}/bloques`

El backend obtiene únicamente los registros de `bloques_declaratorios` cuyo campo `activo` es verdadero. Los 12 registros se insertaban solo desde scripts de inicialización de PostgreSQL. Esos scripts se ejecutan únicamente cuando se crea el volumen de datos por primera vez; no se vuelven a ejecutar al levantar un volumen existente.

La base local tenía la tabla creada, pero contenía `0` registros:

- Total de bloques antes de la corrección: `0`.
- Bloques activos antes de la corrección: `0`.
- Resultado visible: el componente React recibía una lista vacía y `bloques.map(...)` no renderizaba tarjetas.

Durante la verificación también se encontró una clase compilada corrupta en `target/classes`, que impedía iniciar el backend con `ClassFormatError`. Se regeneró el directorio de compilación con `mvn clean compile`.

## Solución aplicada

1. Se agregó `BloquesDeclaratoriosInitializer`, ejecutado al arrancar Spring Boot.
2. El inicializador crea de forma idempotente los 12 bloques faltantes, marca cada uno como obligatorio y activo, y conserva registros existentes sin duplicarlos.
3. Se mantuvo el orden declaratorio del 1 al 12.
4. Se hizo idempotente la aceptación individual: si un bloque ya fue aceptado y el usuario repite la acción, se devuelve la aceptación existente en lugar de crear un registro duplicado.
5. Se regeneraron las clases compiladas con `mvn clean compile`.

## Archivos modificados

- `backend-core/src/main/java/mx/ine/gestiona_t/modules/cartadeclaratoria/config/BloquesDeclaratoriosInitializer.java`
- `backend-core/src/main/java/mx/ine/gestiona_t/modules/cartadeclaratoria/service/CartaDeclaratoriaServiceImpl.java`

## Pruebas realizadas

### Compilación

```powershell
Set-Location backend-core
mvn clean compile
```

Resultado: correcto.

### Verificación de datos

Después de iniciar el backend, la base reportó:

- Total: `12`.
- Activos: `12`.
- Órdenes: del `1` al `12`.

El log de Spring confirmó que el inicializador insertó los 12 registros durante el primer arranque corregido.

### Flujo funcional de aceptación

Se ejecutó una prueba autenticada que realizó estas operaciones:

1. Obtener el perfil y el folio del aspirante.
2. Iniciar la carta cuando no existía.
3. Consultar los bloques.
4. Confirmar que se recibieron `12` bloques.
5. Ejecutar `aceptar-todos`.
6. Consultar nuevamente los bloques y el estatus.
7. Repetir la aceptación del bloque 1 para comprobar idempotencia.

Resultado:

```json
{
  "bloquesAntes": 12,
  "bloquesDespues": 12,
  "aceptados": 12,
  "bloquesCompletos": true,
  "estatus": "BLOQUES_ACEPTADOS",
  "reintentoBloqueId": 1
}
```

No se produjeron errores durante la aceptación masiva ni durante el reintento individual.
