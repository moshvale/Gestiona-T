# Problema y solución: alta de aspirantes

## Problema

El formulario de registro creaba el aspirante en estado `PRE_REGISTRO`, pero el
backend no generaba ni guardaba el código OTP. A pesar de ello, la respuesta
indicaba que el registro estaba completo y el frontend enviaba al usuario al
login. Por tanto, el flujo de verificación nunca podía completarse.

Los intentos posteriores con el mismo correo eran rechazados como duplicados,
porque el primer intento sí había insertado el aspirante en la base de datos.

Además, al acceder al frontend mediante `10.15.0.59`, Next.js bloqueaba el
recurso de desarrollo `/_next/webpack-hmr` porque esa IP no estaba declarada en
`allowedDevOrigins`. Este aviso afecta la recarga en caliente, pero no era la
causa de que faltara el registro en la base de datos.

## Solución aplicada

- El backend ya no genera ni envía OTP durante el alta y guarda directamente al
  aspirante con estado `REGISTRO_VALIDADO`.
- La respuesta confirma que el registro está completo y el frontend redirige a
  `/login` con el correo registrado.
- El envío de correo queda fuera del flujo obligatorio; el SMTP bloqueado no
  impide crear la cuenta ni iniciar sesión.
- Se agregó `10.15.0.59` a `allowedDevOrigins` para eliminar el bloqueo HMR en
  desarrollo desde esa dirección.

## Flujo esperado

1. El usuario envía el formulario.
2. Se crea el aspirante con estado `REGISTRO_VALIDADO`.
3. El frontend redirige al login con el correo precargado.
4. El usuario inicia sesión con la contraseña registrada.

## Nota de desarrollo

El OTP y sus endpoints se conservan para otros flujos que los necesiten, pero
no participan en el alta de aspirantes. No es necesario configurar SMTP para
registrar o autenticar una cuenta.