-- Actualizar el hash al correcto y verificado para la contraseña "admin123"
UPDATE analistas 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE correo_electronico IN ('analista@ine.mx', 'admin@ine.mx');

-- Verificar que la longitud del hash sea 60 (señal de que se guardó correctamente)
SELECT correo_electronico, rol, LENGTH(password_hash) as len FROM analistas;