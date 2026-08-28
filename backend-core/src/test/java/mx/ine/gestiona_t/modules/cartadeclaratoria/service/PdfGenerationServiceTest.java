package mx.ine.gestiona_t.modules.cartadeclaratoria.service;

import mx.ine.gestiona_t.modules.cartadeclaratoria.dto.response.BloqueResponse;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.CartaDeclaratoria;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.enums.EstatusCarta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfGenerationServiceTest {

    @Test
    void generarPdfDebeRetornarBytesValidos() {
        PdfGenerationService service = new PdfGenerationService();
        CartaDeclaratoria carta = new CartaDeclaratoria();
        carta.setFolio("TEST-001");
        carta.setFolioCarta("CARTA-001");
        carta.setVersion("1.0.0");
        carta.setEstatus(EstatusCarta.FIRMADA);

        List<BloqueResponse> bloques = List.of(
                new BloqueResponse(1, "Bloque de prueba", "Texto de prueba", "Fundamento legal", true, 1, true)
        );

        byte[] pdf = service.generarPdf(carta, bloques, "Nombre Aspirante", "CURP123", "TEST-001");

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        assertTrue(new String(pdf, 0, Math.min(pdf.length, 5)).startsWith("%PDF"));
    }
}
