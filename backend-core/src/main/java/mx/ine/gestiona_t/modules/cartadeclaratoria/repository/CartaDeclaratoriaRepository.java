package mx.ine.gestiona_t.modules.cartadeclaratoria.repository;

import mx.ine.gestiona_t.modules.cartadeclaratoria.model.CartaDeclaratoria;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.enums.EstatusCarta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartaDeclaratoriaRepository extends JpaRepository<CartaDeclaratoria, UUID> {
    
        Optional<CartaDeclaratoria> findByFolio(String folio);
    
        // Devuelve el registro más reciente por folio (en caso de duplicados, usa el más nuevo)
        Optional<CartaDeclaratoria> findFirstByFolioOrderByCreatedAtDesc(String folio);
    
        Optional<CartaDeclaratoria> findByFolioCarta(String folioCarta);
    
        // Devuelve la última carta creada para un aspirante (ordenada por createdAt desc)
        Optional<CartaDeclaratoria> findFirstByAspiranteIdOrderByCreatedAtDesc(UUID aspiranteId);
    
    @Query("SELECT COUNT(c) FROM CartaDeclaratoria c WHERE c.estatus = :estatus")
    long countByEstatus(@Param("estatus") EstatusCarta estatus);
    
    List<CartaDeclaratoria> findByEstatus(EstatusCarta estatus);
}