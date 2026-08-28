"""Servicio de calculo de scores para cada seccion del CV.

Implementa la logica de scoring ponderado:
- Escolaridad: 30%
- Experiencia: 35%
- Cursos: 15%
- Habilidades: 20%
"""
from typing import List, Dict, Any, Optional
from loguru import logger
from datetime import date

from app.services.matching.models.cv_models import (
    CvCiego, NivelEstudio, NivelHabilidad,
)
from app.services.matching.models.cedula_models import CedulaPuesto, RequisitosPuesto
from app.services.matching.embeddings import EmbeddingsService
from app.services.matching.nlp_processor import NLPProcessor
from app.core.config import settings


# Orden jerarquico de niveles de estudio
ORDEN_NIVELES = {
    NivelEstudio.PRIMARIA: 1,
    NivelEstudio.SECUNDARIA: 2,
    NivelEstudio.BACHILLERATO: 3,
    NivelEstudio.TECNICO: 4,
    NivelEstudio.LICENCIATURA: 5,
    NivelEstudio.INGENIERIA: 6,
    NivelEstudio.MAESTRIA: 7,
    NivelEstudio.DOCTORADO: 8,
}

# Orden jerarquico de niveles de habilidad
ORDEN_NIVELES_HABILIDAD = {
    NivelHabilidad.BASICO: 1,
    NivelHabilidad.INTERMEDIO: 2,
    NivelHabilidad.AVANZADO: 3,
    NivelHabilidad.EXPERTO: 4,
    NivelHabilidad.NATIVO: 5,
}


