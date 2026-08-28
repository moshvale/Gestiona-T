"""Aplicación principal FastAPI del Backend-AI."""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from loguru import logger

from app.api.v1 import matching as matching_api
from app.api.v1 import ocr as ocr_api  # ✅ NUEVO
from app.api.v1 import autenticidad as autenticidad_api  # ✅ NUEVO
from app.core.config import settings

logger.info("🚀 Iniciando Backend-AI Gestiona-T")

app = FastAPI(
    title="Gestiona-T Backend AI",
    description="Servicio de IA para matching curricular ciego, OCR y autenticidad documental",
    version="1.1.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ✅ Registrar los 3 routers con el prefijo global /api/v1
app.include_router(matching_api.router, prefix="/api/v1", tags=["matching"])
app.include_router(ocr_api.router, prefix="/api/v1", tags=["ocr"])  # ✅ NUEVO
app.include_router(autenticidad_api.router, prefix="/api/v1", tags=["autenticidad"])  # ✅ NUEVO


@app.get("/health")
async def health_check():
    """Endpoint de verificación de salud del servicio."""
    return {
        "status": "healthy",
        "service": "backend-ai",
        "version": "1.1.0",
        "nlp_model": settings.nlp_model,
        "embeddings_model": settings.embeddings_model,
    }


@app.on_event("startup")
async def startup_event():
    """Inicialización de modelos y servicios al arrancar."""
    logger.info("📦 Cargando modelos de NLP y embeddings...")
    from app.services.matching.nlp_processor import NLPProcessor
    from app.services.matching.embeddings import EmbeddingsService
    
    NLPProcessor.initialize()
    EmbeddingsService.initialize()
    
    # ✅ Inicializar OCR
    from app.services.ocr.ocr_service import ocr_service
    ocr_service.initialize()
    
    logger.info("✅ Todos los servicios inicializados correctamente")