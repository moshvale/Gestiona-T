package mx.ine.gestiona_t.modules.cv.integration;

import mx.ine.gestiona_t.modules.cv.integration.dto.OcrResponse;
import mx.ine.gestiona_t.modules.cv.integration.dto.MapeoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

@Component
public class BackendAIClient {
    
    private static final Logger log = LoggerFactory.getLogger(BackendAIClient.class);
    
    private final WebClient webClient;
    
    @Value("${ai.service.url}")
    private String aiServiceUrl;
    
    public BackendAIClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public Mono<OcrResponse> procesarOcr(byte[] archivo, String fileName) {
        log.info("Enviando archivo a Backend-AI para OCR: {}", fileName);
        
        ByteArrayResource resource = new ByteArrayResource(archivo) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
        
        return webClient.post()
            .uri(aiServiceUrl + "/api/v1/ocr/procesar")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData("file", resource))
            .retrieve()
            .bodyToMono(OcrResponse.class)
            .doOnSuccess(response -> log.info("OCR completado. Confianza: {}", response.confianza()))
            .doOnError(error -> log.error("Error en OCR: {}", error.getMessage()))
            .onErrorResume(error -> Mono.just(new OcrResponse(
                null, 0.0, "Error de conexion con Backend-AI"
            )));
    }
    
    public Mono<MapeoResponse> mapearCv(String textoExtraido) {
        log.info("Enviando texto extraido a Backend-AI para mapeo");
        
        return webClient.post()
            .uri(aiServiceUrl + "/api/v1/matching/mapear-cv")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new MapeoRequest(textoExtraido))
            .retrieve()
            .bodyToMono(MapeoResponse.class)
            .doOnSuccess(response -> log.info("Mapeo completado. Exito: {}%", response.porcentajeExito()))
            .doOnError(error -> log.error("Error en mapeo: {}", error.getMessage()))
            .onErrorResume(error -> Mono.just(new MapeoResponse(
                null, null, 0.0, "Error de conexion con Backend-AI"
            )));
    }
    
    private record MapeoRequest(String textoExtraido) {}
}