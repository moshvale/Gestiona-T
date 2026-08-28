package mx.ine.gestiona_t.modules.auditoria.repository;

import mx.ine.gestiona_t.modules.auditoria.model.CadenaHash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CadenaHashRepository extends JpaRepository<CadenaHash, Long> {
    
    Optional<CadenaHash> findByEventoId(UUID eventoId);
    
    @Query("SELECT c FROM CadenaHash c ORDER BY c.secuencia DESC LIMIT 1")
    Optional<CadenaHash> findUltimo();
    
    @Query("SELECT c FROM CadenaHash c WHERE c.secuencia = :secuencia")
    Optional<CadenaHash> findBySecuencia(@Param("secuencia") Long secuencia);
    
    @Query("SELECT MAX(c.secuencia) FROM CadenaHash c")
    Long findSecuenciaMaxima();
    
    @Query("SELECT c FROM CadenaHash c WHERE c.secuencia BETWEEN :desde AND :hasta " +
           "ORDER BY c.secuencia ASC")
    List<CadenaHash> findRango(@Param("desde") Long desde, @Param("hasta") Long hasta);
    
    long count();
}