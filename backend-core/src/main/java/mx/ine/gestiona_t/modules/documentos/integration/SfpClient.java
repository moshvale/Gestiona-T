package mx.ine.gestiona_t.modules.documentos.integration;

import mx.ine.gestiona_t.modules.documentos.integration.dto.SfpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Component
public class SfpClient {
    private static final Logger log = LoggerFactory.getLogger(SfpClient.class);

    private final WebClient webClient;

    @Value("${sfp.api.url}")
    private String apiUrl;

    @Value("${sfp.api.key}")
    private String apiKey;

    public SfpClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<SfpResponse> consultarServidorPublico(String rfc, String curp) {
        log.info("Consultando SFP para RFC: {}", rfc);
        return webClient.post()
            .uri(apiUrl + "/consultar")
            .header("X-API-Key", apiKey)
            .bodyValue(new SfpRequest(rfc, curp))
            .retrieve()
            .bodyToMono(SfpResponse.class)
            .timeout(Duration.ofSeconds(10)) // ✅ Timeout de 10 segundos
            .doOnSuccess(r -> log.info("✅ SFP respondio: inhabilitado={}", r.inhabilitado()))
            .onErrorResume(e -> {
                // ✅ Fallback defensivo
                log.warn("⚠️ SFP no disponible para RFC {}: {}", rfc, e.getMessage());
                return Mono.just(new SfpResponse(
                    false, null, null, null, null, 
                    "Servicio SFP no disponible o timeout. Motivo: " + e.getMessage()
                ));
            });
    }

    private record SfpRequest(String rfc, String curp) {}
}