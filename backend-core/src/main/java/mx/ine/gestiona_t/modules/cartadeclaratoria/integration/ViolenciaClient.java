package mx.ine.gestiona_t.modules.cartadeclaratoria.integration;

import mx.ine.gestiona_t.modules.cartadeclaratoria.integration.dto.ViolenciaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ViolenciaClient {
    
    private static final Logger log = LoggerFactory.getLogger(ViolenciaClient.class);
    private final WebClient webClient;
    
    @Value("${violencia.api.url:https://api.segob.gob.mx/violencia/v1}")
    private String apiUrl;
    
    @Value("${violencia.api.key:CHANGE_ME}")
    private String apiKey;
    
    public ViolenciaClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public Mono<ViolenciaResponse> consultar(String curp) {
        log.info("Consultando registro de violencia para CURP: {}", curp);
        
        return webClient.get()
            .uri(apiUrl + "/consultar?curp=" + curp)
            .header("X-API-Key", apiKey)
            .retrieve()
            .bodyToMono(ViolenciaResponse.class)
            .doOnSuccess(r -> log.info("Violencia respondio: conAntecedentes={}", r.conAntecedentes()))
            .onErrorResume(e -> {
                log.error("Error consultando violencia: {}", e.getMessage());
                return Mono.just(new ViolenciaResponse(
                    false, curp, null, null, null, null,
                    "Error de conexion: " + e.getMessage()
                ));
            });
    }
}