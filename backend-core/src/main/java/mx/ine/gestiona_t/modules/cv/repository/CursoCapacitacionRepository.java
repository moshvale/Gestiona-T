package mx.ine.gestiona_t.modules.cv.repository;

import mx.ine.gestiona_t.modules.cv.model.CursoCapacitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CursoCapacitacionRepository extends JpaRepository<CursoCapacitacion, UUID> {
    
    List<CursoCapacitacion> findByCvId(UUID cvId);
    
    void deleteByCvIdAndId(UUID cvId, UUID id);
}