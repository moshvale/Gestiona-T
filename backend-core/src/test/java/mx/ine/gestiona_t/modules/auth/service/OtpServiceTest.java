package mx.ine.gestiona_t.modules.auth.service;

import mx.ine.gestiona_t.modules.auth.model.CodigoOTP;
import mx.ine.gestiona_t.modules.auth.repository.CodigoOTPRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class OtpServiceTest {

    @Test
    void generarYEnviarOTPShouldReturnCodeWithoutSendingEmailWhenDisabled() {
        CodigoOTPRepository otpRepository = Mockito.mock(CodigoOTPRepository.class);
        when(otpRepository.save(any(CodigoOTP.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OtpService otpService = new OtpService(otpRepository, null);
        ReflectionTestUtils.setField(otpService, "enviarCorreo", false);

        UUID aspiranteId = UUID.randomUUID();

        String codigo = otpService.generarYEnviarOTP(aspiranteId, "prueba@ine.mx", "5551234567");

        assertThat(codigo).hasSize(6).matches("\\d{6}");
    }
}
