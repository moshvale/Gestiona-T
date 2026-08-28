package mx.ine.gestiona_t.modules.expedientes.repository;

import mx.ine.gestiona_t.modules.expedientes.model.JuntaEjecutiva;
import mx.ine.gestiona_t.modules.expedientes.model.enums.TipoJunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JuntaEjecutivaRepository extends JpaRepository<JuntaEjecutiva, UUID> {
    List<JuntaEjecutiva> findByActivaTrueOrderByNombreAsc();
    List<JuntaEjecutiva> findByTipoAndActivaTrue(TipoJunta tipo);
}