package mx.ine.gestiona_t.modules.auth.repository;

import mx.ine.gestiona_t.modules.auth.model.CodigoOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodigoOTPRepository extends JpaRepository<CodigoOTP, Long> {
    
    @Query("SELECT c FROM CodigoOTP c WHERE c.aspiranteId = :aspiranteId " +
           "AND c.utilizado = false AND c.fechaExpiracion > :ahora " +
           "ORDER BY c.createdAt DESC LIMIT 1")
    Optional<CodigoOTP> findUltimoNoUtilizado(@Param("aspiranteId") UUID aspiranteId,
                                               @Param("ahora") LocalDateTime ahora);
    
    @Modifying
    @Transactional
    @Query("UPDATE CodigoOTP c SET c.utilizado = true WHERE c.aspiranteId = :aspiranteId " +
           "AND c.utilizado = false")
    void invalidarTodos(@Param("aspiranteId") UUID aspiranteId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM CodigoOTP c WHERE c.fechaExpiracion < :ahora")
    void eliminarExpirados(@Param("ahora") LocalDateTime ahora);
}