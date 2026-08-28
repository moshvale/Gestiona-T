"""Servicio de validación de autenticidad de documentos oficiales."""
import re
from typing import List, Tuple

from loguru import logger


class AutenticidadService:
    """Valida la autenticidad de documentos oficiales mexicanos."""

    def validar(self, texto: str, tipo_documento: str) -> dict:
        """
        Valida la autenticidad de un documento según su tipo.
        
        Returns:
            Dict con: score_autenticidad, sospechoso, anomalias, mensaje
        """
        if not texto or len(texto.strip()) < 50:
            return {
                "score_autenticidad": 0.0,
                "sospechoso": True,
                "anomalias": ["Texto insuficiente para validar autenticidad"],
                "mensaje": "No se pudo extraer suficiente texto del documento",
            }

        anomalias: List[str] = []
        aciertos: List[str] = []
        score = 50.0  # Score base

        # Validaciones comunes a todos los documentos
        score_common, anom_common, aci_common = self._validaciones_comunes(texto)
        score += score_common
        anomalias.extend(anom_common)
        aciertos.extend(aci_common)

        # Validaciones específicas por tipo
        validadores = {
            "INE": self._validar_ine,
            "CURP": self._validar_curp,
            "RFC_COMPROBANTE": self._validar_rfc,
            "COMPROBANTE_DOMICILIO": self._validar_comprobante_domicilio,
            "ACTA_NACIMIENTO": self._validar_acta_nacimiento,
            "CEDULA_PROFESIONAL": self._validar_cedula,
        }

        validador = validadores.get(tipo_documento)
        if validador:
            score_spec, anom_spec, aci_spec = validador(texto)
            score += score_spec
            anomalias.extend(anom_spec)
            aciertos.extend(aci_spec)
        else:
            anomalias.append(f"Tipo de documento '{tipo_documento}' sin validador específico")

        # Normalizar score entre 0 y 100
        score_final = max(0.0, min(100.0, score))
        sospechoso = score_final < 60.0 or len(anomalias) >= 3

        mensaje = self._generar_mensaje(score_final, sospechoso, anomalias)

        logger.info(
            f"🔍 Autenticidad validada. Score: {score_final:.1f}, "
            f"Sospechoso: {sospechoso}, Anomalías: {len(anomalias)}"
        )

        return {
            "score_autenticidad": round(score_final, 2),
            "sospechoso": sospechoso,
            "anomalias": anomalias,
            "mensaje": mensaje,
        }

    def _validaciones_comunes(self, texto: str) -> Tuple[float, List[str], List[str]]:
        """Validaciones aplicables a cualquier documento oficial."""
        anomalias: List[str] = []
        aciertos: List[str] = []
        score = 0.0

        # Verificar que tenga contenido sustancial
        if len(texto) < 100:
            anomalias.append("Documento con muy poco texto")
        else:
            aciertos.append("Documento con contenido sustancial")
            score += 5.0

        # Verificar presencia de datos estructurados (nombres, fechas, números)
        tiene_numeros = bool(re.search(r"\d{3,}", texto))
        tiene_fechas = bool(re.search(r"\d{2}[/-]\d{2}[/-]\d{2,4}", texto))
        
        if tiene_numeros:
            aciertos.append("Contiene datos numéricos")
            score += 3.0
        else:
            anomalias.append("No se detectaron datos numéricos")

        if tiene_fechas:
            aciertos.append("Contiene fechas")
            score += 3.0
        else:
            anomalias.append("No se detectaron fechas")

        # Detectar texto repetitivo (posible imagen de baja calidad)
        palabras = texto.split()
        if palabras:
            palabra_mas_comun = max(set(palabras), key=palabras.count)
            frecuencia = palabras.count(palabra_mas_comun) / len(palabras)
            if frecuencia > 0.3 and len(palabra_mas_comun) > 3:
                anomalias.append("Texto repetitivo detectado (posible problema de OCR)")

        return score, anomalias, aciertos

    def _validar_ine(self, texto: str) -> Tuple[float, List[str], List[str]]:
        """Valida credencial para votar (INE)."""
        anomalias: List[str] = []
        aciertos: List[str] = []
        score = 0.0

        # Clave de elector
        if re.search(r"[A-Z]{6}\d{2}[HM]\d{3}\d{2}", texto):
            aciertos.append("Clave de elector detectada")
            score += 15.0
        else:
            anomalias.append("No se detectó clave de elector")

        # Sección electoral
        if re.search(r"SECC(?:ION)?\s*[:.]?\s*\d{4}", texto, re.IGNORECASE):
            aciertos.append("Sección electoral detectada")
            score += 10.0
        else:
            anomalias.append("No se detectó sección electoral")

        # Vigencia
        if re.search(r"VIGEN(?:CIA)?\s*[:.]?\s*\d{4}", texto, re.IGNORECASE):
            aciertos.append("Vigencia detectada")
            score += 5.0

        return score, anomalias, aciertos

    def _validar_curp(self, texto: str) -> Tuple[float, List[str], List[str]]:
        """Valida documento CURP."""
        anomalias: List[str] = []
        aciertos: List[str] = []
        score = 0.0

        # CURP completa
        curp_match = re.search(r"[A-Z]{4}\d{6}[HM][A-Z]{5}[A-Z0-9]\d", texto)
        if curp_match:
            curp = curp_match.group()
            aciertos.append(f"CURP detectada: {curp}")
            score += 20.0
            
            # Validar dígito verificador (simplificado)
            if self._validar_curp_algoritmo(curp):
                aciertos.append("CURP con dígito verificador válido")
                score += 5.0
            else:
                anomalias.append("CURP con posible dígito verificador inválido")
        else:
            anomalias.append("No se detectó CURP")

        return score, anomalias, aciertos

    def _validar_rfc(self, texto: str) -> Tuple[float, List[str], List[str]]:
        """Valida comprobante de situación fiscal (RFC)."""
        anomalias: List[str] = []
        aciertos: List[str] = []
        score = 0.0

        # RFC
        rfc_match = re.search(r"[A-ZÑ&]{3,4}\d{6}[A-Z0-9]{3}", texto)
        if rfc_match:
            aciertos.append(f"RFC detectado: {rfc_match.group()}")
            score += 15.0
        else:
            anomalias.append("No se detectó RFC")

        # Referencia al SAT
        if re.search(r"SAT|SERVICIO DE ADMINISTRACION TRIBUTARIA", texto, re.IGNORECASE):
            aciertos.append("Referencia al SAT detectada")
            score += 10.0

        return score, anomalias, aciertos

    def _validar_comprobante_domicilio(self, texto: str) -> Tuple[float, List[str], List[str]]:
        """Valida comprobante de domicilio."""
        anomalias: List[str] = []
        aciertos: List[str] = []
        score = 0.0

        # Código postal
        if re.search(r"\b\d{5}\b", texto):
            aciertos.append("Código postal detectado")
            score += 10.0
        else:
            anomalias.append("No se detectó código postal")

        # Dirección estructurada
        if re.search(r"CALLE|AVENIDA|BLVD", texto, re.IGNORECASE):
            aciertos.append("Dirección detectada")
            score += 10.0

        # Empresa emisora conocida
        if re.search(r"CFE|CONAGUA|TELMEX|IZZI|SKY", texto, re.IGNORECASE):
            aciertos.append("Empresa emisora reconocida")
            score += 5.0

        return score, anomalias, aciertos

    def _validar_acta_nacimiento(self, texto: str) -> Tuple[float, List[str], List[str]]:
        """Valida acta de nacimiento."""
        anomalias: List[str] = []
        aciertos: List[str] = []
        score = 0.0

        if re.search(r"ACTA DE NACIMIENTO", texto, re.IGNORECASE):
            aciertos.append("Encabezado de acta detectado")
            score += 15.0
        else:
            anomalias.append("No se detectó encabezado de acta")

        if re.search(r"RENAPO|REGISTRO CIVIL", texto, re.IGNORECASE):
            aciertos.append("Autoridad emisora detectada")
            score += 10.0

        return score, anomalias, aciertos

    def _validar_cedula(self, texto: str) -> Tuple[float, List[str], List[str]]:
        """Valida cédula profesional."""
        anomalias: List[str] = []
        aciertos: List[str] = []
        score = 0.0

        if re.search(r"CEDULA PROFESIONAL", texto, re.IGNORECASE):
            aciertos.append("Encabezado de cédula detectado")
            score += 15.0

        if re.search(r"SEP|SECRETARIA DE EDUCACION", texto, re.IGNORECASE):
            aciertos.append("Autoridad emisora (SEP) detectada")
            score += 10.0

        if re.search(r"REGISTRO\s*[:.]?\s*\d{7,}", texto, re.IGNORECASE):
            aciertos.append("Número de registro detectado")
            score += 5.0

        return score, anomalias, aciertos

    def _validar_curp_algoritmo(self, curp: str) -> bool:
        """Valida el dígito verificador del CURP (simplificado)."""
        if len(curp) != 18:
            return False
        
        # Mapeo de caracteres a valores
        mapa = "0123456789ABCDEFGHIJKLMNÑOPQRSTUVWXYZ"
        
        suma = 0
        for i, char in enumerate(curp[:17]):
            valor = mapa.find(char.upper())
            if valor == -1:
                return False
            suma += valor * (18 - i)
        
        digito_esperado = (10 - (suma % 10)) % 10
        return str(digito_esperado) == curp[-1]

    def _generar_mensaje(self, score: float, sospechoso: bool, anomalias: List[str]) -> str:
        """Genera un mensaje descriptivo del resultado."""
        if score >= 80:
            return "Documento con alta probabilidad de autenticidad"
        elif score >= 60:
            return "Documento con autenticidad aceptable, se recomienda verificación adicional"
        elif sospechoso:
            return f"Documento con posibles anomalías: {', '.join(anomalias[:2])}"
        else:
            return "No se pudo validar la autenticidad del documento"


# Instancia singleton
autenticidad_service = AutenticidadService()