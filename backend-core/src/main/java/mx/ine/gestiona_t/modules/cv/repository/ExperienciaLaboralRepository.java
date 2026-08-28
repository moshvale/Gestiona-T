package mx.ine.gestiona_t.modules.cv.repository;

import mx.ine.gestiona_t.modules.cv.model.ExperienciaLaboral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExperienciaLaboralRepository extends JpaRepository<ExperienciaLaboral, UUID> {
    
    List<ExperienciaLaboral> findByCvId(UUID cvId);
    
    void deleteByCvIdAndId(UUID cvId, UUID id);
}