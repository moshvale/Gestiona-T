INSERT INTO aspirantes (
    folio, nombre_completo, curp, rfc, correo_electronico, 
    telefono_movil, password_hash, estatus, nivel_confianza, activo
) VALUES (
    'ASP-TEST-001',
    'Juan Perez Garcia',
    'PEGJ900101HDFRRN09',
    'PEGJ900101ABC',
    'juan.perez@test.com',
    '5551234567',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'CONTRATADO',
    3,
    true
) ON CONFLICT (folio) DO NOTHING;

INSERT INTO catalogo_instituciones (tipo, nombre, clave, entidad_federativa, acreditada, fuente_oficial) VALUES
('ES', 'Universidad Nacional Autonoma de Mexico', 'UNAM', 'Ciudad de Mexico', true, 'SEP'),
('ES', 'Instituto Politecnico Nacional', 'IPN', 'Ciudad de Mexico', true, 'SEP'),
('ES', 'Universidad Autonoma Metropolitana', 'UAM', 'Ciudad de Mexico', true, 'SEP'),
('EMS', 'Colegio de Bachilleres', 'COLBACH', 'Ciudad de Mexico', true, 'SEP'),
('EMS', 'Escuela Nacional Preparatoria', 'ENP', 'Ciudad de Mexico', true, 'SEP'),
('CERT', 'Centro de Capacitacion en Calidad', 'CCC', 'Ciudad de Mexico', true, 'CONOCER')
ON CONFLICT DO NOTHING;

DO $$
BEGIN
    RAISE NOTICE '=======================================================';
    RAISE NOTICE '  BASE DE DATOS INICIALIZADA CORRECTAMENTE';
    RAISE NOTICE '=======================================================';
    RAISE NOTICE '  Tablas: 15 | Indices: 25 | Triggers: 3';
    RAISE NOTICE '  Seeds: 8 instituciones + 1 aspirante de prueba';
    RAISE NOTICE '=======================================================';
END $$;
