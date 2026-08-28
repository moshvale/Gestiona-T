package mx.ine.gestiona_t.modules.vacantes.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CrearVacanteRequest(
    @NotBlank(message = "El puesto es obligatorio")
    @Size(max = 200)
    String puesto,

    @NotBlank(message = "El número de plaza es obligatorio")
    String numeroPlaza,

    @NotBlank(message = "El nivel tabular es obligatorio")
    String nivelTabular,

    @NotNull(message = "El número de vacantes es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 vacante")
    Integer numeroVacantes,

    @NotBlank(message = "La descripción de funciones es obligatoria")
    String descripcionFunciones,

    @NotBlank(message = "La escolaridad es obligatoria")
    String escolaridad,

    String experiencia,
    String conocimientos,
    String habilidades,
    String actitudes,

    @NotNull(message = "La percepción bruta es obligatoria")
    @DecimalMin(value = "0.0", message = "La percepción bruta debe ser positiva")
    BigDecimal percepcionBruta,

    @NotNull(message = "La percepción neta es obligatoria")
    @DecimalMin(value = "0.0", message = "La percepción neta debe ser positiva")
    BigDecimal percepcionNeta,

    @NotBlank(message = "La ciudad es obligatoria")
    String ciudadPlaza,

    @NotBlank(message = "La ubicación es obligatoria")
    String ubicacionPlaza,

    @NotBlank(message = "El lugar de recepción de documentos es obligatorio")
    String lugarRecepcionDocumentos,

    @NotNull(message = "La fecha de expedición es obligatoria")
    LocalDate fechaExpedicion,

    @NotNull(message = "La fecha de inicio es obligatoria")
    LocalDate fechaInicio,

    @NotNull(message = "La fecha límite es obligatoria")
    LocalDate fechaLimite,

    String horarioAtencion,
    String personaResponsable,
    String faseConcurso,
    String notaImportante,

    List<String> requisitos,
    List<String> documentacionRequerida
) {}