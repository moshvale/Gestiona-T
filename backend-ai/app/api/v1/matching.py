# app/api/v1/matching.py
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Dict, Any
from app.services.matching.anonymizer import anonimizar_cv
from app.services.matching.matching_service import evaluar_matching

router = APIRouter(prefix="/matching", tags=["Matching"])

class EvaluacionRequest(BaseModel):
    cv_data: Dict[str, Any]
    perfil_puesto: str

@router.post("/evaluar")
async def evaluar_cv(request: EvaluacionRequest):
    try:
        # Paso 1: Anonimizar (Ceguera Curricular)
        cv_anonimo = anonimizar_cv(request.cv_data)
        
        # Paso 2: Evaluar
        resultado = evaluar_matching(cv_anonimo, request.perfil_puesto)
        
        # Paso 3: Devolver resultado (sin devolver el CV anonimizado al frontend por seguridad, solo el score)
        return {
            "score": resultado["score"],
            "nivel": resultado["nivel"],
            "mensaje": resultado["mensaje"]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error en el servicio de matching: {str(e)}")