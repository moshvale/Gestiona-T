package mx.ine.gestiona_t.modules.firma.integration;

import mx.ine.gestiona_t.modules.firma.integration.dto.SatFielResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class SatFielClient {
    
    private static final Logger log = LoggerFactory.getLogger(SatFielClient.class);
    private final WebClient webClient;
    
    @Value("${sat.fiel.api.url:https://api.sat.gob.mx/fiel/v1}")
    private String apiUrl;
    
    @Value("${sat.fiel.api.key:CHANGE_ME}")
    private String apiKey;
    
    public SatFielClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public Mono<SatFielResponse> validarCertificado(String certificadoBase64, String firmaBase64) {
        log.info("Validando certificado FIEL contra SAT");
        
        return webClient.post()
            .uri(apiUrl + "/validar-certificado")
            .header("X-API-Key", apiKey)
            .bodyValue(new ValidarRequest(certificadoBase64, firmaBase64))
            .retrieve()
            .bodyToMono(SatFielResponse.class)
            .doOnSuccess(r -> log.info("SAT respondio: certificadoValido={}", r.certificadoValido()))
            .onErrorResume(e -> {
                log.error("Error validando FIEL: {}", e.getMessage());
                return Mono.just(new SatFielResponse(
                    false, null, null, null, null, null,
                    "Error de conexion con SAT: " + e.getMessage()
                ));
            });
    }
    
    private record ValidarRequest(String certificado, String firma) {}
}