package mx.ine.gestiona_t.modules.cv.service;

import mx.ine.gestiona_t.modules.cv.dto.request.*;
import mx.ine.gestiona_t.modules.cv.dto.response.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface CvService {
    
    Mono<CvResponse> crearCv(UUID aspiranteId, String folio);
    
    Mono<CvCompletoResponse> obtenerCvCompleto(String folio);
    
    Mono<CvResponse> actualizarCv(String folio);
    
    Mono<EscolaridadResponse> agregarEscolaridad(String folio, EscolaridadRequest request);
    
    Mono<EscolaridadResponse> actualizarEscolaridad(String folio, UUID id, EscolaridadRequest request);
    
    Mono<Void> eliminarEscolaridad(String folio, UUID id);
    
    Mono<ExperienciaResponse> agregarExperiencia(String folio, ExperienciaRequest request);
    
    Mono<ExperienciaResponse> actualizarExperiencia(String folio, UUID id, ExperienciaRequest request);
    
    Mono<Void> eliminarExperiencia(String folio, UUID id);
    
    Mono<CursoResponse> agregarCurso(String folio, CursoRequest request);
    
    Mono<CursoResponse> actualizarCurso(String folio, UUID id, CursoRequest request);
    
    Mono<Void> eliminarCurso(String folio, UUID id);
    
    Mono<HabilidadResponse> agregarHabilidad(String folio, HabilidadRequest request);
    
    Mono<HabilidadResponse> actualizarHabilidad(String folio, UUID id, HabilidadRequest request);
    
    Mono<Void> eliminarHabilidad(String folio, UUID id);
    
    Mono<CvUploadResponse> subirCvNoEstructurado(MultipartFile file, UUID aspiranteId);
    
    Mono<ValidacionCvResponse> validarCv(String folio);
    
    Mono<ScoreCompletitudResponse> obtenerScoreCompletitud(String folio);
}