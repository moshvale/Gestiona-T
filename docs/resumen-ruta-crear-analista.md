# Resumen de la ruta `crear-analista`

**Fecha de análisis:** 2026-08-19  
**Ruta de archivo:** `frontend/src/app/(admin)/admin-portal/[secret]/crear-analista/page.tsx`  
**URL renderizada:** `/admin-portal/[secret]/crear-analista`

## 1. Propósito y alcance

La página es un portal administrativo para que un usuario con rol `ADMIN_SISTEMA` pueda:

- Crear cuentas de analista.
- Consultar la lista de analistas activos.
- Desactivar cuentas mediante baja lógica.

El segmento `[secret]` forma parte de la URL y se reutiliza como credencial adicional en el header `X-Admin-Portal-Secret` de las tres llamadas al backend.

## 2. Dependencias directas

### Frontend

- `next/navigation`: `useRouter` para redirecciones y navegación; `useParams` para obtener `secret`.
- `react`: `useState` y `useEffect` para estado y carga inicial.
- `react-hook-form`: gestión del formulario, `register`, `handleSubmit`, `reset` y `watch`.
- `@hookform/resolvers/zod`: integración del esquema con React Hook Form.
- `zod`: validación de nombre, correo, contraseña, confirmación y rol.
- `@/lib/api`: instancia Axios `apiClient` con base URL configurable, JWT automático e interceptor de errores.
- `lucide-react`: iconos de la pantalla.
- Tailwind CSS y clases institucionales (`bg-gradient-ine`, `bg-surface`, `ine-*`, `shadow-institutional`).

### Backend

- `AnalistaAdminController` bajo `/api/v1/admin/analistas`.
- `AnalistaService` y `AnalistaServiceImpl`.
- `CrearAnalistaRequest` para validación de entrada.
- `AnalistaRepository` para persistencia.
- Spring Security con JWT y `@PreAuthorize`.
- Auditoría mediante `@Auditable` en alta y baja.
- `PasswordEncoder` BCrypt para almacenar la contraseña como hash.

## 3. Flujo de ejecución

1. Next carga la página como Client Component (`'use client'`).
2. `useParams()` obtiene el valor de `[secret]`.
3. El `useEffect` compara el valor con `NEXT_PUBLIC_ADMIN_PORTAL_SECRET`.
4. Si no coincide, navega a `/404`.
5. Si coincide, consulta `localStorage.getItem('rol')`.
6. Si el rol no es `ADMIN_SISTEMA`, navega a `/login`.
7. Si pasa ambas comprobaciones, ejecuta `GET /admin/analistas` con el header secreto.
8. Al enviar el formulario, ejecuta `POST /admin/analistas/crear` con los cuatro campos esperados por el backend.
9. Después de crear, limpia el formulario y vuelve a cargar la lista.
10. Al desactivar, solicita confirmación del navegador y ejecuta `DELETE /admin/analistas/{id}`.
11. Un `401` o `403` del `apiClient` elimina los datos de sesión y redirige a `/login`.

## 4. Contrato frontend-backend

| Operación | Cliente | Endpoint backend | Respuesta esperada |
|---|---|---|---|
| Listar activos | `apiClient.get('/admin/analistas')` | `GET /api/v1/admin/analistas` | `200` con arreglo de analistas |
| Crear | `apiClient.post('/admin/analistas/crear', body)` | `POST /api/v1/admin/analistas/crear` | `201` con `MensajeResponse` |
| Desactivar | `apiClient.delete('/admin/analistas/{id}')` | `DELETE /api/v1/admin/analistas/{id}` | `200` con `MensajeResponse` |

Las tres operaciones agregan `X-Admin-Portal-Secret: [secret]`. El interceptor de `apiClient` agrega `Authorization: Bearer <accessToken>` cuando existe token.

### Payload de alta

```json
{
  "nombreCompleto": "María González López",
  "correoElectronico": "analista@ine.mx",
  "password": "NoCompartir123!",
  "rol": "ANALISTA_UR"
}
```

`confirmarPassword` se valida únicamente en el navegador y no se envía al backend.

## 5. Validaciones

### Frontend

- `nombreCompleto`: entre 3 y 100 caracteres; solo letras, acentos, `ñ`, diéresis y espacios.
- `correoElectronico`: formato de correo, termina en `@ine.mx` y se normaliza a minúsculas.
- `password`: entre 12 y 100 caracteres, con mayúscula, número y carácter especial.
- `confirmarPassword`: debe coincidir con `password`.
- `rol`: `ANALISTA_UR`, `ADMIN_SISTEMA` o `CONTRALORIA`.

