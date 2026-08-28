package mx.ine.gestiona_t.config;

import mx.ine.gestiona_t.modules.auth.service.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, 
                         CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .requestCache(cache -> cache.disable())
            .authorizeHttpRequests(auth -> auth
                // ==========================================
                // 1. RUTAS PÚBLICAS (Sin autenticación)
                // ==========================================
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/bloques-declaratorios").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // ==========================================
                // 2. RUTAS EXCLUSIVAS PARA ANALISTAS / ADMINS
                // ==========================================
                .requestMatchers("/api/v1/admin/analistas/**").hasAuthority("ROLE_ADMIN_SISTEMA")
                .requestMatchers("/api/v1/admin/**").hasAnyAuthority("ROLE_ANALISTA_UR", "ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.GET, "/api/v1/documentos/por-folio/**").hasAnyAuthority("ROLE_ANALISTA_UR", "ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.GET, "/api/v1/documentos/por-aspirante/**").hasAnyAuthority("ROLE_ANALISTA_UR", "ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.POST, "/api/v1/documentos/*/validar").hasAnyAuthority("ROLE_ANALISTA_UR", "ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.GET, "/api/v1/carta-declaratoria/por-aspirante/**").hasAnyAuthority("ROLE_ANALISTA_UR", "ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.GET, "/api/v1/carta-declaratoria/por-folio/**").hasAnyAuthority("ROLE_ANALISTA_UR", "ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/aspirante/**").hasAnyAuthority("ROLE_ANALISTA_UR", "ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.GET, "/api/v1/cv/aspirante/**").hasAnyAuthority("ROLE_ANALISTA_UR", "ROLE_ADMIN_SISTEMA")

                // ==========================================
                // ✅ 3. NUEVO: RUTAS DE EXPEDIENTES LABORALES
                // ==========================================
                // Lectura: accesible para analistas, analista principal, responsable JLE y admin
                .requestMatchers(HttpMethod.GET, "/api/v1/expedientes-laborales/**")
                    .hasAnyAuthority("ROLE_ANALISTA_UR", "ROLE_ANALISTA_PRINCIPAL", "ROLE_RESPONSABLE_JLE", "ROLE_ADMIN_SISTEMA")
                // Escritura: solo analista principal, responsable JLE y admin
                .requestMatchers(HttpMethod.POST, "/api/v1/expedientes-laborales/**")
                    .hasAnyAuthority("ROLE_ANALISTA_PRINCIPAL", "ROLE_RESPONSABLE_JLE", "ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.PUT, "/api/v1/expedientes-laborales/**")
                    .hasAnyAuthority("ROLE_ANALISTA_PRINCIPAL", "ROLE_RESPONSABLE_JLE", "ROLE_ADMIN_SISTEMA")
                // Eliminación física: solo admin (baja lógica puede hacerla analista principal)
                .requestMatchers(HttpMethod.DELETE, "/api/v1/expedientes-laborales/**")
                    .hasAuthority("ROLE_ADMIN_SISTEMA")

                // ==========================================
                // ✅ 4. NUEVO: RUTAS DE CATÁLOGOS (Juntas y Vocalías)
                // ==========================================
                // Lectura: cualquier usuario autenticado (para formularios de alta)
                .requestMatchers(HttpMethod.GET, "/api/v1/juntas-ejecutivas/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/vocalias/**").authenticated()
                // Escritura: solo admin
                .requestMatchers(HttpMethod.POST, "/api/v1/juntas-ejecutivas/**").hasAuthority("ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.PUT, "/api/v1/juntas-ejecutivas/**").hasAuthority("ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/juntas-ejecutivas/**").hasAuthority("ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.POST, "/api/v1/vocalias/**").hasAuthority("ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.PUT, "/api/v1/vocalias/**").hasAuthority("ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/vocalias/**").hasAuthority("ROLE_ADMIN_SISTEMA")

                // ==========================================
                // 5. RUTAS EXCLUSIVAS PARA ASPIRANTES
                // ==========================================
                .requestMatchers("/api/v1/aspirante/**").hasAuthority("ROLE_ASPIRANTE")
                .requestMatchers(HttpMethod.POST, "/api/v1/documentos/upload")
                    .hasAnyAuthority("ROLE_ASPIRANTE", "ROLE_ANALISTA_UR", "ROLE_ANALISTA_PRINCIPAL", "ROLE_RESPONSABLE_JLE", "ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.GET, "/api/v1/documentos").hasAuthority("ROLE_ASPIRANTE")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/documentos/**").hasAuthority("ROLE_ASPIRANTE")
                .requestMatchers("/api/v1/cv/**").hasAuthority("ROLE_ASPIRANTE")
                .requestMatchers("/api/v1/matching/**").hasAuthority("ROLE_ASPIRANTE")

                // ==========================================
                // 6. RUTAS COMPARTIDAS (Cualquier usuario logueado)
                // ==========================================
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/perfil").authenticated()

                // ==========================================
                // 7. RUTAS DE VACANTES
                // ==========================================
                .requestMatchers(HttpMethod.GET, "/api/v1/vacantes/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/vacantes/**")
                    .hasAnyAuthority("ROLE_ANALISTA_UR", "ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.PUT, "/api/v1/vacantes/**")
                    .hasAnyAuthority("ROLE_ANALISTA_UR", "ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/vacantes/**")
                    .hasAnyAuthority("ROLE_ANALISTA_UR", "ROLE_ADMIN_SISTEMA")

                // ==========================================
                // 8. RUTAS DE POSTULACIONES
                // ==========================================
                .requestMatchers(HttpMethod.GET, "/api/v1/postulaciones/mis-postulaciones").hasAuthority("ROLE_ASPIRANTE")
                .requestMatchers(HttpMethod.POST, "/api/v1/postulaciones").hasAuthority("ROLE_ASPIRANTE")
                .requestMatchers(HttpMethod.GET, "/api/v1/postulaciones").hasAnyAuthority("ROLE_ANALISTA_UR", "ROLE_ANALISTA_PRINCIPAL", "ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.GET, "/api/v1/postulaciones/vacante/**").hasAnyAuthority("ROLE_ANALISTA_UR", "ROLE_ANALISTA_PRINCIPAL", "ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.PUT, "/api/v1/postulaciones/*/estatus").hasAnyAuthority("ROLE_ANALISTA_UR", "ROLE_ANALISTA_PRINCIPAL", "ROLE_ADMIN_SISTEMA")
                .requestMatchers(HttpMethod.PUT, "/api/v1/postulaciones/*/evaluacion").hasAnyAuthority("ROLE_ANALISTA_UR", "ROLE_ANALISTA_PRINCIPAL", "ROLE_ADMIN_SISTEMA")

                // ==========================================
                // 9. CUALQUIER OTRA RUTA (DEBE SER LA ULTIMA)
                // ==========================================
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}