package mx.ine.gestiona_t.modules.cv.dto.response;

import java.util.List;

public record CvCompletoResponse(
    CvResponse cv,
    List<EscolaridadResponse> escolaridades,
    List<ExperienciaResponse> experiencias,
    List<CursoResponse> cursos,
    List<HabilidadResponse> habilidades
) {}