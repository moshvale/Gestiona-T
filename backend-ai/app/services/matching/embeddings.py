"""Servicio de generacion de embeddings semanticos con sentence-transformers.

Utiliza modelos multilingues para generar vectores semanticos que permiten
comparar textos por significado, no solo por palabras exactas.
"""
from typing import List, Optional
from loguru import logger
import numpy as np


class EmbeddingsService:
    """Servicio de embeddings singleton."""
    
    _instance: Optional["EmbeddingsService"] = None
    _model = None
    
    @classmethod
    def initialize(cls):
        """Inicializa el modelo de embeddings (llamar al startup)."""
        if cls._model is not None:
            return
        
        try:
            from sentence_transformers import SentenceTransformer
            from app.core.config import settings
            
            logger.info(f"Cargando modelo de embeddings: {settings.embeddings_model}")
            cls._model = SentenceTransformer(settings.embeddings_model)
            logger.info("Modelo de embeddings cargado exitosamente")
        except Exception as e:
            logger.error(f"Error al cargar modelo de embeddings: {e}")
            raise
    
    @classmethod
    def get_instance(cls) -> "EmbeddingsService":
        """Obtiene la instancia singleton."""
        if cls._model is None:
            cls.initialize()
        if cls._instance is None:
            cls._instance = cls()
        return cls._instance
    
    def encode(self, texto: str) -> np.ndarray:
        """Codifica un texto en un vector de embeddings."""
        if not texto or not texto.strip():
            return np.zeros(self._model.get_sentence_embedding_dimension())
        
        return self._model.encode(texto, convert_to_numpy=True)
    
    def encode_batch(self, textos: List[str]) -> np.ndarray:
        """Codifica un batch de textos."""
        textos_limpios = [t if t and t.strip() else "" for t in textos]
        return self._model.encode(textos_limpios, convert_to_numpy=True, show_progress_bar=False)
    
    def calcular_similitud(self, texto1: str, texto2: str) -> float:
        """Calcula similitud coseno entre dos textos."""
        emb1 = self.encode(texto1)
        emb2 = self.encode(texto2)
        
        return self._cosine_similarity(emb1, emb2)
    
    def calcular_similitud_matriz(
        self, textos_a: List[str], textos_b: List[str]
    ) -> np.ndarray:
        """Calcula matriz de similitudes entre dos listas de textos."""
        embs_a = self.encode_batch(textos_a)
        embs_b = self.encode_batch(textos_b)
        
        # Normalizar
        embs_a_norm = embs_a / (np.linalg.norm(embs_a, axis=1, keepdims=True) + 1e-8)
        embs_b_norm = embs_b / (np.linalg.norm(embs_b, axis=1, keepdims=True) + 1e-8)
        
        # Producto punto (similitud coseno)
        return np.dot(embs_a_norm, embs_b_norm.T)
    
    def mejor_match(self, texto_query: str, textos_candidatos: List[str]) -> tuple:
        """Encuentra el mejor match semantico para un texto query."""
        if not textos_candidatos:
            return None, 0.0
        
        emb_query = self.encode(texto_query)
        emb_candidatos = self.encode_batch(textos_candidatos)
        
        # Normalizar
        emb_query_norm = emb_query / (np.linalg.norm(emb_query) + 1e-8)
        emb_candidatos_norm = emb_candidatos / (
            np.linalg.norm(emb_candidatos, axis=1, keepdims=True) + 1e-8
        )
        
        # Similitudes
        similitudes = np.dot(emb_candidatos_norm, emb_query_norm)
        
        idx_mejor = int(np.argmax(similitudes))
        return textos_candidatos[idx_mejor], float(similitudes[idx_mejor])
    
    @staticmethod
    def _cosine_similarity(vec1: np.ndarray, vec2: np.ndarray) -> float:
        """Calcula similitud coseno entre dos vectores."""
        norm1 = np.linalg.norm(vec1)
        norm2 = np.linalg.norm(vec2)
        
        if norm1 == 0 or norm2 == 0:
            return 0.0
        
        return float(np.dot(vec1, vec2) / (norm1 * norm2))
    
    def get_dimension(self) -> int:
        """Retorna la dimension de los embeddings."""
        return self._model.get_sentence_embedding_dimension()