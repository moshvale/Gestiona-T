"""Configuracion central del servicio Backend-AI."""
from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):
    """Configuracion cargada desde variables de entorno."""
    
    # Servicio
    app_name: str = "Gestiona-T Backend AI"
    app_version: str = "1.0.0"
    debug: bool = False
    
    # Modelos NLP
    nlp_model: str = "es_core_news_lg"
    embeddings_model: str = "paraphrase-multilingual-MiniLM-L12-v2"
    
    # Matching
    matching_threshold_apto: float = 80.0
    matching_threshold_revision: float = 60.0
    max_batch_size: int = 50
    timeout_seconds: int = 30
    
    # Ponderaciones del scoring
    peso_escolaridad: float = 0.30
    peso_experiencia: float = 0.35
    peso_cursos: float = 0.15
    peso_habilidades: float = 0.20
    
    # Anonimizacion
    anonimizacion_confianza_minima: float = 0.85
    
    # CORS
    cors_origins: List[str] = ["http://localhost:3007", "http://localhost:8087"]
    
    # Logging
    log_level: str = "INFO"
    
    class Config:
        env_file = ".env"
        case_sensitive = False


settings = Settings()