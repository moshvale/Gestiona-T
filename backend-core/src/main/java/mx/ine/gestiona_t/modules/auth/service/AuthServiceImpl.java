package mx.ine.gestiona_t.modules.auth.service;

import mx.ine.gestiona_t.common.exceptions.RegistroDuplicadoException;
import mx.ine.gestiona_t.modules.auth.dto.request.*;
import mx.ine.gestiona_t.modules.auth.dto.response.*;
import mx.ine.gestiona_t.modules.auth.integration.dto.ListaNominalResponse;
import mx.ine.gestiona_t.modules.auth.integration.dto.RenapoResponse;
import mx.ine.gestiona_t.modules.auth.model.Aspirante;
import mx.ine.gestiona_t.modules.auth.model.IntentoAuth;
import mx.ine.gestiona_t.modules.auth.model.enums.*;
import mx.ine.gestiona_t.modules.auth.repository.AspiranteRepository;
import mx.ine.gestiona_t.modules.auth.repository.IntentoAuthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final int BLOQUEO_HORAS = 24;
    
    private final AspiranteRepository aspiranteRepository;
    private final IntentoAuthRepository intentoAuthRepository;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final RenapoIntegrationService renapoService;
    private final ListaNominalService listaNominalService;
    private final BCryptPasswordEncoder passwordEncoder;
    
    public AuthServiceImpl(AspiranteRepository aspiranteRepository,
                           IntentoAuthRepository intentoAuthRepository,
                           JwtService jwtService,
                           OtpService otpService,
                           RenapoIntegrationService renapoService,
                           ListaNominalService listaNominalService) {
        this.aspiranteRepository = aspiranteRepository;
        this.intentoAuthRepository = intentoAuthRepository;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.renapoService = renapoService;
        this.listaNominalService = listaNominalService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

        @Override
        @Transactional
        public Mono<MensajeResponse> reenviarOtp(ReenviarOtpRequest request, String ip, String userAgent) {
        Aspirante aspirante = aspiranteRepository.findByCorreoElectronico(request.correo())
            .filter(item -> item.getEstatus() == EstatusAspirante.PRE_REGISTRO)
            .orElseThrow(() -> new RuntimeException("No existe un registro pendiente para este correo."));

        otpService.generarYEnviarOTP(
            aspirante.getId(),
            aspirante.getCorreoElectronico(),
            aspirante.getTelefonoMovil()
        );
        registrarIntento(ip, userAgent, aspirante.getCurp(), request.correo(),
            TipoIntento.REGISTRO_INICIAR, ResultadoIntento.EXITOSO, "OTP reenviado");

        return Mono.just(new MensajeResponse("Se generó un nuevo código de verificación.", 200));
        }
    
    @Override
    @Transactional
    public Mono<MensajeResponse> iniciarRegistro(RegistroIniciarRequest request, String ip, String userAgent) {
        log.info("🚀 INICIANDO REGISTRO - Validación de duplicados y longitud de campos");

        // 1. Validar si ya existe por Correo
        if (aspiranteRepository.existsByCorreoElectronico(request.correo())) {
            registrarIntento(ip, userAgent, null, request.correo(),
                    TipoIntento.REGISTRO_INICIAR, ResultadoIntento.FALLIDO,
                    "Correo ya registrado");
            throw new RegistroDuplicadoException("El correo electrónico ya está registrado. ¿Olvidaste tu contraseña? Solicita un restablecimiento.");
        }

        // 2. Validar si ya existe por CURP (si se proporciona en el request)
        if (request.curp() != null && !request.curp().isBlank()) {
            String curpBusqueda = request.curp().toUpperCase().trim();
            if (aspiranteRepository.existsByCurp(curpBusqueda)) {
                Optional<Aspirante> aspiranteExistente = aspiranteRepository.findByCurp(curpBusqueda);
                if (aspiranteExistente.isPresent() && aspiranteExistente.get().isActivo()) {
                    registrarIntento(ip, userAgent, curpBusqueda, request.correo(),
                            TipoIntento.REGISTRO_INICIAR, ResultadoIntento.FALLIDO,
                            "CURP ya registrada y activa");
                    throw new RegistroDuplicadoException("Ya existe una cuenta activa con esta CURP. ¿Olvidaste tu contraseña? Solicita un restablecimiento.");
                }
            }
        }

        // 3. Crear la entidad
        Aspirante aspirante = new Aspirante();
        aspirante.setCorreoElectronico(request.correo());
        aspirante.setTelefonoMovil(request.telefono());

        // 4. Asignar CURP (con fallback seguro de exactamente 18 caracteres)
        String curpTemp = (request.curp() != null && !request.curp().isBlank())
                ? request.curp().toUpperCase().trim()
                : "PENDIENTE000000000"; // Exactamente 18 caracteres
        if (curpTemp.length() > 18) {
            log.warn("CURP recibida excede 18 caracteres, truncando a 18: {}", curpTemp);
            curpTemp = curpTemp.substring(0, 18);
        }
        aspirante.setCurp(curpTemp);

        // 5. Asignar estados y seguridad
        aspirante.setEstatus(EstatusAspirante.REGISTRO_VALIDADO);
        aspirante.setActivo(true);
        aspirante.setPasswordHash(passwordEncoder.encode(request.password()));
        aspirante.setNombreCompleto("ASPIRANTE EN PRE-REGISTRO");
        aspirante.setEntidadFederativa("XX");

        // 6. ✅ CORREGIDO: Generar un RFC temporal único de EXACTAMENTE 13 caracteres.
        // "XAXX010101000" causaba duplicados. "PENDIENTE-..." excedía el límite de 13.
        // "TMP" (3 caracteres) + 10 caracteres aleatorios = 13 caracteres exactos.
        String rfcTemporal = "TMP" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        aspirante.setRfc(rfcTemporal);
        
        aspirante.setFechaNacimiento(java.time.LocalDateTime.of(1990, 1, 1, 0, 0));

        // 7. GUARDAR EN LA BASE DE DATOS
        aspirante = aspiranteRepository.save(aspirante);
        log.info("✅ Aspirante guardado exitosamente con ID: {}", aspirante.getId());

        registrarIntento(ip, userAgent, request.curp(), request.correo(),
                TipoIntento.REGISTRO_INICIAR, ResultadoIntento.EXITOSO, null);

        return Mono.just(new MensajeResponse("Registro completado. Ya puedes iniciar sesión con tu contraseña.", 200));
    }
                
    @Override
    @Transactional
    public Mono<TokenResponse> verificarOtp(VerificarOtpRequest request, String ip, String userAgent) {
        log.info("Verificando OTP para correo: {}", request.correo());
        
        Aspirante aspirante = aspiranteRepository.findByCorreoElectronico(request.correo())
            .orElse(null);
        
        if (aspirante == null) {
            registrarIntento(ip, userAgent, null, request.correo(), 
                           TipoIntento.REGISTRO_VERIFICAR_OTP, ResultadoIntento.FALLIDO, 
                           "Correo no encontrado");
            return Mono.error(new RuntimeException("Correo no encontrado"));
        }
        
        boolean otpValido = otpService.validarOTP(aspirante.getId(), request.otp());
        
        if (!otpValido) {
            registrarIntento(ip, userAgent, null, request.correo(), 
                           TipoIntento.REGISTRO_VERIFICAR_OTP, ResultadoIntento.FALLIDO, 
                           "OTP inválido o expirado");
            return Mono.error(new RuntimeException("OTP inválido o expirado"));
        }
        
        aspirante.setEstatus(EstatusAspirante.OTP_VERIFICADO);
        aspiranteRepository.save(aspirante);
        
        String tokenTemporal = jwtService.generateAccessToken(
            aspirante.getId(), aspirante.getFolio(), aspirante.getCorreoElectronico(), 0
        );
        
        registrarIntento(ip, userAgent, null, request.correo(), 
                        TipoIntento.REGISTRO_VERIFICAR_OTP, ResultadoIntento.EXITOSO, null);
        
        return Mono.just(new TokenResponse(tokenTemporal, null, 3600, aspirante.getFolio()));
    }
    
    @Override
    @Transactional
    public Mono<TokenResponse> validarCurp(ValidarCurpRequest request, String tokenTemporal, 
                                            String ip, String userAgent) {
        log.info("Validando CURP: {}", request.curp());
        
        UUID aspiranteId = jwtService.extractAspiranteId(tokenTemporal);
        Aspirante aspirante = aspiranteRepository.findById(aspiranteId)
            .orElseThrow(() -> new RuntimeException("Aspirante no encontrado"));
        
        return renapoService.validarCurp(
            request.curp(), request.nombre(), request.apellidoPaterno(),
            request.apellidoMaterno(), 
            request.fechaNacimiento() != null ? request.fechaNacimiento().toString() : null,
            request.entidadFederativa()
        ).flatMap(response -> {
            if (!response.valido()) {
                registrarIntento(ip, userAgent, request.curp(), aspirante.getCorreoElectronico(),
                               TipoIntento.REGISTRO_VALIDAR_CURP, ResultadoIntento.FALLIDO,
                               response.mensaje());
                return Mono.error(new RuntimeException("CURP no válida: " + response.mensaje()));
            }
            
            aspirante.setCurp(response.curp());
            aspirante.setNombreCompleto(response.nombreCompleto());
            aspirante.setRfc(response.rfc());
            aspirante.setMetodoIdentificacion(MetodoIdentificacion.CURP_RENAPO);
            aspirante.setNivelConfianza(1);
            aspirante.setEstatus(EstatusAspirante.REGISTRO_VALIDADO);
            aspirante.setFechaUltimoAcceso(LocalDateTime.now());
            aspiranteRepository.save(aspirante);
            
            String accessToken = jwtService.generateAccessToken(
                aspirante.getId(), aspirante.getFolio(), 
                aspirante.getCorreoElectronico(), aspirante.getNivelConfianza()
            );
            String refreshToken = jwtService.generateRefreshToken(
                aspirante.getId(), aspirante.getFolio()
            );
            
            registrarIntento(ip, userAgent, request.curp(), aspirante.getCorreoElectronico(),
                           TipoIntento.REGISTRO_VALIDAR_CURP, ResultadoIntento.EXITOSO, null);
            
            return Mono.just(new TokenResponse(accessToken, refreshToken, 3600, aspirante.getFolio()));
        });
    }
    
    @Override
    @Transactional
    public Mono<TokenResponse> validarClaveElector(ValidarClaveElectorRequest request, 
                                                    String tokenTemporal, String ip, String userAgent) {
        log.info("Validando Clave de Elector: {}", request.claveElector());
        
        UUID aspiranteId = jwtService.extractAspiranteId(tokenTemporal);
        Aspirante aspirante = aspiranteRepository.findById(aspiranteId)
            .orElseThrow(() -> new RuntimeException("Aspirante no encontrado"));
        
        return listaNominalService.validarClaveElector(request.claveElector())
            .flatMap(response -> {
                if (!response.vigente()) {
                    registrarIntento(ip, userAgent, null, aspirante.getCorreoElectronico(),
                                   TipoIntento.REGISTRO_VALIDAR_CLAVE_ELECTOR, 
                                   ResultadoIntento.FALLIDO, response.mensaje());
                    return Mono.error(new RuntimeException("Clave de Elector no válida: " + response.mensaje()));
                }
                
                aspirante.setCurp(response.curp());
                aspirante.setNombreCompleto(response.nombreCompleto());
                aspirante.setMetodoIdentificacion(MetodoIdentificacion.CLAVE_ELECTOR_LISTA_NOMINAL);
                aspirante.setNivelConfianza(2);
                aspirante.setEstatus(EstatusAspirante.REGISTRO_VALIDADO);
                aspirante.setFechaUltimoAcceso(LocalDateTime.now());
                aspiranteRepository.save(aspirante);
                
                String accessToken = jwtService.generateAccessToken(
                    aspirante.getId(), aspirante.getFolio(),
                    aspirante.getCorreoElectronico(), aspirante.getNivelConfianza()
                );
                String refreshToken = jwtService.generateRefreshToken(
                    aspirante.getId(), aspirante.getFolio()
                );
                
                registrarIntento(ip, userAgent, null, aspirante.getCorreoElectronico(),
                               TipoIntento.REGISTRO_VALIDAR_CLAVE_ELECTOR, 
                               ResultadoIntento.EXITOSO, null);
                
                return Mono.just(new TokenResponse(accessToken, refreshToken, 3600, aspirante.getFolio()));
            });
    }
    
    @Override
    @Transactional
    public Mono<TokenResponse> login(LoginRequest request, String ip, String userAgent) {
        log.info("🔍 DEBUG LOGIN - Correo recibido: {}", request.correo());
        log.info("🔍 DEBUG LOGIN - ¿Password recibido es NULL? {}", (request.password() == null));
        if (request.password() != null) {
            log.info("🔍 DEBUG LOGIN - Longitud del password recibido: {}", request.password().length());
        }
        
        long intentosFallidos = intentoAuthRepository.contarIntentosPorCorreoDesde(
            request.correo(), TipoIntento.LOGIN, ResultadoIntento.FALLIDO,
            LocalDateTime.now().minusHours(BLOQUEO_HORAS)
        );
        
        if (intentosFallidos >= MAX_INTENTOS_FALLIDOS) {
            registrarIntento(ip, userAgent, null, request.correo(),
                           TipoIntento.LOGIN, ResultadoIntento.BLOQUEADO,
                           "Cuenta bloqueada por intentos fallidos");
            return Mono.error(new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.LOCKED,
                    "Cuenta bloqueada. Intente en " + BLOQUEO_HORAS + " horas"));
        }
        
        // Intentamos buscar al usuario
        Aspirante aspirante = aspiranteRepository.findActivoByCorreo(request.correo())
            .orElse(null);
        
        log.info("🔍 DEBUG LOGIN - ¿Se encontró al aspirante en la BD? {}", (aspirante != null));
        if (aspirante != null) {
            log.info("🔍 DEBUG LOGIN - Hash almacenado en BD: {}", aspirante.getPasswordHash());
        } else {
            log.warn("❌ DEBUG LOGIN: El usuario NO se encontró o no está activo. Revisa el método findActivoByCorreo en el Repositorio.");
        }
        
        if (aspirante == null) {
            registrarIntento(ip, userAgent, null, request.correo(),
                           TipoIntento.LOGIN, ResultadoIntento.FALLIDO,
                           "Usuario no encontrado o inactivo");
            return Mono.error(new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Credenciales inválidas"));
        }
        
        boolean passwordMatch = passwordEncoder.matches(request.password(), aspirante.getPasswordHash());
        log.info("🔍 DEBUG LOGIN - ¿El password coincide con el hash? {}", passwordMatch);
        
        if (!passwordMatch) {
            log.warn("❌ DEBUG LOGIN: El password NO coincide.");
            registrarIntento(ip, userAgent, null, request.correo(),
                           TipoIntento.LOGIN, ResultadoIntento.FALLIDO,
                           "Credenciales inválidas");
            return Mono.error(new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Credenciales inválidas"));
        }
        
        // ✅ Si llegamos aquí, todo es correcto
        aspirante.setFechaUltimoAcceso(LocalDateTime.now());
        aspiranteRepository.save(aspirante);
        
        String accessToken = jwtService.generateAccessToken(
            aspirante.getId(), aspirante.getFolio(),
            aspirante.getCorreoElectronico(), aspirante.getNivelConfianza()
        );
        String refreshToken = jwtService.generateRefreshToken(
            aspirante.getId(), aspirante.getFolio()
        );
        
        registrarIntento(ip, userAgent, null, request.correo(),
                        TipoIntento.LOGIN, ResultadoIntento.EXITOSO, null);
        
        return Mono.just(new TokenResponse(accessToken, refreshToken, 3600, aspirante.getFolio()));
    }
    
    @Override
    public Mono<TokenResponse> refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();
        
        if (!jwtService.isTokenValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            return Mono.error(new RuntimeException("Refresh token inválido"));
        }
        
        UUID aspiranteId = jwtService.extractAspiranteId(refreshToken);
        Aspirante aspirante = aspiranteRepository.findById(aspiranteId)
            .orElseThrow(() -> new RuntimeException("Aspirante no encontrado"));
        
        String newAccessToken = jwtService.generateAccessToken(
            aspirante.getId(), aspirante.getFolio(),
            aspirante.getCorreoElectronico(), aspirante.getNivelConfianza()
        );
        
        return Mono.just(new TokenResponse(newAccessToken, null, 3600, aspirante.getFolio()));
    }
    
    @Override
    public Mono<ValidacionResponse> validarToken(String token) {
        if (!jwtService.isTokenValid(token) || !jwtService.isAccessToken(token)) {
            return Mono.just(new ValidacionResponse(false, null, null, 0));
        }
        
        String folio = jwtService.extractFolio(token);
        String correo = jwtService.extractCorreo(token);
        int nivelConfianza = jwtService.extractNivelConfianza(token);
        
        return Mono.just(new ValidacionResponse(true, folio, correo, nivelConfianza));
    }
    
    @Override
    public void logout(String token) {
        log.info("Logout ejecutado");
    }
    
    @Override
    public Mono<Aspirante> obtenerPerfil(java.util.UUID aspiranteId) {
        return Mono.fromCallable(() -> 
            aspiranteRepository.findById(aspiranteId)
                .orElseThrow(() -> new RuntimeException("No se encontró el perfil del aspirante"))
        );
    }

    private void registrarIntento(String ip, String userAgent, String curp, String correo,
                                   TipoIntento tipo, ResultadoIntento resultado, String motivo) {
        IntentoAuth intento = new IntentoAuth();
        intento.setIpOrigen(ip);
        intento.setUserAgent(userAgent);
        intento.setCurpIntentada(curp);
        intento.setCorreoIntentado(correo);
        intento.setTipo(tipo);
        intento.setResultado(resultado);
        intento.setMotivoFallo(motivo);
        intentoAuthRepository.save(intento);
    }

        @Override
    @Transactional
    public Mono<MensajeResponse> eliminarPerfil(UUID aspiranteId, String ip, String userAgent) {
        log.info("🚨 SOLICITUD DE ELIMINACIÓN DE PERFIL - ID: {}", aspiranteId);
        
        return Mono.fromCallable(() -> {
            Aspirante aspirante = aspiranteRepository.findById(aspiranteId)
                    .orElseThrow(() -> new RuntimeException("Aspirante no encontrado"));

            // 1. Soft Delete: Desactivar la cuenta
            aspirante.setActivo(false);
            
            // 2. Actualizar estatus (Ajusta 'BAJA' al valor real de tu enum EstatusAspirante si es diferente)
            // aspirante.setEstatus(EstatusAspirante.BAJA); 
            
            // 3. Limpiar datos sensibles por seguridad (Opcional pero recomendado)
            aspirante.setPasswordHash("ELIMINADO_POR_USUARIO");
            
            aspiranteRepository.save(aspirante);

            // 4. Registrar en auditoría
            registrarIntento(ip, userAgent, aspirante.getCurp(), aspirante.getCorreoElectronico(),
                    TipoIntento.ELIMINAR_CUENTA, // Asegúrate de que este valor exista en tu enum TipoIntento, o usa uno genérico
                    ResultadoIntento.EXITOSO, 
                    "Cuenta dada de baja voluntariamente por el usuario");

            log.info("✅ Cuenta desactivada exitosamente para el correo: {}", aspirante.getCorreoElectronico());
            return new MensajeResponse("Tu cuenta ha sido eliminada exitosamente. Lamentamos que te vayas.", 200);
        });
    }
}