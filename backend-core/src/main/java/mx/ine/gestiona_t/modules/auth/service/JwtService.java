package mx.ine.gestiona_t.modules.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.ms}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh.expiration.ms}")
    private long refreshExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ============================================
    // MÉTODOS PARA ASPIRANTES (compatibilidad)
    // ============================================

    public String generateAccessToken(UUID aspiranteId, String folio, String correo, int nivelConfianza) {
        return Jwts.builder()
            .subject(aspiranteId.toString())
            .claim("folio", folio)
            .claim("correo", correo)
            .claim("nivelConfianza", nivelConfianza)
            .claim("tipo", "access")
            .claim("rol", "ASPIRANTE")           // ✅ Rol por defecto
            .claim("tipoUsuario", "ASPIRANTE")   // ✅ Para distinguir en el filtro
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(getSigningKey())
            .compact();
    }

    public String generateRefreshToken(UUID userId, String folio) {
        return Jwts.builder()
            .subject(userId.toString())
            .claim("folio", folio)
            .claim("tipo", "refresh")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
            .signWith(getSigningKey())
            .compact();
    }

    // ============================================
    // ✅ MÉTODOS NUEVOS PARA ANALISTAS
    // ============================================

    public String generateAccessTokenAnalista(UUID analistaId, String correo, String rol) {
        return Jwts.builder()
            .subject(analistaId.toString())
            .claim("correo", correo)
            .claim("rol", rol)                    // ANALISTA_UR, ADMIN_SISTEMA, CONTRALORIA
            .claim("tipo", "access")
            .claim("tipoUsuario", "ANALISTA")     // ✅ Clave para distinguir en el filtro
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(getSigningKey())
            .compact();
    }

    public String generateRefreshTokenAnalista(UUID analistaId) {
        return Jwts.builder()
            .subject(analistaId.toString())
            .claim("tipo", "refresh")
            .claim("tipoUsuario", "ANALISTA")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
            .signWith(getSigningKey())
            .compact();
    }

    // ============================================
    // EXTRACTORES
    // ============================================

    public String extractFolio(String token) {
        return extractClaim(token, claims -> claims.get("folio", String.class));
    }

    public String extractCorreo(String token) {
        return extractClaim(token, claims -> claims.get("correo", String.class));
    }

    public int extractNivelConfianza(String token) {
        return extractClaim(token, claims -> claims.get("nivelConfianza", Integer.class));
    }

    // ✅ NUEVO: Extraer el rol del token
    public String extractRol(String token) {
        return extractClaim(token, claims -> claims.get("rol", String.class));
    }

    // ✅ NUEVO: Extraer el tipo de usuario (ASPIRANTE o ANALISTA)
    public String extractTipoUsuario(String token) {
        return extractClaim(token, claims -> claims.get("tipoUsuario", String.class));
    }

    public UUID extractUserId(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        return UUID.fromString(subject);
    }

    // Mantener compatibilidad con el método anterior
    public UUID extractAspiranteId(String token) {
        return extractUserId(token);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractTipo(String token) {
        return extractClaim(token, claims -> claims.get("tipo", String.class));
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractTipo(token));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractTipo(token));
    }

    // ✅ NUEVO: Método para saber si el token es de un analista
    public boolean esTokenDeAnalista(String token) {
        return "ANALISTA".equals(extractTipoUsuario(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}