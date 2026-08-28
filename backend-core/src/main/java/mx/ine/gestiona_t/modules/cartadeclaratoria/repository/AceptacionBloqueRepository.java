package mx.ine.gestiona_t.modules.cartadeclaratoria.repository;

import mx.ine.gestiona_t.modules.cartadeclaratoria.model.AceptacionBloque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AceptacionBloqueRepository extends JpaRepository<AceptacionBloque, Long> {
    
    List<AceptacionBloque> findByCartaId(UUID cartaId);
    
    Optional<AceptacionBloque> findByCartaIdAndBloqueId(UUID cartaId, Integer bloqueId);
    
    @Query("SELECT COUNT(a) FROM AceptacionBloque a WHERE a.cartaId = :cartaId AND a.aceptado = true")
    long countAceptadosPorCarta(@Param("cartaId") UUID cartaId);
    
    @Query("SELECT COUNT(a) FROM AceptacionBloque a WHERE a.cartaId = :cartaId")
    long countTotalPorCarta(@Param("cartaId") UUID cartaId);
    
    boolean existsByCartaIdAndBloqueIdAndAceptadoTrue(UUID cartaId, Integer bloqueId);
}