package mx.ine.gestiona_t.modules.documentos.service;

import mx.ine.gestiona_t.modules.documentos.dto.request.DictamenRevisionRequest;
import mx.ine.gestiona_t.modules.documentos.dto.response.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface DocumentoService {
    
    // ✅ ACTUALIZADO: Se agregó el parámetro boolean esTambienDomicilio
    Mono<UploadResponse> cargarDocumento(MultipartFile file, UUID aspiranteId, 
                                          String folio, String tipoDocumentoStr, 
                                          boolean esTambienDomicilio);
    
    Mono<List<DocumentoResponse>> obtenerDocumentosAspirante(UUID aspiranteId);
    Mono<DocumentoResponse> obtenerDocumento(UUID documentoId);
    Mono<Void> eliminarDocumento(UUID documentoId);
    Mono<byte[]> descargarDocumento(UUID documentoId);
    Mono<UploadResponse> cargarSoporteExpediente(MultipartFile file, UUID expedienteLaboralId);
    
    // Validación Automática (IA / OCR)
    Mono<ValidacionResponse> validarDocumento(UUID documentoId);
    
    // Validación Manual (Panel de Analista)
    Mono<ValidacionResponse> validarDocumentoManual(UUID id, String estatus, String motivo);
    Mono<ValidacionResponse> validarTodosDocumentos(String folio);
    Mono<List<RevisionResponse>> obtenerRevisionesPendientes();
    Mono<RevisionResponse> dictaminarRevision(Long revisionId, UUID analistaId, DictamenRevisionRequest request);
    Mono<List<DocumentoResponse>> obtenerDocumentosPorFolio(String folio);
    Mono<List<DocumentoResponse>> obtenerDocumentosPorAspiranteId(UUID aspiranteId);
}