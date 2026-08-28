"""Utilidades para procesamiento de texto."""
import re
import unicodedata
from typing import List


def normalizar_texto(texto: str) -> str:
    """Normaliza texto: minusculas, sin acentos, sin caracteres especiales."""
    if not texto:
        return ""
    
    # Convertir a minusculas
    texto = texto.lower()
    
    # Eliminar acentos
    texto = unicodedata.normalize("NFD", texto)
    texto = "".join(c for c in texto if unicodedata.category(c) != "Mn")
    
    # Eliminar caracteres especiales
    texto = re.sub(r"[^\w\s]", " ", texto)
    
    # Normalizar espacios
    texto = re.sub(r"\s+", " ", texto).strip()
    
    return texto


def extraer_palabras_clave(texto: str, min_length: int = 3) -> List[str]:
    """Extrae palabras clave de un texto."""
    if not texto:
        return []
    
    texto_norm = normalizar_texto(texto)
    palabras = texto_norm.split()
    
    # Stopwords basicas en espanol
    stopwords = {
        "el", "la", "los", "las", "un", "una", "unos", "unas",
        "de", "del", "al", "en", "con", "por", "para", "que",
        "y", "o", "es", "son", "fue", "ser", "estar", "haber",
        "como", "mas", "pero", "sus", "su", "me", "mi", "tu",
    }
    
    return [p for p in palabras if len(p) >= min_length and p not in stopwords]


def calcular_similitud_jaccard(texto1: str, texto2: str) -> float:
    """Calcula similitud de Jaccard entre dos textos."""
    palabras1 = set(extraer_palabras_clave(texto1))
    palabras2 = set(extraer_palabras_clave(texto2))
    
    if not palabras1 or not palabras2:
        return 0.0
    
    interseccion = palabras1 & palabras2
    union = palabras1 | palabras2
    
    return len(interseccion) / len(union)