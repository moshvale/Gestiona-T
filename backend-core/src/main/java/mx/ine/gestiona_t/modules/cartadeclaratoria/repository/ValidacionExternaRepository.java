package mx.ine.gestiona_t.modules.cartadeclaratoria.repository;

import mx.ine.gestiona_t.modules.cartadeclaratoria.model.ValidacionExterna;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.enums.TipoValidacionExterna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ValidacionExternaRepository extends JpaRepository<ValidacionExterna, Long> {
    
    List<ValidacionExterna> findByCartaId(UUID cartaId);
    
    Optional<ValidacionExterna> findByCartaIdAndTipoValidacion(UUID cartaId, TipoValidacionExterna tipo);
    
    @Query("SELECT COUNT(v) FROM ValidacionExterna v WHERE v.cartaId = :cartaId AND v.resultado = false")
    long countRechazadasPorCarta(@Param("cartaId") UUID cartaId);
    
    @Query("SELECT v FROM ValidacionExterna v WHERE v.cartaId = :cartaId AND v.resultado = false")
    List<ValidacionExterna> findRechazadasPorCarta(@Param("cartaId") UUID cartaId);
}