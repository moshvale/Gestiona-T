package mx.ine.gestiona_t.modules.cv.repository;

import mx.ine.gestiona_t.modules.cv.model.CvFormacionAcademica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CvFormacionAcademicaRepository extends JpaRepository<CvFormacionAcademica, UUID> {
    List<CvFormacionAcademica> findByCvId(UUID cvId);
    void deleteByCvId(UUID cvId);
}