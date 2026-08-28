# Problema y solución de `AnalistaAdminController`

**Fecha:** 2026-08-19  
**Archivo analizado:** `backend-core/src/main/java/mx/ine/gestiona_t/modules/auth/controller/AnalistaAdminController.java`

## Problema detectado

El editor reportaba dos errores Java `33554502`:

- `ALTA cannot be resolved or is not a field` en la anotación de auditoría del método de alta.
- `ALTA cannot be resolved or is not a field` en la anotación de auditoría del método de desactivación.

El código utilizaba:

```java
severidad = NivelSeveridad.ALTA
```

## Causa raíz

`NivelSeveridad` es un `enum` y no contiene ningún valor llamado `ALTA`. Sus valores válidos son:

```java
INFO,
WARNING,
ERROR,
CRITICAL
```

Por tanto, el compilador no podía resolver `NivelSeveridad.ALTA` como una constante válida.

El problema no estaba en los valores `tipo = "ALTA_ANALISTA"` ni `tipo = "BAJA_ANALISTA"`; ambos son cadenas válidas para la anotación `@Auditable`.

## Solución realizada

Se reemplazó `NivelSeveridad.ALTA` por `NivelSeveridad.WARNING` en las dos anotaciones `@Auditable` del controlador:

- `crearAnalista`: auditoría de alta de analista.
- `desactivarAnalista`: auditoría de baja/desactivación de analista.

`WARNING` es un nivel existente y coherente con las operaciones administrativas sensibles. El aspecto de auditoría conserva además la posibilidad de registrar `ERROR` cuando una operación falla.

## Revisión de dependencias relacionadas

Durante el análisis se comprobó que:

- `@Auditable.severidad()` requiere exactamente un valor de `NivelSeveridad`.
- `AnalistaService.listarAnalistas()` devuelve `List<AnalistaResumenDTO>`, que coincide con el tipo de respuesta del controlador.
- `AnalistaResumenDTO` evita devolver directamente la entidad `Analista` y su hash de contraseña.
- Los métodos continúan protegidos por `@PreAuthorize("hasAuthority('ROLE_ADMIN_SISTEMA')")`.
- La validación del secreto del portal sigue centralizada en `validarSecret(...)`.

## Verificación

Se ejecutó desde `backend-core`:

```text
mvn -DskipTests compile
```

Resultado:

```text
BUILD SUCCESS
```

El backend compiló sus 246 archivos fuente sin errores. Maven mostró únicamente advertencias existentes sobre APIs restringidas/deprecadas y operaciones unchecked en `MatchingService`, sin relación con este controlador.

## Resultado

Los dos errores `ALTA cannot be resolved or is not a field` quedaron corregidos. No fue necesario modificar el enum ni cambiar el contrato público de los endpoints.
