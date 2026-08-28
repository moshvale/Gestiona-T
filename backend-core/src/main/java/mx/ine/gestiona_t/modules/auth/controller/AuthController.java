package mx.ine.gestiona_t.modules.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import mx.ine.gestiona_t.modules.auth.dto.request.*;
import mx.ine.gestiona_t.modules.auth.dto.response.*;
import mx.ine.gestiona_t.modules.auth.model.Aspirante;
import mx.ine.gestiona_t.modules.auth.service.AuthService;
import mx.ine.gestiona_t.modules.auth.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "${cors.allowed.origins}")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/registro/iniciar")
    public ResponseEntity<Mono<MensajeResponse>> iniciarRegistro(@Valid @RequestBody RegistroIniciarRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        log.info("POST /api/v1/auth/registro/iniciar - IP: {}", ip);
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.iniciarRegistro(request, ip, userAgent));
    }

    @PostMapping("/registro/reenviar-otp")
    public ResponseEntity<Mono<MensajeResponse>> reenviarOtp(@Valid @RequestBody ReenviarOtpRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        log.info("POST /api/v1/auth/registro/reenviar-otp - IP: {}", ip);
        return ResponseEntity.ok(authService.reenviarOtp(request, ip, userAgent));
    }

    @PostMapping("/registro/verificar-otp")
    public ResponseEntity<Mono<TokenResponse>> verificarOtp(@Valid @RequestBody VerificarOtpRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        log.info("POST /api/v1/auth/registro/verificar-otp - IP: {}", ip);
        return ResponseEntity.ok(authService.verificarOtp(request, ip, userAgent));
    }

    @PostMapping("/registro/validar-curp")
    public ResponseEntity<Mono<TokenResponse>> validarCurp(@Valid @RequestBody ValidarCurpRequest request, @RequestHeader("Authorization") String authHeader, HttpServletRequest httpRequest) {
        String tokenTemporal = extractToken(authHeader);
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        log.info("POST /api/v1/auth/registro/validar-curp - IP: {}", ip);
        return ResponseEntity.ok(authService.validarCurp(request, tokenTemporal, ip, userAgent));
    }

    @PostMapping("/registro/validar-clave-elector")
    public ResponseEntity<Mono<TokenResponse>> validarClaveElector(@Valid @RequestBody ValidarClaveElectorRequest request, @RequestHeader("Authorization") String authHeader, HttpServletRequest httpRequest) {
        String tokenTemporal = extractToken(authHeader);
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        log.info("POST /api/v1/auth/registro/validar-clave-elector - IP: {}", ip);
        return ResponseEntity.ok(authService.validarClaveElector(request, tokenTemporal, ip, userAgent));
    }

    @PostMapping("/login")
    public ResponseEntity<Mono<TokenResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        log.info("POST /api/v1/auth/login - IP: {}", ip);
        return ResponseEntity.ok(authService.login(request, ip, userAgent));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Mono<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("POST /api/v1/auth/refresh");
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<Mono<ValidacionResponse>> validarToken(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        log.info("GET /api/v1/auth/validate");
        return ResponseEntity.ok(authService.validarToken(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<MensajeResponse> logout(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        authService.logout(token);
        log.info("POST /api/v1/auth/logout");
        return ResponseEntity.ok(new MensajeResponse("Sesión cerrada exitosamente"));
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> obtenerPerfil() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String aspiranteIdStr = authentication.getName(); 
        try {
            UUID aspiranteId = UUID.fromString(aspiranteIdStr);
            Aspirante aspirante = authService.obtenerPerfil(aspiranteId).block();
            if (aspirante != null) {
                aspirante.setPasswordHash(null);
            }
            return ResponseEntity.ok(aspirante);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ✅ NUEVO: Obtener perfil de un aspirante específico por su ID (Solo Analistas)
    // Asegúrate de que este método aparezca UNA SOLA VEZ en el archivo.
    @GetMapping("/aspirante/{aspiranteId}")
    public ResponseEntity<?> obtenerPerfilAspirante(@PathVariable UUID aspiranteId) {
        log.info("GET /api/v1/auth/aspirante/{} - Consultando perfil de aspirante específico", aspiranteId);
        try {
            Aspirante aspirante = authService.obtenerPerfil(aspiranteId).block();
            if (aspirante != null) {
                aspirante.setPasswordHash(null);
            }
            return ResponseEntity.ok(aspirante);
        } catch (RuntimeException e) {
            log.warn("⚠️ No se encontró aspirante con ID {}: {}", aspiranteId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/perfil")
    public Mono<ResponseEntity<MensajeResponse>> eliminarPerfil(@RequestHeader("Authorization") String authHeader, HttpServletRequest request) {
        String token = authHeader.substring(7);
        UUID aspiranteId = jwtService.extractAspiranteId(token);
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return authService.eliminarPerfil(aspiranteId, ip, userAgent).map(response -> ResponseEntity.ok(response));
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Token no proporcionado");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}