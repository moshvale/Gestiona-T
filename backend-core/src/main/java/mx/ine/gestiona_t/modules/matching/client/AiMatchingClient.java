package mx.ine.gestiona_t.modules.matching.client;

import mx.ine.gestiona_t.modules.matching.dto.MatchingRequest;
import mx.ine.gestiona_t.modules.matching.dto.MatchingResponse;

/**
 * Contrato para el cliente de IA de matching.
 * Permite intercambiar fácilmente entre el Mock y el cliente real (Backend-AI).
 */
public interface AiMatchingClient {
    MatchingResponse evaluar(MatchingRequest request);
    String getModeloName();
}