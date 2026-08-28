package mx.ine.gestiona_t.modules.documentos.service;

import mx.ine.gestiona_t.modules.documentos.dto.response.CatalogoResponse;
import mx.ine.gestiona_t.modules.documentos.model.CatalogoInstitucion;
import mx.ine.gestiona_t.modules.documentos.repository.CatalogoInstitucionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogoService {
    
    private static final Logger log = LoggerFactory.getLogger(CatalogoService.class);
    private final CatalogoInstitucionRepository catalogoRepository;
    
    public CatalogoService(CatalogoInstitucionRepository catalogoRepository) {
        this.catalogoRepository = catalogoRepository;
    }
    
    public List<CatalogoResponse> obtenerInstituciones(String tipo) {
        return catalogoRepository.findByTipoAndAcreditadaTrue(tipo).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public List<CatalogoResponse> buscarInstituciones(String nombre) {
        return catalogoRepository.buscarPorNombre(nombre).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    private CatalogoResponse mapToResponse(CatalogoInstitucion c) {
        return new CatalogoResponse(
            c.getId(), c.getTipo(), c.getNombre(), c.getClave(),
            c.getEntidadFederativa(), c.isAcreditada(), c.getFuenteOficial()
        );
    }
}