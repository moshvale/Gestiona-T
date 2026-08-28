"""Endpoints de validación de autenticidad documental."""
from fastapi import APIRouter, File, HTTPException, UploadFile
from loguru import logger

from app.services.autenticidad.autenticidad_service import autenticidad_service
from app.services.ocr.ocr_service import ocr_service

router = APIRouter(prefix="/autenticidad", tags=["Autenticidad"])


@router.post("/validar")
async def validar_autenticidad(file: UploadFile = File(...)) -> dict:
    """
    Valida la autenticidad de un documento oficial.
    
    Esperado por Backend-Core como:
    {
        "scoreAutenticidad": float,
        "sospechoso": bool,
        "anomalias": list[str],
        "mensaje": str
    }
    """
    if not file.filename:
        raise HTTPException(status_code=400, detail="Nombre de archivo no proporcionado")

    logger.info(f"🔍 Validando autenticidad de: {file.filename}")

    try:
        archivo_bytes = await file.read()
        
        if len(archivo_bytes) == 0:
            raise HTTPException(status_code=400, detail="Archivo vacío")

        # 1. Extraer texto con OCR (paso previo necesario)
        texto_extraido = ocr_service.extraer_texto(archivo_bytes, file.filename)

        if not texto_extraido:
            return {
                "scoreAutenticidad": 0.0,
                "sospechoso": True,
                "anomalias": ["No se pudo extraer texto del documento"],
                "mensaje": "Documento ilegible o no soportado",
            }

        # 2. Clasificar primero para saber qué validador usar
        from app.services.ocr.document_classifier import document_classifier
        clasificacion = document_classifier.clasificar(texto_extraido, file.filename)
        tipo_detectado = clasificacion["tipo_detectado"]

        # 3. Validar autenticidad
        resultado = autenticidad_service.validar(texto_extraido, tipo_detectado)

        return {
            "scoreAutenticidad": resultado["score_autenticidad"],
            "sospechoso": resultado["sospechoso"],
            "anomalias": resultado["anomalias"],
            "mensaje": resultado["mensaje"],
        }

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ Error validando autenticidad: {e}")
        return {
            "scoreAutenticidad": 0.0,
            "sospechoso": True,
            "anomalias": [f"Error de procesamiento: {str(e)}"],
            "mensaje": "Error al validar el documento",
        }