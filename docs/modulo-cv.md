# Módulo CV - Gestiona-T

**Versión:** 1.1.0  
**Fecha:** 01 de agosto de 2026  
**Estado:** Documento basado en la estructura real existente en frontend, backend core, backend AI y base de datos

Actualización: el flujo moderno del CV institucional ya incluye la tabla cv_cursos_capacitaciones_institucionales, creada y aplicada en PostgreSQL.

---

## 1. Objetivo del módulo

El módulo de CV permite capturar, almacenar y consultar la información curricular del aspirante dentro del flujo de reclutamiento del INE. El diseño actual combina:

- un flujo institucional moderno en el frontend y backend core;
- un flujo legacy de secciones por folio;
- un flujo de integración con IA para anonimización y matching.

---

## 2. Estructura de archivos y carpetas

### 2.1 Frontend

```text
frontend/
└── src/
    ├── app/
    │   └── (protected)/
    │       ├── cv/
    │       │   └── page.tsx
    │       └── panel/
    │           └── page.tsx
    ├── services/
    │   └── cv.service.ts
    └── lib/
        └── api.ts
```

#### Archivos principales
- [frontend/src/app/(protected)/cv/page.tsx](../frontend/src/app/(protected)/cv/page.tsx)
  - Formulario multi-paso del CV institucional.
  - Maneja formación académica, experiencia laboral e idiomas.
  - Añade una guardia (`hasLoadedCvRef`) para evitar cargas duplicadas del CV en el montaje.
- [frontend/src/app/(protected)/panel/page.tsx](../frontend/src/app/(protected)/panel/page.tsx)
  - Muestra estado del CV y permite subir documentos.
  - Añade una guardia (`cvLoadGuard`) para evitar llamadas repetidas a `obtenerMiCv()` en modo desarrollo.
- [frontend/src/services/cv.service.ts](../frontend/src/services/cv.service.ts)
  - Servicio cliente para obtener y guardar el CV.
  - Maneja `404` devolviendo un CV vacío y deduplica peticiones concurrentes.
- [frontend/src/lib/api.ts](../frontend/src/lib/api.ts)
  - Configuración central de Axios.
  - Intercepta respuestas y redirige en caso de 401/403.

### 2.2 Backend Core

```text
backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/
├── controller/
│   ├── CvInstitucionalController.java
│   ├── CvSeccionesController.java
│   └── CvUploadController.java
├── dto/
│   ├── request/
│   └── response/
├── integration/
├── model/
│   ├── CvInstitucional.java
│   ├── CvFormacionAcademica.java
│   ├── CvExperienciaLaboral.java
│   ├── CvIdioma.java
│   ├── CvEstructurado.java
│   └── ...
├── repository/
│   ├── CvInstitucionalRepository.java
│   ├── CvFormacionAcademicaRepository.java
│   ├── CvExperienciaLaboralRepository.java
│   ├── CvIdiomaRepository.java
│   └── CvEstructuradoRepository.java
├── service/
│   ├── CvInstitucionalService.java
│   ├── CvInstitucionalServiceImpl.java
│   ├── CvService.java
│   └── CvServiceImpl.java
└── README.md
```

#### Archivos principales
- [backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/controller/CvInstitucionalController.java](../backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/controller/CvInstitucionalController.java)
- [backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/controller/CvSeccionesController.java](../backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/controller/CvSeccionesController.java)
- [backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/controller/CvUploadController.java](../backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/controller/CvUploadController.java)
- [backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/service/CvInstitucionalServiceImpl.java](../backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/service/CvInstitucionalServiceImpl.java)
- [backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/service/CvServiceImpl.java](../backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/service/CvServiceImpl.java)

### 2.3 Backend AI

```text
backend-ai/app/services/matching/
├── anonymizer.py
├── matching_service.py
└── scoring.py
```

#### Archivos principales
- [backend-ai/app/services/matching/anonymizer.py](../backend-ai/app/services/matching/anonymizer.py)
- [backend-ai/app/services/matching/matching_service.py](../backend-ai/app/services/matching/matching_service.py)
- [backend-ai/app/services/matching/scoring.py](../backend-ai/app/services/matching/scoring.py)
- [backend-ai/app/api/v1/matching.py](../backend-ai/app/api/v1/matching.py)

### 2.4 Base de datos

```text
database/postgres/init/
└── 003_cv_tables.sql
```

---

## 3. Dependencias del módulo

### 3.1 Dependencias de backend core
El módulo usa las siguientes capacidades de Spring Boot y Java:

- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Security
- Spring WebFlux
- PostgreSQL
- JWT
- MinIO
- PDFBox / iText
- Springdoc OpenAPI

Fuente: [backend-core/pom.xml](../backend-core/pom.xml)

### 3.2 Dependencias de frontend
El módulo se apoya en:

- Next.js
- React
- Axios
- Tailwind CSS
- React Hook Form
- Zod
- Lucide React

Fuente: [frontend/package.json](../frontend/package.json)

### 3.3 Dependencias de backend AI
El módulo de IA usa:

- FastAPI
- Pydantic
- spaCy
- sentence-transformers
- scikit-learn
- numpy / pandas

Fuente: [backend-ai/requirements.txt](../backend-ai/requirements.txt)

---

## 4. Fase de implementación actual

