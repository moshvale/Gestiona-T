package mx.ine.gestiona_t.modules.cv.dto.response;

public record ScoreCompletitudResponse(
    int scoreTotal,
    int scoreEscolaridad,
    int scoreExperiencia,
    int scoreCursos,
    int scoreHabilidades,
    boolean completo,
    String mensaje
) {}