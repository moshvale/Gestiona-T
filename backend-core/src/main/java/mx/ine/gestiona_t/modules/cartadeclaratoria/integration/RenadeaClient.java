package mx.ine.gestiona_t.modules.cartadeclaratoria.integration;

import mx.ine.gestiona_t.modules.cartadeclaratoria.integration.dto.RenadeaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class RenadeaClient {
    
    private static final Logger log = LoggerFactory.getLogger(RenadeaClient.class);
    private final WebClient webClient;
    
    @Value("${renadea.api.url:https://api.consejojudicial.gob.mx/renadea/v1}")
    private String apiUrl;
    
    @Value("${renadea.api.key:CHANGE_ME}")
    private String apiKey;
    
    public RenadeaClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public Mono<RenadeaResponse> consultar(String curp) {
        log.info("Consultando RENADEA para CURP: {}", curp);
        
        return webClient.get()
            .uri(apiUrl + "/consultar?curp=" + curp)
            .header("X-API-Key", apiKey)
            .retrieve()
            .bodyToMono(RenadeaResponse.class)
            .doOnSuccess(r -> log.info("RENADEA respondio: registrado={}", r.registrado()))
            .onErrorResume(e -> {
                log.error("Error consultando RENADEA: {}", e.getMessage());
                return Mono.just(new RenadeaResponse(
                    false, curp, null, null, null, null,
                    "Error de conexion con RENADEA: " + e.getMessage()
                ));
            });
    }
}