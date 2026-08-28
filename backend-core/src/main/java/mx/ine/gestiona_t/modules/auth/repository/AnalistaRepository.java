package mx.ine.gestiona_t.modules.auth.repository;

import mx.ine.gestiona_t.modules.auth.model.Analista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalistaRepository extends JpaRepository<Analista, UUID> {
    Optional<Analista> findByCorreoElectronicoAndActivoTrue(String correoElectronico);
    Optional<Analista> findByIdAndActivoTrue(UUID id);
}