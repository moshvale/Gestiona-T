package mx.ine.gestiona_t.modules.expedientes.repository;

import mx.ine.gestiona_t.modules.expedientes.model.Vocalia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VocaliaRepository extends JpaRepository<Vocalia, UUID> {
    List<Vocalia> findByJuntaEjecutivaIdAndActivaTrue(UUID juntaEjecutivaId);
}