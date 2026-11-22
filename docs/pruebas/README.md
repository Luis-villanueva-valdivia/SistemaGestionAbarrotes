# Evidencia de pruebas

Una carpeta por entrega (`entrega-2/`, `entrega-3/`) con el reporte de `target/surefire-reports` y la captura de `mvn test` en un clon limpio. Ver `docs/convenciones.md` §11.4.

## Ejecución desde Maven

Las pruebas deben ejecutarse mediante Maven desde la carpeta `sistema-caja/`, utilizando `mvn test` o `mvn clean test`. No deben ejecutarse con el botón de pruebas del IDE, ya que este no utiliza necesariamente el `workingDirectory` configurado en Maven Surefire. Ejecutarlas desde el IDE podría hacer que la ruta relativa `data/tienda.db` apunte a la base de datos original en lugar de la copia aislada.

## Evidencia de las entregas

Para cada entrega se debe crear una carpeta correspondiente (`entrega-2/`, `entrega-3/`) dentro de `docs/pruebas/`. En ella se guardará el reporte generado en `target/surefire-reports/` y una captura de la ejecución de `mvn test` en un clon limpio. Esta estructura se utilizará para reunir la evidencia de cada entrega, de acuerdo con `docs/convenciones.md`, sección 11.4.