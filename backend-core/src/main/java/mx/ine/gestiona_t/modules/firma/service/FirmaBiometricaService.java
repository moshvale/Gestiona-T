package mx.ine.gestiona_t.modules.firma.service;

import mx.ine.gestiona_t.modules.firma.dto.request.FirmarBiometricaRequest;
import mx.ine.gestiona_t.modules.firma.dto.response.FirmaResponse;
import mx.ine.gestiona_t.modules.firma.integration.BiometriaClient;
import mx.ine.gestiona_t.modules.firma.integration.dto.BiometriaResponse;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class FirmaBiometricaService {
    
    private static final Logger log = LoggerFactory.getLogger(FirmaBiometricaService.class);
    
    @Value("${biometria.threshold:0.85}")
    private double biometriaThreshold;
    
    private final BiometriaClient biometriaClient;
    private final TimestampService timestampService;
    private final PdfSignatureService pdfService;
    private final MinioFirmaService minioService;
    private final DocumentoFirmadoRepository documentoRepository;
    private final SelloDigitalRepository selloRepository;
    private final FirmaMetadataRepository metadataRepository;
    
    public FirmaBiometricaService(BiometriaClient biometriaClient, TimestampService timestampService,
                                   PdfSignatureService pdfService, MinioFirmaService minioService,
                                   DocumentoFirmadoRepository documentoRepository,
                                   SelloDigitalRepository selloRepository,
                                   FirmaMetadataRepository metadataRepository) {
        this.biometriaClient = biometriaClient;
        this.timestampService = timestampService;
        this.pdfService = pdfService;
        this.minioService = minioService;
        this.documentoRepository = documentoRepository;
        this.selloRepository = selloRepository;
        this.metadataRepository = metadataRepository;
    }
    
    @Transactional
    public Mono<FirmaResponse> firmar(FirmarBiometricaRequest request, String ip, String userAgent) {
        log.info("Iniciando firma biometrica para: {}", request.nombreArchivo());
        
        byte[] pdfOriginal = Base64.getDecoder().decode(request.contenidoDocumentoBase64());
        String hashOriginal = pdfService.calcularHash(pdfOriginal);
        
        DocumentoFirmado docInicial = new DocumentoFirmado();
        docInicial.setTipoDocumento(request.tipoDocumento());
        docInicial.setFolioAspirante(request.folioAspirante());
        docInicial.setNivelFirma(NivelFirma.BIOMETRICA_OTP);
        docInicial.setEstatus(EstatusFirma.EN_VALIDACION_BIOMETRIA);
        docInicial.setNombreArchivo(request.nombreArchivo());
        docInicial.setHashOriginal(hashOriginal);
        final DocumentoFirmado docGuardado = documentoRepository.save(docInicial);
        
        return biometriaClient.validarBiometria(request.selfieBase64(), request.curp())
            .flatMap(bioResp -> {
                if (!bioResp.coincide() || bioResp.scoreCoincidencia() < biometriaThreshold) {
                    docGuardado.setEstatus(EstatusFirma.RECHAZADA);
                    docGuardado.setMotivoRechazo("Biometria no coincide. Score: " + bioResp.scoreCoincidencia());
                    documentoRepository.save(docGuardado);
                    return Mono.just(crearResponse(docGuardado, "Validacion biometrica fallida"));
                }
                
                docGuardado.setEstatus(EstatusFirma.EN_VALIDACION_OTP);
                documentoRepository.save(docGuardado);
                
                return timestampService.solicitarTimestamp(hashOriginal)
                    .map(tsResp -> {
                        if (!tsResp.exitoso()) {
                            docGuardado.setEstatus(EstatusFirma.ERROR);
                            docGuardado.setMotivoRechazo("Error en timestamp");
                            documentoRepository.save(docGuardado);
                            return crearResponse(docGuardado, "Error en timestamp");
                        }
                        
                        byte[] pdfFirmado = pdfService.aplicarSelloDigital(
                            pdfOriginal, hashOriginal, tsResp.timestampToken(),
                            "BIOMETRICA:score=" + bioResp.scoreCoincidencia()
                        );
                        String hashFirmado = pdfService.calcularHash(pdfFirmado);
                        
                        String pathFirmado = minioService.guardarDocumentoFirmado(
                            pdfFirmado, docGuardado.getFolioDocumento(), request.nombreArchivo()
                        );
                        String pathOriginal = minioService.guardarDocumentoOriginal(
                            pdfOriginal, docGuardado.getFolioDocumento(), request.nombreArchivo()
                        );
                        
                        guardarSelloDigital(docGuardado.getId(), tsResp, hashOriginal);
                        guardarMetadata(docGuardado.getId(), bioResp, request.otp(), ip, userAgent);
                        
                        docGuardado.setStoragePathOriginal(pathOriginal);
                        docGuardado.setStoragePathFirmado(pathFirmado);
                        docGuardado.setHashFirmado(hashFirmado);
                        docGuardado.setEstatus(EstatusFirma.FIRMADA);
                        docGuardado.setFechaFirma(LocalDateTime.now());
                        documentoRepository.save(docGuardado);
                        
                        return crearResponse(docGuardado, "Firma biometrica completada");
                    });
            })
            .onErrorResume(e -> {
                log.error("Error en firma biometrica: {}", e.getMessage());
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
    
    private void guardarMetadata(UUID documentoId, BiometriaResponse bio, String otp, 
                                  String ip, String ua) {
        FirmaMetadata meta = new FirmaMetadata();
        meta.setDocumentoFirmadoId(documentoId);
        meta.setIpOrigen(ip != null ? ip : "desconocida");
        meta.setUserAgent(ua != null ? ua : "desconocido");
        meta.setScoreCoincidenciaBiometrica(bio.scoreCoincidencia());
        meta.setOtpHash(calcularHash(otp));
        metadataRepository.save(meta);
    }
    
    private String calcularHash(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            return "ERROR";
        }
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