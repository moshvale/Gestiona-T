package mx.ine.gestiona_t.modules.matching.service;

import mx.ine.gestiona_t.modules.cv.dto.response.CvInstitucionalResponse;
import mx.ine.gestiona_t.modules.cv.service.CvInstitucionalService;
import mx.ine.gestiona_t.modules.matching.model.ResultadoMatching;
import mx.ine.gestiona_t.modules.matching.repository.ResultadoMatchingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MatchingService {

    private static final Logger log = LoggerFactory.getLogger(MatchingService.class);
    private final CvInstitucionalService cvService;
    private final ResultadoMatchingRepository matchingRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.service.url:http://localhost:8007/api/v1}")
    private String aiServiceUrl;

    public MatchingService(CvInstitucionalService cvService, 
                           ResultadoMatchingRepository matchingRepository) {
        this.cvService = cvService;
        this.matchingRepository = matchingRepository;
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    public Map<String, Object> evaluarCv(UUID aspiranteId, String perfilPuesto) {
        log.info("🧠 Iniciando evaluación de matching ciego para aspirante: {}", aspiranteId);

        // 1. Obtener el CV completo
        CvInstitucionalResponse cv = cvService.obtenerCvPorAspirante(aspiranteId);

        // 2. Preparar payload para el Backend AI
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("cv_data", cv); // El Backend AI se encargará de anonimizarlo
        requestPayload.put("perfil_puesto", perfilPuesto);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

        try {
            // 3. Llamar al servicio de IA
            String url = aiServiceUrl + "/matching/evaluar";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> resultadoAi = response.getBody();

            if (resultadoAi == null) {
                throw new RuntimeException("El servicio de IA no devolvió resultados");
            }

            // 4. Guardar el resultado en la base de datos
            ResultadoMatching resultado = new ResultadoMatching();
            resultado.setAspiranteId(aspiranteId);
            // Convertir el score a Double de forma segura
            Object scoreObj = resultadoAi.get("score");
            resultado.setScore(scoreObj instanceof Number ? ((Number) scoreObj).doubleValue() : 0.0);
            resultado.setNivelCompatibilidad((String) resultadoAi.getOrDefault("nivel", "DESCONOCIDO"));
            resultado.setMensaje((String) resultadoAi.getOrDefault("mensaje", "Sin mensaje"));

            matchingRepository.save(resultado);
            log.info("✅ Matching evaluado y guardado. Score: {}%", resultado.getScore());

            return resultadoAi;

        } catch (Exception e) {
            log.error("❌ Error al comunicar con el Backend AI para matching", e);
            throw new RuntimeException("No se pudo realizar la evaluación de matching: " + e.getMessage());
        }
    }

    public Map<String, Object> obtenerResultado(UUID aspiranteId) {
        // ✅ Usamos el nuevo método que garantiza obtener solo el más reciente
        ResultadoMatching resultado = matchingRepository.findFirstByAspiranteIdOrderByFechaEvaluacionDesc(aspiranteId)
                .orElseThrow(() -> new RuntimeException("No se ha realizado ninguna evaluación de matching para este aspirante"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("score", resultado.getScore());
        response.put("nivel", resultado.getNivelCompatibilidad());
        response.put("mensaje", resultado.getMensaje());
        response.put("fecha", resultado.getFechaEvaluacion());
        return response;
    }
}