### 4.1 Fase implementada
El módulo ya presenta una implementación funcional en estas capas:

- Captura del CV institucional desde el frontend.
- Persistencia y consulta del CV a través del backend core.
- Integración con base de datos PostgreSQL.
- Inicio de integración con servicios de IA para matching y anonimización.

### 4.2 Flujo actual del usuario
1. El aspirante entra al panel.
2. El frontend carga el estado del CV mediante el servicio.
3. El aspirante completa el formulario multi-paso del CV.
4. El frontend envía el contenido al endpoint del backend core.
5. El backend guarda o actualiza la información del CV.

### 4.3 Estado de madurez
- Muy avanzado en la capa de captura y persistencia del CV institucional.
- Parcialmente integrado con la carga de documentos y el flujo de documentos oficiales.
- En desarrollo o consolidación la integración completa con matching y validación documental por IA.

---

## 5. Endpoints existentes

### 5.1 Backend core - CV institucional

| Método | Endpoint | Descripción |
|---|---|---|
| POST | /api/v1/cv | Guarda o actualiza el CV institucional del aspirante autenticado |
| GET | /api/v1/cv | Obtiene el CV institucional del aspirante autenticado |

Implementado en [backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/controller/CvInstitucionalController.java](../backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/controller/CvInstitucionalController.java)

### 5.2 Backend core - CV legacy por secciones

| Método | Endpoint | Descripción |
|---|---|---|
| POST | /api/v1/cv/{folio}/escolaridad | Agrega escolaridad |
| PUT | /api/v1/cv/{folio}/escolaridad/{id} | Actualiza escolaridad |
| DELETE | /api/v1/cv/{folio}/escolaridad/{id} | Elimina escolaridad |
| POST | /api/v1/cv/{folio}/experiencia | Agrega experiencia laboral |
| PUT | /api/v1/cv/{folio}/experiencia/{id} | Actualiza experiencia |
| DELETE | /api/v1/cv/{folio}/experiencia/{id} | Elimina experiencia |
| POST | /api/v1/cv/{folio}/cursos | Agrega cursos |
| PUT | /api/v1/cv/{folio}/cursos/{id} | Actualiza curso |
| DELETE | /api/v1/cv/{folio}/cursos/{id} | Elimina curso |
| POST | /api/v1/cv/{folio}/habilidades | Agrega habilidad |
| PUT | /api/v1/cv/{folio}/habilidades/{id} | Actualiza habilidad |
| DELETE | /api/v1/cv/{folio}/habilidades/{id} | Elimina habilidad |

Implementado en [backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/controller/CvSeccionesController.java](../backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/controller/CvSeccionesController.java)

### 5.3 Backend core - CV no estructurado

| Método | Endpoint | Descripción |
|---|---|---|
| POST | /api/v1/cv/upload | Sube un CV en formato no estructurado (PDF/Word) |

Implementado en [backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/controller/CvUploadController.java](../backend-core/src/main/java/mx/ine/gestiona_t/modules/cv/controller/CvUploadController.java)

### 5.4 Backend AI - matching y anonimización

| Método | Endpoint | Descripción |
|---|---|---|
| POST | /api/v1/matching/evaluar | Evalúa un CV contra un perfil de puesto |
| POST | /api/v1/matching/evaluar-lote | Evalúa múltiples CVs |
| POST | /api/v1/matching/anonimizar | Anonimiza un CV |
| GET | /api/v1/matching/modelos | Lista modelos disponibles |
| GET | /api/v1/matching/config | Obtiene configuración |

---

## 6. Tablas existentes relacionadas con CV

### 6.1 Tablas legacy

| Tabla | Propósito |
|---|---|
| cv_estructurados | Cabecera del CV estructurado |
| cv_escolaridad | Formación académica |
| cv_cursos_capacitaciones | Cursos y capacitaciones |
| cv_habilidades_tecnicas | Habilidades técnicas |

### 6.2 Tablas del flujo institucional actual

| Tabla | Propósito |
|---|---|
| cv_institucionales | Cabecera del CV institucional del aspirante |
| cv_formacion_academica | Formación académica del aspirante |
| cv_experiencia_laboral | Experiencia laboral |
| cv_idiomas | Idiomas del aspirante |

### 6.3 Tablas relacionadas indirectamente

| Tabla | Propósito |
|---|---|
| aspirantes | Usuario/aspirante propietario del CV |
| documentos | Documentos oficiales cargados para el proceso |

Fuente: [database/postgres/init/003_cv_tables.sql](../database/postgres/init/003_cv_tables.sql)

---

## 7. Resumen funcional del módulo

El módulo CV actual está orientado a:

- Registrar información curricular del aspirante.
- Mantener un CV institucional con secciones bien delimitadas.
- Persistir datos de formación, experiencia e idiomas.
- Servir como base para validación documental y matching curricular.
- Preparar el flujo para futuras mejoras de IA y revisión automática.

---

## 8. Observaciones técnicas

- El frontend actual está consumiendo el flujo institucional moderno.
- El backend conserva un enfoque legacy por folio, lo que sugiere una posible consolidación futura.
- El módulo de IA está separado del proceso de captura, pero listo para apoyar scoring y anonimización.
- La arquitectura actual permite evolucionar hacia un flujo más robusto, con menor duplicidad y mejor trazabilidad.
