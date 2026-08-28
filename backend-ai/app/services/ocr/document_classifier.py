"""Clasificador de documentos basado en patrones de texto extraído."""
import re
from typing import Optional

from loguru import logger


# Patrones regex para identificar tipos de documentos oficiales mexicanos
PATRONES_DOCUMENTOS = {
    "INE": {
        "patrones": [
            r"CREDENCIAL PARA VOTAR",
            r"INSTITUTO NACIONAL ELECTORAL",
            r"CLAVE DE ELECTOR\s*[:.]?\s*[A-Z]{6}\d{2}[HM]\d{3}\d{2}",
            r"SECCION\s*[:.]?\s*\d{4}",
            r"VIGENCIA\s*[:.]?\s*\d{4}",
        ],
        "peso": 3,  # Peso alto: muy específico
    },
    "CURP": {
        "patrones": [
            r"CURP\s*[:.]?\s*[A-Z]{4}\d{6}[HM][A-Z]{5}[A-Z0-9]\d",
            r"CLAVE UNICA DE REGISTRO DE POBLACION",
            r"SECRETARIA DE GOBERNACION",
        ],
        "peso": 3,
    },
    "RFC_COMPROBANTE": {
        "patrones": [
            r"SISTEMA DE ADMINISTRACION TRIBUTARIA",
            r"COMPROBANTE DE SITUACION FISCAL",
            r"RFC\s*[:.]?\s*[A-ZÑ&]{3,4}\d{6}[A-Z0-9]{3}",
            r"SAT",
        ],
        "peso": 3,
    },
    "COMPROBANTE_DOMICILIO": {
        "patrones": [
            r"COMPROBANTE DE DOMICILIO",
            r"RECIBO DE (LUZ|AGUA|TELEFONO|GAS)",
            r"CFE|CONAGUA|TEL[Mm]EX",
            r"CALLE\s+\w+.*\d+.*COLONIA",
            r"CODIGO POSTAL\s*[:.]?\s*\d{5}",
        ],
        "peso": 2,
    },
    "ACTA_NACIMIENTO": {
        "patrones": [
            r"ACTA DE NACIMIENTO",
            r"RENAPO",
            r"REGISTRO CIVIL",
            r"CURP\s*[:.]?\s*[A-Z]{4}\d{6}[HM][A-Z]{5}",
        ],
        "peso": 3,
    },
    "CEDULA_PROFESIONAL": {
        "patrones": [
            r"CEDULA PROFESIONAL",
            r"SECRETARIA DE EDUCACION PUBLICA",
            r"SEP",
            r"REGISTRO\s*[:.]?\s*\d{7,8}",
        ],
        "peso": 3,
    },
    "CERTIFICADO_ESTUDIOS": {
        "patrones": [
            r"CERTIFICADO DE (ESTUDIOS|BACHILLERATO|PREPARATORIA)",
            r"UNIVERSIDAD",
            r"INSTITUTO|COLEGIO",
            r"PROMEDIO\s*[:.]?\s*\d+[.,]\d+",
        ],
        "peso": 2,
    },
}


class DocumentClassifier:
    """Clasifica documentos según su contenido textual."""

    def clasificar(self, texto: str, nombre_archivo: Optional[str] = None) -> dict:
        """
        Clasifica un documento basándose en su texto extraído.
        
        Returns:
            Dict con: tipo_detectado, confianza, metadata
        """
        if not texto or len(texto.strip()) < 20:
            logger.warning("Texto insuficiente para clasificar documento")
            return {
                "tipo_detectado": "DESCONOCIDO",
                "confianza": 0.0,
                "metadata": {"razon": "Texto insuficiente"},
            }

        texto_upper = texto.upper()
        scores: dict[str, float] = {}

        for tipo_doc, config in PATRONES_DOCUMENTOS.items():
            coincidencias = 0
            for patron in config["patrones"]:
                if re.search(patron, texto_upper, re.IGNORECASE | re.MULTILINE):
                    coincidencias += 1
            
            if coincidencias > 0:
                # Score = (coincidencias / total patrones) * peso
                score = (coincidencias / len(config["patrones"])) * config["peso"]
                scores[tipo_doc] = score

        if not scores:
            return {
                "tipo_detectado": "DESCONOCIDO",
                "confianza": 0.0,
                "metadata": {"razon": "No se encontraron patrones conocidos"},
            }

        # Obtener el tipo con mayor score
        tipo_ganador = max(scores, key=scores.get)  # type: ignore
        score_max = scores[tipo_ganador]
        
        # Normalizar confianza a 0-1
        confianza = min(score_max / 3.0, 1.0)

        logger.info(f"📄 Documento clasificado: {tipo_ganador} (confianza: {confianza:.2f})")

        return {
            "tipo_detectado": tipo_ganador,
            "confianza": round(confianza, 3),
            "metadata": {
                "scores": {k: round(v, 3) for k, v in scores.items()},
                "longitud_texto": len(texto),
            },
        }


# Instancia singleton
document_classifier = DocumentClassifier()