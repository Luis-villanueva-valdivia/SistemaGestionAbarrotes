# Base de pruebas — ISS-22

## Ejecutar las pruebas

Se necesita JDK 17 y Maven 3.9 o superior. Desde la carpeta `sistema-caja/`, ejecutar:

```bash
mvn clean test
```

El `pom.xml` configura JUnit 5 y copia el contenido completo de `data/` a `target/test-run/data/` durante la fase `process-test-resources`. Maven Surefire ejecuta las pruebas desde `target/test-run/`. Por eso, la ruta relativa `data/tienda.db` que utiliza `ConexionDB` resuelve a la copia de pruebas y no a `sistema-caja/data/tienda.db`. No se modifica `src/main` ni se requiere cambiar la ruta de conexión de la aplicación.

## Ejemplos incluidos

Los cuatro ejemplos iniciales están en `sistema-caja/src/test/java/com/tienda/AppTest.java`:

- **Controlador con DAO falso:** `calcularTotal_conDaoFalso_sumaPrecioPorCantidad` prueba `VentaController.calcularTotal` sin abrir la base de datos.
- **DAO contra la base aislada:** `listarTodos_conBaseAislada_consultaCopiaYNoModificaOriginal` usa `VentaDAO.listarTodos`, verifica que la copia exista dentro de `target/test-run/data` y comprueba que el archivo original conserva exactamente su contenido.
- **Método que imprime:** `generarTicket_conVentaValida_imprimeDatosDeLaVenta` captura temporalmente `System.out` y verifica el texto del ticket.
- **Vista de consola:** `mostrarMenu_conEntradaCero_muestraMenuYRegresa` sustituye temporalmente `System.in` por una entrada simulada y captura la salida del menú.

## Cómo reutilizar los ejemplos

Cada responsable debe crear sus propias clases de prueba bajo `src/test/java`, siguiendo el paquete de la clase que prueba (por ejemplo, `com/tienda/cliente/ClienteDAOTest.java`). Para controladores, inyectar un DAO falso y evitar SQLite; para DAO, usar la conexión relativa `data/tienda.db`, que apunta automáticamente a la copia aislada; para métodos que imprimen, guardar y restaurar `System.out`; y para vistas de consola, guardar y restaurar `System.in` y pasar un `Scanner` construido con esa entrada. No colocar documentación dentro de `src/test/java`: esta guía vive en `docs/pruebas/` a nivel raíz del repositorio.
