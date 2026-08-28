"""Comparador semantico entre CV y Cedula de Puesto.

Orquesta el proceso completo de comparacion usando NLP y embeddings.
"""
from typing import Dict, Any, List
from loguru import logger

from app.services.matching.models.cv_models import CvCiego
from app.services.matching.models.cedula_models import CedulaPuesto
from app.services.matching.nlp_processor import NLPProcessor
from app.services.matching.embeddings import EmbeddingsService


class Comparator:
    """Comparador semantico de alto nivel."""
    
    def __init__(self):
        self.nlp = NLPProcessor.get_instance()
        self.embeddings = EmbeddingsService.get_instance()
    
    def comparar_perfil_completo(
        self, cv: CvCiego, cedula: CedulaPuesto
    ) -> Dict[str, Any]:
        """Realiza una comparacion completa del perfil."""
        logger.debug(f"Comparando CV {cv.folio} con puesto {cedula.codigo}")
        
        resultado = {
            "terminos_cv": self._extraer_terminos_cv(cv),
            "terminos_puesto": self._extraer_terminos_puesto(cedula),
            "similitud_global": 0.0,
        }
        
        # Generar texto completo para similitud global
        texto_cv = self._cv_a_texto(cv)
        texto_puesto = self._cedula_a_texto(cedula)
        
        resultado["similitud_global"] = self.embeddings.calcular_similitud(
            texto_cv, texto_puesto
        )
        
        return resultado
    
    def _extraer_terminos_cv(self, cv: CvCiego) -> List[str]:
        """Extrae terminos clave del CV."""
        terminos = set()
        
        for esc in cv.escolaridad:
            terminos.add(esc.titulo.lower())
            if esc.institucion:
                terminos.add(esc.institucion.lower())
        
        for exp in cv.experiencia:
            terminos.add(exp.puesto.lower())
            terminos.update(self.nlp.extraer_terminos_tecnicos(exp.funciones))
        
        for cur in cv.cursos:
            terminos.add(cur.nombre.lower())
        
        for hab in cv.habilidades:
            terminos.add(hab.nombre.lower())
        
        return list(terminos)
    
    def _extraer_terminos_puesto(self, cedula: CedulaPuesto) -> List[str]:
        """Extrae terminos clave de la cedula."""
        terminos = set()
        req = cedula.requisitos
        
        terminos.add(cedula.nombre.lower())
        terminos.update(t.lower() for t in req.area_estudio)
        terminos.update(t.lower() for t in req.habilidades_tecnicas)
        
        if req.experiencia_especifica:
            terminos.update(self.nlp.extraer_terminos_tecnicos(req.experiencia_especifica))
        
        return list(terminos)
    
    def _cv_a_texto(self, cv: CvCiego) -> str:
        """Convierte CV a texto plano para procesamiento."""
        partes = []
        
        for esc in cv.escolaridad:
            partes.append(f"{esc.titulo} {esc.institucion or ''}")
        
        for exp in cv.experiencia:
            partes.append(f"{exp.puesto} {exp.funciones}")
        
        for cur in cv.cursos:
            partes.append(cur.nombre)
        
        for hab in cv.habilidades:
            partes.append(hab.nombre)
        
        return " . ".join(partes)
    
    def _cedula_a_texto(self, cedula: CedulaPuesto) -> str:
        """Convierte cedula a texto plano."""
        req = cedula.requisitos
        partes = [cedula.nombre]
        
        partes.extend(req.area_estudio)
        if req.experiencia_especifica:
            partes.append(req.experiencia_especifica)
        partes.extend(req.habilidades_tecnicas)
        partes.extend(req.certificaciones_deseables)
        
        return " . ".join(partes)