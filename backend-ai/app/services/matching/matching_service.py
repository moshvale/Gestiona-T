# app/services/matching/matching_service.py
from typing import Any, Dict

import torch
from sentence_transformers import SentenceTransformer, util


class MatchingService:
    """Servicio de matching semántico para comparar CV contra perfiles de puesto."""

    _modelo = None

    @classmethod
    def obtener_modelo(cls) -> SentenceTransformer:
        if cls._modelo is None:
            cls._modelo = SentenceTransformer("paraphrase-multilingual-MiniLM-L12-v2")
        return cls._modelo

    @staticmethod
    def texto_a_vector(texto: str) -> torch.Tensor:
        return MatchingService.obtener_modelo().encode(texto, convert_to_tensor=True)

    @staticmethod
    def evaluar_matching(cv_anonimo: Dict[str, Any], perfil_puesto: str) -> Dict[str, Any]:
        """
        Compara el CV anonimizado contra el perfil del puesto y devuelve un score y detalles.
        """
        partes_cv = []

        if cv_anonimo.get("areasInteres"):
            partes_cv.append(f"Intereses: {cv_anonimo['areasInteres']}")
        if cv_anonimo.get("habilidades"):
            partes_cv.append(f"Habilidades: {cv_anonimo['habilidades']}")
        if cv_anonimo.get("logrosProfesionales"):
            partes_cv.append(f"Logros: {cv_anonimo['logrosProfesionales']}")

        for exp in cv_anonimo.get("experienciaLaboral", []):
            partes_cv.append(f"Puesto: {exp.get('puesto')}. Funciones: {exp.get('funciones')}")

        for form in cv_anonimo.get("formacionAcademica", []):
            partes_cv.append(f"Formación: {form.get('nivel')} en {form.get('carrera')}")

        texto_cv_completo = " | ".join(partes_cv)
        embedding_cv = MatchingService.texto_a_vector(texto_cv_completo)
        embedding_perfil = MatchingService.texto_a_vector(perfil_puesto)

        similitud = util.cos_sim(embedding_cv, embedding_perfil).item()
        score_porcentaje = round(similitud * 100, 1)

        if score_porcentaje >= 80:
            nivel = "ALTA COMPATIBILIDAD"
        elif score_porcentaje >= 60:
            nivel = "COMPATIBILIDAD MEDIA"
        else:
            nivel = "COMPATIBILIDAD BAJA"

        return {
            "score": score_porcentaje,
            "nivel": nivel,
            "mensaje": f"El perfil muestra un {nivel.lower()} con los requisitos del puesto.",
        }

    def evaluar(self, cv_anonimo: Dict[str, Any], perfil_puesto: str) -> Dict[str, Any]:
        return self.evaluar_matching(cv_anonimo, perfil_puesto)


def texto_a_vector(texto: str) -> torch.Tensor:
    return MatchingService.texto_a_vector(texto)


def evaluar_matching(cv_anonimo: Dict[str, Any], perfil_puesto: str) -> Dict[str, Any]:
    return MatchingService.evaluar_matching(cv_anonimo, perfil_puesto)