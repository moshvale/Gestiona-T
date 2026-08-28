-- ============================================================
-- SEED: 12 Bloques Declaratorios de la Carta
-- ============================================================

INSERT INTO bloques_declaratorios (id, titulo, texto, fundamento_legal, obligatorio, orden, activo) VALUES
(1, 'VERACIDAD DOCUMENTAL', 
 'Declaro que toda la informacion y documentos proporcionados durante el proceso de seleccion son autenticos, veraces y verificables. Tengo conocimiento que la falsedad en declaraciones constituye delito conforme al articulo 183 del Codigo Penal Federal y causa de inhabilitacion administrativa.',
 'Art. 183 Codigo Penal Federal', true, 1, true),

(2, 'NO INHABILITACION ADMINISTRATIVA', 
 'Declaro que NO me encuentro inhabilitado para desempenar empleo, cargo o comision en el servicio publico por resolucion de la Secretaria de la Funcion Publica, Organos Internos de Control, o autoridad competente federal, estatal o municipal.',
 'LGRA Arts. 7, 19, 38', true, 2, true),

(3, 'ANTECEDENTES PENALES', 
 'Declaro que NO he sido condenado mediante sentencia ejecutoriada por delito doloso que amerite pena privativa de libertad.',
 'Codigo Penal Federal', true, 3, true),

(4, 'OBLIGACIONES FISCALES', 
 'Declaro que cumplo cabalmente con mis obligaciones fiscales conforme al Codigo Fiscal de la Federacion y la Ley del ISR, y me encuentro dado de alta en el RFC correspondiente.',
 'Codigo Fiscal de la Federacion', true, 4, true),

(5, 'PREVENCION DE VIOLENCIA CONTRA LAS MUJERES', 
 'Declaro bajo protesta de decir verdad: a) NO he sido sujeto a procedimiento o sentencia por violencia familiar, violencia de genero o violencia contra las mujeres. b) NO me encuentro registrado en el Registro Nacional de Obligaciones Alimentarias como deudor alimentario moroso. c) NO he ejercido conductas de acoso u hostigamiento sexual en ambitos laborales, academicos o personales. d) Conozco y acepto que la comprobacion de falsedad en esta declaracion sera causal inmediata de rescision contractual y denuncia penal.',
 'Politica de Igualdad de Genero INE', true, 5, true),

(6, 'CONFLICTO DE INTERES', 
 'Declaro que NO tengo conflicto de interes para desempenar el cargo al que aspiro. Especificamente declaro: a) NO tener parentesco hasta cuarto grado de consanguinidad o segundo de afinidad con funcionarios del INE involucrados en el proceso. b) NO tener intereses economicos, financieros o comerciales que puedan contraponerse a las funciones del cargo. c) NO haber prestado servicios profesionales a partidos politicos, candidatos o precandidatos en los ultimos 3 anos.',
 'Lineamientos de Conflictos de Interes INE', true, 6, true),

(7, 'AFILIACION POLITICA', 
 'Declaro que NO estoy afiliado a ningun partido politico nacional o local, ni he sido candidato a cargo de eleccion popular en los ultimos 3 anos anteriores a la solicitud.',
 'LGIPE Art. 44', true, 7, true),

(8, 'NO VIOLENCIA LABORAL', 
 'Declaro que NO he sido sancionado por conductas de violencia laboral, acoso psicologico, hostigamiento, discriminacion o cualquier forma de violencia en el ambito laboral en empleos anteriores.',
 'Ley Federal del Trabajo', true, 8, true),

(9, 'COMPROMISO ETICO', 
 'Declaro conocer, aceptar y comprometerme a cumplir: a) El Codigo de Etica de la Funcion Publica Electoral. b) El Codigo de Conducta del INE. c) Los valores institucionales: Transparencia, Confianza, Compromiso, Rendicion de Cuentas y Tolerancia. d) Las politicas de austeridad y honestidad institucional.',
 'Codigo de Etica de la Funcion Publica Electoral', true, 9, true),

(10, 'PROTECCION DE DATOS PERSONALES', 
 'Autorizo al INE, conforme a la Ley General de Proteccion de Datos Personales en Posesion de Sujetos Obligados, al tratamiento de mis datos personales para fines del proceso de seleccion y, en su caso, para la relacion laboral. Conozco mis derechos ARCO.',
 'LGDPPP', true, 10, true),

(11, 'DECLARACION PATRIMONIAL', 
 'Declaro que, de ser contratado, presentare mi Declaracion Patrimonial y de Intereses dentro de los plazos establecidos por la Ley General de Responsabilidades Administrativas, conforme a los formatos del Sistema Nacional Anticorrupcion.',
 'LGRA', true, 11, true),

(12, 'CONOCIMIENTO DE CONSECUENCIAS LEGALES', 
 'Declaro conocer que la falsedad en cualquiera de las anteriores declaraciones tendra las siguientes consecuencias: a) Nulidad del proceso de seleccion. b) Rescision inmediata del contrato laboral. c) Inhabilitacion para ocupar cargos publicos. d) Denuncia penal ante la Fiscalia correspondiente. e) Responsabilidad civil por danos y perjuicios. f) Inscripcion en el Registro Nacional de Servidores Publicos Sancionados.',
 'LGRA + Codigo Penal Federal', true, 12, true);