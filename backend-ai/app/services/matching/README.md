# Módulo de Matching Curricular Ciego

**Responsabilidad:** FLUJO 4 - Evaluación de perfiles sin sesgos mediante NLP
**Versión:** 1.0.0
**Fecha:** 09 de julio de 2026
**Stack:** Python 3.12 + FastAPI + spaCy + transformers

---

## 1. Arquitectura del Módulo

### 1.1 Diagrama de Componentes

```
+------------------------------------------------------------------+
|              MATCHING MODULE (Backend-AI)                          |
+------------------------------------------------------------------+
|                                                                     |
|  +------------------+      +------------------+                   |
|  | matching_api.py  |----->| matching_service |                   |
|  | (FastAPI)        |      | (Business Logic) |                   |
|  +------------------+      +--------+---------+                   |
|                                     |                              |
|                    +----------------+----------------+            |
|                    |                |                |            |
|           +--------v------+  +------v-------+  +----v--------+  |
|           | anonimizer.py  |  | nlp_processor|  | scoring.py  |  |
|           | (Privacy)      |  | (spaCy+BERT) |  | (Matching)  |  |
|           +----------------+  +--------------+  +---------------+  |
|                    |                |                |            |
|           +--------v------+  +------v-------+  +----v--------+  |
|           | ner_processor  |  | embeddings   |  | comparator  |  |
|           | (NER)          |  | (Sentence-   |  | (Similarity)|  |
|           |                |  |  Transformers)|  |              |  |
|           +----------------+  +--------------+  +---------------+  |
|                                                                     |
|  +------------------+      +------------------+                   |
|  | models/          |      | evaluation/      |                   |
|  | (ML trained)     |      | (Bias testing)   |                   |
|  +------------------+      +------------------+                   |
|                                                                     |
+------------------------------------------------------------------+
```

### 1.2 Responsabilidades

- **Anonimización:** Eliminación de datos sensibles (Privacy by Design)
- **Procesamiento NLP:** Extracción de entidades y tokenización
- **Embeddings:** Generación de vectores semánticos con BERT
- **Matching:** Comparación semántica entre CV y Cédula de Puesto
- **Scoring:** Cálculo de puntaje de compatibilidad (0-100)
- **Evaluación de sesgos:** Tests estadísticos para garantizar equidad

### 1.3 Principios de Diseño

- **Ceguera curricular:** El algoritmo NO ve datos personales
- **Transparencia:** Cada puntaje es explicable
- **Equidad:** Tests trimestrales de sesgo
- **Auditabilidad:** Logs completos del proceso

---

## 2. Logística de Datos

### 2.1 Estructura de Entrada

#### CV Ciego (JSON anonimizado)
```json
{
  "folio": "01HXYZ...",
  "escolaridad": [
    {
      "nivel": "LICENCIATURA",
      "titulo": "Ingeniería en Sistemas Computacionales",
      "institucion": "Instituto Politécnico Nacional",
      "fecha_inicio": "2015-08-01",
      "fecha_termino": "2019-12-31",
      "status": "CONCLUIDO"
    }
  ],
  "experiencia": [
    {
      "puesto": "Analista de Sistemas",
      "funciones": "Desarrollo de aplicaciones web con Python y Django...",
      "fecha_inicio": "2020-01-01",
      "fecha_termino": "2023-06-30",
      "nivel_mando": "OPERATIVO"
    }
  ],
  "cursos": [
    {
      "nombre": "Machine Learning con Python",
      "institucion": "Coursera",
      "duracion_horas": 40,
      "fecha": "2022-03-15"
    }
  ],
  "habilidades": [
    {
      "tipo": "SOFTWARE",
      "nombre": "Python",
      "nivel": "AVANZADO"
    },
    {
      "tipo": "IDIOMA",
      "nombre": "Inglés",
      "nivel": "INTERMEDIO"
    }
  ]
}
```

#### Cédula de Puesto (JSON)
```json
{
  "codigo": "DS-001",
  "nombre": "Desarrollador de Software Senior",
  "requisitos": {
    "escolaridad_minima": "LICENCIATURA",
    "area_estudio": ["Sistemas", "Informática", "Computación"],
    "experiencia_anios": 5,
    "experiencia_especifica": "Desarrollo backend con Python",
    "habilidades_tecnicas": ["Python", "Django", "PostgreSQL", "Docker"],
    "idiomas": [{"idioma": "Inglés", "nivel_minimo": "INTERMEDIO"}],
    "certificaciones_deseables": ["AWS", "Azure"]
  }
}
```

