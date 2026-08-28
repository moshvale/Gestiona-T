package mx.ine.gestiona_t.modules.auth.service;

import mx.ine.gestiona_t.modules.auth.dto.request.CrearAnalistaRequest;
import mx.ine.gestiona_t.modules.auth.dto.response.AnalistaResumenDTO;
import mx.ine.gestiona_t.modules.auth.dto.response.MensajeResponse;
import mx.ine.gestiona_t.modules.auth.model.Analista;
import mx.ine.gestiona_t.modules.auth.repository.AnalistaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnalistaServiceImpl implements AnalistaService {

    private static final Logger log = LoggerFactory.getLogger(AnalistaServiceImpl.class);
    private final AnalistaRepository analistaRepository;
    private final PasswordEncoder passwordEncoder;

    public AnalistaServiceImpl(AnalistaRepository analistaRepository, PasswordEncoder passwordEncoder) {
        this.analistaRepository = analistaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public MensajeResponse crearAnalista(CrearAnalistaRequest request, UUID adminId, String ip, String userAgent) {
        log.info("🔐 Intento de alta de analista - Admin: {} | IP: {} | Correo destino: {}", adminId, ip, request.correoElectronico());

        if (analistaRepository.findByCorreoElectronicoAndActivoTrue(request.correoElectronico()).isPresent()) {
            throw new RuntimeException("Ya existe un analista activo con el correo: " + request.correoElectronico());
        }

        Analista nuevoAnalista = new Analista();
        nuevoAnalista.setNombreCompleto(request.nombreCompleto().trim());
        nuevoAnalista.setCorreoElectronico(request.correoElectronico().trim().toLowerCase());
        nuevoAnalista.setPasswordHash(passwordEncoder.encode(request.password()));
        nuevoAnalista.setRol(request.rol());
        nuevoAnalista.setActivo(true);

        analistaRepository.save(nuevoAnalista);

        log.info("✅ Analista creado exitosamente - ID: {} | Correo: {} | Rol: {} | Creado por Admin: {}", 
                 nuevoAnalista.getId(), nuevoAnalista.getCorreoElectronico(), nuevoAnalista.getRol(), adminId);

        return new MensajeResponse(String.format("Analista '%s' creado exitosamente con rol '%s'.", nuevoAnalista.getNombreCompleto(), nuevoAnalista.getRol()), 201);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalistaResumenDTO> listarAnalistas() {
        // ✅ CAMBIO: Mapeamos a DTO seguro, nunca exponemos la entidad JPA ni el passwordHash
        return analistaRepository.findAll().stream()
                .filter(Analista::getActivo)
                .map(a -> new AnalistaResumenDTO(
                        a.getId(),
                        a.getNombreCompleto(),
                        a.getCorreoElectronico(),
                        a.getRol(),
                        a.getActivo(),
                        a.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MensajeResponse desactivarAnalista(UUID analistaId, UUID adminId) {
        Analista analista = analistaRepository.findById(analistaId)
                .orElseThrow(() -> new RuntimeException("Analista no encontrado"));

        if (!analista.getActivo()) {
            throw new RuntimeException("El analista ya está desactivado");
        }

        analista.setActivo(false);
        analistaRepository.save(analista);

        log.info("🚫 Analista desactivado - ID: {} | Correo: {} | Desactivado por Admin: {}", analistaId, analista.getCorreoElectronico(), adminId);
        return new MensajeResponse("Analista desactivado exitosamente.", 200);
    }
}