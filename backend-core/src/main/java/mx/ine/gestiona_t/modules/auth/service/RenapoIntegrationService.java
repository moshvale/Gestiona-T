package mx.ine.gestiona_t.modules.auth.service;

import mx.ine.gestiona_t.modules.auth.integration.RenapoClient;
import mx.ine.gestiona_t.modules.auth.integration.dto.RenapoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RenapoIntegrationService {
    
    private static final Logger log = LoggerFactory.getLogger(RenapoIntegrationService.class);
    
    private final RenapoClient renapoClient;
    
    public RenapoIntegrationService(RenapoClient renapoClient) {
        this.renapoClient = renapoClient;
    }
    
    public Mono<RenapoResponse> validarCurp(String curp, String nombre, 
                                             String apellidoPaterno, 
                                             String apellidoMaterno,
                                             String fechaNacimiento,
                                             String entidadFederativa) {
        log.info("Iniciando validación de CURP en RENAPO: {}", curp);
        
        return renapoClient.validarCurp(curp, nombre, apellidoPaterno, 
                                        apellidoMaterno, fechaNacimiento, 
                                        entidadFederativa)
            .doOnSuccess(response -> {
                if (response.valido()) {
                    log.info("CURP validada exitosamente en RENAPO");
                } else {
                    log.warn("CURP no válida en RENAPO: {}", response.mensaje());
                }
            })
            .doOnError(error -> log.error("Error al validar CURP en RENAPO: {}", error.getMessage()));
    }
}