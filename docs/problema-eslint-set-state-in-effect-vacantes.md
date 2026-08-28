# Problema y solucion: `set-state-in-effect` en vacantes

## Problema

ESLint reportaba el error `react-hooks/set-state-in-effect` en
`frontend/src/app/(admin)/vacantes/page.tsx`:

```text
Calling setState synchronously within an effect can trigger cascading renders
```

El aviso se producia en la llamada `cargarVacantes()` realizada directamente
dentro de `useEffect`.

## Causa

La funcion `cargarVacantes` estaba definida fuera del efecto y actualizaba los
estados `vacantes` e `isLoading`. Aunque esas actualizaciones ocurrían despues
de una peticion HTTP, la regla de React Hooks analizaba la llamada indirecta
desde el cuerpo del efecto como una posible actualizacion sincrona de estado.

Ademas, el `useCallback` solo se utilizaba para estabilizar esa funcion y no
era necesario para ninguna otra accion del componente.

## Solucion aplicada

En `frontend/src/app/(admin)/vacantes/page.tsx`:

- Se elimino la importacion y el uso innecesario de `useCallback`.
- La carga inicial se coloco dentro de una funcion asincrona local al
  `useEffect`.
- La peticion se inicia con `void cargarVacantes()` para dejar explicito que
  el efecto no necesita esperar su resultado.
- Se agrego una bandera `activo` y una funcion de limpieza para evitar
  actualizar estado si el componente se desmonta antes de terminar la
  peticion.
- Se mantuvieron los estados y el comportamiento de carga existentes.

## Validacion

Se ejecuto:

```text
npm run lint -- "src/app/(admin)/vacantes/page.tsx"
```

Resultado: correcto, sin errores ni advertencias.