### 2.2 Estructura de Salida

#### Resultado de Matching
```json
{
  "folio": "01HXYZ...",
  "score_total": 87.5,
  "desglose": {
    "score_escolaridad": 95.0,
    "score_experiencia": 88.0,
    "score_cursos": 85.0,
    "score_habilidades": 82.0
  },
  "coincidencias": {
    "habilidades_match": ["Python", "Django", "PostgreSQL"],
    "habilidades_faltantes": ["Docker"],
    "experiencia_relevante": "Desarrollo backend con Python",
    "certificaciones": []
  },
  "recomendacion": "APTO",
  "nivel_confianza": 0.92,
  "metadatos": {
    "tiempo_procesamiento_ms": 245,
    "modelo_nlp": "es_core_news_lg",
    "modelo_embeddings": "paraphrase-multilingual-MiniLM-L12-v2"
  }
}
```

---

## 3. Detalles Técnicos

### 3.1 Dependencias Python

```txt
# requirements.txt
fastapi==0.109.0
uvicorn[standard]==0.27.0
pydantic==2.5.3
spacy==3.7.2
transformers==4.37.0
torch==2.1.2
sentence-transformers==2.3.1
scikit-learn==1.4.0
numpy==1.26.3
pandas==2.2.0
python-multipart==0.0.6
python-dotenv==1.0.0
loguru==0.7.2
```

### 3.2 Modelos de ML Requeridos

```bash
# Descargar modelos de spaCy
python -m spacy download es_core_news_lg

# Modelos de transformers (se descargan automáticamente)
# - paraphrase-multilingual-MiniLM-L12-v2
# - bert-base-spanish-wwm-uncased
```

### 3.3 Endpoints REST (FastAPI)

```
POST /api/v1/matching/evaluar
Body: {
  "cv_ciego": {...},
  "cedula_puesto": {...}
}
Response: {
  "folio": "...",
  "score_total": 87.5,
  "desglose": {...},
  "recomendacion": "APTO"
}

POST /api/v1/matching/evaluar-lote
Body: {
  "cv_ciegos": [{...}, {...}],
  "cedula_puesto": {...}
}
Response: {
  "resultados": [{...}, {...}],
  "ranking": ["folio1", "folio2"]
}

POST /api/v1/matching/anonimizar
Body: {
  "cv_completo": {...}
}
Response: {
  "cv_ciego": {...}
}

GET /api/v1/matching/modelos
Response: {
  "modelo_nlp": "es_core_news_lg",
  "modelo_embeddings": "paraphrase-multilingual-MiniLM-L12-v2",
  "version": "1.0.0"
}

POST /api/v1/matching/evaluar-sesgo
Body: {
  "cv_ciegos": [{...}],
  "cedula_puesto": {...},
  "datos_demograficos": [{...}]
}
Response: {
  "test_resultado": "SIN_SESGO",
  "metricas": {...}
}
```

### 3.4 Estructura de Archivos

```
backend-ai/app/services/matching/
+-- __init__.py
+-- matching_service.py          # Lógica principal
+-- anonymizer.py                # Anonimización de CV
+-- nlp_processor.py             # Procesamiento NLP
+-- embeddings.py                # Generación de embeddings
+-- scoring.py                   # Cálculo de scores
+-- comparator.py                # Comparación semántica
+-- models/                      # Modelos Pydantic
|   +-- __init__.py
|   +-- cv_models.py
|   +-- cedula_models.py
|   +-- matching_models.py
+-- utils/
    +-- __init__.py
    +-- text_utils.py
    +-- date_utils.py

backend-ai/app/api/v1/
+-- matching.py                  # Endpoints FastAPI
```

---

## 4. Algoritmo de Matching

### 4.1 Flujo Principal

```
1. Recibir CV ciego + Cédula de Puesto
2. Procesar CV con NLP (spaCy):
   - Tokenización
   - Lematización
   - Extracción de entidades (NER)
3. Generar embeddings semánticos (BERT):
   - Para cada sección del CV
   - Para cada requisito de la Cédula
4. Calcular similitudes:
   - Escolaridad vs Requisito escolaridad
   - Experiencia vs Requisito experiencia
   - Cursos vs Capacitaciones requeridas
   - Habilidades vs Competencias técnicas
5. Ponderar scores:
   - Escolaridad: 30%
   - Experiencia: 35%
   - Cursos: 15%
   - Habilidades: 20%
6. Generar score total (0-100)
7. Generar recomendación:
   - >= 80: APTO
   - 60-79: EN_REVISION
   - < 60: NO_APTO
8. Retornar resultado con desglose
```

