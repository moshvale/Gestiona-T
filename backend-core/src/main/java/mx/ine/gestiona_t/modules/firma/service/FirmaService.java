package mx.ine.gestiona_t.modules.firma.service;

import mx.ine.gestiona_t.modules.firma.dto.request.FirmarBiometricaRequest;
import mx.ine.gestiona_t.modules.firma.dto.request.FirmarFEARequest;
import mx.ine.gestiona_t.modules.firma.dto.request.FirmarOTPRequest;
import mx.ine.gestiona_t.modules.firma.dto.response.FirmaResponse;
import mx.ine.gestiona_t.modules.firma.dto.response.MetadataFirmaResponse;
import mx.ine.gestiona_t.modules.firma.dto.response.ValidacionFirmaResponse;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;

public interface FirmaService {
    
    Mono<FirmaResponse> firmarConFEA(FirmarFEARequest request, String ip, String userAgent);
    
    Mono<FirmaResponse> firmarConBiometria(FirmarBiometricaRequest request, String ip, String userAgent);
    
    Mono<FirmaResponse> firmarConOTP(FirmarOTPRequest request, String ip, String userAgent,
                                      String geolocalizacion, String dispositivoId);
    
    Mono<FirmaResponse> obtenerDocumento(String folioDocumento);
    
    Mono<List<FirmaResponse>> obtenerDocumentosAspirante(UUID aspiranteId);
    
    Mono<ValidacionFirmaResponse> validarFirma(String folioDocumento);
    
    Mono<MetadataFirmaResponse> obtenerMetadata(String folioDocumento);
    
    Mono<byte[]> obtenerPdfFirmado(String folioDocumento);
}