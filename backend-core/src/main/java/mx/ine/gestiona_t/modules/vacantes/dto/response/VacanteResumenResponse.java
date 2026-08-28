package mx.ine.gestiona_t.modules.vacantes.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record VacanteResumenResponse(
    UUID id,
    String puesto,
    String numeroPlaza,
    String nivelTabular,
    Integer numeroVacantes,
    BigDecimal percepcionBruta,
    BigDecimal percepcionNeta,
    String ciudadPlaza,
    LocalDate fechaInicio,
    LocalDate fechaLimite,
    boolean vigente,
    String faseConcurso
) {}