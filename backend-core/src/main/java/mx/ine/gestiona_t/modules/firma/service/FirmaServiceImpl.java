package mx.ine.gestiona_t.modules.firma.service;

import mx.ine.gestiona_t.modules.firma.dto.request.FirmarBiometricaRequest;
import mx.ine.gestiona_t.modules.firma.dto.request.FirmarFEARequest;
import mx.ine.gestiona_t.modules.firma.dto.request.FirmarOTPRequest;
import mx.ine.gestiona_t.modules.firma.dto.response.FirmaResponse;
import mx.ine.gestiona_t.modules.firma.dto.response.MetadataFirmaResponse;
import mx.ine.gestiona_t.modules.firma.dto.response.ValidacionFirmaResponse;
import mx.ine.gestiona_t.modules.firma.model.DocumentoFirmado;
import mx.ine.gestiona_t.modules.firma.model.FirmaMetadata;
import mx.ine.gestiona_t.modules.firma.model.SelloDigital;
import mx.ine.gestiona_t.modules.firma.model.enums.EstatusFirma;
import mx.ine.gestiona_t.modules.firma.repository.DocumentoFirmadoRepository;
import mx.ine.gestiona_t.modules.firma.repository.FirmaMetadataRepository;
import mx.ine.gestiona_t.modules.firma.repository.SelloDigitalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FirmaServiceImpl implements FirmaService {
    
    private static final Logger log = LoggerFactory.getLogger(FirmaServiceImpl.class);
    
    private final FirmaFEAService feaService;
    private final FirmaBiometricaService biometricaService;
    private final FirmaOTPService otpService;
    private final DocumentoFirmadoRepository documentoRepository;
    private final SelloDigitalRepository selloRepository;
    private final FirmaMetadataRepository metadataRepository;
    private final MinioFirmaService minioService;
    
    public FirmaServiceImpl(FirmaFEAService feaService, FirmaBiometricaService biometricaService,
                             FirmaOTPService otpService, DocumentoFirmadoRepository documentoRepository,
                             SelloDigitalRepository selloRepository, FirmaMetadataRepository metadataRepository,
                             MinioFirmaService minioService) {
        this.feaService = feaService;
        this.biometricaService = biometricaService;
        this.otpService = otpService;
        this.documentoRepository = documentoRepository;
        this.selloRepository = selloRepository;
        this.metadataRepository = metadataRepository;
        this.minioService = minioService;
    }
    
    @Override
    public Mono<FirmaResponse> firmarConFEA(FirmarFEARequest request, String ip, String userAgent) {
        return feaService.firmar(request, ip, userAgent);
    }
    
    @Override
    public Mono<FirmaResponse> firmarConBiometria(FirmarBiometricaRequest request, String ip, String userAgent) {
        return biometricaService.firmar(request, ip, userAgent);
    }
    
    @Override
    public Mono<FirmaResponse> firmarConOTP(FirmarOTPRequest request, String ip, String userAgent,
                                              String geolocalizacion, String dispositivoId) {
        return otpService.firmar(request, ip, userAgent, geolocalizacion, dispositivoId);
    }
    
    @Override
    public Mono<FirmaResponse> obtenerDocumento(String folioDocumento) {
        DocumentoFirmado doc = documentoRepository.findByFolioDocumento(folioDocumento)
            .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
        return Mono.just(mapToResponse(doc, "Documento encontrado"));
    }
    
    @Override
    public Mono<List<FirmaResponse>> obtenerDocumentosAspirante(UUID aspiranteId) {
        List<DocumentoFirmado> docs = documentoRepository.findByAspiranteId(aspiranteId);
        return Mono.just(docs.stream()
            .map(d -> mapToResponse(d, ""))
            .collect(Collectors.toList()));
    }
    
    @Override
    public Mono<ValidacionFirmaResponse> validarFirma(String folioDocumento) {
        DocumentoFirmado doc = documentoRepository.findByFolioDocumento(folioDocumento)
            .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
        
        SelloDigital sello = selloRepository.findByDocumentoFirmadoId(doc.getId())
            .orElse(null);
        
        boolean valida = doc.getEstatus() == EstatusFirma.FIRMADA && 
                         doc.getHashFirmado() != null && 
                         sello != null;
        
        return Mono.just(new ValidacionFirmaResponse(
            valida, folioDocumento, doc.getNivelFirma(),
            doc.getHashOriginal(), doc.getHashFirmado(),
            sello != null ? sello.getAutoridadTimestamp() : null,
            sello != null ? sello.getTimestampCertificado() : null,
            valida ? "Firma valida" : "Firma invalida o incompleta"
        ));
    }
    
    @Override
    public Mono<MetadataFirmaResponse> obtenerMetadata(String folioDocumento) {
        DocumentoFirmado doc = documentoRepository.findByFolioDocumento(folioDocumento)
            .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
        
        FirmaMetadata meta = metadataRepository.findByDocumentoFirmadoId(doc.getId())
            .orElseThrow(() -> new RuntimeException("Metadata no encontrada"));
        
        return Mono.just(new MetadataFirmaResponse(
            folioDocumento, doc.getNivelFirma(),
            meta.getIpOrigen(), meta.getUserAgent(), meta.getGeolocalizacion(),
            meta.getDispositivoId(), meta.getScoreCoincidenciaBiometrica(),
            meta.getCertificadoSubject(), meta.getCertificadoValidoHasta(),
            doc.getFechaFirma()
        ));
    }
    
    @Override
    public Mono<byte[]> obtenerPdfFirmado(String folioDocumento) {
        DocumentoFirmado doc = documentoRepository.findByFolioDocumento(folioDocumento)
            .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
        
        if (doc.getStoragePathFirmado() == null) {
            return Mono.error(new RuntimeException("PDF firmado no disponible"));
        }
        
        return Mono.fromCallable(() -> minioService.descargarDocumento(doc.getStoragePathFirmado()));
    }
    
    private FirmaResponse mapToResponse(DocumentoFirmado doc, String mensaje) {
        return new FirmaResponse(
            doc.getId(), doc.getFolioDocumento(), doc.getFolioAspirante(),
            doc.getNivelFirma(), doc.getEstatus(), doc.getNombreArchivo(),
            doc.getHashOriginal(), doc.getHashFirmado(),
            doc.getFechaSolicitud(), doc.getFechaFirma(), mensaje
        );
    }
}