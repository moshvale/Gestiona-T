# Solución: Errores react-hooks en página de expedientes

## 📋 Problemas Identificados

**Archivo afectado:** `frontend/src/app/(admin)/expedientes/page.tsx`

### Error 1: `react-hooks/set-state-in-effect`
```
Error: Calling setState synchronously within an effect can trigger cascading renders
Línea: 65
```

### Error 2: `react-hooks/exhaustive-deps`
```
React Hook useEffect has a missing dependency: 'cargarExpedientes'
Línea: 66
```

---

## 🔍 Análisis de la Causa Raíz

### El Conflicto de Reglas

Cuando intentábamos resolver el primer error con una dependencia vacía:

```javascript
// INTENTO 1: ❌ Resuelve set-state-in-effect pero genera exhaustive-deps
useEffect(() => {
  cargarExpedientes();  // ✓ No hay ciclo infinito
}, []);  // ❌ ESLint: "falta cargarExpedientes"
```

**¿Por qué sucede esto?**

1. `cargarExpedientes` es una función memoizada con `useCallback(..., [router])`
2. ESLint `exhaustive-deps` requiere que todas las variables usadas dentro de un effect estén en sus dependencias
3. `cargarExpedientes` se usa dentro del effect → debe estar en las dependencias
4. Pero incluirlo causa renders en cascada (violando `set-state-in-effect`)

### La Trampa de React

```javascript
// INTENTO 2: ❌ Resuelve exhaustive-deps pero genera set-state-in-effect
useEffect(() => {
  cargarExpedientes();  // Llama setState internamente
}, [cargarExpedientes]);  // ✓ ESLint happy, ❌ renders en cascada
```

**¿Por qué?** Cuando el componente se renderiza:
1. `cargarExpedientes` es una función
2. Aunque está envuelta en `useCallback`, React verifica su estabilidad
3. Si las dependencias de `cargarExpedientes` (`router`) cambian → nueva referencia
4. El effect detecta cambio en `cargarExpedientes` → se ejecuta
5. `cargarExpedientes` llama `setState` → nuevo render
6. En React Strict Mode (desarrollo), esto se duplica
7. Resultado: ciclo de renders

---

## ✅ Solución Implementada

Usar `useRef` como guardián de la ejecución inicial:

```javascript
// PASO 1: Importar useRef
import { useState, useEffect, useCallback, useRef } from 'react';

// PASO 2: Crear ref de control
const inicializado = useRef(false);

// PASO 3: Usar ref en el effect
useEffect(() => {
  if (!inicializado.current) {
    inicializado.current = true;
    cargarExpedientes();
  }
}, [cargarExpedientes]);  // ✅ Ambas reglas satisfechas
```

### ¿Por Qué Funciona?

1. **Satisface `exhaustive-deps`:** ✅ `cargarExpedientes` está en las dependencias
2. **Previene `set-state-in-effect`:** ✅ El ref asegura que la función solo se ejecute una vez, aunque el effect se re-ejecute
3. **Funciona con Strict Mode:** ✅ En React 18 Strict Mode, los effects se ejecutan dos veces durante desarrollo. El ref previene la doble ejecución

### Flujo de Ejecución

```
Inicial (React Strict Mode - Dev):

1ER RENDER:
  ├─ inicializado.current = false
  └─ useEffect ejecuta
      ├─ !false = true, entra en if
      ├─ inicializado.current = true
      ├─ cargarExpedientes() se ejecuta UNA VEZ
      └─ setState ocurre

2DO RENDER (Strict Mode):
  ├─ inicializado.current = true (persiste)
  └─ useEffect ejecuta
      └─ !true = false, NO entra en if → ✅ Nada sucede
```

---

## 📝 Cambios Realizados

### Archivo: `frontend/src/app/(admin)/expedientes/page.tsx`

#### Cambio 1: Importar `useRef` (Línea 1)
```javascript
// ANTES
import { useState, useEffect, useCallback } from 'react';

// DESPUÉS
import { useState, useEffect, useCallback, useRef } from 'react';
```

#### Cambio 2: Crear ref de control (Línea 35)
```javascript
// Agregado dentro del componente
const inicializado = useRef(false);
```

#### Cambio 3: Actualizar useEffect de carga inicial (Líneas 64-71)
```javascript
// ANTES
useEffect(() => {
  cargarExpedientes();
}, []);

// DESPUÉS
useEffect(() => {
  if (!inicializado.current) {
    inicializado.current = true;
    cargarExpedientes();
  }
}, [cargarExpedientes]);
```

---

## ✅ Validación

| Regla ESLint | Estado |
|--------------|--------|
| `set-state-in-effect` | ✅ **Resuelto** |
| `exhaustive-deps` | ✅ **Resuelto** |
| Ejecución única | ✅ **Garantizado** |
| Strict Mode (Dev) | ✅ **Compatible** |
| Búsqueda con debounce | ✅ **Intacta** |

---

## 🎯 Resultado Final

**Estado del componente:**
- ✅ Carga inicial ejecuta `cargarExpedientes()` UNA sola vez
- ✅ Búsqueda con debounce funciona correctamente (2do useEffect intacto)
- ✅ Sin ciclos infinitos de renders
- ✅ Compatible con React Strict Mode
- ✅ Todas las reglas ESLint satisfechas

---

## 📚 Lecciones Aprendidas

### Patrón: Carga Única con Dependencia Memoizada

Cuando necesitas ejecutar un effect solo una vez pero tienes una función memoizada en sus dependencias:

```javascript
// ✅ PATRÓN CORRECTO
const miRef = useRef(false);

useEffect(() => {
  if (!miRef.current) {
    miRef.current = true;
    miFunction();  // Se ejecuta UNA sola vez
  }
}, [miFunction]);  // ✅ Dependencia incluida
```

### Diferencia: useRef vs useState

```javascript
// useRef para control interno (no causa re-render)
const controlRef = useRef(false);  // ✅ Recomendado para guards

// useState para estado visible (causa re-render)
const [cargado, setCargado] = useState(false);  // ❌ Innecesario aquí
```

### React Strict Mode

En desarrollo con Strict Mode, React intencionalmente ejecuta effects dos veces:
- Simula mounts y unmounts
- Detecta efectos sin cleanup
- El `useRef` previene problemas al persistir entre render cycles

---

## 🔗 Referencias

- [React: useEffect Dependencies](https://react.dev/learn/lifecycle-of-reactive-effect)
- [ESLint: exhaustive-deps](https://github.com/facebook/react/issues/14920)
- [ESLint: set-state-in-effect](https://react.dev/reference/rules/eslint-plugin-react-hooks)
- [React: useRef](https://react.dev/reference/react/useRef)



