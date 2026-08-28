package mx.ine.gestiona_t.modules.cv.repository;

import mx.ine.gestiona_t.modules.cv.model.Escolaridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EscolaridadRepository extends JpaRepository<Escolaridad, UUID> {
    
    List<Escolaridad> findByCvId(UUID cvId);
    
    void deleteByCvIdAndId(UUID cvId, UUID id);
}