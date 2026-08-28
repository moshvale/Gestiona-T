package mx.ine.gestiona_t.modules.documentos.service;

import mx.ine.gestiona_t.modules.auditoria.annotation.Auditable;
import mx.ine.gestiona_t.modules.auditoria.model.enums.NivelSeveridad;
import mx.ine.gestiona_t.modules.documentos.dto.request.DictamenRevisionRequest;
import mx.ine.gestiona_t.modules.documentos.dto.response.*;
import mx.ine.gestiona_t.modules.documentos.integration.DocumentosBackendAIClient;
import mx.ine.gestiona_t.modules.documentos.integration.dto.AutenticidadResponse;
import mx.ine.gestiona_t.modules.documentos.integration.dto.OcrDocumentoResponse;
import mx.ine.gestiona_t.modules.documentos.model.Documento;
import mx.ine.gestiona_t.modules.documentos.model.enums.EstatusDocumento;
import mx.ine.gestiona_t.modules.documentos.model.enums.TipoDocumento;
import mx.ine.gestiona_t.modules.documentos.repository.DocumentoRepository;
import mx.ine.gestiona_t.modules.expedientes.model.ExpedienteLaboral;
import mx.ine.gestiona_t.modules.expedientes.repository.ExpedienteLaboralRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentoServiceImpl implements DocumentoService {

    private static final Logger log = LoggerFactory.getLogger(DocumentoServiceImpl.class);

    private final DocumentoRepository documentoRepository;
    private final MinioDocumentosService minioService;
    private final ClasificadorDocumentoService clasificadorService;
    private final ValidacionTipoAService validacionAService;
    private final ValidacionTipoBService validacionBService;
    private final ValidacionTipoCService validacionCService;
    private final ExpedienteService expedienteService;
    private final DocumentosBackendAIClient aiClient;
    private final ExpedienteLaboralRepository expedienteLaboralRepository;

    public DocumentoServiceImpl(DocumentoRepository documentoRepository,
                                MinioDocumentosService minioService,
                                ClasificadorDocumentoService clasificadorService,
                                ValidacionTipoAService validacionAService,
                                ValidacionTipoBService validacionBService,
                                ValidacionTipoCService validacionCService,
                                ExpedienteService expedienteService,
                                DocumentosBackendAIClient aiClient,
                                ExpedienteLaboralRepository expedienteLaboralRepository) {
        this.documentoRepository = documentoRepository;
        this.minioService = minioService;
        this.clasificadorService = clasificadorService;
        this.validacionAService = validacionAService;
        this.validacionBService = validacionBService;
        this.validacionCService = validacionCService;
        this.expedienteService = expedienteService;
        this.aiClient = aiClient;
        this.expedienteLaboralRepository = expedienteLaboralRepository;
    }

    @Override
    @Transactional
    public Mono<UploadResponse> cargarSoporteExpediente(MultipartFile file, UUID expedienteLaboralId) {
        if (file == null || file.isEmpty() || !"application/pdf".equalsIgnoreCase(file.getContentType())) {
            return Mono.error(new IllegalArgumentException("El soporte debe ser un archivo PDF."));
        }

        ExpedienteLaboral expediente = expedienteLaboralRepository.findById(expedienteLaboralId)
                .orElseThrow(() -> new IllegalArgumentException("Expediente laboral no encontrado."));
        try {
            documentoRepository.findFirstByExpedienteLaboral_IdOrderByFechaCargaDesc(expedienteLaboralId)
                    .ifPresent(anterior -> {
                        minioService.deleteDocumento(anterior.getStoragePath());
                        documentoRepository.delete(anterior);
                    });

            String storagePath = minioService.uploadDocumento(file, expediente.getAspiranteId());
            Documento soporte = new Documento();
            soporte.setAspiranteId(expediente.getAspiranteId());
            soporte.setFolio(expediente.getAspiranteId().toString());
            soporte.setNombreArchivo(file.getOriginalFilename() != null ? file.getOriginalFilename() : "soporte-expediente.pdf");
            soporte.setStoragePath(storagePath);
            soporte.setContentType("application/pdf");
            soporte.setTamanoBytes(file.getSize());
            soporte.setEstatus(EstatusDocumento.CARGADO);
            soporte.setTipoDocumento(TipoDocumento.SOPORTE_EXPEDIENTE_LABORAL);
            soporte.setTipoValidacion(clasificadorService.determinarTipoValidacion(TipoDocumento.SOPORTE_EXPEDIENTE_LABORAL));
            soporte.setExpedienteLaboral(expediente);
            Documento guardado = documentoRepository.save(soporte);
            return Mono.just(new UploadResponse(guardado.getId(), soporte.getFolio(), soporte.getNombreArchivo(),
                    soporte.getTamanoBytes(), storagePath, "Soporte cargado exitosamente"));
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Error al cargar el soporte del expediente", e));
        }
    }

    @Override
    @Transactional
    public Mono<UploadResponse> cargarDocumento(MultipartFile file, UUID aspiranteId, 
                                                String folio, String tipoDocumentoStr, boolean esTambienDomicilio) {
        log.info("Cargando documento para aspirante {}: {} (EsDomicilio: {})", aspiranteId, file.getOriginalFilename(), esTambienDomicilio);
        try {
            TipoDocumento tipo = tipoDocumentoStr != null && !tipoDocumentoStr.isEmpty()
                ? convertirTipoDocumento(tipoDocumentoStr)
                : clasificadorService.detectarTipoDesdeNombre(file.getOriginalFilename());

            var existentesPorTipo = documentoRepository.findByAspiranteIdAndTipoDocumento(aspiranteId, tipo);
            if (existentesPorTipo != null && !existentesPorTipo.isEmpty()) {
                Documento existente = existentesPorTipo.get(0);
                log.info("⚠️ Ya existe un documento del tipo {} para aspirante {}. Retornando existente: {}", tipo, aspiranteId, existente.getId());
                return Mono.just(new UploadResponse(existente.getId(), folio, existente.getNombreArchivo(), existente.getTamanoBytes(), existente.getStoragePath(), "Documento ya registrado"));
            }

            String storagePath = minioService.uploadDocumento(file, aspiranteId);

            var existenteList = documentoRepository.findByAspiranteIdAndStoragePath(aspiranteId, storagePath);
            if (existenteList != null && !existenteList.isEmpty()) {
                Documento existente = existenteList.get(0);
                log.info("⚠️ Documento ya existe para aspirante {} y ruta {}. Retornando existente: {}", aspiranteId, storagePath, existente.getId());
                return Mono.just(new UploadResponse(existente.getId(), folio, existente.getNombreArchivo(), existente.getTamanoBytes(), existente.getStoragePath(), "Documento ya existente"));
            }

            Documento docPrincipal = new Documento();
            docPrincipal.setAspiranteId(aspiranteId);
            docPrincipal.setFolio(folio);
            docPrincipal.setNombreArchivo(file.getOriginalFilename());
            docPrincipal.setStoragePath(storagePath);
            docPrincipal.setContentType(file.getContentType());
            docPrincipal.setTamanoBytes(file.getSize());
            docPrincipal.setEstatus(EstatusDocumento.CARGADO);
            docPrincipal.setTipoDocumento(tipo);
            docPrincipal.setTipoValidacion(clasificadorService.determinarTipoValidacion(tipo));

            try {
                docPrincipal = documentoRepository.save(docPrincipal);
            } catch (DataIntegrityViolationException dive) {
                log.warn("⚠️ Colisión al insertar documento principal, intentando recuperar existente: {}", dive.getMessage());
                var existentes = documentoRepository.findByAspiranteIdAndStoragePath(aspiranteId, storagePath);
                if (existentes != null && !existentes.isEmpty()) {
                    docPrincipal = existentes.get(0);
                } else {
                    throw dive;
                }
            }

            if (esTambienDomicilio && tipo == TipoDocumento.IDENTIFICACION_OFICIAL) {
                Documento docDomicilio = new Documento();
                docDomicilio.setAspiranteId(aspiranteId);
                docDomicilio.setFolio(folio);
                docDomicilio.setTipoDocumento(TipoDocumento.COMPROBANTE_DOMICILIO);
                docDomicilio.setTipoValidacion(clasificadorService.determinarTipoValidacion(TipoDocumento.COMPROBANTE_DOMICILIO));
                docDomicilio.setEstatus(EstatusDocumento.CARGADO);
                docDomicilio.setNombreArchivo(file.getOriginalFilename() + " (Copia para Domicilio)");
                docDomicilio.setStoragePath(storagePath);
                docDomicilio.setContentType(file.getContentType());
                docDomicilio.setTamanoBytes(file.getSize());
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("esDocumentoEspejo", true);
                metadata.put("documentoOriginalId", docPrincipal.getId().toString());
                docDomicilio.setMetadataValidacion(metadata);
                try {
                    documentoRepository.save(docDomicilio);
                } catch (DataIntegrityViolationException dive) {
                    log.warn("⚠️ Colisión al insertar documento espejo, recuperando existente: {}", dive.getMessage());
                }
                log.info("✅ Documento dual creado: INE también cuenta como Comprobante de Domicilio (ID Espejo: {})", docDomicilio.getId());
                iniciarValidacionAutomatica(docPrincipal.getId());
                iniciarValidacionAutomatica(docDomicilio.getId());
            } else {
                iniciarValidacionAutomatica(docPrincipal.getId());
            }

            expedienteService.obtenerOCrear(aspiranteId, folio);
            expedienteService.actualizarExpediente(aspiranteId);

            return Mono.just(new UploadResponse(docPrincipal.getId(), folio, file.getOriginalFilename(),
                file.getSize(), storagePath, "Documento cargado exitosamente"));
        } catch (Exception e) {
            log.error("Error al cargar documento: {}", e.getMessage());
            return Mono.error(new RuntimeException("Error al cargar documento", e));
        }
    }

    private TipoDocumento convertirTipoDocumento(String tipoDocumentoStr) {
        return switch (tipoDocumentoStr) {
            case "COMPROBANTE_ESTUDIOS" -> TipoDocumento.CERTIFICADO_BACHILLERATO;
            case "3 Cartas de recomendación" -> TipoDocumento.TRES_CARTAS_RECOMENDACION;
            case "Formato de inscripción al FONAC" -> TipoDocumento.FORMATO_INSCRIPCION_FONAC;
            case "Formato de gastos funerarios" -> TipoDocumento.FORMATO_GASTOS_FUNERARIOS;
            case "Formato de seguro de vida institucional" -> TipoDocumento.FORMATO_SEGURO_VIDA_INSTITUCIONAL;
            case "Alta del ISSSTE" -> TipoDocumento.ALTA_ISSSTE;
            default -> TipoDocumento.valueOf(tipoDocumentoStr);
        };
    }

    @Async
    @Transactional
    public void iniciarValidacionAutomatica(UUID documentoId) {
        try {
            log.info("🤖 Iniciando validación automática en background para documento: {}", documentoId);
            validarDocumento(documentoId).block();
            log.info("✅ Validación automática completada exitosamente para documento: {}", documentoId);
        } catch (Exception e) {
            log.error("❌ Error en validación automática de documento {}: {}", documentoId, e.getMessage(), e);
            documentoRepository.findById(documentoId).ifPresent(doc -> {
                doc.setEstatus(EstatusDocumento.EN_REVISION_MANUAL);
                doc.setMotivoRechazo("Error en el proceso de validación automática: " + e.getMessage());
                documentoRepository.save(doc);
                expedienteService.actualizarExpediente(doc.getAspiranteId());
            });
        }
    }

    @Override
    @Transactional
    public Mono<ValidacionResponse> validarDocumento(UUID documentoId) {
        Documento doc = documentoRepository.findById(documentoId)
            .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        log.info("Iniciando validación automática de documento {} - Tipo: {}", documentoId, doc.getTipoValidacion());
        doc.setEstatus(EstatusDocumento.EN_OCR);
        documentoRepository.save(doc);

        try {
            byte[] archivo = minioService.downloadDocumento(doc.getStoragePath());

            log.info("Enviando documento a IA para OCR...");
            OcrDocumentoResponse ocr = aiClient.procesarDocumento(archivo, doc.getNombreArchivo())
                .timeout(java.time.Duration.ofSeconds(30))
                .onErrorResume(e -> {
                    log.warn("⚠️ OCR falló o tardó demasiado: {}", e.getMessage());
                    return Mono.just(new OcrDocumentoResponse(null, 0.0, null, null, "OCR no disponible"));
                })
                .block();

            if (ocr != null && ocr.textoExtraido() != null) { 
                doc.setTextoExtraido(ocr.textoExtraido());
                if (ocr.camposExtraidos() != null && !ocr.camposExtraidos().isEmpty()) {
                    Map<String, Object> ocrMeta = new HashMap<>();
                    ocrMeta.put("tipoDetectado", ocr.tipoDetectado());
                    ocrMeta.put("confianza", ocr.confianza());
                    ocrMeta.put("camposExtraidos", ocr.camposExtraidos());
                    doc.getMetadataValidacion().put("ocr", ocrMeta);
                }
            }

            if (ocr != null && (ocr.mensaje() == null || !ocr.mensaje().toLowerCase().contains("error"))) { 
                log.info("Enviando documento a IA para validación de autenticidad...");
                AutenticidadResponse autenticidad = aiClient.validarAutenticidad(archivo, doc.getNombreArchivo())
                    .timeout(java.time.Duration.ofSeconds(30))
                    .onErrorResume(e -> {
                        log.warn("⚠️ Validación de autenticidad falló: {}", e.getMessage());
                        return Mono.just(new AutenticidadResponse(0.0, false, new String[]{}, "Servicio de autenticidad no disponible"));
                    })
                    .block();

                if (autenticidad != null) {
                    doc.setScoreAutenticidad(autenticidad.scoreAutenticidad());
                    Map<String, Object> authMeta = new HashMap<>();
                    authMeta.put("score", autenticidad.scoreAutenticidad());
                    authMeta.put("sospechoso", autenticidad.sospechoso());
                    authMeta.put("mensaje", autenticidad.mensaje());
                    authMeta.put("alteracionesDetectadas", 
                        autenticidad.alteracionesDetectadas() != null 
                            ? java.util.Arrays.asList(autenticidad.alteracionesDetectadas()) 
                            : java.util.Collections.emptyList());
                    doc.getMetadataValidacion().put("autenticidad", authMeta);

                    if (autenticidad.sospechoso()) {
                        doc.setEstatus(EstatusDocumento.RECHAZADO);
                        String alteracionesStr = autenticidad.alteracionesDetectadas() != null 
                            ? String.join(", ", autenticidad.alteracionesDetectadas()) 
                            : "Alteraciones detectadas";
                        doc.setMotivoRechazo("El sistema de IA detectó posibles anomalías: " + alteracionesStr);
                        documentoRepository.save(doc);
                        expedienteService.actualizarExpediente(doc.getAspiranteId());
                        return Mono.just(new ValidacionResponse(
                            doc.getId(), false, doc.getMotivoRechazo(), doc.getScoreAutenticidad(), "IA_AUTENTICIDAD"
                        ));
                    }
                }
            } else if (ocr != null && ocr.mensaje() != null) {
                log.warn("OCR reportó un problema: {}", ocr.mensaje());
            }

            Mono<Map<String, Object>> resultadoMono = switch (doc.getTipoValidacion()) {
                case TIPO_A -> validacionAService.validar(doc, archivo)
                    .timeout(java.time.Duration.ofSeconds(15))
                    .onErrorResume(e -> {
                        log.error("❌ Validación Tipo A falló o tardó demasiado: {}", e.getMessage());
                        doc.setEstatus(EstatusDocumento.EN_REVISION_MANUAL);
                        doc.setMotivoRechazo("La validación con la API oficial (" + doc.getTipoDocumento() + ") no respondió. Requiere revisión manual.");
                        Map<String, Object> respaldo = new HashMap<>();
                        respaldo.put("exitoso", false);
                        respaldo.put("mensaje", "Validación externa no disponible. Enviado a revisión manual.");
                        respaldo.put("metodo", "REVISION_MANUAL");
                        return Mono.just(respaldo);
                    });
                case TIPO_B -> validacionBService.validar(doc, archivo)
                    .timeout(java.time.Duration.ofSeconds(30))
                    .onErrorResume(e -> {
                        log.error("❌ Validación Tipo B falló: {}", e.getMessage());
                        doc.setEstatus(EstatusDocumento.EN_REVISION_MANUAL);
                        doc.setMotivoRechazo("Error en validación Tipo B. Requiere revisión manual.");
                        Map<String, Object> respaldo = new HashMap<>();
                        respaldo.put("exitoso", false);
                        respaldo.put("mensaje", "Error en validación Tipo B.");
                        respaldo.put("metodo", "REVISION_MANUAL");
                        return Mono.just(respaldo);
                    });
                case TIPO_C -> Mono.fromCallable(() -> validacionCService.encolarParaRevision(doc, 5));
            };

            return resultadoMono.map(resultado -> {
                boolean exitoso = (Boolean) resultado.getOrDefault("exitoso", false);
                if (exitoso) {
                    doc.setEstatus(EstatusDocumento.VALIDADO_MANUAL); 
                } else if (doc.getEstatus() != EstatusDocumento.EN_REVISION_MANUAL) {
                    doc.setEstatus(EstatusDocumento.RECHAZADO);
                    doc.setMotivoRechazo((String) resultado.getOrDefault("mensaje", "No cumple con los requisitos"));
                }
                documentoRepository.save(doc);
                expedienteService.actualizarExpediente(doc.getAspiranteId());
                return new ValidacionResponse(
                    doc.getId(),
                    exitoso,
                    (String) resultado.getOrDefault("mensaje", ""),
                    doc.getScoreAutenticidad(),
                    (String) resultado.getOrDefault("metodo", "DESCONOCIDO")
                );
            });
        } catch (Exception e) {
            log.error("Error en el proceso de validación automática: {}", e.getMessage(), e);
            doc.setEstatus(EstatusDocumento.EN_REVISION_MANUAL);
            doc.setMotivoRechazo("Error interno en el proceso de validación: " + e.getMessage());
            documentoRepository.save(doc);
            expedienteService.actualizarExpediente(doc.getAspiranteId());
            return Mono.just(new ValidacionResponse(
                doc.getId(), false, doc.getMotivoRechazo(), doc.getScoreAutenticidad(), "ERROR_SISTEMA"
            ));
        }
    }

    @Override
    public Mono<List<DocumentoResponse>> obtenerDocumentosAspirante(UUID aspiranteId) {
        log.info("📂 Obteniendo documentos para aspiranteId: {}", aspiranteId);
        List<Documento> docs = documentoRepository.findByAspiranteIdOrderByFechaCargaDesc(aspiranteId);
        log.info("✅ Documentos encontrados para aspiranteId {}: {}", aspiranteId, docs.size());
        return Mono.just(docs.stream().map(this::mapToResponse).collect(Collectors.toList()));
    }

    @Override
    public Mono<List<DocumentoResponse>> obtenerDocumentosPorFolio(String folio) {
        log.info("📂 Obteniendo documentos para folio: {}", folio);
        List<Documento> docs = documentoRepository.findByFolio(folio);
        log.info("✅ Documentos encontrados para folio {}: {}", folio, docs.size());
        
        // ✅ DEBUG: Si no encuentra documentos, buscar por aspiranteId por si acaso
        if (docs.isEmpty()) {
            try {
                UUID aspiranteId = UUID.fromString(folio);
                log.warn("⚠️ No se encontraron documentos con folio '{}'. Intentando buscar por aspiranteId...", folio);
                docs = documentoRepository.findByAspiranteIdOrderByFechaCargaDesc(aspiranteId);
                log.info("✅ Documentos encontrados usando aspiranteId {}: {}", aspiranteId, docs.size());
            } catch (IllegalArgumentException e) {
                log.debug("El folio '{}' no es un UUID válido, no se puede buscar por aspiranteId", folio);
            }
        }
        
        return Mono.just(docs.stream().map(this::mapToResponse).collect(Collectors.toList()));
    }

    @Override
    public Mono<DocumentoResponse> obtenerDocumento(UUID documentoId) {
        Documento doc = documentoRepository.findById(documentoId).orElseThrow(() -> new RuntimeException("Documento no encontrado"));
        return Mono.just(mapToResponse(doc));
    }

    @Override
    @Transactional
    @Auditable(
        categoria = "DOCUMENTOS", 
        tipo = "ELIMINACION", 
        severidad = NivelSeveridad.WARNING, 
        recurso = "Documento", 
        descripcion = "Eliminación de documento del expediente"
    )
    public Mono<Void> eliminarDocumento(UUID documentoId) {
        Documento doc = documentoRepository.findById(documentoId)
            .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
        minioService.deleteDocumento(doc.getStoragePath());
        documentoRepository.delete(doc);
        expedienteService.actualizarExpediente(doc.getAspiranteId());
        return Mono.empty();
    }

    @Override
    public Mono<byte[]> descargarDocumento(UUID documentoId) {
        Documento doc = documentoRepository.findById(documentoId).orElseThrow(() -> new RuntimeException("Documento no encontrado"));
        return Mono.fromCallable(() -> minioService.downloadDocumento(doc.getStoragePath()));
    }

    @Override
    @Transactional
    @Auditable(
        categoria = "DOCUMENTOS", 
        tipo = "VALIDACION_MANUAL", 
        severidad = NivelSeveridad.INFO, 
        recurso = "Documento", 
        descripcion = "Validación o rechazo manual de documento por analista"
    )
    public Mono<ValidacionResponse> validarDocumentoManual(UUID id, String estatus, String motivo) {
        Documento documento = documentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        EstatusDocumento nuevoEstatus;
        String estatusLimpio = estatus != null ? estatus.trim().toUpperCase() : "";

        if ("VALIDADO".equals(estatusLimpio) || "VALIDADO_MANUAL".equals(estatusLimpio)) {
            nuevoEstatus = EstatusDocumento.VALIDADO_MANUAL;
        } else if ("RECHAZADO".equals(estatusLimpio)) {
            nuevoEstatus = EstatusDocumento.RECHAZADO;
        } else {
            return Mono.error(new RuntimeException("Estatus no válido. Debe ser VALIDADO o RECHAZADO. Recibido: '" + estatusLimpio + "'"));
        }

        documento.setEstatus(nuevoEstatus);
        if ("RECHAZADO".equals(estatusLimpio)) {
            documento.setMotivoRechazo(motivo != null && !motivo.trim().isEmpty() ? motivo.trim() : "Sin motivo especificado");
        } else {
            documento.setMotivoRechazo(null);
        }
        documento.setFechaValidacion(LocalDateTime.now());

        return Mono.fromCallable(() -> {
            documentoRepository.save(documento);
            expedienteService.actualizarExpediente(documento.getAspiranteId());
            return new ValidacionResponse(
                documento.getId(),
                nuevoEstatus == EstatusDocumento.VALIDADO_MANUAL,
                nuevoEstatus == EstatusDocumento.VALIDADO_MANUAL ? "Documento validado correctamente" : "Documento rechazado: " + documento.getMotivoRechazo(),
                documento.getScoreAutenticidad(),
                "MANUAL"
            );
        });
    }

    @Override
    @Transactional
    public Mono<ValidacionResponse> validarTodosDocumentos(String folio) {
        List<Documento> docs = documentoRepository.findByFolio(folio);
        if (docs.isEmpty()) return Mono.error(new RuntimeException("No hay documentos para validar"));

        int validados = 0, rechazados = 0;
        for (Documento doc : docs) {
            if (doc.getEstatus() == EstatusDocumento.CARGADO || doc.getEstatus() == EstatusDocumento.EN_OCR) {
                try {
                    ValidacionResponse r = validarDocumento(doc.getId()).block();
                    if (r != null && r.exitoso()) validados++; else rechazados++;
                } catch (Exception e) { log.error("Error validando documento {}: {}", doc.getId(), e.getMessage()); }
            }
        }
        return Mono.just(new ValidacionResponse(docs.get(0).getId(), rechazados == 0,
            "Validacion completada. Validados: " + validados + ", Rechazados: " + rechazados, null, "VALIDACION_MASIVA"));
    }

    @Override
    public Mono<List<RevisionResponse>> obtenerRevisionesPendientes() {
        return Mono.fromCallable(() -> documentoRepository.findAll().stream()
            .filter(d -> d.getEstatus() == EstatusDocumento.EN_REVISION_MANUAL)
            .map(this::mapToRevisionResponse).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    @Auditable(
        categoria = "DOCUMENTOS", 
        tipo = "DICTAMEN_REVISION", 
        severidad = NivelSeveridad.INFO, 
        recurso = "RevisionManual", 
        descripcion = "Dictamen de revisión manual de documento"
    )
    public Mono<RevisionResponse> dictaminarRevision(Long revisionId, UUID analistaId, DictamenRevisionRequest request) {
        validacionCService.dictaminar(revisionId, analistaId, request.estatus(), request.dictamen(), request.motivo());
        return Mono.just(new RevisionResponse(
            revisionId, null, analistaId, request.estatus(),
            request.dictamen(), request.motivo(), null, null, LocalDateTime.now()
        ));
    }

    @Override
    public Mono<List<DocumentoResponse>> obtenerDocumentosPorAspiranteId(UUID aspiranteId) {
    log.info("📂 Obteniendo documentos para aspiranteId: {}", aspiranteId);
    List<Documento> docs = documentoRepository.findByAspiranteIdOrderByFechaCargaDesc(aspiranteId);
    return Mono.just(docs.stream().map(this::mapToResponse).collect(Collectors.toList()));
    }

    private DocumentoResponse mapToResponse(Documento d) {
        return new DocumentoResponse(d.getId(), d.getAspiranteId(), d.getFolio(), d.getTipoDocumento(), d.getTipoValidacion(), d.getEstatus(), d.getNombreArchivo(), d.getScoreAutenticidad(), d.getMotivoRechazo(), d.getFechaCarga(), d.getFechaValidacion(), d.getExpedienteLaboral() != null ? d.getExpedienteLaboral().getId() : null);
    }

    private RevisionResponse mapToRevisionResponse(Documento d) {
        return new RevisionResponse(null, d.getId(), d.getAnalistaId(), null, null, null, null, null, null);
    }
}