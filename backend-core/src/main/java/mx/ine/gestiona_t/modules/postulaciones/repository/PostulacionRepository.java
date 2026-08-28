package mx.ine.gestiona_t.modules.postulaciones.repository;

import mx.ine.gestiona_t.modules.postulaciones.model.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostulacionRepository extends JpaRepository<Postulacion, UUID> {
    List<Postulacion> findAllByOrderByFechaPostulacionDesc();
    List<Postulacion> findByAspiranteIdOrderByFechaPostulacionDesc(UUID aspiranteId);
    List<Postulacion> findByVacanteIdOrderByFechaPostulacionDesc(UUID vacanteId);
    Optional<Postulacion> findByAspiranteIdAndVacanteId(UUID aspiranteId, UUID vacanteId);
    boolean existsByAspiranteIdAndVacanteId(UUID aspiranteId, UUID vacanteId);
}