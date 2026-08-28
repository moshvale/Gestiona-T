package mx.ine.gestiona_t.modules.auth.controller;

import mx.ine.gestiona_t.modules.auth.dto.request.LoginAnalistaRequest;
import mx.ine.gestiona_t.modules.auth.dto.response.TokenResponse;
import mx.ine.gestiona_t.modules.auth.model.Analista;
import mx.ine.gestiona_t.modules.auth.repository.AnalistaRepository;
import mx.ine.gestiona_t.modules.auth.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth/analista")
public class AnalistaAuthController {

    private static final Logger log = LoggerFactory.getLogger(AnalistaAuthController.class);

    private final AnalistaRepository analistaRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration.ms}")
    private long jwtExpirationMs;

    public AnalistaAuthController(AnalistaRepository analistaRepository,
                                   JwtService jwtService,
                                   PasswordEncoder passwordEncoder) {
        this.analistaRepository = analistaRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> loginAnalista(
            @RequestBody LoginAnalistaRequest request,
            HttpServletRequest httpRequest) {

        log.info("🔐 Intento de login de analista: {}", request.correo());

        Analista analista = analistaRepository
            .findByCorreoElectronicoAndActivoTrue(request.correo())
            .orElse(null);

        if (analista == null) {
            log.warn("❌ Analista no encontrado o inactivo: {}", request.correo());
            return ResponseEntity.status(401).build();
        }

        if (!passwordEncoder.matches(request.password(), analista.getPasswordHash())) {
            log.warn("❌ Contraseña incorrecta para analista: {}", request.correo());
            return ResponseEntity.status(401).build();
        }

        analista.setUltimoAcceso(LocalDateTime.now());
        analistaRepository.save(analista);

        String accessToken = jwtService.generateAccessTokenAnalista(
            analista.getId(), 
            analista.getCorreoElectronico(), 
            analista.getRol()
        );
        String refreshToken = jwtService.generateRefreshTokenAnalista(analista.getId());

        log.info("✅ Analista autenticado exitosamente: {} - Rol: {}", analista.getCorreoElectronico(), analista.getRol());

        long expiresIn = jwtExpirationMs / 1000;
        TokenResponse response = new TokenResponse(
            accessToken,
            refreshToken,
            expiresIn,
            "ANALISTA",
            analista.getRol()
        );

        return ResponseEntity.ok(response);
    }
}