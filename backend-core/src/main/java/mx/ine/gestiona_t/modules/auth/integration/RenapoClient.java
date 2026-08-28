package mx.ine.gestiona_t.modules.auth.integration;

import mx.ine.gestiona_t.modules.auth.integration.dto.RenapoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class RenapoClient {
    
    private static final Logger log = LoggerFactory.getLogger(RenapoClient.class);
    
    private final WebClient webClient;
    
    @Value("${renapo.api.url}")
    private String apiUrl;
    
    @Value("${renapo.api.key}")
    private String apiKey;
    
    public RenapoClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public Mono<RenapoResponse> validarCurp(String curp, String nombre, 
                                             String apellidoPaterno, 
                                             String apellidoMaterno,
                                             String fechaNacimiento,
                                             String entidadFederativa) {
        log.info("Validando CURP contra RENAPO: {}", curp);
        
        return webClient.post()
            .uri(apiUrl + "/validar-curp")
            .header("X-API-Key", apiKey)
            .header("Content-Type", "application/json")
            .bodyValue(new CurpValidationRequest(
                curp, nombre, apellidoPaterno, apellidoMaterno, 
                fechaNacimiento, entidadFederativa
            ))
            .retrieve()
            .bodyToMono(RenapoResponse.class)
            .doOnSuccess(response -> log.info("Respuesta RENAPO: valido={}", response.valido()))
            .doOnError(error -> log.error("Error al validar CURP en RENAPO: {}", error.getMessage()))
            .onErrorResume(error -> Mono.just(new RenapoResponse(
                false, null, null, null, null, null, null, null, 
                "Error de conexión con RENAPO"
            )));
    }
    
    private record CurpValidationRequest(
        String curp,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String fechaNacimiento,
        String entidadFederativa
    ) {}
}