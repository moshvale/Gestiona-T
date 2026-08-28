package mx.ine.gestiona_t.modules.documentos.integration;

import mx.ine.gestiona_t.modules.documentos.integration.dto.SatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Component
public class SatClient {
    private static final Logger log = LoggerFactory.getLogger(SatClient.class);

    private final WebClient webClient;

    @Value("${sat.api.url}")
    private String apiUrl;

    @Value("${sat.api.key}")
    private String apiKey;

    public SatClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<SatResponse> validarRfc(String rfc) {
        log.info("Validando RFC en SAT: {}", rfc);
        return webClient.get()
            .uri(apiUrl + "/rfc/" + rfc)
            .header("X-API-Key", apiKey)
            .retrieve()
            .bodyToMono(SatResponse.class)
            .timeout(Duration.ofSeconds(10)) // ✅ Timeout de 10 segundos
            .doOnSuccess(r -> log.info("✅ SAT respondio: valido={}", r.valido()))
            .onErrorResume(e -> {
                // ✅ Fallback defensivo
                log.warn("⚠️ SAT no disponible para RFC {}: {}", rfc, e.getMessage());
                return Mono.just(new SatResponse(
                    false, null, null, null, 
                    "Servicio SAT no disponible o timeout. Motivo: " + e.getMessage()
                ));
            });
    }
}