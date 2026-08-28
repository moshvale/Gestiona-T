package mx.ine.gestiona_t.modules.cv.dto.response;

import java.util.List;

public record ValidacionCvResponse(
    boolean valido,
    int scoreCompletitud,
    boolean completo,
    List<String> errores,
    List<String> advertencias,
    List<String> sugerencias
) {}