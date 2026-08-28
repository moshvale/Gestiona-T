package mx.ine.gestiona_t.modules.cv.service;

import mx.ine.gestiona_t.modules.cv.dto.request.CvInstitucionalRequest;
import mx.ine.gestiona_t.modules.cv.dto.response.CvInstitucionalResponse;
import java.util.UUID;

public interface CvInstitucionalService {
    CvInstitucionalResponse guardarOActualizarCv(UUID aspiranteId, CvInstitucionalRequest request);
    CvInstitucionalResponse obtenerCvPorAspirante(UUID aspiranteId);
    boolean existeCv(UUID aspiranteId);
    
    // ✅ NUEVO: Método para generar el PDF
    byte[] generarPdfCv(UUID aspiranteId);
}