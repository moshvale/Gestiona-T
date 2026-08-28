"""Endpoints de OCR y clasificación de documentos."""
from fastapi import APIRouter, File, HTTPException, UploadFile
from loguru import logger

from app.services.ocr.document_classifier import document_classifier
from app.services.ocr.ocr_service import ocr_service

router = APIRouter(prefix="/ocr", tags=["OCR"])


@router.post("/documento")
async def procesar_documento(file: UploadFile = File(...)) -> dict:
    """
    Extrae texto de un documento PDF/imagen y lo clasifica.
    
    Esperado por Backend-Core como:
    {
        "tipoDetectado": str,
        "confianza": float,
        "textoExtraido": str,
        "metadata": dict,
        "error": str | None
    }
    """
    if not file.filename:
        raise HTTPException(status_code=400, detail="Nombre de archivo no proporcionado")

    logger.info(f"📄 Procesando documento: {file.filename}")

    try:
        archivo_bytes = await file.read()
        
        if len(archivo_bytes) == 0:
            raise HTTPException(status_code=400, detail="Archivo vacío")
        
        if len(archivo_bytes) > 15 * 1024 * 1024:  # 15 MB
            raise HTTPException(status_code=413, detail="Archivo demasiado grande (máx 15MB)")

        # 1. Extraer texto con OCR
        texto_extraido = ocr_service.extraer_texto(archivo_bytes, file.filename)

        if not texto_extraido:
            return {
                "tipoDetectado": None,
                "confianza": 0.0,
                "textoExtraido": None,
                "camposExtraidos": {},
                "metadata": None,
                "mensaje": "No se pudo extraer texto del documento",
                "error": "No se pudo extraer texto del documento",
            }

        # 2. Clasificar el documento
        clasificacion = document_classifier.clasificar(texto_extraido, file.filename)

        return {
            "tipoDetectado": clasificacion["tipo_detectado"],
            "confianza": clasificacion["confianza"],
            "textoExtraido": texto_extraido,
            "camposExtraidos": clasificacion["metadata"],
            "metadata": clasificacion["metadata"],
            "mensaje": None,
            "error": None,
        }

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ Error procesando documento: {e}")
        return {
            "tipoDetectado": None,
            "confianza": 0.0,
            "textoExtraido": None,
            "camposExtraidos": {},
            "metadata": None,
            "mensaje": f"Error de procesamiento: {str(e)}",
            "error": f"Error de procesamiento: {str(e)}",
        }