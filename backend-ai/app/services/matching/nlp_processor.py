"""Procesador NLP con spaCy para extraccion de entidades y limpieza de texto.

Utiliza el modelo es_core_news_lg de spaCy para:
- Tokenizacion y lematizacion en espanol
- Extraccion de entidades nombradas (NER)
- Deteccion y eliminacion de datos personales
"""
from typing import List, Optional, Set
from loguru import logger
import re


class NLPProcessor:
    """Procesador NLP singleton con spaCy."""
    
    _instance: Optional["NLPProcessor"] = None
    _nlp = None
    
    # Etiquetas NER que indican datos personales
    ENTIDADES_PERSONALES = {"PER", "LOC", "ORG"}
    
    # Patrones adicionales para datos personales
    PATRONES_PERSONALES = [
        r"\b[A-Z]{4}\d{6}[HM][A-Z]{5}[A-Z0-9]\d\b",  # CURP
        r"\b[A-Z&N]{3,4}\d{6}[A-Z0-9]{3}\b",  # RFC
        r"\b[\w.+-]+@[\w-]+\.[\w.-]+\b",  # Email
        r"\b\d{10}\b",  # Telefono
    ]
    
    @classmethod
    def initialize(cls):
        """Inicializa el modelo NLP (llamar al startup)."""
        if cls._nlp is not None:
            return
        
        try:
            import spacy
            from app.core.config import settings
            
            logger.info(f"Cargando modelo NLP: {settings.nlp_model}")
            cls._nlp = spacy.load(settings.nlp_model)
            logger.info("Modelo NLP cargado exitosamente")
        except OSError as e:
            logger.error(f"Error al cargar modelo NLP: {e}")
            logger.error("Ejecuta: python -m spacy download es_core_news_lg")
            raise
        except ImportError:
            logger.error("spaCy no instalado. Ejecuta: pip install spacy")
            raise
    
    @classmethod
    def get_instance(cls) -> "NLPProcessor":
        """Obtiene la instancia singleton."""
        if cls._nlp is None:
            cls.initialize()
        if cls._instance is None:
            cls._instance = cls()
        return cls._instance
    
    def procesar(self, texto: str):
        """Procesa un texto con spaCy."""
        if not texto:
            return None
        return self._nlp(texto)
    
    def extraer_entidades(self, texto: str) -> List[dict]:
        """Extrae entidades nombradas del texto."""
        doc = self.procesar(texto)
        if doc is None:
            return []
        
        return [
            {
                "texto": ent.text,
                "label": ent.label_,
                "inicio": ent.start_char,
                "fin": ent.end_char,
            }
            for ent in doc.ents
        ]
    
    def eliminar_entidades_personales(self, texto: str) -> str:
        """Elimina entidades personales del texto usando NER + regex."""
        if not texto:
            return ""
        
        texto_limpio = texto
        
        # 1. Eliminar con regex primero (mas preciso para formatos conocidos)
        for patron in self.PATRONES_PERSONALES:
            texto_limpio = re.sub(patron, "[ELIMINADO]", texto_limpio)
        
        # 2. Eliminar con NER
        try:
            doc = self._nlp(texto_limpio)
            entidades = list(doc.ents)
            
            # Procesar de atras hacia adelante para no alterar indices
            for ent in reversed(entidades):
                if ent.label_ in self.ENTIDADES_PERSONALES:
                    texto_limpio = (
                        texto_limpio[:ent.start_char] + 
                        "[ELIMINADO]" + 
                        texto_limpio[ent.end_char:]
                    )
        except Exception as e:
            logger.warning(f"Error en NER: {e}")
        
        return texto_limpio
    
    def lematizar(self, texto: str) -> List[str]:
        """Lematiza un texto y retorna lista de lemas."""
        doc = self.procesar(texto)
        if doc is None:
            return []
        
        return [
            token.lemma_ 
            for token in doc 
            if not token.is_stop and not token.is_punct and token.pos_ in {"NOUN", "VERB", "ADJ", "PROPN"}
        ]
    
    def extraer_terminos_tecnicos(self, texto: str) -> Set[str]:
        """Extrae posibles terminos tecnicos (sustantivos propios y compuestos)."""
        doc = self.procesar(texto)
        if doc is None:
            return set()
        
        terminos = set()
        
        # Nouns y proper nouns
        for token in doc:
            if token.pos_ in {"NOUN", "PROPN"} and len(token.text) > 2:
                terminos.add(token.lemma_.lower())
        
        # Chunking de noun phrases
        for chunk in doc.noun_chunks:
            if len(chunk.text.split()) <= 4:
                terminos.add(chunk.text.lower())
        
        return terminos
    
    def calcular_similitud_lexica(self, texto1: str, texto2: str) -> float:
        """Calcula similitud lexica basada en lemas compartidos."""
        lemas1 = set(self.lematizar(texto1))
        lemas2 = set(self.lematizar(texto2))
        
        if not lemas1 or not lemas2:
            return 0.0
        
        interseccion = lemas1 & lemas2
        union = lemas1 | lemas2
        
        return len(interseccion) / len(union)