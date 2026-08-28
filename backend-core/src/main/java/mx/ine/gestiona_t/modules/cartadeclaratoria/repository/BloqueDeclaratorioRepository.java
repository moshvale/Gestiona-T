package mx.ine.gestiona_t.modules.cartadeclaratoria.repository;

import mx.ine.gestiona_t.modules.cartadeclaratoria.model.BloqueDeclaratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BloqueDeclaratorioRepository extends JpaRepository<BloqueDeclaratorio, Integer> {
    
    List<BloqueDeclaratorio> findByActivoTrueOrderByOrdenAsc();
    
    List<BloqueDeclaratorio> findByObligatorioTrueAndActivoTrue();
}