package mx.ine.gestiona_t.modules.auth.service;

import mx.ine.gestiona_t.modules.auth.dto.request.*;
import mx.ine.gestiona_t.modules.auth.dto.response.*;
import mx.ine.gestiona_t.modules.auth.model.Aspirante;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface AuthService {
    
    Mono<MensajeResponse> iniciarRegistro(RegistroIniciarRequest request, String ip, String userAgent);

    Mono<MensajeResponse> reenviarOtp(ReenviarOtpRequest request, String ip, String userAgent);
    
    Mono<TokenResponse> verificarOtp(VerificarOtpRequest request, String ip, String userAgent);
    
    Mono<TokenResponse> validarCurp(ValidarCurpRequest request, String tokenTemporal, 
                                     String ip, String userAgent);
    
    Mono<TokenResponse> validarClaveElector(ValidarClaveElectorRequest request, 
                                             String tokenTemporal, String ip, String userAgent);
    
    Mono<TokenResponse> login(LoginRequest request, String ip, String userAgent);
    
    Mono<TokenResponse> refreshToken(RefreshTokenRequest request);
    
    Mono<ValidacionResponse> validarToken(String token);

    Mono<Aspirante> obtenerPerfil(java.util.UUID aspiranteId);

    // En: backend-core/src/main/java/mx/ine/gestiona_t/modules/auth/service/AuthService.java
    Mono<MensajeResponse> eliminarPerfil(UUID aspiranteId, String ip, String userAgent);
    
    void logout(String token);
}