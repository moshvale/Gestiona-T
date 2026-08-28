"""Modelos Pydantic para la Cedula de Puesto."""
from pydantic import BaseModel, Field
from typing import List, Optional
from .cv_models import NivelEstudio, NivelHabilidad


class RequisitoIdioma(BaseModel):
    """Requisito de idioma."""
    idioma: str
    nivel_minimo: NivelHabilidad
    
    class Config:
        use_enum_values = True


class RequisitosPuesto(BaseModel):
    """Requisitos del puesto a cubrir."""
    escolaridad_minima: NivelEstudio
    area_estudio: List[str] = Field(default_factory=list)
    experiencia_anios: int = Field(default=0, ge=0)
    experiencia_especifica: Optional[str] = None
    habilidades_tecnicas: List[str] = Field(default_factory=list)
    idiomas: List[RequisitoIdioma] = Field(default_factory=list)
    certificaciones_deseables: List[str] = Field(default_factory=list)
    nivel_mando_preferente: Optional[str] = None
    
    class Config:
        use_enum_values = True


class CedulaPuesto(BaseModel):
    """Cedula de puesto completa."""
    codigo: str = Field(..., max_length=20)
    nombre: str = Field(..., max_length=200)
    descripcion: Optional[str] = None
    requisitos: RequisitosPuesto
    
    class Config:
        json_schema_extra = {
            "example": {
                "codigo": "DS-001",
                "nombre": "Desarrollador Senior",
                "requisitos": {
                    "escolaridad_minima": "LICENCIATURA",
                    "area_estudio": ["Sistemas", "Informatica"],
                    "experiencia_anios": 5,
                    "habilidades_tecnicas": ["Python", "Django"]
                }
            }
        }