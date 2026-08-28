package mx.ine.gestiona_t.modules.auditoria.repository;

import mx.ine.gestiona_t.modules.auditoria.model.EventoAuditoria;
import mx.ine.gestiona_t.modules.auditoria.model.enums.CategoriaEvento;
import mx.ine.gestiona_t.modules.auditoria.model.enums.NivelSeveridad;
import mx.ine.gestiona_t.modules.auditoria.model.enums.TipoEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventoAuditoriaRepository extends JpaRepository<EventoAuditoria, UUID> {
    
    List<EventoAuditoria> findByActorIdOrderByTimestampDesc(UUID actorId);
    
    Page<EventoAuditoria> findByCategoriaAndTimestampBetween(
        CategoriaEvento categoria, LocalDateTime desde, LocalDateTime hasta, Pageable pageable
    );
    
    Page<EventoAuditoria> findBySeveridad(NivelSeveridad severidad, Pageable pageable);
    
    @Query("SELECT e FROM EventoAuditoria e WHERE e.timestamp BETWEEN :desde AND :hasta " +
           "ORDER BY e.timestamp DESC")
    Page<EventoAuditoria> findByRangoFechas(
        @Param("desde") LocalDateTime desde,
        @Param("hasta") LocalDateTime hasta,
        Pageable pageable
    );
    
    @Query("SELECT e FROM EventoAuditoria e WHERE e.moduloOrigen = :modulo " +
           "ORDER BY e.timestamp DESC")
    List<EventoAuditoria> findByModulo(@Param("modulo") String modulo);
    
    @Query("SELECT e FROM EventoAuditoria e WHERE e.recursoAfectado = :recurso " +
           "ORDER BY e.timestamp DESC")
    List<EventoAuditoria> findByRecurso(@Param("recurso") String recurso);
    
    @Query("SELECT e FROM EventoAuditoria e ORDER BY e.timestamp DESC LIMIT 1")
    Optional<EventoAuditoria> findUltimoEvento();
    
    @Query("SELECT COUNT(e) FROM EventoAuditoria e WHERE e.timestamp >= :desde")
    long countEventosDesde(@Param("desde") LocalDateTime desde);
    
    @Query("SELECT COUNT(e) FROM EventoAuditoria e WHERE e.categoria = :categoria " +
           "AND e.timestamp BETWEEN :desde AND :hasta")
    long countPorCategoriaEnRango(
        @Param("categoria") CategoriaEvento categoria,
        @Param("desde") LocalDateTime desde,
        @Param("hasta") LocalDateTime hasta
    );
    
    @Query("SELECT e.moduloOrigen, COUNT(e) FROM EventoAuditoria e " +
           "WHERE e.timestamp >= :desde GROUP BY e.moduloOrigen")
    List<Object[]> contarPorModulo(@Param("desde") LocalDateTime desde);
    
    @Query("SELECT e.severidad, COUNT(e) FROM EventoAuditoria e " +
           "WHERE e.timestamp >= :desde GROUP BY e.severidad")
    List<Object[]> contarPorSeveridad(@Param("desde") LocalDateTime desde);
}