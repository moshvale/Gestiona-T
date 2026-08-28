package mx.ine.gestiona_t.modules.auth.service;

import mx.ine.gestiona_t.modules.auth.integration.ListaNominalClient;
import mx.ine.gestiona_t.modules.auth.integration.dto.ListaNominalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ListaNominalService {
    
    private static final Logger log = LoggerFactory.getLogger(ListaNominalService.class);
    
    private final ListaNominalClient listaNominalClient;
    
    public ListaNominalService(ListaNominalClient listaNominalClient) {
        this.listaNominalClient = listaNominalClient;
    }
    
    public Mono<ListaNominalResponse> validarClaveElector(String claveElector) {
        log.info("Iniciando validación de Clave de Elector en Lista Nominal: {}", claveElector);
        
        return listaNominalClient.validarClaveElector(claveElector)
            .doOnSuccess(response -> {
                if (response.vigente()) {
                    log.info("Clave de Elector validada exitosamente");
                } else {
                    log.warn("Clave de Elector no vigente: {}", response.mensaje());
                }
            })
            .doOnError(error -> log.error("Error al validar Clave de Elector: {}", error.getMessage()));
    }
}