UPDATE analistas 
SET password_hash = '\\\',
    activo = true
WHERE correo_electronico LIKE '%analista@ine.mx%';

UPDATE analistas 
SET password_hash = '\\\',
    activo = true
WHERE correo_electronico LIKE '%admin@ine.mx%';

SELECT id, correo_electronico, rol, activo, LENGTH(password_hash) as len FROM analistas;
