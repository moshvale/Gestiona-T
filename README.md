# GESTIONA-T

**Plataforma de gestión de reclutamiento y selección del Instituto Nacional Electoral**

[![Java](https://img.shields.io/badge/Java-17-orange.svg)]()
[![Python](https://img.shields.io/badge/Python-3.12-blue.svg)]()
[![Next.js](https://img.shields.io/badge/Next.js-16.2.10-black.svg)]()
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)]()
[![MinIO](https://img.shields.io/badge/MinIO-Storage-red.svg)]()

---

## Estado verificado

El entorno local del proyecto quedó operativo y validado con los siguientes servicios:

- Frontend en http://localhost:3007
- Backend Core en http://localhost:8087
- Backend AI en http://localhost:8007
- PostgreSQL en localhost:5439
- MinIO API en http://localhost:9007
- MinIO Console en http://localhost:9008

La documentación del repositorio fue alineada con esta configuración real. La base de datos local incluye la tabla de cursos y capacitaciones institucionales aplicada mediante [database/postgres/init/004_cv_cursos_institucionales.sql](database/postgres/init/004_cv_cursos_institucionales.sql).

## Flujos verificados

- Carta declaratoria: aceptación, firma electrónica y visualización/descarga del PDF.
- CV: evaluación de compatibilidad desde `CV > Idiomas e Info`.
- Matching AI: endpoint `POST /api/v1/matching/evaluar`; resultado validado localmente con 23.8% de compatibilidad.

## Documentación principal

- [Arquitectura](ARQUITECTURA_SISTEMA.md)
- [Configuración](CONFIGURACION_SISTEMA.md)
- [Estructura del proyecto](ESTRUCTURA_PROYECTO.md)
- [Base de datos](Base_de_Datos.md)
- [Caso de prueba de matching](docs/caso-prueba-evaluacion-compatibilidad-ia.md)