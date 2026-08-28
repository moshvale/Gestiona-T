INSERT INTO analistas (nombre_completo, correo_electronico, password_hash, rol, activo)
VALUES (
    'Analista de Prueba INE',
    'analista@ine.mx',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ANALISTA_UR',
    true
) ON CONFLICT (correo_electronico) DO NOTHING;

INSERT INTO analistas (nombre_completo, correo_electronico, password_hash, rol, activo)
VALUES (
    'Administrador del Sistema',
    'admin@ine.mx',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN_SISTEMA',
    true
) ON CONFLICT (correo_electronico) DO NOTHING;

SELECT id, nombre_completo, correo_electronico, rol, activo FROM analistas;