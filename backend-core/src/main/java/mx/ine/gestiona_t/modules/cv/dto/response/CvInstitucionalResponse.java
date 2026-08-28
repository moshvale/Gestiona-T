package mx.ine.gestiona_t.modules.cv.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CvInstitucionalResponse(
        UUID id,
        UUID aspiranteId,
        String entidadPreferida,
        BigDecimal sueldoDeseado,
        String disponibilidad,
        String areasInteres,
        String sistemasOperativos,
        String lenguajesProgramacion,
        String basesDeDatos,
        String habilidades,
        String logrosProfesionales,
        int scoreCompletitud,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<FormacionResponse> formacionAcademica,
        List<ExperienciaResponse> experienciaLaboral,
        List<IdiomaResponse> idiomas,
        List<CursoResponse> cursos // ✅ Agregado en el encabezado del record
) {
    public record FormacionResponse(
            UUID id, String nivel, String carrera, String institucion,
            LocalDate fechaInicio, LocalDate fechaFin, String cedulaProfesional, String estatus
    ) {}

    public record ExperienciaResponse(
            UUID id, String tipoExperiencia, String empresa, String puesto,
            String funciones, LocalDate fechaInicio, LocalDate fechaFin, BigDecimal sueldo, boolean actualmenteLaborando
    ) {}

    public record IdiomaResponse(
            UUID id, String idioma, String nivelEscritura, String nivelLectura, String nivelConversacion
    ) {}

    // ✅ Nuevo Record para Cursos en la respuesta
    public record CursoResponse(
            UUID id,
            String nombreCurso,
            String institucion,
            Integer duracionHoras,
            LocalDate fechaRealizacion,
            String documentoSoportePath
    ) {}
}