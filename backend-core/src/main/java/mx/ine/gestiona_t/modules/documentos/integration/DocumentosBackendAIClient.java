package mx.ine.gestiona_t.modules.documentos.integration;

import mx.ine.gestiona_t.modules.documentos.integration.dto.OcrDocumentoResponse;
import mx.ine.gestiona_t.modules.documentos.integration.dto.AutenticidadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Component
public class DocumentosBackendAIClient {
    private static final Logger log = LoggerFactory.getLogger(DocumentosBackendAIClient.class);

    private final WebClient webClient;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    public DocumentosBackendAIClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<OcrDocumentoResponse> procesarDocumento(byte[] archivo, String fileName) {
        log.info("Enviando documento a Backend-AI para OCR y clasificacion: {}", fileName);
        ByteArrayResource resource = new ByteArrayResource(archivo) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
        return webClient.post()
            .uri(aiServiceUrl + "/ocr/documento")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData("file", resource))
            .retrieve()
            .bodyToMono(OcrDocumentoResponse.class)
            .timeout(Duration.ofSeconds(30)) // ✅ Timeout de 30 segundos para OCR
            .doOnSuccess(r -> log.info("✅ OCR completado. Tipo detectado: {}, Confianza: {}", 
                                        r.tipoDetectado(), r.confianza()))
            .onErrorResume(e -> {
                // ✅ Fallback defensivo: si el Backend AI falla, no rechazamos de golpe
                log.warn("⚠️ Backend-AI OCR no disponible para {}: {}", fileName, e.getMessage());
                return Mono.just(new OcrDocumentoResponse(
                    null, 0.0, null, null, 
                    "Servicio Backend-AI no disponible o timeout. Motivo: " + e.getMessage()
                ));
            });
    }

    public Mono<AutenticidadResponse> validarAutenticidad(byte[] archivo, String fileName) {
        log.info("Enviando documento a Backend-AI para validacion de autenticidad: {}", fileName);
        ByteArrayResource resource = new ByteArrayResource(archivo) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
        return webClient.post()
            .uri(aiServiceUrl + "/autenticidad/validar")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData("file", resource))
            .retrieve()
            .bodyToMono(AutenticidadResponse.class)
            .timeout(Duration.ofSeconds(30)) // ✅ Timeout de 30 segundos
            .doOnSuccess(r -> log.info("✅ Autenticidad validada. Score: {}, Sospechoso: {}", 
                                        r.scoreAutenticidad(), r.sospechoso()))
            .onErrorResume(e -> {
                // ✅ Fallback defensivo
                log.warn("⚠️ Backend-AI Autenticidad no disponible para {}: {}", fileName, e.getMessage());
                return Mono.just(new AutenticidadResponse(
                    0.0, false, new String[]{"Servicio de autenticidad no disponible"}, 
                    "Servicio Backend-AI no disponible o timeout. Motivo: " + e.getMessage()
                ));
            });
    }
}