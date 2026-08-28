"""Modelos Pydantic para el CV anonimizado."""
from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import date
from enum import Enum


class NivelEstudio(str, Enum):
    """Niveles de estudio."""
    PRIMARIA = "PRIMARIA"
    SECUNDARIA = "SECUNDARIA"
    BACHILLERATO = "BACHILLERATO"
    TECNICO = "TECNICO"
    LICENCIATURA = "LICENCIATURA"
    INGENIERIA = "INGENIERIA"
    MAESTRIA = "MAESTRIA"
    DOCTORADO = "DOCTORADO"


class StatusEstudio(str, Enum):
    """Status del estudio."""
    CONCLUIDO = "CONCLUIDO"
    EN_CURSO = "EN_CURSO"
    TRUNCO = "TRUNCO"
    INCONCLUSO = "INCONCLUSO"


class NivelMando(str, Enum):
    """Niveles de mando."""
    OPERATIVO = "OPERATIVO"
    MANDO_MEDIO = "MANDO_MEDIO"
    DIRECTIVO = "DIRECTIVO"
    ENLACE = "ENLACE"


class TipoHabilidad(str, Enum):
    """Tipos de habilidades."""
    IDIOMA = "IDIOMA"
    SOFTWARE = "SOFTWARE"
    CONOCIMIENTO_TECNICO = "CONOCIMIENTO_TECNICO"
    CERTIFICACION = "CERTIFICACION"
    COMPETENCIA_TRANSVERSAL = "COMPETENCIA_TRANSVERSAL"


class NivelHabilidad(str, Enum):
    """Niveles de dominio."""
    BASICO = "BASICO"
    INTERMEDIO = "INTERMEDIO"
    AVANZADO = "AVANZADO"
    EXPERTO = "EXPERTO"
    NATIVO = "NATIVO"


class EscolaridadCiego(BaseModel):
    """Escolaridad anonimizada."""
    nivel: NivelEstudio
    titulo: str = Field(..., max_length=200)
    institucion: Optional[str] = Field(None, max_length=200)
    fecha_inicio: date
    fecha_termino: Optional[date] = None
    status: StatusEstudio
    
    class Config:
        use_enum_values = True


class ExperienciaCiego(BaseModel):
    """Experiencia laboral anonimizada."""
    puesto: str = Field(..., max_length=100)
    funciones: str = Field(..., max_length=1000)
    fecha_inicio: date
    fecha_termino: Optional[date] = None
    actualmente_laborando: bool = False
    nivel_mando: NivelMando
    
    class Config:
        use_enum_values = True


class CursoCiego(BaseModel):
    """Curso de capacitacion anonimizado."""
    nombre: str = Field(..., max_length=200)
    institucion: Optional[str] = Field(None, max_length=200)
    duracion_horas: int = Field(..., ge=1)
    fecha: date


class HabilidadCiego(BaseModel):
    """Habilidad tecnica anonimizada."""
    tipo: TipoHabilidad
    nombre: str = Field(..., max_length=100)
    nivel: NivelHabilidad
    
    class Config:
        use_enum_values = True


class CvCiego(BaseModel):
    """CV anonimizado completo listo para matching."""
    folio: str = Field(..., max_length=36)
    escolaridad: List[EscolaridadCiego] = []
    experiencia: List[ExperienciaCiego] = []
    cursos: List[CursoCiego] = []
    habilidades: List[HabilidadCiego] = []
    
    class Config:
        json_schema_extra = {
            "example": {
                "folio": "01HXYZ123",
                "escolaridad": [
                    {
                        "nivel": "LICENCIATURA",
                        "titulo": "Ingenieria en Sistemas",
                        "institucion": "Universidad Anonima",
                        "fecha_inicio": "2015-08-01",
                        "fecha_termino": "2019-12-31",
                        "status": "CONCLUIDO"
                    }
                ],
                "experiencia": [],
                "cursos": [],
                "habilidades": []
            }
        }