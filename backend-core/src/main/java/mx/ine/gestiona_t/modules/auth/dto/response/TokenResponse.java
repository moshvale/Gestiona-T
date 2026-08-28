package mx.ine.gestiona_t.modules.auth.dto.response;

public record TokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    String folio,
    String tipoUsuario,  // ✅ NUEVO: "ASPIRANTE" o "ANALISTA"
    String rol           // ✅ NUEVO: "ASPIRANTE", "ANALISTA_UR", "ADMIN_SISTEMA"
) {
    // ✅ Constructor para ASPIRANTES (mantiene compatibilidad con código existente)
    public TokenResponse(String accessToken, String refreshToken, long expiresIn, String folio) {
        this(accessToken, refreshToken, "Bearer", expiresIn, folio, "ASPIRANTE", "ASPIRANTE");
    }

    // ✅ Constructor para ANALISTAS (nuevo)
    public TokenResponse(String accessToken, String refreshToken, long expiresIn, 
                         String tipoUsuario, String rol) {
        this(accessToken, refreshToken, "Bearer", expiresIn, null, tipoUsuario, rol);
    }
}