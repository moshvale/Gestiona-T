package mx.ine.gestiona_t.modules.cartadeclaratoria.service;

import mx.ine.gestiona_t.modules.cartadeclaratoria.dto.response.BloqueResponse;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.BloqueDeclaratorio;
import mx.ine.gestiona_t.modules.cartadeclaratoria.repository.AceptacionBloqueRepository;
import mx.ine.gestiona_t.modules.cartadeclaratoria.repository.BloqueDeclaratorioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BloquesService {
    
    private static final Logger log = LoggerFactory.getLogger(BloquesService.class);
    private static final int TOTAL_BLOQUES = 12;
    
    private final BloqueDeclaratorioRepository bloqueRepository;
    private final AceptacionBloqueRepository aceptacionRepository;
    
    public BloquesService(BloqueDeclaratorioRepository bloqueRepository,
                          AceptacionBloqueRepository aceptacionRepository) {
        this.bloqueRepository = bloqueRepository;
        this.aceptacionRepository = aceptacionRepository;
    }
    
    public List<BloqueResponse> obtenerBloquesConEstatus(UUID cartaId) {
        List<BloqueDeclaratorio> bloques = bloqueRepository.findByActivoTrueOrderByOrdenAsc();
        
        return bloques.stream()
            .map(b -> {
                boolean aceptado = aceptacionRepository
                    .existsByCartaIdAndBloqueIdAndAceptadoTrue(cartaId, b.getId());
                
                return new BloqueResponse(
                    b.getId(),
                    b.getTitulo(),
                    b.getTexto(),
                    b.getFundamentoLegal(),
                    b.isObligatorio(),
                    b.getOrden(),
                    aceptado
                );
            })
            .collect(Collectors.toList());
    }
    
    public int contarBloquesActivos() {
        return bloqueRepository.findByActivoTrueOrderByOrdenAsc().size();
    }
    
    public boolean todosBloquesAceptados(UUID cartaId) {
        long aceptados = aceptacionRepository.countAceptadosPorCarta(cartaId);
        int totalActivos = contarBloquesActivos();
        return aceptados >= totalActivos;
    }
}