"""Modelos Pydantic para request/response del matching."""
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from enum import Enum
from .cv_models import CvCiego
from .cedula_models import CedulaPuesto


class Recomendacion(str, Enum):
    """Recomendacion final del matching."""
    APTO = "APTO"
    EN_REVISION = "EN_REVISION"
    NO_APTO = "NO_APTO"


class DesgloseScores(BaseModel):
    """Desglose de scores por seccion."""
    score_escolaridad: float = Field(..., ge=0, le=100)
    score_experiencia: float = Field(..., ge=0, le=100)
    score_cursos: float = Field(..., ge=0, le=100)
    score_habilidades: float = Field(..., ge=0, le=100)


class Coincidencias(BaseModel):
    """Detalle de coincidencias encontradas."""
    habilidades_match: List[str] = Field(default_factory=list)
    habilidades_faltantes: List[str] = Field(default_factory=list)
    experiencia_relevante: Optional[str] = None
    certificaciones_match: List[str] = Field(default_factory=list)
    idiomas_match: List[str] = Field(default_factory=list)


class MetadatosMatching(BaseModel):
    """Metadatos del proceso de matching."""
    tiempo_procesamiento_ms: int
    modelo_nlp: str
    modelo_embeddings: str
    metodo_matching: str = "SEMANTICO_ESTRUCTURADO"


class MatchingResponse(BaseModel):
    """Respuesta del matching individual."""
    folio: str
    score_total: float = Field(..., ge=0, le=100)
    desglose: DesgloseScores
    coincidencias: Coincidencias
    recomendacion: Recomendacion
    nivel_confianza: float = Field(..., ge=0, le=1)
    metadatos: MetadatosMatching


class MatchingRequest(BaseModel):
    """Request de matching individual."""
    cv_ciego: CvCiego
    cedula_puesto: CedulaPuesto


class MatchingBatchRequest(BaseModel):
    """Request de matching en lote."""
    cv_ciegos: List[CvCiego] = Field(..., max_length=50)
    cedula_puesto: CedulaPuesto


class MatchingBatchResponse(BaseModel):
    """Respuesta del matching en lote."""
    resultados: List[MatchingResponse]
    ranking: List[str]


class AnonimizacionRequest(BaseModel):
    """Request de anonimizacion de CV completo."""
    cv_completo: Dict[str, Any]


class AnonimizacionResponse(BaseModel):
    """Respuesta de anonimizacion."""
    cv_ciego: CvCiego
    campos_eliminados: List[str]
    confianza_anonimizacion: float