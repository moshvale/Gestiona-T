package mx.ine.gestiona_t.modules.firma.integration;

import mx.ine.gestiona_t.modules.firma.integration.dto.BiometriaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class BiometriaClient {
    
    private static final Logger log = LoggerFactory.getLogger(BiometriaClient.class);
    private final WebClient webClient;
    
    @Value("${biometria.api.url:https://api.renapo.gob.mx/biometria/v1}")
    private String apiUrl;
    
    @Value("${biometria.api.key:CHANGE_ME}")
    private String apiKey;
    
    @Value("${biometria.threshold:0.85}")
    private double threshold;
    
    public BiometriaClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public Mono<BiometriaResponse> validarBiometria(String selfieBase64, String curp) {
        log.info("Validando biometria facial para CURP: {}", curp);
        
        return webClient.post()
            .uri(apiUrl + "/validar-facial")
            .header("X-API-Key", apiKey)
            .bodyValue(new BiometriaRequest(selfieBase64, curp, threshold))
            .retrieve()
            .bodyToMono(BiometriaResponse.class)
            .doOnSuccess(r -> log.info("Biometria respondio: coincide={}, score={}", 
                                        r.coincide(), r.scoreCoincidencia()))
            .onErrorResume(e -> {
                log.error("Error validando biometria: {}", e.getMessage());
                return Mono.just(new BiometriaResponse(
                    false, 0.0, curp, null, null,
                    "Error de conexion: " + e.getMessage()
                ));
            });
    }
    
    private record BiometriaRequest(String selfie, String curp, double threshold) {}
}