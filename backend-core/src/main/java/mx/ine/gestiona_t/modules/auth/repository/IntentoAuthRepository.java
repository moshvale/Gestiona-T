package mx.ine.gestiona_t.modules.auth.repository;

import mx.ine.gestiona_t.modules.auth.model.IntentoAuth;
import mx.ine.gestiona_t.modules.auth.model.enums.ResultadoIntento;
import mx.ine.gestiona_t.modules.auth.model.enums.TipoIntento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface IntentoAuthRepository extends JpaRepository<IntentoAuth, Long> {
    
    @Query("SELECT COUNT(i) FROM IntentoAuth i WHERE i.ipOrigen = :ip " +
           "AND i.resultado = :resultado AND i.timestamp >= :desde")
    long contarIntentosPorIpDesde(@Param("ip") String ip, 
                                   @Param("resultado") ResultadoIntento resultado,
                                   @Param("desde") LocalDateTime desde);
    
    @Query("SELECT COUNT(i) FROM IntentoAuth i WHERE i.correoIntentado = :correo " +
           "AND i.tipo = :tipo AND i.resultado = :resultado AND i.timestamp >= :desde")
    long contarIntentosPorCorreoDesde(@Param("correo") String correo,
                                       @Param("tipo") TipoIntento tipo,
                                       @Param("resultado") ResultadoIntento resultado,
                                       @Param("desde") LocalDateTime desde);
}