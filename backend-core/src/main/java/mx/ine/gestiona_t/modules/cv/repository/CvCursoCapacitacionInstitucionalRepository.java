package mx.ine.gestiona_t.modules.cv.repository;

import mx.ine.gestiona_t.modules.cv.model.CvCursoCapacitacionInstitucional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CvCursoCapacitacionInstitucionalRepository 
        extends JpaRepository<CvCursoCapacitacionInstitucional, UUID> {
    
    List<CvCursoCapacitacionInstitucional> findByCvId(UUID cvId);
    
    void deleteByCvId(UUID cvId);
    
    boolean existsByCvId(UUID cvId);
}