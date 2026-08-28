package mx.ine.gestiona_t.modules.documentos.repository;

import mx.ine.gestiona_t.modules.documentos.model.CatalogoInstitucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogoInstitucionRepository extends JpaRepository<CatalogoInstitucion, Long> {
    
    List<CatalogoInstitucion> findByTipoAndAcreditadaTrue(String tipo);
    
    @Query("SELECT c FROM CatalogoInstitucion c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND c.acreditada = true")
    List<CatalogoInstitucion> buscarPorNombre(@Param("nombre") String nombre);
    
    Optional<CatalogoInstitucion> findByClaveAndTipo(String clave, String tipo);
}