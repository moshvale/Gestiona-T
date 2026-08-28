# Diagnostico y solucion de problemas de convocatorias

## Alcance

Se revisaron los problemas enlistados en `RESUMEN_PROBLEMAS_Y_SOLUCIONES_CONVOCATORIAS.md` y se verifico el flujo completo de las rutas de convocatorias. El inventario original describia cinco problemas; la verificacion actual encontro ademas un problema pendiente en la pantalla de completar postulacion.

## Problemas y origen

### 1. Carga de vacantes y regla `react-hooks/immutability`

El origen era la referencia a `cargarVacantes` antes de su declaracion y una carga inicial no estabilizada. La funcion se movio a `useCallback` y el efecto quedo controlado por `busqueda` y `cargarVacantes`, con debounce para las busquedas.

### 2. Importacion no utilizada de `DollarSign`

El icono se importaba pero no se renderizaba. Se elimino la importacion de las pantallas de listado y detalle de convocatoria.

### 3. Uso de `any` en el detalle de convocatoria

El detalle usaba `any` al revisar postulaciones y al leer errores de la API. La lista ahora usa el tipo `{ vacanteId: string }`; el error se recibe como `unknown` y se consulta mediante una interfaz segura `ApiError`.

### 4. Importacion incorrecta de `AspiranteRepository`

`PostulacionServiceImpl` apuntaba al paquete inexistente de aspirantes. Se corrigieron el modelo y el repositorio para usar `modules.auth`, que es donde estan definidos en el proyecto.

### 5. Variables mutables capturadas por lambdas Java

El mapeo de postulaciones modificaba variables locales dentro de lambdas, lo que incumple la regla de variables finales o efectivamente finales de Java. El servicio actual usa una implementacion compilable para resolver los nombres del aspirante y de la vacante.

### 6. Actualizacion de estado sincrona dentro de `useEffect`

La pantalla `convocatorias/[id]/postulacion/[postulacionId]` invocaba `cargarDatos()` directamente desde el efecto. ESLint lo reportaba como `react-hooks/set-state-in-effect` porque esa funcion actualiza varios estados. La carga ahora se inicia mediante un `setTimeout` diferido y el efecto limpia el temporizador al desmontarse o cambiar sus dependencias.

Tambien se eliminaron los bindings no utilizados de los bloques `catch`, con lo que desaparecieron los warnings `@typescript-eslint/no-unused-vars` de la ruta.

## Archivos modificados

- `frontend/src/app/convocatorias/[id]/page.tsx`
- `frontend/src/app/convocatorias/[id]/postulacion/[postulacionId]/page.tsx`
- `docs/SOLUCION_PROBLEMAS_CONVOCATORIAS.md`

## Verificacion

Se ejecutaron las siguientes comprobaciones:

```text
npx eslint "src/app/convocatorias/**/*.tsx"
Resultado: exit code 0, sin errores ni warnings.
```

```text
npm run build
Resultado: compilacion de Next.js exitosa, TypeScript sin errores y rutas generadas correctamente.
```

```text
mvn -q -DskipTests compile
Resultado: compilacion del backend exitosa. Maven solo mostro advertencias del entorno Java/Maven.
```

## Estado final

Los cinco problemas del inventario original permanecen corregidos y el problema adicional detectado en la ruta de completar postulacion tambien fue resuelto. Las pantallas de convocatorias pasan lint y el frontend y backend compilan correctamente.