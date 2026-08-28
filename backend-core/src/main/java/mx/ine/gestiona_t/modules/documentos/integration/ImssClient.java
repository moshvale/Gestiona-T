package mx.ine.gestiona_t.modules.documentos.integration;

import mx.ine.gestiona_t.modules.documentos.integration.dto.ImssResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Component
public class ImssClient {
    private static final Logger log = LoggerFactory.getLogger(ImssClient.class);

    private final WebClient webClient;

    @Value("${imss.api.url}")
    private String apiUrl;

    @Value("${imss.api.key}")
    private String apiKey;

    public ImssClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<ImssResponse> validarRegistroPatronal(String registroPatronal) {
        log.info("Validando registro patronal en IMSS: {}", registroPatronal);
        return webClient.get()
            .uri(apiUrl + "/registro/" + registroPatronal)
            .header("X-API-Key", apiKey)
            .retrieve()
            .bodyToMono(ImssResponse.class)
            .timeout(Duration.ofSeconds(10)) // ✅ Timeout de 10 segundos
            .doOnSuccess(r -> log.info("✅ IMSS respondio: valido={}", r.valido()))
            .onErrorResume(e -> {
                // ✅ Fallback defensivo
                log.warn("⚠️ IMSS no disponible para registro {}: {}", registroPatronal, e.getMessage());
                return Mono.just(new ImssResponse(
                    false, null, null, 
                    "Servicio IMSS no disponible o timeout. Motivo: " + e.getMessage()
                ));
            });
    }
}