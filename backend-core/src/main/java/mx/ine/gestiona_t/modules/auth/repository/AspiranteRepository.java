package mx.ine.gestiona_t.modules.auth.repository;

import mx.ine.gestiona_t.modules.auth.model.Aspirante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AspiranteRepository extends JpaRepository<Aspirante, UUID> {
    
    Optional<Aspirante> findByFolio(String folio);
    
    Optional<Aspirante> findByCorreoElectronico(String correoElectronico);
    
    Optional<Aspirante> findByCurp(String curp);
    
    boolean existsByCorreoElectronico(String correoElectronico);
    
    boolean existsByCurp(String curp);
    
    @Query("SELECT a FROM Aspirante a WHERE a.correoElectronico = :correo AND a.activo = true")
    Optional<Aspirante> findActivoByCorreo(@Param("correo") String correo);

    Optional<Aspirante> findByRfc(String rfc);
    boolean existsByRfc(String rfc);
}