"""Modelos Pydantic para el modulo de matching."""
from .cv_models import (
    CvCiego,
    EscolaridadCiego,
    ExperienciaCiego,
    CursoCiego,
    HabilidadCiego,
)
from .cedula_models import CedulaPuesto, RequisitosPuesto
from .matching_models import (
    MatchingRequest,
    MatchingResponse,
    MatchingBatchRequest,
    MatchingBatchResponse,
    AnonimizacionRequest,
    AnonimizacionResponse,
    DesgloseScores,
    Coincidencias,
    Recomendacion,
    MetadatosMatching,
)

__all__ = [
    "CvCiego", "EscolaridadCiego", "ExperienciaCiego", "CursoCiego", "HabilidadCiego",
    "CedulaPuesto", "RequisitosPuesto",
    "MatchingRequest", "MatchingResponse", "MatchingBatchRequest", "MatchingBatchResponse",
    "AnonimizacionRequest", "AnonimizacionResponse",
    "DesgloseScores", "Coincidencias", "Recomendacion", "MetadatosMatching",
]