package mx.ine.gestiona_t.modules.cartadeclaratoria.service;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import mx.ine.gestiona_t.modules.cartadeclaratoria.dto.response.BloqueResponse;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.CartaDeclaratoria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerationService.class);

    public byte[] generarPdf(CartaDeclaratoria carta, List<BloqueResponse> bloques, 
                             String nombreAspirante, String curp, String folio) {
        log.info("Generando PDF real para carta: {}", carta.getId());

        // 1. Construir el HTML
        String html = construirHtml(carta, bloques, nombreAspirante, curp, folio);

        // 2. Convertir HTML a PDF binario real usando iText html2pdf
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ConverterProperties properties = new ConverterProperties();
            
            // HtmlConverter transforma el string HTML en bytes de PDF válidos
            HtmlConverter.convertToPdf(html, outputStream, properties);
            
            byte[] pdfBytes = outputStream.toByteArray();
            log.info("✅ PDF generado exitosamente. Tamaño: {} bytes", pdfBytes.length);
            
            return pdfBytes;
        } catch (Exception e) {
            log.error("❌ Error al generar el PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el PDF de la carta declaratoria", e);
        }
    }

    private String construirHtml(CartaDeclaratoria carta, List<BloqueResponse> bloques, 
                                 String nombreAspirante, String curp, String folio) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String fecha = carta.getFechaFirma() != null ? carta.getFechaFirma().format(formatter) : "N/A";

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
            .append("<style>")
            .append("body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }")
            .append("h1 { text-align: center; color: #1a4d8f; font-size: 18px; }")
            .append(".header-info { margin-bottom: 20px; font-size: 14px; }")
            .append(".bloque { margin: 15px 0; padding: 10px; border: 1px solid #ccc; border-radius: 4px; }")
            .append(".titulo { font-weight: bold; color: #1a4d8f; font-size: 14px; margin-bottom: 5px; }")
            .append(".fundamento { font-style: italic; font-size: 12px; color: #666; margin-top: 5px; }")
            .append(".firma { margin-top: 60px; text-align: center; }")
            .append("</style></head><body>");

        html.append("<h1>CARTA DECLARATORIA BAJO PROTESTA DE DECIR VERDAD</h1>");
        html.append("<div class='header-info'>");
        html.append("<p><strong>Folio Carta:</strong> ").append(carta.getFolioCarta() != null ? carta.getFolioCarta() : "N/A").append("</p>");
        html.append("<p><strong>Folio Aspirante:</strong> ").append(folio).append("</p>");
        html.append("<p><strong>Nombre:</strong> ").append(nombreAspirante).append("</p>");
        html.append("<p><strong>CURP:</strong> ").append(curp).append("</p>");
        html.append("<p><strong>Fecha:</strong> ").append(fecha).append("</p>");
        html.append("<p><strong>Versión:</strong> ").append(carta.getVersion() != null ? carta.getVersion() : "1.0.0").append("</p>");
        html.append("</div><hr>");

        html.append("<p>En cumplimiento a lo establecido en los artículos 109 de la Constitución Política de los Estados Unidos Mexicanos; 7, 19 y 38 de la Ley General de Responsabilidades Administrativas; el Estatuto del Servicio Profesional Electoral Nacional; el Código de Ética de la Función Pública Electoral; y demás normatividad aplicable del Instituto Nacional Electoral, manifiesto bajo protesta de decir verdad lo siguiente:</p>");

        for (BloqueResponse bloque : bloques) {
            html.append("<div class='bloque'>");
            html.append("<p class='titulo'>BLOQUE ").append(bloque.orden()).append(": ").append(bloque.titulo()).append("</p>");
            html.append("<p>").append(bloque.texto()).append("</p>");
            html.append("<p class='fundamento'>Fundamento: ").append(bloque.fundamentoLegal()).append("</p>");
            html.append("<p><strong>Aceptado: </strong>").append(bloque.aceptado() ? "SÍ" : "NO").append("</p>");
            html.append("</div>");
        }

        html.append("<hr>");
        html.append("<div class='firma'>");
        html.append("<p>Lo anterior lo manifiesto bajo protesta de decir verdad.</p>");
        html.append("<br><br>");
        html.append("<p>_________________________________</p>");
        html.append("<p>").append(nombreAspirante).append("</p>");
        html.append("</div>");
        html.append("</body></html>");

        return html.toString();
    }
}