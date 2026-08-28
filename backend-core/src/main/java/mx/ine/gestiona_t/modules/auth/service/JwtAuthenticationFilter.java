package mx.ine.gestiona_t.modules.auth.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                     HttpServletResponse response, 
                                     FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String requestUri = request.getRequestURI();
        final String method = request.getMethod();

        log.debug("🔍 [JWT FILTER] {} {} - Authorization header: {}", method, requestUri, 
                  authHeader != null ? (authHeader.substring(0, Math.min(20, authHeader.length())) + "...") : "null");

        // 1. Si no hay header, continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("⚠️ [JWT FILTER] {} {} - No Authorization header o formato inválido", method, requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            boolean tokenValid = jwtService.isTokenValid(jwt);
            boolean isAccess = jwtService.isAccessToken(jwt);

            log.debug("🔍 [JWT FILTER] Token válido: {}, Es access token: {}", tokenValid, isAccess);

            if (!tokenValid || !isAccess) {
                log.warn("⚠️ [JWT FILTER] {} {} - Token inválido o no es access token", method, requestUri);
                filterChain.doFilter(request, response);
                return;
            }

            // ✅ 2. Extraer datos del token (incluyendo rol y tipoUsuario)
            UUID userId = jwtService.extractUserId(jwt);
            String correo = jwtService.extractCorreo(jwt);
            String rol = jwtService.extractRol(jwt);
            String tipoUsuario = jwtService.extractTipoUsuario(jwt);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // ✅ 3. Construir la lista de authorities (roles) según el tipo de usuario
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                if ("ANALISTA".equals(tipoUsuario)) {
                    // Es un analista: asignar su rol específico
                    if (rol != null) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + rol));
                        log.info("✅ [JWT FILTER] {} {} - Analista autenticado: {} ({})", 
                                method, requestUri, correo, rol);
                    }
                } else {
                    // Es un aspirante (comportamiento original)
                    authorities.add(new SimpleGrantedAuthority("ROLE_ASPIRANTE"));
                    log.info("✅ [JWT FILTER] {} {} - Aspirante autenticado: {} ({})", 
                            method, requestUri, correo, userId);
                }

                // ✅ 4. Crear el token de autenticación con las authorities
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userId,       // Principal (ID del usuario)
                    correo,       // Credentials (correo, para referencia)
                    authorities   // Authorities (roles)
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            log.error("❌ [JWT FILTER] Error al procesar JWT: {} - {}", e.getMessage(), e.getClass().getSimpleName());
        }

        filterChain.doFilter(request, response);
    }
}