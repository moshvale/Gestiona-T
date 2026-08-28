"""Servicio de OCR para extracción de texto de documentos PDF e imágenes."""
import io
import tempfile
from pathlib import Path
from typing import Optional

import pytesseract
from loguru import logger
from pdf2image import convert_from_bytes
from PIL import Image


class OcrService:
    """Servicio encargado de extraer texto de documentos usando Tesseract."""

    def __init__(self) -> None:
        self._initialized = False

    def initialize(self) -> None:
        """Verifica que Tesseract esté disponible."""
        try:
            pytesseract.get_tesseract_version()
            self._initialized = True
            logger.info("✅ Tesseract OCR inicializado correctamente")
        except Exception as e:
            logger.warning(f"⚠️ Tesseract no disponible: {e}. OCR usará fallback.")
            self._initialized = False

    def extraer_texto(self, archivo_bytes: bytes, nombre_archivo: str) -> str:
        """
        Extrae texto de un documento PDF o imagen.
        
        Args:
            archivo_bytes: Contenido binario del archivo.
            nombre_archivo: Nombre original del archivo (para detectar tipo).
            
        Returns:
            Texto extraído del documento.
        """
        extension = Path(nombre_archivo).suffix.lower()
        
        try:
            if extension == ".pdf":
                return self._extraer_de_pdf(archivo_bytes)
            elif extension in (".png", ".jpg", ".jpeg", ".tiff", ".bmp"):
                return self._extraer_de_imagen(archivo_bytes)
            else:
                logger.warning(f"Tipo de archivo no soportado para OCR: {extension}")
                return ""
        except Exception as e:
            logger.error(f"Error en OCR: {e}")
            return ""

    def _extraer_de_pdf(self, pdf_bytes: bytes) -> str:
        """Convierte PDF a imágenes y aplica OCR a cada página."""
        logger.info("Convirtiendo PDF a imágenes para OCR...")
        
        # Convertir PDF a imágenes (300 DPI para mejor calidad)
        imagenes = convert_from_bytes(
            pdf_bytes,
            dpi=300,
            first_page=1,
            last_page=10,  # Limitar a 10 páginas para no saturar memoria
        )
        
        textos_paginas = []
        for i, imagen in enumerate(imagenes, 1):
            logger.debug(f"Procesando página {i} de {len(imagenes)}...")
            texto = self._ocr_imagen(imagen)
            if texto.strip():
                textos_paginas.append(f"--- Página {i} ---\n{texto}")
        
        texto_completo = "\n\n".join(textos_paginas)
        logger.info(f"✅ OCR completado. Texto extraído: {len(texto_completo)} caracteres")
        return texto_completo

    def _extraer_de_imagen(self, imagen_bytes: bytes) -> str:
        """Aplica OCR directamente a una imagen."""
        imagen = Image.open(io.BytesIO(imagen_bytes))
        return self._ocr_imagen(imagen)

    def _ocr_imagen(self, imagen: Image.Image) -> str:
        """Aplica Tesseract OCR a una imagen con configuración para español."""
        if not self._initialized:
            logger.warning("Tesseract no inicializado, retornando texto vacío")
            return ""
        
        # Configuración para español (México)
        custom_config = r"--oem 3 --psm 6 -l spa"
        texto = pytesseract.image_to_string(imagen, config=custom_config)
        return texto.strip()


# Instancia singleton
ocr_service = OcrService()