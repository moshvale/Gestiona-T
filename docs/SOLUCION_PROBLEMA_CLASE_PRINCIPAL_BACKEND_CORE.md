# Solucion: `ClassNotFoundException` al levantar backend-core

## Problema observado

Al ejecutar `mvn spring-boot:run` dentro de `backend-core` Maven terminaba con:

```text
Error: Could not find or load main class mx.ine.gestiona_t.GestionaTApplication
Caused by: java.lang.ClassNotFoundException: mx.ine.gestiona_t.GestionaTApplication
```

El fallo ocurria durante la ejecucion del plugin de Spring Boot, despues de que Maven indicara que no habia nada que compilar.

## Analisis del origen

La clase principal si existe y es correcta:

- Archivo: `backend-core/src/main/java/mx/ine/gestiona_t/GestionaTApplication.java`
- Paquete: `mx.ine.gestiona_t`
- Clase: `GestionaTApplication`
- Anotacion: `@SpringBootApplication`
- Metodo de entrada: `SpringApplication.run(GestionaTApplication.class, args)`

Ademas, el archivo `target/classes/mx/ine/gestiona_t/GestionaTApplication.class` estaba presente. Por tanto, no era necesario cambiar el paquete ni crear otra clase principal.

La causa fue un estado inconsistente u obsoleto del directorio de compilacion `target` y del classpath usado por `spring-boot:run`. La compilacion incremental reportaba que las clases estaban actualizadas, aunque el lanzamiento no podia resolver la clase principal.

Los avisos sobre `Jansi`, `Unsafe`, mutacion de campos finales y acceso nativo de Java no causaron este error. Son advertencias de compatibilidad del Maven y sus dependencias con una version reciente de Java.

## Solucion aplicada

Se elimino el resultado de compilacion anterior y se recompilo todo el modulo desde `backend-core`:

```powershell
cd "F:\codes\Gestiona T_3\backend-core"
mvn clean compile
mvn spring-boot:run
```

Tambien puede ejecutarse desde la raiz del repositorio usando una ubicacion explicita:

```powershell
Push-Location backend-core
mvn clean compile
mvn spring-boot:run
Pop-Location
```

No fue necesario modificar el codigo fuente ni el `pom.xml`.

## Resultado de la verificacion

La compilacion limpia finalizo con `BUILD SUCCESS`.

El arranque posterior confirmo:

```text
Tomcat started on port 8087 (http)
Started GestionaTApplication
```

Tambien se comprobo la conexion a PostgreSQL mediante Hikari y la inicializacion de Spring Data JPA.

Verificaciones disponibles:

- Actuator: `http://localhost:8087/actuator/health`
- Swagger UI: `http://localhost:8087/swagger-ui/index.html`

## Procedimiento de recuperacion

Si vuelve a aparecer `ClassNotFoundException` para la clase principal:

1. Confirmar que la terminal esta dentro de `backend-core`.
2. Ejecutar `mvn clean compile`.
3. Ejecutar nuevamente `mvn spring-boot:run`.
4. Si el puerto `8087` ya esta ocupado, detener el proceso anterior antes de iniciar otro.

El comando debe ejecutarse desde el modulo que contiene `pom.xml`; desde la raiz del repositorio Maven produce errores diferentes, como `MissingProjectException` o `NoPluginFoundForPrefixException`.

## Nota sobre los avisos de Java

Los avisos de acceso nativo y APIs internas no impidieron el arranque. Pueden atenderse posteriormente actualizando Maven y sus dependencias, pero no forman parte de la correccion de esta incidencia.