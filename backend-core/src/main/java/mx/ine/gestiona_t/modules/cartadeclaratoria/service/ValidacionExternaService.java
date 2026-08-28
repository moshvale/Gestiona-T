package mx.ine.gestiona_t.modules.cartadeclaratoria.service;

import mx.ine.gestiona_t.modules.cartadeclaratoria.dto.response.ValidacionExternaResponse;
import mx.ine.gestiona_t.modules.cartadeclaratoria.integration.RenadeaClient;
import mx.ine.gestiona_t.modules.cartadeclaratoria.integration.ViolenciaClient;
import mx.ine.gestiona_t.modules.cartadeclaratoria.integration.dto.RenadeaResponse;
import mx.ine.gestiona_t.modules.cartadeclaratoria.integration.dto.ViolenciaResponse;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.CartaDeclaratoria;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.ValidacionExterna;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.enums.EstatusCarta;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.enums.TipoValidacionExterna;
import mx.ine.gestiona_t.modules.cartadeclaratoria.repository.CartaDeclaratoriaRepository;
import mx.ine.gestiona_t.modules.cartadeclaratoria.repository.ValidacionExternaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ValidacionExternaService {
    
    private static final Logger log = LoggerFactory.getLogger(ValidacionExternaService.class);
    
    private final RenadeaClient renadeaClient;
    private final ViolenciaClient violenciaClient;
    private final ValidacionExternaRepository validacionRepository;
    private final CartaDeclaratoriaRepository cartaRepository;
    
    public ValidacionExternaService(RenadeaClient renadeaClient,
                                     ViolenciaClient violenciaClient,
                                     ValidacionExternaRepository validacionRepository,
                                     CartaDeclaratoriaRepository cartaRepository) {
        this.renadeaClient = renadeaClient;
        this.violenciaClient = violenciaClient;
        this.validacionRepository = validacionRepository;
        this.cartaRepository = cartaRepository;
    }
    
    @Transactional
    public Mono<Boolean> ejecutarValidaciones(String folio, String curp) {
        log.info("Ejecutando validaciones externas para carta: {}", folio);
        
        CartaDeclaratoria carta = cartaRepository.findFirstByFolioOrderByCreatedAtDesc(folio)
            .orElseThrow(() -> new RuntimeException("Carta no encontrada"));
        
        carta.setEstatus(EstatusCarta.EN_VALIDACION_EXTERNA);
        cartaRepository.save(carta);
        
        Mono<RenadeaResponse> renadeaMono = renadeaClient.consultar(curp);
        Mono<ViolenciaResponse> violenciaMono = violenciaClient.consultar(curp);
        
        return Mono.zip(renadeaMono, violenciaMono)
            .map(tuple -> {
                RenadeaResponse renadea = tuple.getT1();
                ViolenciaResponse violencia = tuple.getT2();
                
                boolean renadeaOk = procesarRenadea(carta.getId(), renadea);
                boolean violenciaOk = procesarViolencia(carta.getId(), violencia);
                
                boolean todasOk = renadeaOk && violenciaOk;
                
                if (todasOk) {
                    carta.setEstatus(EstatusCarta.VALIDACION_EXTERNA_OK);
                } else {
                    carta.setEstatus(EstatusCarta.VALIDACION_EXTERNA_RECHAZADA);
                }
                
                cartaRepository.save(carta);
                
                log.info("Validaciones externas completadas para {}: todasOk={}", folio, todasOk);
                return todasOk;
            })
            .onErrorResume(e -> {
                log.error("Error en validaciones externas: {}", e.getMessage());
                carta.setEstatus(EstatusCarta.EN_REVISION_MANUAL);
                cartaRepository.save(carta);
                return Mono.just(false);
            });
    }
    
    private boolean procesarRenadea(UUID cartaId, RenadeaResponse respuesta) {
        ValidacionExterna validacion = new ValidacionExterna();
        validacion.setCartaId(cartaId);
        validacion.setTipoValidacion(TipoValidacionExterna.RENADEA);
        validacion.setResultado(!respuesta.registrado());
        validacion.setMensaje(respuesta.mensaje());
        validacion.setRespuestaApi(respuesta.toString());
        
        validacionRepository.save(validacion);
        
        return !respuesta.registrado();
    }
    
    private boolean procesarViolencia(UUID cartaId, ViolenciaResponse respuesta) {
        ValidacionExterna validacion = new ValidacionExterna();
        validacion.setCartaId(cartaId);
        validacion.setTipoValidacion(TipoValidacionExterna.VIOLENCIA_GENERO);
        validacion.setResultado(!respuesta.conAntecedentes());
        validacion.setMensaje(respuesta.mensaje());
        validacion.setRespuestaApi(respuesta.toString());
        
        validacionRepository.save(validacion);
        
        return !respuesta.conAntecedentes();
    }
    
    public List<ValidacionExternaResponse> obtenerValidaciones(String folio) {
        CartaDeclaratoria carta = cartaRepository.findFirstByFolioOrderByCreatedAtDesc(folio)
            .orElseThrow(() -> new RuntimeException("Carta no encontrada"));
        
        return validacionRepository.findByCartaId(carta.getId()).stream()
            .map(v -> new ValidacionExternaResponse(
                v.getId(),
                v.getTipoValidacion(),
                v.isResultado(),
                v.getMensaje(),
                v.getFechaConsulta()
            ))
            .collect(Collectors.toList());
    }
}