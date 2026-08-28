package mx.ine.gestiona_t.modules.expedientes.repository;

import mx.ine.gestiona_t.modules.expedientes.model.ExpedienteLaboral;
import mx.ine.gestiona_t.modules.expedientes.model.enums.TipoContratacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpedienteLaboralRepository extends JpaRepository<ExpedienteLaboral, UUID> {

    List<ExpedienteLaboral> findByAspiranteIdOrderByFechaInicioDesc(UUID aspiranteId);

    @Query("SELECT e FROM ExpedienteLaboral e WHERE e.aspiranteId = :aspiranteId AND e.vigente = true")
    Optional<ExpedienteLaboral> findVigenteByAspiranteId(@Param("aspiranteId") UUID aspiranteId);

    List<ExpedienteLaboral> findByVigenteTrueOrderByFechaInicioDesc();

    List<ExpedienteLaboral> findByTipoContratacionAndVigenteTrue(TipoContratacion tipoContratacion);

    @Query("SELECT e FROM ExpedienteLaboral e WHERE e.juntaEjecutiva.id = :juntaId AND e.vigente = true")
    List<ExpedienteLaboral> findByJuntaEjecutivaAndVigente(@Param("juntaId") UUID juntaId);

    boolean existsByNumeroEmpleado(String numeroEmpleado);

    @Query("SELECT e FROM ExpedienteLaboral e WHERE e.numeroEmpleado = :numeroEmpleado AND e.vigente = true")
    Optional<ExpedienteLaboral> findVigenteByNumeroEmpleado(@Param("numeroEmpleado") String numeroEmpleado);
}