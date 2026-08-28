package mx.ine.gestiona_t.modules.auth.integration;

import mx.ine.gestiona_t.modules.auth.integration.dto.ListaNominalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ListaNominalClient {
    
    private static final Logger log = LoggerFactory.getLogger(ListaNominalClient.class);
    
    private final WebClient webClient;
    
    @Value("${ine.lista.nominal.url}")
    private String apiUrl;
    
    @Value("${ine.api.key}")
    private String apiKey;
    
    public ListaNominalClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public Mono<ListaNominalResponse> validarClaveElector(String claveElector) {
        log.info("Validando Clave de Elector contra Lista Nominal: {}", claveElector);
        
        return webClient.post()
            .uri(apiUrl + "/validar-clave-elector")
            .header("X-API-Key", apiKey)
            .header("Content-Type", "application/json")
            .bodyValue(new ClaveElectorRequest(claveElector))
            .retrieve()
            .bodyToMono(ListaNominalResponse.class)
            .doOnSuccess(response -> log.info("Respuesta Lista Nominal: vigente={}", response.vigente()))
            .doOnError(error -> log.error("Error al validar Clave de Elector: {}", error.getMessage()))
            .onErrorResume(error -> Mono.just(new ListaNominalResponse(
                false, null, null, null, null, null, "Error de conexión con Lista Nominal"
            )));
    }
    
    private record ClaveElectorRequest(String claveElector) {}
}