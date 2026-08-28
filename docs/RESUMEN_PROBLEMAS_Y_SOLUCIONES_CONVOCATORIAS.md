# Resumen de problemas y soluciones

## 1) Frontend: warning `react-hooks/immutability` en `convocatorias/page.tsx`

### Origen
La función `cargarVacantes` se estaba invocando desde un `useEffect` antes de que fuera declarada en el mismo bloque del componente. En React/ES modules, esta referencia temporal de una constante antes de su inicialización dispara la regla de inmutabilidad/hoisting del linter y además puede producir un comportamiento inconsistente al actualizar estado.

### Solución aplicada
- Se movió la lógica a una función memoizada con `useCallback`.
- Se consolidó la carga inicial y la búsqueda por texto en el mismo `useEffect` con dependencia de `busqueda` y `cargarVacantes`.
- Se evitó la llamada directa a la función antes de su definición.

### Archivos afectados
- [frontend/src/app/convocatorias/page.tsx](../frontend/src/app/convocatorias/page.tsx)

---

## 2) Frontend: import no utilizado `DollarSign`

### Origen
El icono `DollarSign` se importaba desde `lucide-react`, pero no se renderizaba en ninguna parte del componente. ESLint lo marca como variable no utilizada.

### Solución aplicada
- Se eliminó la importación de `DollarSign` de los íconos usados en ambas páginas.

### Archivos afectados
- [frontend/src/app/convocatorias/page.tsx](../frontend/src/app/convocatorias/page.tsx)
- [frontend/src/app/convocatorias/[id]/page.tsx](../frontend/src/app/convocatorias/[id]/page.tsx)

---

## 3) Frontend: `@typescript-eslint/no-explicit-any` en detalle de convocatoria

### Origen
Se estaban usando tipos `any` en dos sitios:
- al recorrer la lista de postulaciones para comparar `vacanteId`
- en el bloque `catch` del manejo de la postulación

Esto rompe la intención de TypeScript y provoca errores de lint por tipado débil.

### Solución aplicada
- Se reemplazó la colección `response.data.some((p: any) => ...)` por una estructura tipada con `Array<{ vacanteId: string }>`.
- Se reemplazó el catch `error: any` por `error: unknown` con una cast segura a un objeto de error con `response?.data?.message`.

### Archivo afectado
- [frontend/src/app/convocatorias/[id]/page.tsx](../frontend/src/app/convocatorias/[id]/page.tsx)

---

## 4) Backend Java: import de `AspiranteRepository` no resuelto

### Origen
El servicio de postulaciones importaba dos clases desde un paquete incorrecto:
- `mx.ine.gestiona_t.modules.aspirantes.model.Aspirante`
- `mx.ine.gestiona_t.modules.aspirantes.repository.AspiranteRepository`

El proyecto real usa el paquete de autenticación:
- `mx.ine.gestiona_t.modules.auth.model.Aspirante`
- `mx.ine.gestiona_t.modules.auth.repository.AspiranteRepository`

### Solución aplicada
- Se corrigieron las importaciones para apuntar al repositorio y modelo correctos.

### Archivo afectado
- [backend-core/src/main/java/mx/ine/gestiona_t/modules/postulaciones/service/PostulacionServiceImpl.java](../backend-core/src/main/java/mx/ine/gestiona_t/modules/postulaciones/service/PostulacionServiceImpl.java)

---

## 5) Backend Java: lambda capturando variables mutables

### Origen
En el método `mapToResponse`, se modificaban variables locales dentro de lambdas:
- `nombreAspirante = a.getNombreCompleto()`
- `nombrePuesto = v.getPuesto()`

Java exige que las variables capturadas en lambdas sean finales o efectivamente finales, por eso el compilador fallaba.

### Solución aplicada
- Se sustituyeron las variables por arreglos finales (`final String[]`) para permitir la actualización dentro de la lambda sin romper la regla del lenguaje.

### Archivo afectado
- [backend-core/src/main/java/mx/ine/gestiona_t/modules/postulaciones/service/PostulacionServiceImpl.java](../backend-core/src/main/java/mx/ine/gestiona_t/modules/postulaciones/service/PostulacionServiceImpl.java)

---

## Verificación realizada

### Frontend
Comando ejecutado:

```bash
cd "F:\codes\Gestiona T_3\frontend"; npx eslint "src/app/convocatorias/page.tsx" "src/app/convocatorias/[id]/page.tsx"
```

Resultado: sin salida y exit code 0, lo cual confirma que ESLint quedó limpio en los archivos corregidos.

### Backend
Comando ejecutado:

```bash
cd "F:\codes\Gestiona T_3\backend-core"; mvn -q -DskipTests compile
```

Resultado: compilación exitosa con warnings de entorno de Maven/JVM, pero sin errores de compilación del proyecto.

---

## Estado final
Los problemas reportados fueron analizados en su causa raíz y corregidos. El proyecto queda con el frontend sin errores de lint en las pantallas revisadas y el backend compilando correctamente en el servicio de postulaciones.
