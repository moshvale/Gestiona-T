# Resumen del proyecto Gestiona-T

**Versión:** 1.4.0  
**Fecha:** 26 de agosto de 2026  
**Propósito:** Documento base para elaborar un video explicativo del proyecto

---

## 1. ¿Qué es Gestiona-T?

Gestiona-T es una plataforma web para digitalizar el reclutamiento y la
selección de aspirantes del Instituto Nacional Electoral. Centraliza en un
mismo sistema el registro, la autenticación, la integración del currículum, la
carga y validación de documentos, la evaluación de compatibilidad con
inteligencia artificial, la carta declaratoria, la firma electrónica y el
seguimiento administrativo.

La solución está organizada como un monorepo con tres servicios principales y
una infraestructura local compartida.

---

## 2. Arquitectura explicada para el video

```text
						  Usuario / Administrador
										|
										v
					  Frontend Next.js :3007
						  /                    \
						 v                      v
		  Backend Core Spring Boot     Rutas protegidas
					  :8087
				 /       |        \
				v        v         v
	  PostgreSQL    MinIO    Backend AI
		 :5439     :9007/08   FastAPI :8007
```

### Frontend web

Es la interfaz que utilizan aspirantes y personal administrativo. Sus rutas
se agrupan en:

- **Públicas:** inicio, registro y login.
- **Protegidas:** panel, perfil, CV, documentos, carta y seguimiento.
- **Administrativas:** aspirantes, vacantes, postulaciones, expedientes y
  contratación.

### Backend Core

Es el núcleo transaccional del sistema. Controla la autenticación JWT, las
reglas de negocio, la persistencia, los documentos, el CV, el matching, la
carta declaratoria, la firma y la auditoría.

### Backend AI

Expone servicios especializados para procesar texto y documentos: matching
curricular, OCR, extracción de información, anonimización y evaluación de
autenticidad.

### PostgreSQL y MinIO

PostgreSQL conserva usuarios, aspirantes, vacantes, postulaciones, resultados y
trazabilidad. MinIO almacena CV, documentos, cartas declaratorias y archivos
firmados como objetos tipo S3.

---

## 3. Recorrido principal de un aspirante

### 3.1 Registro y acceso

1. El aspirante captura CURP, correo, teléfono y contraseña.
2. El frontend envía los datos a `POST /api/v1/auth/registro/iniciar`.
3. El Backend Core valida duplicados, genera los datos iniciales y guarda la
	cuenta con estado `REGISTRO_VALIDADO`.
4. El registro se completa inmediatamente, sin depender de OTP ni de SMTP.
5. El frontend redirige al login con el correo precargado.

El código de verificación y los endpoints OTP se conservan para otros flujos,
pero no forman parte del alta actual de aspirantes. Esta decisión permite que
el registro siga funcionando cuando el SMTP está bloqueado por la
administración del sistema. El detalle del incidente está en
[docs/problema-registro-aspirante.md](docs/problema-registro-aspirante.md).

### 3.2 Perfil y currículum

Después de iniciar sesión, el aspirante entra a su panel, completa su
información y captura su currículum. Los datos quedan asociados a su cuenta y
pueden ser utilizados en la evaluación de compatibilidad.

### 3.3 Documentos

El aspirante carga sus documentos desde la interfaz. El Backend Core valida la
sesión, registra los metadatos y almacena los archivos en MinIO. El Backend AI
puede realizar OCR, extracción de campos, anonimización y validación de
autenticidad.

### 3.4 Vacantes y postulaciones

El aspirante consulta las vacantes publicadas, revisa sus requisitos y envía
una postulación. El personal administrativo puede consultar aspirantes,
postulaciones y expedientes para continuar el proceso de selección.

### 3.5 Matching curricular

El Backend Core solicita al Backend AI la evaluación mediante
`POST /api/v1/matching/evaluar`. El servicio compara el perfil con los
requisitos de la vacante, calcula un porcentaje y devuelve el nivel de
compatibilidad. El resultado se guarda en PostgreSQL y se muestra en el
frontend.

La integración fue comprobada desde **CV > Idiomas e Info**, con un resultado
de referencia de 23.8% de compatibilidad baja.

### 3.6 Carta declaratoria y firma

