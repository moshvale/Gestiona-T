package mx.ine.gestiona_t.modules.firma.service;

import mx.ine.gestiona_t.modules.firma.dto.request.FirmarFEARequest;
import mx.ine.gestiona_t.modules.firma.dto.response.FirmaResponse;
import mx.ine.gestiona_t.modules.firma.integration.SatFielClient;
import mx.ine.gestiona_t.modules.firma.integration.dto.SatFielResponse;
import mx.ine.gestiona_t.modules.firma.integration.dto.TimestampResponse;
import mx.ine.gestiona_t.modules.firma.model.DocumentoFirmado;
import mx.ine.gestiona_t.modules.firma.model.FirmaMetadata;
import mx.ine.gestiona_t.modules.firma.model.SelloDigital;
import mx.ine.gestiona_t.modules.firma.model.enums.EstatusFirma;
import mx.ine.gestiona_t.modules.firma.model.enums.NivelFirma;
import mx.ine.gestiona_t.modules.firma.repository.DocumentoFirmadoRepository;
import mx.ine.gestiona_t.modules.firma.repository.FirmaMetadataRepository;
import mx.ine.gestiona_t.modules.firma.repository.SelloDigitalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class FirmaFEAService {
    
    private static final Logger log = LoggerFactory.getLogger(FirmaFEAService.class);
    
    private final SatFielClient satClient;
    private final TimestampService timestampService;
    private final PdfSignatureService pdfService;
    private final MinioFirmaService minioService;
    private final DocumentoFirmadoRepository documentoRepository;
    private final SelloDigitalRepository selloRepository;
    private final FirmaMetadataRepository metadataRepository;
    
    public FirmaFEAService(SatFielClient satClient, TimestampService timestampService,
                            PdfSignatureService pdfService, MinioFirmaService minioService,
                            DocumentoFirmadoRepository documentoRepository,
                            SelloDigitalRepository selloRepository,
                            FirmaMetadataRepository metadataRepository) {
        this.satClient = satClient;
        this.timestampService = timestampService;
        this.pdfService = pdfService;
        this.minioService = minioService;
        this.documentoRepository = documentoRepository;
        this.selloRepository = selloRepository;
        this.metadataRepository = metadataRepository;
    }
    
    @Transactional
    public Mono<FirmaResponse> firmar(FirmarFEARequest request, String ip, String userAgent) {
        log.info("Iniciando firma FEA para: {}", request.nombreArchivo());
        
        byte[] pdfOriginal = Base64.getDecoder().decode(request.contenidoDocumentoBase64());
        String hashOriginal = pdfService.calcularHash(pdfOriginal);
        
        DocumentoFirmado docInicial = new DocumentoFirmado();
        docInicial.setTipoDocumento(request.tipoDocumento());
        docInicial.setFolioAspirante(request.folioAspirante());
        docInicial.setNivelFirma(NivelFirma.FEA_FIEL);
        docInicial.setEstatus(EstatusFirma.EN_VALIDACION_IDENTIDAD);
        docInicial.setNombreArchivo(request.nombreArchivo());
        docInicial.setHashOriginal(hashOriginal);
        final DocumentoFirmado docGuardado = documentoRepository.save(docInicial);
        
        return satClient.validarCertificado(request.certificadoFielBase64(), request.firmaFielBase64())
            .flatMap(satResp -> {
                if (!satResp.certificadoValido()) {
                    docGuardado.setEstatus(EstatusFirma.RECHAZADA);
                    docGuardado.setMotivoRechazo("Certificado FIEL invalido: " + satResp.mensaje());
                    documentoRepository.save(docGuardado);
                    return Mono.just(crearResponse(docGuardado, "Certificado FIEL invalido"));
                }
                
                docGuardado.setEstatus(EstatusFirma.EN_FIRMA_DIGITAL);
                documentoRepository.save(docGuardado);
                
                return timestampService.solicitarTimestamp(hashOriginal)
                    .map(tsResp -> {
                        if (!tsResp.exitoso()) {
                            docGuardado.setEstatus(EstatusFirma.ERROR);
                            docGuardado.setMotivoRechazo("Error en timestamp: " + tsResp.mensaje());
                            documentoRepository.save(docGuardado);
                            return crearResponse(docGuardado, "Error en timestamp");
                        }
                        
                        byte[] pdfFirmado = pdfService.aplicarSelloDigital(
                            pdfOriginal, hashOriginal, tsResp.timestampToken(), 
                            "FEA_FIEL:" + satResp.serial()
                        );
                        String hashFirmado = pdfService.calcularHash(pdfFirmado);
                        
                        String pathFirmado = minioService.guardarDocumentoFirmado(
                            pdfFirmado, docGuardado.getFolioDocumento(), request.nombreArchivo()
                        );
                        String pathOriginal = minioService.guardarDocumentoOriginal(
                            pdfOriginal, docGuardado.getFolioDocumento(), request.nombreArchivo()
                        );
                        
                        guardarSelloDigital(docGuardado.getId(), tsResp, hashOriginal);
                        guardarMetadata(docGuardado.getId(), satResp, ip, userAgent);
                        
                        docGuardado.setStoragePathOriginal(pathOriginal);
                        docGuardado.setStoragePathFirmado(pathFirmado);
                        docGuardado.setHashFirmado(hashFirmado);
                        docGuardado.setEstatus(EstatusFirma.FIRMADA);
                        docGuardado.setFechaFirma(LocalDateTime.now());
                        documentoRepository.save(docGuardado);
                        
                        return crearResponse(docGuardado, "Firma FEA completada exitosamente");
                    });
            })
            .onErrorResume(e -> {
                log.error("Error en firma FEA: {}", e.getMessage());
                docGuardado.setEstatus(EstatusFirma.ERROR);
                docGuardado.setMotivoRechazo(e.getMessage());
                documentoRepository.save(docGuardado);
                return Mono.just(crearResponse(docGuardado, "Error: " + e.getMessage()));
            });
    }
    
    private void guardarSelloDigital(UUID documentoId, TimestampResponse ts, String hash) {
        SelloDigital sello = new SelloDigital();
        sello.setDocumentoFirmadoId(documentoId);
        sello.setTimestampToken(ts.timestampToken() != null ? ts.timestampToken() : "");
        sello.setTimestampCertificado(ts.timestampCertificado() != null 
            ? timestampService.parseTimestamp(ts.timestampCertificado()) : LocalDateTime.now());
        sello.setAutoridadTimestamp(ts.autoridad() != null ? ts.autoridad() : "TSA-INE");
        sello.setHashDocumento(hash);
        sello.setAlgoritmoHash("SHA-256");
        sello.setAlgoritmoFirma("SHA256withRSA");
        selloRepository.save(sello);
    }
    
    private void guardarMetadata(UUID documentoId, SatFielResponse sat, String ip, String ua) {
        FirmaMetadata meta = new FirmaMetadata();
        meta.setDocumentoFirmadoId(documentoId);
        meta.setIpOrigen(ip != null ? ip : "desconocida");
        meta.setUserAgent(ua != null ? ua : "desconocido");
        meta.setCertificadoSerial(sat.serial());
        meta.setCertificadoSubject(sat.subject());
        meta.setCertificadoValidoHasta(sat.validoHasta());
        metadataRepository.save(meta);
    }
    
    private FirmaResponse crearResponse(DocumentoFirmado doc, String mensaje) {
        return new FirmaResponse(
            doc.getId(), doc.getFolioDocumento(), doc.getFolioAspirante(),
            doc.getNivelFirma(), doc.getEstatus(), doc.getNombreArchivo(),
            doc.getHashOriginal(), doc.getHashFirmado(),
            doc.getFechaSolicitud(), doc.getFechaFirma(), mensaje
        );
    }
}