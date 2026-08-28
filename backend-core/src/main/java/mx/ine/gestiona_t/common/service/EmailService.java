package mx.ine.gestiona_t.common.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@Service
public class EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${app.mail.from}")
    private String fromEmail;
    
    @Value("${app.mail.from-name}")
    private String fromName;
    
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }
    
    /**
     * Envía un correo HTML usando una plantilla Thymeleaf.
     */
    public void enviarCorreoHtml(String destinatario, String asunto, String plantilla, Map<String, Object> variables) {
        try {
            // 1. Procesar la plantilla HTML con Thymeleaf
            Context context = new Context();
            variables.forEach(context::setVariable);
            String contenidoHtml = templateEngine.process(plantilla, context);
            
            // 2. Crear el mensaje MIME
            MimeMessage mensaje = mailSender.createMimeMessage();
            // El tercer parámetro "UTF-8" asegura la codificación correcta de caracteres especiales
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            
            // ✅ CORREGIDO: Ahora el catch maneja tanto MessagingException como UnsupportedEncodingException
            helper.setFrom(fromEmail, fromName);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true); // true = es HTML
            
            // 3. Enviar el correo
            mailSender.send(mensaje);
            
            log.info("✅ Correo enviado exitosamente a {} con asunto: {}", destinatario, asunto);
            
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("❌ Error al enviar correo a {}: {}", destinatario, e.getMessage(), e);
            throw new RuntimeException("No se pudo enviar el correo de verificación", e);
        }
    }
}