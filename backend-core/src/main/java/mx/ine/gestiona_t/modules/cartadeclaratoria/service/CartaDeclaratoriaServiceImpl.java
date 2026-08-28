package mx.ine.gestiona_t.modules.cartadeclaratoria.service;

import mx.ine.gestiona_t.modules.auditoria.annotation.Auditable;
import mx.ine.gestiona_t.modules.auditoria.model.enums.NivelSeveridad;
import mx.ine.gestiona_t.modules.cartadeclaratoria.dto.request.AceptarBloqueRequest;
import mx.ine.gestiona_t.modules.cartadeclaratoria.dto.request.FirmarCartaRequest;
import mx.ine.gestiona_t.modules.cartadeclaratoria.dto.response.*;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.AceptacionBloque;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.BloqueDeclaratorio;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.CartaDeclaratoria;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.enums.EstatusCarta;
import mx.ine.gestiona_t.modules.cartadeclaratoria.repository.AceptacionBloqueRepository;
import mx.ine.gestiona_t.modules.cartadeclaratoria.repository.BloqueDeclaratorioRepository;
import mx.ine.gestiona_t.modules.cartadeclaratoria.repository.CartaDeclaratoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CartaDeclaratoriaServiceImpl implements CartaDeclaratoriaService {

    private static final Logger log = LoggerFactory.getLogger(CartaDeclaratoriaServiceImpl.class);

    private final CartaDeclaratoriaRepository cartaRepository;
    private final BloqueDeclaratorioRepository bloqueRepository;
    private final AceptacionBloqueRepository aceptacionRepository;
    private final BloquesService bloquesService;
    private final ValidacionExternaService validacionService;
    private final PdfGenerationService pdfService;

    public CartaDeclaratoriaServiceImpl(CartaDeclaratoriaRepository cartaRepository,
                                        BloqueDeclaratorioRepository bloqueRepository,
                                        AceptacionBloqueRepository aceptacionRepository,
                                        BloquesService bloquesService,
                                        ValidacionExternaService validacionService,
                                        PdfGenerationService pdfService) {
        this.cartaRepository = cartaRepository;
        this.bloqueRepository = bloqueRepository;
        this.aceptacionRepository = aceptacionRepository;
        this.bloquesService = bloquesService;
        this.validacionService = validacionService;
        this.pdfService = pdfService;
    }

    @Override
    @Transactional
    @Auditable(
        categoria = "CARTA_DECLARATORIA", 
        tipo = "INICIO_CARTA", 
        severidad = NivelSeveridad.INFO, 
        recurso = "CartaDeclaratoria", 
        descripcion = "Se inició el proceso de carta declaratoria para el aspirante"
    )
    public Mono<CartaDeclaratoriaResponse> iniciarCarta(UUID aspiranteId, String folio) {
        log.info("Iniciando carta declaratoria para aspirante: {}", aspiranteId);
        
        if (cartaRepository.findFirstByAspiranteIdOrderByCreatedAtDesc(aspiranteId).isPresent()) {
            return Mono.error(new RuntimeException("El aspirante ya tiene una carta iniciada"));
        }
        
        CartaDeclaratoria carta = new CartaDeclaratoria();
        carta.setAspiranteId(aspiranteId);
        carta.setFolio(folio);
        
        String folioCarta = "CD-" + 
            folio.substring(0, Math.min(8, folio.length())).toUpperCase() + 
            "-" + System.currentTimeMillis();
            
        carta.setFolioCarta(folioCarta);
        carta.setVersion("1.0.0");
        carta.setEstatus(EstatusCarta.INICIADA);
        carta = cartaRepository.save(carta);
        
        return Mono.just(mapToResponse(carta));
    }

    @Override
    public Mono<CartaDeclaratoriaResponse> obtenerCarta(String folio) {
        CartaDeclaratoria carta = cartaRepository.findFirstByFolioOrderByCreatedAtDesc(folio)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carta no encontrada"));
        return Mono.just(mapToResponse(carta));
    }

    // ✅ NUEVO: Método para buscar carta por ID del aspirante (uso exclusivo de analistas)
    @Override
    public Mono<CartaDeclaratoriaResponse> obtenerCartaPorAspiranteId(UUID aspiranteId) {
        log.info("Buscando carta para aspiranteId: {}", aspiranteId);
        CartaDeclaratoria carta = cartaRepository.findFirstByAspiranteIdOrderByCreatedAtDesc(aspiranteId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carta no encontrada para este aspirante"));
        return Mono.just(mapToResponse(carta));
    }

    @Override
    public Mono<List<BloqueResponse>> obtenerBloques(String folio) {
        CartaDeclaratoria carta = cartaRepository.findFirstByFolioOrderByCreatedAtDesc(folio)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carta no encontrada"));
        return Mono.just(bloquesService.obtenerBloquesConEstatus(carta.getId()));
    }

    @Override
    @Transactional
    @Auditable(
        categoria = "CARTA_DECLARATORIA", 
        tipo = "ACEPTACION_BLOQUE", 
        severidad = NivelSeveridad.INFO, 
        recurso = "BloqueDeclaratorio", 
        descripcion = "El aspirante aceptó un bloque declaratorio específico bajo protesta de decir verdad"
    )
    public Mono<AceptacionResponse> aceptarBloque(String folio, AceptarBloqueRequest request,
                                                   String ip, String userAgent) {
        log.info("Aceptando bloque {} de carta {}", request.bloqueId(), folio);
        
        CartaDeclaratoria carta = cartaRepository.findFirstByFolioOrderByCreatedAtDesc(folio)
            .orElseThrow(() -> new RuntimeException("Carta no encontrada"));
            
        BloqueDeclaratorio bloque = bloqueRepository.findById(request.bloqueId())
            .orElseThrow(() -> new RuntimeException("Bloque no encontrado"));
            
        if (!request.aceptado()) {
            return Mono.error(new RuntimeException("El bloque debe ser aceptado"));
        }

        AceptacionBloque aceptacionExistente = aceptacionRepository
                .findByCartaIdAndBloqueId(carta.getId(), bloque.getId())
                .orElse(null);
        if (aceptacionExistente != null && aceptacionExistente.isAceptado()) {
            return Mono.just(new AceptacionResponse(
                    aceptacionExistente.getId(),
                    aceptacionExistente.getBloqueId(),
                    true,
                    aceptacionExistente.getTimestampAceptacion(),
                    aceptacionExistente.getHashTextoBloque()
            ));
        }
        
        AceptacionBloque aceptacion = new AceptacionBloque();
        aceptacion.setCartaId(carta.getId());
        aceptacion.setBloqueId(bloque.getId());
        aceptacion.setAceptado(true);
        aceptacion.setTimestampAceptacion(LocalDateTime.now());
        aceptacion.setIpOrigen(ip != null ? ip : "desconocida");
        aceptacion.setUserAgent(userAgent != null ? userAgent : "desconocido");
        aceptacion.setHashTextoBloque(calcularHash(bloque.getTexto()));
        aceptacion = aceptacionRepository.save(aceptacion);
        
        carta.setEstatus(EstatusCarta.EN_ACEPTACION_BLOQUES);
        cartaRepository.save(carta);
        
        return Mono.just(new AceptacionResponse(
                aceptacion.getId(),
                aceptacion.getBloqueId(),
                aceptacion.isAceptado(),
                aceptacion.getTimestampAceptacion(),
                aceptacion.getHashTextoBloque()
        ));
    }

    @Override
    @Transactional
    @Auditable(
        categoria = "CARTA_DECLARATORIA", 
        tipo = "ACEPTACION_COMPLETA", 
        severidad = NivelSeveridad.INFO, 
        recurso = "CartaDeclaratoria", 
        descripcion = "El aspirante aceptó todos los bloques declaratorios"
    )
    public Mono<CartaDeclaratoriaResponse> aceptarTodosBloques(String folio, String ip, String userAgent) {
        log.info("Aceptando todos los bloques de carta: {}", folio);
        
        CartaDeclaratoria carta = cartaRepository.findFirstByFolioOrderByCreatedAtDesc(folio)
            .orElseThrow(() -> new RuntimeException("Carta no encontrada"));
            
        List<BloqueDeclaratorio> bloques = bloqueRepository.findByActivoTrueOrderByOrdenAsc();
        
        for (BloqueDeclaratorio bloque : bloques) {
            if (!aceptacionRepository.existsByCartaIdAndBloqueIdAndAceptadoTrue(carta.getId(), bloque.getId())) {
                AceptacionBloque aceptacion = new AceptacionBloque();
                aceptacion.setCartaId(carta.getId());
                aceptacion.setBloqueId(bloque.getId());
                aceptacion.setAceptado(true);
                aceptacion.setTimestampAceptacion(LocalDateTime.now());
                aceptacion.setIpOrigen(ip != null ? ip : "desconocida");
                aceptacion.setUserAgent(userAgent != null ? userAgent : "desconocido");
                aceptacion.setHashTextoBloque(calcularHash(bloque.getTexto()));
                aceptacionRepository.save(aceptacion);
            }
        }
        
        carta.setEstatus(EstatusCarta.BLOQUES_ACEPTADOS);
        carta.setFechaAceptacionCompleta(LocalDateTime.now());
        carta = cartaRepository.save(carta);
        
        return Mono.just(mapToResponse(carta));
    }

    @Override
    @Auditable(
        categoria = "CARTA_DECLARATORIA", 
        tipo = "VALIDACION_EXTERNA", 
        severidad = NivelSeveridad.INFO, 
        recurso = "ValidacionExterna", 
        descripcion = "Se ejecutaron validaciones externas (RENADEA, Violencia) para la carta"
    )
    public Mono<Boolean> ejecutarValidacionesExternas(String folio, String curp) {
        return validacionService.ejecutarValidaciones(folio, curp);
    }

    @Override
    public Mono<List<ValidacionExternaResponse>> obtenerValidaciones(String folio) {
        return Mono.just(validacionService.obtenerValidaciones(folio));
    }

    @Override
    public Mono<EstatusCartaResponse> obtenerEstatus(String folio) {
        CartaDeclaratoria carta = cartaRepository.findFirstByFolioOrderByCreatedAtDesc(folio)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carta no encontrada"));
            
        long aceptados = aceptacionRepository.countAceptadosPorCarta(carta.getId());
        int totalBloques = bloquesService.contarBloquesActivos();
        boolean bloquesCompletos = aceptados >= totalBloques;
        
        long validacionesRechazadas = validacionService.obtenerValidaciones(folio).stream()
            .filter(v -> !v.resultado())
            .count();
        boolean validacionOk = validacionesRechazadas == 0;
        
        boolean pdfGenerado = carta.getPdfStoragePath() != null;
        boolean firmada = carta.getFechaFirma() != null;
        
        List<String> mensajes = new ArrayList<>();
        if (!bloquesCompletos) {
            mensajes.add("Faltan " + (totalBloques - aceptados) + " bloques por aceptar");
        }
        if (!validacionOk) {
            mensajes.add("Validaciones externas rechazadas");
        }
        if (!pdfGenerado) {
            mensajes.add("PDF no generado");
        }
        if (!firmada) {
            mensajes.add("Carta no firmada");
        }
        if (mensajes.isEmpty()) {
            mensajes.add("Carta completada exitosamente");
        }
        
        return Mono.just(new EstatusCartaResponse(
                folio,
                carta.getEstatus(),
                (int) aceptados,
                totalBloques,
                bloquesCompletos,
                validacionOk,
                pdfGenerado,
                firmada,
                mensajes
        ));
    }

    @Override
    @Transactional
    @Auditable(
        categoria = "CARTA_DECLARATORIA", 
        tipo = "FIRMA_CARTA", 
        severidad = NivelSeveridad.WARNING, 
        recurso = "CartaDeclaratoria", 
        descripcion = "Se aplicó firma electrónica a la carta declaratoria"
    )
    public Mono<CartaDeclaratoriaResponse> firmarCarta(String folio, FirmarCartaRequest request,
                                                        String ip, String userAgent) {
        log.info("Firmando carta: {} con metodo: {}", folio, request.metodoFirma());
        
        CartaDeclaratoria carta = cartaRepository.findFirstByFolioOrderByCreatedAtDesc(folio)
            .orElseThrow(() -> new RuntimeException("Carta no encontrada"));
            
        if (!bloquesService.todosBloquesAceptados(carta.getId())) {
            return Mono.error(new RuntimeException("No se han aceptado todos los bloques"));
        }
        
        carta.setMetodoFirma(request.metodoFirma());
        carta.setEstatus(EstatusCarta.EN_FIRMA);
        cartaRepository.save(carta);
        
        // Simulación de firma (en producción se integraría con servicio de firma)
        String firmaHash = calcularHash(folio + System.currentTimeMillis());
        carta.setFirmaDigitalHash(firmaHash);
        carta.setFechaFirma(LocalDateTime.now());
        carta.setEstatus(EstatusCarta.FIRMADA);
        carta = cartaRepository.save(carta);
        
        log.info("Carta {} firmada exitosamente con metodo {}", folio, request.metodoFirma());
        return Mono.just(mapToResponse(carta));
    }

    @Override
    public Mono<byte[]> obtenerPdf(String folio) {
        CartaDeclaratoria carta = cartaRepository.findFirstByFolioOrderByCreatedAtDesc(folio)
            .orElseThrow(() -> new RuntimeException("Carta no encontrada"));
            
        List<BloqueResponse> bloques = bloquesService.obtenerBloquesConEstatus(carta.getId());
        
        byte[] pdf = pdfService.generarPdf(
                carta, bloques, "Nombre Aspirante", "CURP123", folio
        );
        return Mono.just(pdf);
    }

    private CartaDeclaratoriaResponse mapToResponse(CartaDeclaratoria carta) {
        return new CartaDeclaratoriaResponse(
                carta.getId(),
                carta.getAspiranteId(),
                carta.getFolio(),
                carta.getFolioCarta(),
                carta.getVersion(),
                carta.getEstatus(),
                carta.getMetodoFirma(),
                carta.getFechaAceptacionCompleta(),
                carta.getFechaFirma(),
                carta.getCreatedAt()
        );
    }

    private String calcularHash(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error calculando hash", e);
        }
    }

    @Override
    public Mono<byte[]> obtenerPdfPorAspiranteId(UUID aspiranteId) {
    log.info("Generando PDF para aspiranteId: {}", aspiranteId);
    
    // Buscamos la carta más reciente de este aspirante
    CartaDeclaratoria carta = cartaRepository.findFirstByAspiranteIdOrderByCreatedAtDesc(aspiranteId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carta no encontrada para este aspirante"));
    
    List<BloqueResponse> bloques = bloquesService.obtenerBloquesConEstatus(carta.getId());
    
    // Generamos el PDF (usamos placeholders para nombre y CURP, o puedes extraerlos del perfil si lo deseas)
    byte[] pdf = pdfService.generarPdf(
            carta, bloques, "Aspirante", "CURP", carta.getFolio()
    );
    
    return Mono.just(pdf);
}
}