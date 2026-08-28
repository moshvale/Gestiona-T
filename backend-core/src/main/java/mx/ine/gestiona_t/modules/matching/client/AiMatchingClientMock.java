package mx.ine.gestiona_t.modules.matching.client;

import mx.ine.gestiona_t.modules.matching.dto.MatchingRequest;
import mx.ine.gestiona_t.modules.matching.dto.MatchingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Cliente Mock que simula la respuesta de un motor de IA.
 * Analiza el contenido del perfil y genera un score realista basado en:
 * - Longitud y riqueza del perfil profesional
 * - Presencia de palabras clave institucionales
 * - Cantidad de habilidades e idiomas declarados
 */
@Component
public class AiMatchingClientMock implements AiMatchingClient {

    private static final Logger log = LoggerFactory.getLogger(AiMatchingClientMock.class);

    // Palabras clave valoradas para procesos electorales del INE
    private static final List<String> PALABRAS_CLAVE_INE = Arrays.asList(
        "electoral", "elección", "voto", "casilla", "distrito", "sección",
        "capacitación", "logística", "organización", "ciudadano", "democracia",
        "institucional", "transparencia", "fiscalización", "partido", "coalición",
        "administrativo", "gestión", "proceso", "normativa", "legal"
    );

    private static final List<String> HABILIDADES_VALORADAS = Arrays.asList(
        "liderazgo", "trabajo en equipo", "comunicación", "análisis",
        "organización", "planificación", "resolución de problemas",
        "atención al detalle", "gestión del tiempo", "adaptabilidad"
    );

    @Override
    public MatchingResponse evaluar(MatchingRequest request) {
        log.info("🤖 [MOCK IA] Evaluando perfil del aspirante: {}", request.aspiranteId());

        // Simular latencia de red (500-1500ms)
        try {
            Thread.sleep(500 + (long)(Math.random() * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 1. Calcular score base
        int score = calcularScoreBase(request);

        // 2. Generar fortalezas
        List<String> fortalezas = generarFortalezas(request, score);

        // 3. Generar áreas de mejora
        List<String> areasMejora = generarAreasMejora(request, score);

        // 4. Generar resumen ejecutivo
        String resumen = generarResumen(request, score, fortalezas, areasMejora);

        log.info("🤖 [MOCK IA] Evaluación completada. Score: {}/100", score);

        return new MatchingResponse(score, resumen, fortalezas, areasMejora);
    }

    @Override
    public String getModeloName() {
        return "MOCK-INSTITUCIONAL-v1.0";
    }

    private int calcularScoreBase(MatchingRequest request) {
        int score = 40; // Base inicial

        String perfil = request.perfilProfesionalResumido().toLowerCase();

        // +2 puntos por cada palabra clave INE encontrada (máx +20)
        long palabrasClaveEncontradas = PALABRAS_CLAVE_INE.stream()
            .filter(perfil::contains)
            .count();
        score += Math.min(palabrasClaveEncontradas * 2, 20);

        // +5 puntos por cada habilidad declarada (máx +15)
        if (request.habilidades() != null) {
            score += Math.min(request.habilidades().size() * 5, 15);
        }

        // +5 puntos por cada idioma (máx +10)
        if (request.idiomas() != null) {
            score += Math.min(request.idiomas().size() * 5, 10);
        }

        // +5 puntos si menciona formación académica
        if (perfil.contains("formación académica") && perfil.length() > 200) {
            score += 5;
        }

        // +5 puntos si menciona experiencia laboral
        if (perfil.contains("experiencia laboral") && perfil.length() > 300) {
            score += 5;
        }

        // +5 puntos si el perfil es extenso y detallado
        if (perfil.length() > 500) {
            score += 5;
        }

        // Ajuste aleatorio sutil (-3 a +3) para simular variabilidad
        score += (int)(Math.random() * 7) - 3;

        // Clamp entre 0 y 100
        return Math.max(0, Math.min(100, score));
    }

    private List<String> generarFortalezas(MatchingRequest request, int score) {
        List<String> fortalezas = new ArrayList<>();
        String perfil = request.perfilProfesionalResumido().toLowerCase();

        if (score >= 70) {
            fortalezas.add("Perfil altamente compatible con los requisitos institucionales del INE.");
        }

        // Detectar palabras clave presentes
        List<String> clavesPresentes = PALABRAS_CLAVE_INE.stream()
            .filter(perfil::contains)
            .toList();
        
        if (clavesPresentes.size() >= 3) {
            fortalezas.add("Experiencia demostrable en procesos electorales y logística institucional.");
        }

        if (perfil.contains("licenciatura") || perfil.contains("maestría")) {
            fortalezas.add("Formación académica sólida y pertinente.");
        }

        if (request.idiomas() != null && request.idiomas().size() >= 2) {
            fortalezas.add("Dominio de múltiples idiomas, útil para contextos internacionales.");
        }

        if (request.habilidades() != null && request.habilidades().size() >= 3) {
            fortalezas.add("Amplio conjunto de habilidades blandas y técnicas declaradas.");
        }

        if (perfil.contains("experiencia laboral") && perfil.length() > 400) {
            fortalezas.add("Trayectoria laboral extensa y detallada.");
        }

        // Si no hay fortalezas, agregar una genérica
        if (fortalezas.isEmpty()) {
            fortalezas.add("Perfil con potencial de desarrollo para roles administrativos.");
        }

        return fortalezas;
    }

    private List<String> generarAreasMejora(MatchingRequest request, int score) {
        List<String> areas = new ArrayList<>();
        String perfil = request.perfilProfesionalResumido().toLowerCase();

        if (score < 60) {
            areas.add("Se recomienda ampliar la descripción de experiencia relevante al ámbito electoral.");
        }

        if (request.habilidades() == null || request.habilidades().size() < 3) {
            areas.add("Incluir más habilidades blandas y técnicas en el CV institucional.");
        }

        if (request.idiomas() == null || request.idiomas().isEmpty()) {
            areas.add("Declarar conocimientos de idiomas (al menos inglés básico).");
        }

        if (!perfil.contains("logística") && !perfil.contains("organización")) {
            areas.add("Destacar experiencia en organización de eventos o procesos logísticos.");
        }

        if (perfil.length() < 300) {
            areas.add("El perfil profesional es breve; se sugiere detallar más funciones y logros.");
        }

        if (!perfil.contains("normativa") && !perfil.contains("legal")) {
            areas.add("Conocer o mencionar experiencia con marcos normativos y regulatorios.");
        }

        if (areas.isEmpty()) {
            areas.add("Perfil sólido; mantener actualización continua en temas electorales.");
        }

        return areas;
    }

    private String generarResumen(MatchingRequest request, int score, 
                                   List<String> fortalezas, List<String> areasMejora) {
        String nivel;
        if (score >= 80) nivel = "ALTAMENTE COMPATIBLE";
        else if (score >= 60) nivel = "COMPATIBLE";
        else if (score >= 40) nivel = "PARCIALMENTE COMPATIBLE";
        else nivel = "REQUIERE REVISIÓN";

        return String.format(
            "El aspirante presenta un perfil %s (score: %d/100) para los requisitos de la vacante. " +
            "Se identificaron %d fortalezas clave y %d áreas de oportunidad. " +
            "El análisis se realizó considerando la formación académica, experiencia laboral, " +
            "habilidades declaradas y afinidad con el contexto institucional del INE.",
            nivel, score, fortalezas.size(), areasMejora.size()
        );
    }
}