### Backend

`CrearAnalistaRequest` repite los límites de longitud, la validación de correo, la política de contraseña y el conjunto permitido de roles. `AnalistaServiceImpl` vuelve a recortar nombre/correo, normaliza el correo y rechaza un correo que ya tenga un analista activo.

La contraseña se transforma con BCrypt antes de persistirse.

## 6. Seguridad y autorización

Hay varias capas de protección:

1. **Segmento secreto en URL:** el middleware valida `/admin-portal/[secret]` contra `NEXT_PUBLIC_ADMIN_PORTAL_SECRET`.
2. **Comprobación cliente:** la página compara nuevamente el parámetro con la variable pública.
3. **Rol cliente:** la página exige `localStorage.rol === 'ADMIN_SISTEMA'`.
4. **JWT:** `apiClient` envía el token de sesión.
5. **Autorización backend:** los tres métodos tienen `@PreAuthorize("hasAuthority('ROLE_ADMIN_SISTEMA')")`.
6. **Header secreto backend:** el controlador compara `X-Admin-Portal-Secret` con `admin.portal.secret-path`.
7. **Auditoría:** alta y desactivación están anotadas con `@Auditable`.

### Observaciones de seguridad

- La variable `NEXT_PUBLIC_ADMIN_PORTAL_SECRET` queda expuesta al navegador por definición de Next.js. El secreto de URL/header no debe considerarse un secreto fuerte; el control real depende del JWT, el rol y el backend.
- El middleware intenta consultar `localStorage` mediante `typeof window !== 'undefined'`. En middleware de Next.js se ejecuta contexto servidor/Edge, por lo que esa rama no puede recuperar el rol del navegador. La autorización efectiva queda en la comprobación cliente y, sobre todo, en Spring Security.
- `ADMIN_PORTAL_PATTERN` está declarado en `frontend/middleware.ts` pero no se utiliza.
- La configuración global permite `/api/v1/admin/**` a `ROLE_ANALISTA_UR` o `ROLE_ADMIN_SISTEMA`; los endpoints de esta página quedan restringidos por `@PreAuthorize`, pero cualquier endpoint administrativo nuevo podría quedar expuesto accidentalmente a analistas si no añade una restricción propia.
- El backend devuelve `List<Analista>` directamente. Actualmente `AnalistaServiceImpl` ejecuta `setPasswordHash(null)` antes de devolverla, pero es más robusto usar un DTO de respuesta que no contenga `passwordHash`.
- El valor secreto tiene un fallback conocido tanto en frontend como en backend. Debe sobrescribirse en todos los entornos no locales y mantenerse igual en ambos servicios.

## 7. Estados y comportamiento de UI

- `isLoading`: deshabilita el alta mientras está en curso.
- `mensaje`: muestra resultado de alta o baja.
- `analistas`: almacena la lista activa.
- `cargandoLista`: controla el indicador de carga.
- `mostrarLista`: oculta la lista por defecto y la muestra bajo demanda.
- La fortaleza de contraseña es un indicador visual; no sustituye la validación Zod/backend.
- La baja usa `window.confirm`; no existe modal accesible ni mecanismo explícito para prevenir doble clic durante la eliminación.
- Los errores de carga inicial solo se registran en consola; la pantalla no informa al usuario si falla el listado.

## 8. Hallazgos y pendientes

### Prioridad alta

- Añadir pruebas automatizadas del flujo: acceso con secreto incorrecto, rol no autorizado, alta válida, errores de validación, correo duplicado y desactivación.
- Sustituir la respuesta de entidad `Analista` por un DTO seguro.
- Revisar el middleware para basar la autenticación en cookies/sesión disponibles en servidor o dejar explícito que su control de rol es solo cliente, manteniendo backend como autoridad.

### Prioridad media

- Restringir en `SecurityConfig` las rutas sensibles de `/api/v1/admin/analistas/**` a `ROLE_ADMIN_SISTEMA` también a nivel de configuración.
- Mostrar un error de carga cuando falle `GET /admin/analistas`.
- Deshabilitar el botón de desactivación mientras la operación está en progreso y reemplazar `confirm` por un control accesible si el diseño lo requiere.
- Extraer el contrato y operaciones a un servicio frontend para evitar que la página conozca directamente todos los detalles HTTP.

