package mx.ine.gestiona_t.modules.vacantes.repository;

import mx.ine.gestiona_t.modules.vacantes.model.Vacante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VacanteRepository extends JpaRepository<Vacante, UUID> {

    List<Vacante> findByActivaTrueOrderByCreatedAtDesc();

    Optional<Vacante> findByNumeroPlaza(String numeroPlaza);

    boolean existsByNumeroPlaza(String numeroPlaza);

    @Query("SELECT v FROM Vacante v WHERE v.activa = true AND v.fechaLimite >= :hoy ORDER BY v.fechaLimite ASC")
    List<Vacante> findVacantesVigentes(@Param("hoy") LocalDate hoy);

    @Query("SELECT v FROM Vacante v WHERE v.activa = true AND " +
           "(LOWER(v.puesto) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(v.numeroPlaza) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(v.nivelTabular) LIKE LOWER(CONCAT('%', :busqueda, '%'))) " +
           "ORDER BY v.createdAt DESC")
    List<Vacante> buscarVacantes(@Param("busqueda") String busqueda);

    @Query("SELECT COUNT(v) FROM Vacante v WHERE v.activa = true")
    long countVacantesActivas();

    @Query("SELECT COALESCE(SUM(v.numeroVacantes), 0) FROM Vacante v WHERE v.activa = true")
    long sumTotalPlazas();
}