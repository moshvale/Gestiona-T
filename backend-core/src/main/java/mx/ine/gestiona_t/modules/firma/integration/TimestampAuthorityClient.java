package mx.ine.gestiona_t.modules.firma.integration;

import mx.ine.gestiona_t.modules.firma.integration.dto.TimestampResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TimestampAuthorityClient {
    
    private static final Logger log = LoggerFactory.getLogger(TimestampAuthorityClient.class);
    private final WebClient webClient;
    
    @Value("${tsa.url:https://tsa.ine.mx/rfc3161}")
    private String tsaUrl;
    
    @Value("${tsa.username:CHANGE_ME}")
    private String username;
    
    @Value("${tsa.password:CHANGE_ME}")
    private String password;
    
    public TimestampAuthorityClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public Mono<TimestampResponse> solicitarTimestamp(String hashDocumento) {
        log.info("Solicitando timestamp RFC 3161 para hash: {}", hashDocumento);
        
        return webClient.post()
            .uri(tsaUrl + "/timestamp")
            .headers(headers -> headers.setBasicAuth(username, password))
            .bodyValue(new TimestampRequest(hashDocumento, "SHA-256"))
            .retrieve()
            .bodyToMono(TimestampResponse.class)
            .doOnSuccess(r -> log.info("TSA respondio: exitoso={}", r.exitoso()))
            .onErrorResume(e -> {
                log.error("Error solicitando timestamp: {}", e.getMessage());
                return Mono.just(new TimestampResponse(
                    false, null, null, null,
                    "Error de conexion con TSA: " + e.getMessage()
                ));
            });
    }
    
    private record TimestampRequest(String hash, String algoritmo) {}
}