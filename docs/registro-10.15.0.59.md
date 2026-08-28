# Problema: Peticiones al backend fallan por IP 10.15.0.59 (Timeout)

Fecha: 2026-08-05

Resumen
-------
Durante el flujo de registro en `http://localhost:3007/registro` el frontend hacía peticiones a `http://10.15.0.59:8087/api/v1/auth/registro/iniciar` y fallaban con `ERR_CONNECTION_TIMED_OUT` / `AxiosError: Network Error`.

Diagnóstico
-----------
- El backend (`backend-core`) estaba corriendo en `http://localhost:8087` y el `actuator/health` respondió `200`.
- La máquina local no tenía asignada la IP `10.15.0.59` en sus interfaces (IP activa: `192.168.1.245`).
- Por tanto, las peticiones desde el navegador a `10.15.0.59:8087` no llegaban y resultaban en timeout.

Pruebas realizadas
------------------
- `curl http://localhost:8087/actuator/health` → `200 {"status":"UP"}`
- `curl -X POST http://localhost:8087/api/v1/auth/registro/iniciar -d '{...}'` → `HTTP/1.1 201 {"mensaje":"Registro completado..."}`
- `curl http://10.15.0.59:8087/actuator/health` → `Connection timed out`
- Inicié el contenedor de PostgreSQL (`docker compose up -d postgres`) y lancé el backend con `mvn spring-boot:run`.

Causa raíz
----------
El frontend (o su configuración) tenía valores por defecto apuntando a la IP de la red local `10.15.0.59` que en este equipo no estaba asignada. Al no resolverse ese host, el navegador devolvía `ERR_CONNECTION_TIMED_OUT`.

Acción tomada
-------------
Para dejar el proyecto configurado para `localhost` y conservar la referencia original comentada para futuras comprobaciones:

- Modifiqué `frontend/src/services/carta.service.ts`:
  - Comentado el fallback `http://10.15.0.59:8087/api/v1` y cambiado a `http://localhost:8087/api/v1`.

- Modifiqué `frontend/next.config.ts`:
  - Comentado la entrada `10.15.0.59` en `allowedDevOrigins` y en `images.remotePatterns`.
  - Mantengo `localhost` activo.

- Modifiqué `backend-core/src/main/resources/application.yml`:
  - Quité `10.15.0.59` de la lista por defecto de orígenes CORS (los valores originales se dejaron comentados cerca).

- Modifiqué `backend-core/src/main/java/mx/ine/gestiona_t/config/CorsConfig.java`:
  - Comenté el valor anterior y dejé por defecto `http://localhost:3007` en la anotación `@Value`.

Archivos modificados
-------------------
- `frontend/src/services/carta.service.ts`
- `frontend/next.config.ts`
- `backend-core/src/main/resources/application.yml`
- `backend-core/src/main/java/mx/ine/gestiona_t/config/CorsConfig.java`

Cómo revertir o habilitar la IP 10.15.0.59
-----------------------------------------
- Si necesitas que el proyecto atienda peticiones en `10.15.0.59`, puedes:
  - Asignar esa IP a la interfaz de red del equipo o usar la IP real que tenga la máquina (por ejemplo `192.168.1.245`).
  - O bien descomentar los valores originales en los archivos modificados (busca las líneas con `10.15.0.59` comentadas) y reiniciar frontend/backend.

Comandos útiles (desarrollador)
-------------------------------
Iniciar servicios:

```bash
# Desde la raíz del repo
docker compose up -d postgres minio
# Backend
cd backend-core
mvn spring-boot:run
# Frontend
cd frontend
npm run dev
```

Verificar salud del backend:

```bash
curl http://localhost:8087/actuator/health
```

Notas adicionales
-----------------
- Durante el arranque se detectaron errores de conexión a MinIO (`localhost:9007`) — si tu flujo depende de MinIO, levanta el servicio `minio` del `docker compose`.
- Las referencias originales a `10.15.0.59` se conservaron comentadas en los archivos modificados para facilitar futuras pruebas.