### 4.2 Fórmula de Scoring

```python
score_total = (
    0.30 * score_escolaridad +
    0.35 * score_experiencia +
    0.15 * score_cursos +
    0.20 * score_habilidades
)
```

### 4.3 Similitud Semántica

```python
# Usando cosine similarity con embeddings de BERT
from sklearn.metrics.pairwise import cosine_similarity

embedding_cv = model.encode(texto_cv)
embedding_requisito = model.encode(texto_requisito)

similitud = cosine_similarity([embedding_cv], [embedding_requisito])[0][0]
```

---

## 5. Guía de Mantenimiento

### 5.1 Actualización de Modelos

```bash
# Actualizar modelo de spaCy
python -m spacy download es_core_news_lg --upgrade

# Actualizar modelo de embeddings
# Editar config.py y cambiar MODEL_NAME
```

### 5.2 Monitoreo

- **Métricas clave:**
  - Tiempo promedio de procesamiento por CV
  - Distribución de scores (histograma)
  - Tasa de recomendaciones APTO/EN_REVISION/NO_APTO
  - Uso de memoria RAM (modelos grandes)

- **Alertas:**
  - Tiempo de procesamiento > 5s
  - Memoria RAM > 80%
  - Distribución de scores anómala

### 5.3 Evaluación de Sesgos

```bash
# Ejecutar test trimestral de sesgo
python -m app.services.matching.evaluation.bias_test
```

El test verifica que el score NO correlacione con:
- Género (inferido de nombres)
- Edad (inferida de fechas)
- Entidad federativa (inferida de instituciones)

---

## 6. Consideraciones de Seguridad

### 6.1 Privacy by Design

- **Anonimización obligatoria:** El módulo SOLO recibe CVs anonimizados
- **No almacenamiento:** Los CVs no se persisten en este servicio
- **Procesamiento en memoria:** Todo se procesa en RAM y se descarta
- **Logs sanitizados:** Nunca registrar datos personales

### 6.2 Protección de Modelos

- **Modelos versionados:** Cada versión del modelo tiene su hash
- **Integridad:** Verificación de checksum al cargar modelos
- **Acceso restringido:** Solo el servicio de matching puede usar los modelos

---

## 7. Testing

### 7.1 Tests Unitarios

```bash
pytest tests/unit/matching/
```

- **Cobertura mínima:** 85%
- **Casos a cubrir:**
  - Anonimización completa (sin fuga de datos)
  - Matching con CV perfecto (score 100)
  - Matching con CV incompleto (score bajo)
  - Manejo de casos edge (CV vacío, Cédula vacía)

### 7.2 Tests de Integración

```bash
pytest tests/integration/matching/
```

- **Casos a cubrir:**
  - Flujo completo de matching
  - Procesamiento en lote
  - Manejo de errores de modelos

### 7.3 Tests de Sesgo

```bash
pytest tests/evaluation/bias/
```

- **Casos a cubrir:**
  - Test de paridad demográfica
  - Test de igualdad de oportunidades
  - Test de correlación con variables protegidas

---

## 8. Despliegue

### 8.1 Requisitos de Hardware

- **RAM mínima:** 8GB (modelos en memoria)
- **RAM recomendada:** 16GB
- **CPU:** 4+ cores
- **GPU:** Opcional (acelera embeddings 10x)

### 8.2 Variables de Entorno

```bash
# Modelos
NLP_MODEL=es_core_news_lg
EMBEDDINGS_MODEL=paraphrase-multilingual-MiniLM-L12-v2

# Performance
MAX_BATCH_SIZE=50
TIMEOUT_SECONDS=30

# Logging
LOG_LEVEL=INFO
```

---

## 9. Roadmap

### Versión 1.0 (Actual)
- [x] Anonimización de CV
- [x] Matching con NLP + embeddings
- [x] Scoring ponderado
- [x] API REST completa

### Versión 1.1 (Próxima)
- [ ] Soporte para CVs en lenguas indígenas
- [ ] Matching con experiencia en sector público
- [ ] Explicabilidad con SHAP/LIME

### Versión 2.0 (Futuro)
- [ ] Fine-tuning con datos históricos del INE
- [ ] Detección de patrones de éxito
- [ ] Recomendaciones de capacitación

---
**Fin del documento README.md del Módulo de Matching**