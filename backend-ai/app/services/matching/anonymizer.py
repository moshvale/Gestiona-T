# app/services/matching/anonymizer.py
import re
from typing import Dict, Any

def anonimizar_cv(cv_data: Dict[str, Any]) -> Dict[str, Any]:
    """
    Elimina datos personales sensibles del CV para garantizar la ceguera curricular.
    """
    cv_anonimo = cv_data.copy()
    
    # 1. Eliminar datos explícitos del objeto
    campos_a_eliminar = ['nombreCompleto', 'correoElectronico', 'telefono', 'curp', 'rfc']
    for campo in campos_a_eliminar:
        if campo in cv_anonimo:
            del cv_anonimo[campo]
            
    # 2. Anonimizar textos libres (Habilidades, Logros, Funciones) usando Regex
    patrones_pii = {
        'email': r'[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}',
        'telefono': r'\b\d{10}\b', # Teléfonos de 10 dígitos
        'curp': r'\b[A-Z]{4}\d{6}[HM][A-Z]{5}[A-Z0-9]{2}\b',
        'rfc': r'\b[A-ZÑ&]{3,4}\d{6}[A-Z0-9]{3}\b'
    }
    
    def limpiar_texto(texto: str) -> str:
        if not texto:
            return texto
        texto_limpio = texto
        for tipo, patron in patrones_pii.items():
            if tipo == 'email':
                texto_limpio = re.sub(patron, '[CORREO_ANONIMIZADO]', texto_limpio, flags=re.IGNORECASE)
            elif tipo == 'telefono':
                texto_limpio = re.sub(patron, '[TELÉFONO_ANONIMIZADO]', texto_limpio)
            elif tipo == 'curp':
                texto_limpio = re.sub(patron, '[CURP_ANONIMIZADO]', texto_limpio, flags=re.IGNORECASE)
            elif tipo == 'rfc':
                texto_limpio = re.sub(patron, '[RFC_ANONIMIZADO]', texto_limpio, flags=re.IGNORECASE)
        return texto_limpio

    # Aplicar limpieza a campos de texto libre
    if 'habilidades' in cv_anonimo:
        cv_anonimo['habilidades'] = limpiar_texto(cv_anonimo['habilidades'])
    if 'logrosProfesionales' in cv_anonimo:
        cv_anonimo['logrosProfesionales'] = limpiar_texto(cv_anonimo['logrosProfesionales'])
        
    if 'experienciaLaboral' in cv_anonimo and cv_anonimo['experienciaLaboral']:
        for exp in cv_anonimo['experienciaLaboral']:
            exp['empresa'] = '[INSTITUCIÓN ANONIMIZADA]' # Ocultamos el nombre de la empresa para evitar sesgos
            exp['funciones'] = limpiar_texto(exp.get('funciones', ''))
            
    if 'formacionAcademica' in cv_anonimo and cv_anonimo['formacionAcademica']:
        for form in cv_anonimo['formacionAcademica']:
            form['institucion'] = '[INSTITUCIÓN EDUCATIVA ANONIMIZADA]'
            
    return cv_anonimo