### Prioridad baja

- Eliminar `ADMIN_PORTAL_PATTERN` si no se va a usar.
- Centralizar constantes de roles y del header secreto para evitar divergencias.
- Considerar una ruta administrativa basada en autorización normal, en vez de depender de un path secreto como parte del descubrimiento de la pantalla.

## 9. Archivos relacionados

- [`frontend/src/app/(admin)/admin-portal/[secret]/crear-analista/page.tsx`](../frontend/src/app/(admin)/admin-portal/[secret]/crear-analista/page.tsx)
- [`frontend/src/lib/api.ts`](../frontend/src/lib/api.ts)
- [`frontend/middleware.ts`](../frontend/middleware.ts)
- [`backend-core/src/main/java/mx/ine/gestiona_t/modules/auth/controller/AnalistaAdminController.java`](../backend-core/src/main/java/mx/ine/gestiona_t/modules/auth/controller/AnalistaAdminController.java)
- [`backend-core/src/main/java/mx/ine/gestiona_t/modules/auth/dto/request/CrearAnalistaRequest.java`](../backend-core/src/main/java/mx/ine/gestiona_t/modules/auth/dto/request/CrearAnalistaRequest.java)
- [`backend-core/src/main/java/mx/ine/gestiona_t/modules/auth/service/AnalistaServiceImpl.java`](../backend-core/src/main/java/mx/ine/gestiona_t/modules/auth/service/AnalistaServiceImpl.java)
- [`backend-core/src/main/java/mx/ine/gestiona_t/config/SecurityConfig.java`](../backend-core/src/main/java/mx/ine/gestiona_t/config/SecurityConfig.java)
- [`backend-core/src/main/resources/application.properties`](../backend-core/src/main/resources/application.properties)

## 10. Verificación realizada

- Se leyó la página y sus imports directos.
- Se rastrearon los tres endpoints usados por la pantalla.
- Se contrastaron los nombres de campos, roles y política de contraseña entre frontend y backend.
- Se revisaron `apiClient`, middleware, controlador, DTO, servicio, modelo y configuración de seguridad.
- No se localizaron pruebas específicas de `crear-analista` en `tests/` ni en `backend-core/src/test/` mediante búsqueda de nombres relevantes.

### Lint de la página

Se ejecutó `npm run lint -- "src/app/(admin)/admin-portal/[secret]/crear-analista/page.tsx"` desde `frontend/`.

- **2 errores:** uso de `any` en los `catch` de alta y desactivación (`@typescript-eslint/no-explicit-any`).
- **3 advertencias:** `ShieldCheck` importado sin uso, dependencia `cargarAnalistas` ausente en el `useEffect` y advertencia de React Compiler sobre `watch()` de React Hook Form.

El lint termina con código de salida `1` por los dos errores de tipo explícito. Estos problemas son independientes del contrato backend, pero afectan la calidad de compilación/lint de la ruta.

## 11. Incidencia corregida: `TS2769` en `z.enum`

### Causa

La página tenía `zod` versión `^4.4.3`, pero el esquema usaba la opción `errorMap` de Zod 3:

```ts
z.enum(['ANALISTA_UR', 'ADMIN_SISTEMA', 'CONTRALORIA'], {
  errorMap: () => ({ message: 'Selecciona un rol válido' })
})
```

En Zod 4 la sobrecarga de `z.enum` acepta `error` o `message`; `errorMap` ya no forma parte del tipo de opciones. Por eso TypeScript emitía `TS2769` y no encontraba una sobrecarga compatible.

### Solución aplicada

Se reemplazó únicamente la opción incompatible, conservando el mensaje funcional:

```ts
z.enum(['ANALISTA_UR', 'ADMIN_SISTEMA', 'CONTRALORIA'], {
  error: 'Selecciona un rol válido'
})
```

### Verificación posterior

Se ejecutó `npx tsc --noEmit` desde `frontend/`. El error `TS2769` de `page.tsx` ya no aparece. El chequeo global todavía reporta un error preexistente e independiente en `frontend/middleware.ts:21`: `NextRequest` no expone la propiedad `ip` en los tipos instalados.

Este documento es un análisis estático; no sustituye una prueba E2E con frontend, backend, JWT y base de datos levantados.
