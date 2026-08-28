package mx.ine.gestiona_t.modules.auditoria.repository;

import mx.ine.gestiona_t.modules.auditoria.model.ConfiguracionRetencion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConfiguracionRetencionRepository extends JpaRepository<ConfiguracionRetencion, Long> {
    
    Optional<ConfiguracionRetencion> findByCategoria(String categoria);
    
    List<ConfiguracionRetencion> findByActivoTrue();
    
    boolean existsByCategoria(String categoria);
}