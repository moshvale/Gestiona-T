package mx.ine.gestiona_t.modules.cv.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CvInstitucionalRequest(
        String entidadPreferida,
        BigDecimal sueldoDeseado,
        String disponibilidad,
        String areasInteres,
        String sistemasOperativos,
        String lenguajesProgramacion,
        String basesDeDatos,
        String habilidades,
        String logrosProfesionales,
        List<FormacionRequest> formacionAcademica,
        List<ExperienciaRequest> experienciaLaboral,
        List<IdiomaRequest> idiomas,
        List<CursoRequest> cursos // ✅ Agregado en el encabezado del record
) {
    public record FormacionRequest(
            UUID id,
            @NotBlank(message = "El nivel es obligatorio") String nivel,
            @NotBlank(message = "La carrera es obligatoria") String carrera,
            @NotBlank(message = "La institución es obligatoria") String institucion,
            @NotNull(message = "La fecha de inicio es obligatoria") LocalDate fechaInicio,
            LocalDate fechaFin,
            String cedulaProfesional,
            String estatus
    ) {}

    public record ExperienciaRequest(
            UUID id,
            @NotBlank(message = "El tipo de experiencia es obligatorio") String tipoExperiencia,
            @NotBlank(message = "La empresa es obligatoria") String empresa,
            @NotBlank(message = "El puesto es obligatorio") String puesto,
            String funciones,
            @NotNull(message = "La fecha de inicio es obligatoria") LocalDate fechaInicio,
            LocalDate fechaFin,
            BigDecimal sueldo,
            boolean actualmenteLaborando
    ) {}

    public record IdiomaRequest(
            UUID id,
            @NotBlank(message = "El idioma es obligatorio") String idioma,
            String nivelEscritura,
            String nivelLectura,
            String nivelConversacion
    ) {}

    // ✅ Nuevo Record para Cursos
    public record CursoRequest(
            UUID id,
            @NotBlank(message = "El nombre del curso es obligatorio") String nombreCurso,
            @NotBlank(message = "La institución es obligatoria") String institucion,
            @NotNull(message = "La duración en horas es obligatoria") @Min(value = 1, message = "La duración debe ser al menos 1 hora") Integer duracionHoras,
            @NotNull(message = "La fecha de realización es obligatoria") LocalDate fechaRealizacion,
            String documentoSoportePath
    ) {}
}