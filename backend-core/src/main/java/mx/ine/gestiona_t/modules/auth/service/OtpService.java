package mx.ine.gestiona_t.modules.auth.service;

import mx.ine.gestiona_t.common.service.EmailService;
import mx.ine.gestiona_t.modules.auth.model.CodigoOTP;
import mx.ine.gestiona_t.modules.auth.model.enums.CanalOTP;
import mx.ine.gestiona_t.modules.auth.repository.CodigoOTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class OtpService {
    
    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRATION_MINUTES = 5;
    
    private final CodigoOTPRepository otpRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;
    private final EmailService emailService; // ✅ NUEVO: Servicio de correo
    
    @Value("${otp.max.attempts:3}")
    private int maxAttempts;

    @Value("${otp.email.enabled:false}")
    private boolean enviarCorreo;
    
    // ✅ ACTUALIZADO: Inyectar EmailService en el constructor
    public OtpService(CodigoOTPRepository otpRepository, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.secureRandom = new SecureRandom();
        this.emailService = emailService;
    }
    
    @Transactional
    public String generarYEnviarOTP(UUID aspiranteId, String correo, String telefono) {
        // 1. Invalidar OTPs anteriores para este aspirante
        otpRepository.invalidarTodos(aspiranteId);
        
        // 2. Generar y hashear el código
        String codigo = generarCodigoOTP();
        String codigoHash = passwordEncoder.encode(codigo);
        
        // 3. Guardar en base de datos
        CodigoOTP otp = new CodigoOTP();
        otp.setAspiranteId(aspiranteId);
        otp.setCodigoHash(codigoHash);
        otp.setCanal(CanalOTP.AMBOS);
        otp.setFechaExpiracion(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
        otp.setUtilizado(false);
        otpRepository.save(otp);
        
        if (enviarCorreo) {
            enviarOtpPorEmail(correo, codigo);
        } else {
            log.info("📧 Envío de correo deshabilitado por configuración. Se continuará con el flujo local usando el código generado.");
        }
        
        // (Opcional) Enviar por SMS cuando se integre el proveedor
        // enviarOtpPorSMS(telefono, codigo);
        
        log.info("✅ OTP generado para aspirante: {}", aspiranteId);
        
        return codigo;
    }
    
    @Transactional
    public boolean validarOTP(UUID aspiranteId, String codigoIngresado) {
        Optional<CodigoOTP> otpOpt = otpRepository.findUltimoNoUtilizado(
            aspiranteId, LocalDateTime.now()
        );
        
        if (otpOpt.isEmpty()) {
            log.warn("No se encontró OTP válido para aspirante: {}", aspiranteId);
            return false;
        }
        
        CodigoOTP otp = otpOpt.get();
        
        if (!passwordEncoder.matches(codigoIngresado, otp.getCodigoHash())) {
            log.warn("OTP inválido para aspirante: {}", aspiranteId);
            return false;
        }
        
        otp.setUtilizado(true);
        otpRepository.save(otp);
        
        log.info("✅ OTP validado exitosamente para aspirante: {}", aspiranteId);
        return true;
    }
    
    private String generarCodigoOTP() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }
    
    // ✅ ACTUALIZADO: Uso real de EmailService con plantilla Thymeleaf
    private void enviarOtpPorEmail(String correo, String codigo) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("nombre", "Aspirante"); // Se puede mejorar pasando el nombre real desde el registro
            variables.put("codigoOtp", codigo);
            variables.put("minutosExpiracion", OTP_EXPIRATION_MINUTES);
            
            emailService.enviarCorreoHtml(
                correo,
                "🔐 Código de Verificación - Gestiona-T INE",
                "email-otp",
                variables
            );
            log.info("📧 Correo OTP enviado exitosamente a {}", correo);
        } catch (Exception e) {
            log.error("❌ Error al enviar correo OTP a {}: {}", correo, e.getMessage(), e);
            // No lanzamos la excepción para no bloquear el registro si el servicio de correo falla,
            // el OTP queda guardado en BD y se puede consultar manualmente en desarrollo.
        }
    }
    
    private void enviarOtpPorSMS(String telefono, String codigo) {
        // Pendiente de implementación con proveedor de SMS (ej. Twilio, AWS SNS)
        log.info("📱 (Simulado) Enviando OTP por SMS a {}: {}", telefono, codigo);
    }
}