package mx.ine.gestiona_t.modules.cartadeclaratoria.config;

import mx.ine.gestiona_t.modules.cartadeclaratoria.model.BloqueDeclaratorio;
import mx.ine.gestiona_t.modules.cartadeclaratoria.repository.BloqueDeclaratorioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class BloquesDeclaratoriosInitializer {

    private static final Logger log = LoggerFactory.getLogger(BloquesDeclaratoriosInitializer.class);

    private static final List<BloqueSeed> BLOQUES = List.of(
            new BloqueSeed(1, "VERACIDAD DOCUMENTAL", "Declaro que toda la informacion y documentos proporcionados durante el proceso de seleccion son autenticos, veraces y verificables.", "Art. 183 Codigo Penal Federal"),
            new BloqueSeed(2, "NO INHABILITACION ADMINISTRATIVA", "Declaro que NO me encuentro inhabilitado para desempenar empleo, cargo o comision en el servicio publico.", "LGRA Arts. 7, 19, 38"),
            new BloqueSeed(3, "ANTECEDENTES PENALES", "Declaro que NO he sido condenado mediante sentencia ejecutoriada por delito doloso.", "Codigo Penal Federal"),
            new BloqueSeed(4, "OBLIGACIONES FISCALES", "Declaro que cumplo cabalmente con mis obligaciones fiscales conforme al Codigo Fiscal de la Federacion.", "Codigo Fiscal de la Federacion"),
            new BloqueSeed(5, "PREVENCION DE VIOLENCIA CONTRA LAS MUJERES", "Declaro bajo protesta de decir verdad que NO he ejercido violencia contra las mujeres ni soy deudor alimentario moroso.", "Politica de Igualdad de Genero INE"),
            new BloqueSeed(6, "CONFLICTO DE INTERES", "Declaro que NO tengo conflicto de interes para desempenar el cargo al que aspiro.", "Lineamientos de Conflictos de Interes INE"),
            new BloqueSeed(7, "AFILIACION POLITICA", "Declaro que NO estoy afiliado a ningun partido politico nacional o local.", "LGIPE Art. 44"),
            new BloqueSeed(8, "NO VIOLENCIA LABORAL", "Declaro que NO he sido sancionado por conductas de violencia laboral, acoso psicologico, hostigamiento o discriminacion.", "Ley Federal del Trabajo"),
            new BloqueSeed(9, "COMPROMISO ETICO", "Declaro conocer, aceptar y comprometerme a cumplir el Codigo de Etica y el Codigo de Conducta del INE.", "Codigo de Etica de la Funcion Publica Electoral"),
            new BloqueSeed(10, "PROTECCION DE DATOS PERSONALES", "Autorizo al INE al tratamiento de mis datos personales para fines del proceso de seleccion y, en su caso, para la relacion laboral.", "LGDPPP"),
            new BloqueSeed(11, "DECLARACION PATRIMONIAL", "Declaro que, de ser contratado, presentare mi Declaracion Patrimonial y de Intereses dentro de los plazos legales.", "LGRA"),
            new BloqueSeed(12, "CONOCIMIENTO DE CONSECUENCIAS LEGALES", "Declaro conocer las consecuencias legales de la falsedad en cualquiera de las declaraciones anteriores.", "LGRA + Codigo Penal Federal")
    );

    @Bean
    ApplicationRunner inicializarBloques(BloqueDeclaratorioRepository repository) {
        return args -> {
            Map<Integer, BloqueDeclaratorio> existentes = repository.findAll().stream()
                    .collect(Collectors.toMap(BloqueDeclaratorio::getId, Function.identity()));
            int insertados = 0;

            for (BloqueSeed seed : BLOQUES) {
                if (existentes.containsKey(seed.id())) {
                    continue;
                }

                BloqueDeclaratorio bloque = new BloqueDeclaratorio();
                bloque.setId(seed.id());
                bloque.setTitulo(seed.titulo());
                bloque.setTexto(seed.texto());
                bloque.setFundamentoLegal(seed.fundamentoLegal());
                bloque.setObligatorio(true);
                bloque.setOrden(seed.id());
                bloque.setActivo(true);
                repository.save(bloque);
                insertados++;
            }

            log.info("Bloques declaratorios disponibles: {}. Registros insertados en este arranque: {}", repository.findByActivoTrueOrderByOrdenAsc().size(), insertados);
        };
    }

    private record BloqueSeed(int id, String titulo, String texto, String fundamentoLegal) {
    }
}
