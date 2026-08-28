# ESTRUCTURA DEL PROYECTO "GESTIONA-T"

**Versión:** 1.3.1
**Fecha:** 04 de agosto de 2026
**Arquitectura:** Monorepo con servicios desacoplados y estructura verificada en el entorno local

---

## 1. Visión general

El proyecto Gestiona-T está organizado como un monorepo con tres capas principales de ejecución:

- Frontend web.
- Backend core.
- Backend AI.

Además, incluye carpetas compartidas para infraestructura, documentación, scripts y soporte operativo.

---

## 2. Estructura actual del repositorio

```text
Gestiona-T/
├── backend-ai/
│   ├── app/
│   │   ├── api/
│   │   ├── core/
│   │   ├── services/
│   │   └── utils/
│   ├── tests/
│   ├── requirements.txt
│   └── venv/
├── backend-core/
│   ├── src/
│   └── pom.xml
├── database/
│   ├── postgres/
│   └── minio/
├── docker/
├── docs/
├── frontend/
│   ├── public/
│   ├── src/
│   ├── package.json
│   └── README.md
├── logs/
├── scripts/
├── Temp/
├── tests/
├── docker-compose.yml
├── README.md
├── ALGORITMO_GENERAL.md
├── ARQUITECTURA_SISTEMA.md
├── CONFIGURACION_SISTEMA.md
├── ESTRUCTURA_PROYECTO.md
├── GUIA_LEVANTAR_SERVICIOS.md
├── RESUMEN_PROYECTO.md
├── Base_de_Datos.md
└── docs/
```

---

## 3. Descripción de las carpetas principales

### 3.1 Frontend
Se encuentra en la carpeta frontend y contiene la interfaz web del proyecto.

Componentes clave:
- Rutas públicas para login, registro y seguimiento.
- Rutas protegidas para panel del aspirante.
- Módulos de CV, documentos, carta declaratoria y firma.

### 3.2 Backend Core
Se encuentra en backend-core y centraliza la lógica transaccional.

Módulos funcionales principales:
- auth
- cv
- documentos
- cartadeclaratoria
- firma
- matching
- auditoria

### 3.3 Backend AI
Se encuentra en backend-ai y agrupa los servicios orientados a IA.

Servicios principales:
- matching
- ocr
- anonimizacion
- autenticidad

### 3.4 Infraestructura
La carpeta database y docker contienen la base de datos, almacenamiento y componentes de despliegue local.

---

## 4. Estado actual de la estructura

La estructura actual evidencia que el proyecto ya pasó de una base conceptual a un monorepo funcional con:

- Capas separadas para frontend, backend core y backend AI.
- Módulos funcionales claramente diferenciados.
- Infraestructura local preparada con Docker y almacenamiento de objetos.
- Scripts y documentación de operación sincronizados con el entorno real.

---

## 5. Recomendaciones de mantenimiento

- Mantener la estructura modular al agregar nuevas funcionalidades.
- Evitar mezclar lógica de negocio y lógica de presentación en el frontend.
- Mantener los servicios de IA independientes del backend transaccional.
- Actualizar esta estructura cada vez que se agreguen nuevos módulos, servicios o rutas.

---

**Fin del documento ESTRUCTURA_PROYECTO.md**
