# 📘 ALGORITMO GENERAL DEL SISTEMA "GESTIONA-T"

**Proyecto:** Plataforma de gestión de reclutamiento y selección del INE  
**Versión:** 1.3.3  
**Fecha:** 10 de agosto de 2026  
**Estado:** Documento alineado con la implementación actual y verificado en entorno local

---

## 1. DESCRIPCIÓN GENERAL DEL SISTEMA

Gestiona-T es una plataforma modular orientada a digitalizar y reforzar los procesos de registro, validación documental, matching curricular, carta declaratoria, firma electrónica y auditoría para el Instituto Nacional Electoral. El sistema está organizado como un monorepo con tres servicios principales que operan de forma coordinada:

- Frontend web para aspirantes y administración.
- Backend core para lógica de negocio, seguridad y persistencia.
- Backend AI para procesos de OCR, anonimización y matching.

Los objetivos principales del sistema siguen siendo:

- Transparencia y trazabilidad.
- Validación automatizada contra fuentes oficiales.
- Ceguera curricular para reducir sesgos.
- Integridad y auditoría de cada etapa.
- Accesibilidad y uso inclusivo.

---

## 2. AVANCE ACTUAL DEL PROYECTO

El repositorio ya refleja avance real en los siguientes puntos:

- Frontend implementado con Next.js 16.2.10, React 19.2.4 y TypeScript 5.
- Rutas principales organizadas en grupos de aplicación: público, protegido y administrativo.
- Backend core basado en Spring Boot 3.3.2 con módulos funcionales de auth, cv, documentos, cartadeclaratoria, firma, matching y auditoria.
- Backend AI preparado con FastAPI, spaCy, transformers y servicios para matching, OCR, anonimización y autenticidad documental.
- Infraestructura local con Docker Compose para PostgreSQL 16 y MinIO.
- Guía de levantamiento de servicios documentada con puertos y comandos operativos.

---

## 3. ARQUITECTURA FUNCIONAL ACTUAL

El sistema opera bajo un modelo de microservicios desacoplados dentro de un monorepo:

- Frontend: consume APIs REST y orquesta el flujo de usuario.
- Backend core: expone endpoints transaccionales y gestiona autenticación, seguridad y acceso a datos.
- Backend AI: ofrece servicios especializados que el backend core invoca cuando el proceso requiere IA.
- Persistencia: PostgreSQL para datos transaccionales y MinIO para documentos y archivos.

---

## 4. FLUJOS PRINCIPALES Y ESTADO ACTUAL

### Flujo 1: Registro y autenticación
- El frontend ofrece el flujo de acceso y registro desde rutas públicas.
- El backend core gestiona autenticación, JWT y validación de identidad.
- La lógica de integración con fuentes externas queda preparada para activación según el contexto operativo.

### Flujo 2: Captura y carga de documentos
- El módulo de documentos ya está presente en el backend core.
- El almacenamiento de archivos se gestiona mediante MinIO.
- La carga y validación documental se trata como punto crítico del sistema.

### Flujo 3: Matching curricular y anonimización
- El backend AI contiene servicios dedicados a matching, OCR y anonimización.
- El backend core envía el CV anonimizado al endpoint `POST /api/v1/matching/evaluar` del backend AI.
- El resultado incluye score, nivel de compatibilidad y mensaje; el backend core lo persiste en PostgreSQL.
- La evaluación validada en entorno local mostró un score de 23.8% y nivel de compatibilidad baja.

### Flujo 4: Carta declaratoria y firma electrónica
- El backend core incorpora módulos específicos para carta declaratoria y firma.
- La firma electrónica se modela como un paso regulatorio y de trazabilidad.

### Flujo 5: Auditoría transversal
- El módulo de auditoría está presente y preparado para registrar eventos críticos del sistema.
- El enfoque de trazabilidad y control sigue siendo un pilar central del diseño.

---

## 5. STACK TECNOLÓGICO ACTUAL

### Frontend
- Next.js 16.2.10
- React 19.2.4
- TypeScript 5
- Tailwind CSS 4
- Axios, react-hook-form, zod y Radix UI

### Backend core
- Java 17
- Spring Boot 3.3.2
- Spring Security
- Spring Data JPA
- PostgreSQL + MinIO + JWT

### Backend AI
- Python 3.12
- FastAPI
- Uvicorn
- spaCy, transformers, sentence-transformers
- scikit-learn, numpy, pandas

### Infraestructura
- Docker Compose
- PostgreSQL 16
- MinIO

---

## 6. CONFIGURACIÓN OPERATIVA ACTUAL

Los servicios principales se levantan en los siguientes puertos:

- Frontend: 3007
- Backend core: 8087
- Backend AI: 8007
- PostgreSQL: 5439
- MinIO API: 9007
- MinIO Console: 9008

La guía de arranque del repositorio documenta estos puertos y los comandos recomendados para cada componente.

---

## 7. PRÓXIMOS PASOS RECOMENDADOS

1. Mantener la documentación sincronizada con cada cambio funcional importante.
2. Completar la validación end-to-end de los flujos de registro, documentos y firma.
3. Revisar y consolidar variables de entorno para entornos de desarrollo y producción.
4. Continuar con la integración real entre frontend, backend core y backend AI.
5. Añadir pruebas automáticas y validaciones de despliegue continuas.

### Verificación reciente

- Se validó el flujo `CV > Idiomas e Info > Evaluar mi Compatibilidad`.
- Se corrigió la composición de prefijos del backend AI (`/api/v1` + `/matching`).
- Se alineó `AI_SERVICE_URL` del backend core con `http://localhost:8007/api/v1`.

---

**Fin del documento ALGORITMO_GENERAL.md**
