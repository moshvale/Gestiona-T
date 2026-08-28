package mx.ine.gestiona_t.modules.documentos.service;

import mx.ine.gestiona_t.modules.documentos.model.enums.TipoDocumento;
import mx.ine.gestiona_t.modules.documentos.model.enums.TipoValidacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class ClasificadorDocumentoService {
    
    private static final Logger log = LoggerFactory.getLogger(ClasificadorDocumentoService.class);
    
    private static final Map<TipoDocumento, TipoValidacion> MAPEO_TIPOS = new HashMap<>();
    
    static {
        MAPEO_TIPOS.put(TipoDocumento.CEDULA_PROFESIONAL, TipoValidacion.TIPO_A);
        MAPEO_TIPOS.put(TipoDocumento.CURP, TipoValidacion.TIPO_A);
        MAPEO_TIPOS.put(TipoDocumento.RFC, TipoValidacion.TIPO_A);
        MAPEO_TIPOS.put(TipoDocumento.COMPROBANTE_SITUACION_FISCAL, TipoValidacion.TIPO_A);
        MAPEO_TIPOS.put(TipoDocumento.CERTIFICADO_BACHILLERATO, TipoValidacion.TIPO_B);
        MAPEO_TIPOS.put(TipoDocumento.CERTIFICADO_MEDIO_SUPERIOR, TipoValidacion.TIPO_B);
        MAPEO_TIPOS.put(TipoDocumento.CERTIFICACION_TECNICA, TipoValidacion.TIPO_B);
        MAPEO_TIPOS.put(TipoDocumento.CONSTANCIA_LABORAL, TipoValidacion.TIPO_B);
        MAPEO_TIPOS.put(TipoDocumento.COMPROBANTE_DOMICILIO, TipoValidacion.TIPO_B);
        MAPEO_TIPOS.put(TipoDocumento.IDENTIFICACION_OFICIAL, TipoValidacion.TIPO_B);
        MAPEO_TIPOS.put(TipoDocumento.ACTA_NACIMIENTO, TipoValidacion.TIPO_B);
        MAPEO_TIPOS.put(TipoDocumento.EVALUACION_CONOCIMIENTOS, TipoValidacion.TIPO_C);
        MAPEO_TIPOS.put(TipoDocumento.EVALUACION_PSICOMETRICA, TipoValidacion.TIPO_C);
        MAPEO_TIPOS.put(TipoDocumento.EVALUACION_ENTREVISTA, TipoValidacion.TIPO_C);
        MAPEO_TIPOS.put(TipoDocumento.OTRO, TipoValidacion.TIPO_C);
    }
    
    public TipoValidacion determinarTipoValidacion(TipoDocumento tipo) {
        return MAPEO_TIPOS.getOrDefault(tipo, TipoValidacion.TIPO_C);
    }
    
    public TipoDocumento detectarTipoDesdeNombre(String nombreArchivo) {
        if (nombreArchivo == null) return TipoDocumento.OTRO;
        
        String lower = nombreArchivo.toLowerCase();
        
        if (lower.contains("cedula") || lower.contains("profesional")) return TipoDocumento.CEDULA_PROFESIONAL;
        if (lower.contains("curp")) return TipoDocumento.CURP;
        if (lower.contains("rfc") || lower.contains("sat")) return TipoDocumento.RFC;
        if (lower.contains("bachillerato") || lower.contains("preparatoria")) return TipoDocumento.CERTIFICADO_BACHILLERATO;
        if (lower.contains("constancia") && lower.contains("laboral")) return TipoDocumento.CONSTANCIA_LABORAL;
        if (lower.contains("domicilio")) return TipoDocumento.COMPROBANTE_DOMICILIO;
        if (lower.contains("ine") || lower.contains("identificacion")) return TipoDocumento.IDENTIFICACION_OFICIAL;
        if (lower.contains("acta") && lower.contains("nacimiento")) return TipoDocumento.ACTA_NACIMIENTO;
        
        return TipoDocumento.OTRO;
    }
}