package mx.ine.gestiona_t.modules.documentos.integration;

import mx.ine.gestiona_t.modules.documentos.integration.dto.RnpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Component
public class RnpClient {
    private static final Logger log = LoggerFactory.getLogger(RnpClient.class);

    private final WebClient webClient;

    @Value("${rnp.api.url}")
    private String apiUrl;

    @Value("${rnp.api.key}")
    private String apiKey;

    public RnpClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<RnpResponse> validarCedula(String numeroCedula, String nombre) {
        log.info("Validando cedula profesional en RNP: {}", numeroCedula);
        return webClient.post()
            .uri(apiUrl + "/validar-cedula")
            .header("X-API-Key", apiKey)
            .bodyValue(new CedulaRequest(numeroCedula, nombre))
            .retrieve()
            .bodyToMono(RnpResponse.class)
            .timeout(Duration.ofSeconds(10)) // ✅ Timeout de 10 segundos
            .doOnSuccess(r -> log.info("✅ RNP respondio: valido={}", r.valido()))
            .onErrorResume(e -> {
                // ✅ Fallback defensivo: si falla o tarda demasiado, no rechazamos
                log.warn("⚠️ RNP no disponible para cedula {}: {}", numeroCedula, e.getMessage());
                return Mono.just(new RnpResponse(
                    false, null, null, null, null, 
                    "Servicio RNP no disponible o timeout. Motivo: " + e.getMessage()
                ));
            });
    }

    private record CedulaRequest(String numeroCedula, String nombre) {}
}