class ScoringService:
    """Servicio de calculo de scores."""
    
    def __init__(self):
        self.embeddings = EmbeddingsService.get_instance()
        self.nlp = NLPProcessor.get_instance()
    
    def calcular_scores(
        self, cv: CvCiego, cedula: CedulaPuesto
    ) -> Dict[str, float]:
        """Calcula scores por seccion."""
        req = cedula.requisitos
        
        score_esc = self._score_escolaridad(cv.escolaridad, req)
        score_exp = self._score_experiencia(cv.experiencia, req)
        score_cur = self._score_cursos(cv.cursos, req)
        score_hab = self._score_habilidades(cv.habilidades, req)
        
        logger.debug(
            f"Scores - Esc:{score_esc:.1f}, Exp:{score_exp:.1f}, "
            f"Cur:{score_cur:.1f}, Hab:{score_hab:.1f}"
        )
        
        return {
            "score_escolaridad": score_esc,
            "score_experiencia": score_exp,
            "score_cursos": score_cur,
            "score_habilidades": score_hab,
        }
    
    def calcular_score_total(self, scores: Dict[str, float]) -> float:
        """Calcula el score total ponderado."""
        total = (
            settings.peso_escolaridad * scores["score_escolaridad"] +
            settings.peso_experiencia * scores["score_experiencia"] +
            settings.peso_cursos * scores["score_cursos"] +
            settings.peso_habilidades * scores["score_habilidades"]
        )
        return round(min(100.0, max(0.0, total)), 2)
    
    # ============ ESCOLARIDAD ============
    
    def _score_escolaridad(
        self, escolaridad_cv: List[Any], req: RequisitosPuesto
    ) -> float:
        """Score de escolaridad (0-100)."""
        if not escolaridad_cv:
            return 0.0
        
        # 1. Nivel minimo requerido (50%)
        nivel_max_cv = max(
            (ORDEN_NIVELES.get(e.nivel, 0) for e in escolaridad_cv if e.status == "CONCLUIDO"),
            default=0
        )
        nivel_req = ORDEN_NIVELES.get(req.escolaridad_minima, 0)
        
        if nivel_max_cv >= nivel_req:
            score_nivel = 100.0
        elif nivel_max_cv == nivel_req - 1:
            score_nivel = 70.0
        else:
            score_nivel = max(0.0, 50.0 * (nivel_max_cv / max(nivel_req, 1)))
        
        # 2. Area de estudio (30%) - similitud semantica
        if req.area_estudio:
            titulos_cv = [e.titulo for e in escolaridad_cv if e.titulo]
            texto_titulos = " ".join(titulos_cv)
            texto_areas = " ".join(req.area_estudio)
            
            similitud = self.embeddings.calcular_similitud(texto_titulos, texto_areas)
            score_area = similitud * 100.0
        else:
            score_area = 100.0
        
        # 3. Status concluido (20%)
        concluidos = sum(1 for e in escolaridad_cv if e.status == "CONCLUIDO")
        score_status = (concluidos / len(escolaridad_cv)) * 100.0 if escolaridad_cv else 0.0
        
        return 0.50 * score_nivel + 0.30 * score_area + 0.20 * score_status
    
    # ============ EXPERIENCIA ============
    
    def _score_experiencia(
        self, experiencia_cv: List[Any], req: RequisitosPuesto
    ) -> float:
        """Score de experiencia (0-100)."""
        if not experiencia_cv:
            return 0.0 if req.experiencia_anios > 0 else 50.0
        
        # 1. Anios de experiencia (50%)
        anios_totales = self._calcular_anios_experiencia(experiencia_cv)
        
        if req.experiencia_anios == 0:
            score_anios = 100.0
        elif anios_totales >= req.experiencia_anios:
            score_anios = 100.0
        else:
            score_anios = (anios_totales / req.experiencia_anios) * 100.0
        
        # 2. Experiencia especifica (40%) - similitud semantica
        if req.experiencia_especifica:
            funciones_cv = " ".join(e.funciones for e in experiencia_cv if e.funciones)
            similitud = self.embeddings.calcular_similitud(
                funciones_cv, req.experiencia_especifica
            )
            score_especifica = similitud * 100.0
        else:
            score_especifica = 70.0
        
        # 3. Nivel de mando (10%)
        if req.nivel_mando_preferente:
            niveles_cv = [e.nivel_mando for e in experiencia_cv]
            if req.nivel_mando_preferente in niveles_cv:
                score_mando = 100.0
            else:
                score_mando = 50.0
        else:
            score_mando = 100.0
        
        return 0.50 * score_anios + 0.40 * score_especifica + 0.10 * score_mando
    
    def _calcular_anios_experiencia(self, experiencia: List[Any]) -> float:
        """Calcula anios totales de experiencia."""
        hoy = date.today()
        total_meses = 0
        
        for exp in experiencia:
            inicio = exp.fecha_inicio
            fin = exp.fecha_termino if not exp.actualmente_laborando else hoy
            
            if inicio and fin and fin > inicio:
                meses = (fin.year - inicio.year) * 12 + (fin.month - inicio.month)
                total_meses += max(0, meses)
        
        return total_meses / 12.0
    
    # ============ CURSOS ============
    
    def _score_cursos(self, cursos_cv: List[Any], req: RequisitosPuesto) -> float:
        """Score de cursos (0-100)."""
        if not cursos_cv:
            return 50.0 if not req.certificaciones_deseables else 0.0
        
        # 1. Horas totales (30%)
        horas_totales = sum(c.duracion_horas for c in cursos_cv)
        score_horas = min(100.0, (horas_totales / 200.0) * 100.0)
        
        # 2. Relevancia semantica (50%)
        if req.habilidades_tecnicas:
            nombres_cursos = [c.nombre for c in cursos_cv if c.nombre]
            texto_cursos = " ".join(nombres_cursos)
            texto_req = " ".join(req.habilidades_tecnicas)
            
            similitud = self.embeddings.calcular_similitud(texto_cursos, texto_req)
            score_relevancia = similitud * 100.0
        else:
            score_relevancia = 70.0
        
        # 3. Recencia (20%)
        hoy = date.today()
        cursos_recientes = sum(
            1 for c in cursos_cv 
            if c.fecha and (hoy - c.fecha).days <= 1095  # 3 anios
        )
        score_recencia = (cursos_recientes / len(cursos_cv)) * 100.0 if cursos_cv else 0.0
        
        return 0.30 * score_horas + 0.50 * score_relevancia + 0.20 * score_recencia
    
    # ============ HABILIDADES ============
    
    def _score_habilidades(
        self, habilidades_cv: List[Any], req: RequisitosPuesto
    ) -> tuple:
        """Score de habilidades (0-100). Retorna (score, coincidencias)."""
        if not habilidades_cv and not req.habilidades_tecnicas:
            return 70.0, [], []
        
        # Separar por tipo
        habilidades_tec = [h for h in habilidades_cv if h.tipo in {"SOFTWARE", "CONOCIMIENTO_TECNICO", "CERTIFICACION"}]
        habilidades_idioma = [h for h in habilidades_cv if h.tipo == "IDIOMA"]
        
        # 1. Habilidades tecnicas requeridas (60%)
        if req.habilidades_tecnicas:
            match, faltantes = self._matching_habilidades(
                habilidades_tec, req.habilidades_tecnicas
            )
            if req.habilidades_tecnicas:
                score_tecnicas = (len(match) / len(req.habilidades_tecnicas)) * 100.0
            else:
                score_tecnicas = 100.0
        else:
            match, faltantes = [], []
            score_tecnicas = 70.0
        
        # 2. Idiomas requeridos (30%)
        if req.idiomas:
            matches_idioma = []
            for req_idioma in req.idiomas:
                for hab_idioma in habilidades_idioma:
                    if self._idiomas_coinciden(hab_idioma.nombre, req_idioma.idioma):
                        nivel_cv = ORDEN_NIVELES_HABILIDAD.get(hab_idioma.nivel, 0)
                        nivel_req = ORDEN_NIVELES_HABILIDAD.get(req_idioma.nivel_minimo, 0)
                        if nivel_cv >= nivel_req:
                            matches_idioma.append(req_idioma.idioma)
                            break
            
            score_idiomas = (len(matches_idioma) / len(req.idiomas)) * 100.0
        else:
            matches_idioma = []
            score_idiomas = 100.0
        
        # 3. Certificaciones deseables (10%)
        if req.certificaciones_deseables:
            certs_cv = [h.nombre for h in habilidades_cv if h.tipo == "CERTIFICACION"]
            match_certs = []
            for cert_req in req.certificaciones_deseables:
                for cert_cv in certs_cv:
                    sim = self.embeddings.calcular_similitud(cert_cv, cert_req)
                    if sim > 0.75:
                        match_certs.append(cert_req)
                        break
            
            score_certs = (len(match_certs) / len(req.certificaciones_deseables)) * 100.0
        else:
            match_certs = []
            score_certs = 100.0
        
        score_total = 0.60 * score_tecnicas + 0.30 * score_idiomas + 0.10 * score_certs
        
        # Guardar coincidencias para el response (via atributo temporal)
        self._ultimas_coincidencias = {
            "habilidades_match": match,
            "habilidades_faltantes": faltantes,
            "idiomas_match": matches_idioma,
            "certificaciones_match": match_certs,
        }
        
        return score_total
    
    def get_ultimas_coincidencias(self) -> Dict[str, List[str]]:
        """Retorna las coincidencias del ultimo calculo."""
        return getattr(self, "_ultimas_coincidencias", {
            "habilidades_match": [],
            "habilidades_faltantes": [],
            "idiomas_match": [],
            "certificaciones_match": [],
        })
    
    def _matching_habilidades(
        self, habilidades_cv: List[Any], habilidades_req: List[str]
    ) -> tuple:
        """Matching de habilidades tecnicas usando similitud semantica."""
        match = []
        faltantes = []
        
        nombres_cv = [h.nombre.lower() for h in habilidades_cv]
        
        for req in habilidades_req:
            req_lower = req.lower()
            
            # 1. Match exacto
            if req_lower in nombres_cv:
                match.append(req)
                continue
            
            # 2. Match semantico
            mejor_match, similitud = self.embeddings.mejor_match(req, nombres_cv)
            
            if similitud > 0.80 and mejor_match:
                match.append(req)
            else:
                faltantes.append(req)
        
        return match, faltantes
    
    def _idiomas_coinciden(self, idioma_cv: str, idioma_req: str) -> bool:
        """Verifica si dos nombres de idioma coinciden."""
        cv_lower = idioma_cv.lower().strip()
        req_lower = idioma_req.lower().strip()
        
        if cv_lower == req_lower:
            return True
        
        # Variantes comunes
        variantes = {
            "ingles": ["inglés", "english", "ing"],
            "frances": ["francés", "french", "fra"],
            "aleman": ["alemán", "german", "ger"],
            "portugues": ["portugués", "portuguese", "por"],
            "italiano": ["italian", "ita"],
        }
        
        for base, vars_ in variantes.items():
            if base in cv_lower or any(v in cv_lower for v in vars_):
                if base in req_lower or any(v in req_lower for v in vars_):
                    return True
        
        # Similitud semantica como fallback
        sim = self.embeddings.calcular_similitud(idioma_cv, idioma_req)
        return sim > 0.85