El aspirante revisa y acepta los bloques declaratorios, firma electrónicamente
y puede visualizar o descargar el PDF generado. Los documentos firmados se
conservan en MinIO y el evento queda disponible para trazabilidad.

---

## 4. Tecnologías

| Capa | Tecnologías principales |
|---|---|
| Frontend | Next.js 16.2.10, React 19.2.4, TypeScript 5, Tailwind CSS 4 |
| Formularios y API | Axios, React Hook Form, Zod, Radix UI |
| Backend Core | Java 17, Spring Boot 3.3.2, Spring Security, Spring Data JPA |
| Seguridad | JWT, BCrypt y control de acceso por roles |
| Backend AI | Python 3.12, FastAPI, Uvicorn, spaCy, transformers |
| Persistencia | PostgreSQL 16 y MinIO |
| Operación local | Docker Compose, Maven y npm |

---

## 5. Puertos y servicios locales

| Servicio | Puerto | Función |
|---|---:|---|
| Frontend Next.js | 3007 | Interfaz web |
| Backend Core | 8087 | API principal y reglas de negocio |
| Backend AI | 8007 | Procesamiento inteligente |
| PostgreSQL | 5439 | Base de datos transaccional |
| MinIO API | 9007 | Almacenamiento de objetos |
| MinIO Console | 9008 | Administración del almacenamiento |

La guía de arranque se encuentra en
[GUIA_LEVANTAR_SERVICIOS.md](GUIA_LEVANTAR_SERVICIOS.md).

---

## 6. Estado actual del proyecto

### Implementado

- Arquitectura desacoplada con frontend, Backend Core y Backend AI.
- Registro directo de aspirantes sin dependencia de SMTP.
- Login y autenticación basada en JWT.
- Gestión de perfiles, CV, documentos y expedientes.
- Almacenamiento de archivos en MinIO.
- Carta declaratoria, firma electrónica y generación de PDF.
- Matching curricular integrado con el Backend AI.
- Rutas diferenciadas para aspirantes y administración.
- Configuración de Next.js para desarrollo desde `10.15.0.59`, evitando el
  bloqueo del recurso HMR.

### Validado recientemente

- Compilación del Backend Core con Maven: correcta.
- Build de producción del frontend con Next.js y TypeScript: correcto.
- Integración de matching con resultado visible en la interfaz.

### Pendiente de consolidación

- Ejecutar una prueba end-to-end del nuevo registro contra la base de datos
  activa.
- Completar pruebas automáticas de los flujos de documentos, firma y
  postulaciones.
- Centralizar variables de entorno y preparar el despliegue objetivo.
- Fortalecer observabilidad, auditoría y controles de seguridad para producción.

---

## 7. Guion sugerido para el video

1. **Presentación:** explicar que Gestiona-T digitaliza el ciclo de
	reclutamiento y selección.
2. **Arquitectura:** mostrar el frontend, el Backend Core, el Backend AI,
	PostgreSQL y MinIO.
3. **Registro:** crear un aspirante y mostrar que el alta se completa al hacer
	clic en “Iniciar registro”, sin OTP ni correo SMTP.
4. **Acceso:** iniciar sesión y mostrar el panel del aspirante.
5. **CV y documentos:** completar información, cargar archivos y explicar su
	procesamiento.
6. **Inteligencia artificial:** ejecutar la evaluación de compatibilidad y
	mostrar el porcentaje obtenido.
7. **Proceso institucional:** recorrer vacantes, postulación, carta
	declaratoria y firma.
8. **Administración:** mostrar la consulta de aspirantes, expedientes y
	postulaciones.
9. **Cierre:** resumir la trazabilidad, la separación de servicios y los
	siguientes pasos de consolidación.

---

## 8. Documentación complementaria

- [Arquitectura del sistema](ARQUITECTURA_SISTEMA.md)
- [Configuración del sistema](CONFIGURACION_SISTEMA.md)
- [Estructura del proyecto](ESTRUCTURA_PROYECTO.md)
- [Base de datos](Base_de_Datos.md)
- [Guía para levantar servicios](GUIA_LEVANTAR_SERVICIOS.md)
- [Caso de prueba de matching con IA](docs/caso-prueba-evaluacion-compatibilidad-ia.md)
- [Problema y solución del registro](docs/problema-registro-aspirante.md)

---

**Fin del documento RESUMEN_PROYECTO.md**
