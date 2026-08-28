package mx.ine.gestiona_t.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank(message = "El refresh token es obligatorio")
    String refreshToken
) {}