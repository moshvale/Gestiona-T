package mx.ine.gestiona_t.modules.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MensajeResponse(
    String mensaje,
    Integer expiracionSegundos,
    String codigoOtp
) {
    public MensajeResponse(String mensaje) {
        this(mensaje, null, null);
    }

    public MensajeResponse(String mensaje, Integer expiracionSegundos) {
        this(mensaje, expiracionSegundos, null);
    }
}