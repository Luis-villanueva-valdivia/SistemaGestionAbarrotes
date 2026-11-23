# Matriz de refactorizaciones

Registro de los *code smells* detectados y de la técnica que se aplica en cada caso. Responsable de integrarla y revisarla: **Katherin**. Reglas completas en `docs/convenciones.md` §12.1 y §5.

- Cada quien escribe **solo en la sección de su unidad** (así no hay conflictos en Git).
- **ID:** prefijo de la unidad y número: `GRA`, `GEN`, `CON`, `MOV`, `LLA`, `DAT` (por ejemplo, `DAT-03`). Es el mismo que va en el comentario `// [DAT-03] Técnica` del código.
- **Code smell** y **Técnica:** con el nombre del curso.
- **Estado:** Detectado · En progreso · Aplicada (#PR) · No aplica.
- **Justificación:** solo cuando una técnica no se aplica.
- Al final de cada unidad, la lista de técnicas del curso que **no aplican** en el sistema y por qué.

---

## Grandes refactorizaciones — Luis (coordina)

| ID | Code smell | Ubicación | Técnica | RF afectados | Estado | Justificación |
|---|---|---|---|---|---|---|
| GRA-01 | | | | | | |

**Técnicas que no aplican:**

---

## Manejo de la generalización — Luis

| ID | Code smell | Ubicación | Técnica | RF afectados | Estado | Justificación |
|---|---|---|---|---|---|---|
| GEN-01 | | | | | | |

**Técnicas que no aplican:**

---

## Simplificación de expresiones condicionales — Oswaldo

| ID | Code smell | Ubicación | Técnica | RF afectados | Estado | Justificación |
|---|---|---|---|---|---|---|
| CON-01 | | | | | | |

**Técnicas que no aplican:**

---

## Movimiento de funciones entre objetos — Paola

| ID | Code smell | Ubicación | Técnica | RF afectados | Estado | Justificación |
|---|---|---|---|---|---|---|
| MOV-01 | Envidia de características (*Feature Envy*): el subtotal de un renglón (`precio * cantidad`) se calcula fuera de `ProductoModel`, con los datos del producto, en 10 lugares | `venta/VentaDAO.java`, `registrarVenta()` (l. 78); `venta/VentaController.java`, `calcularTotal()` (l. 37) y `generarTicket()` (l. 58); `venta/VentaView.java`, `mostrarVenta()` (l. 30) y `pedirDatosVenta()` (l. 91); `ui/JavaFXApp.java`, `createVentasPane()` (l. 317 y 412), `buildTicket()` (l. 521 y 526) y `updateTotalLabel()` (l. 652). JavaFX y vistas de consola: se refactorizan después de #34 | Mover método (*Move Method*): `ProductoModel.calcularSubtotal()` y que todos lo llamen | RF-06, RF-07, RF-08 | Detectado | |
| MOV-02 | Envidia de características: el total de la venta (suma de subtotales) se calcula en cinco clases distintas en vez de en `VentaModel`, que es quien tiene los productos | `venta/VentaController.java`, `calcularTotal(VentaModel)` (l. 32–41); `venta/VentaDAO.java`, `registrarVenta()` (l. 76–80); `venta/VentaView.java`, `pedirDatosVenta()` (l. 91); `ui/JavaFXApp.java`, `createVentasPane()` (l. 412), `buildTicket()` (l. 521) y `updateTotalLabel()` (l. 652). JavaFX y vistas de consola: después de #34 | Mover método (*Move Method*): `VentaModel.calcularTotal()`; `VentaController.calcularTotal` delega en él | RF-06, RF-07 | Detectado | |
| MOV-03 | Envidia de características: un método de `ClienteDAO` consulta la tabla `venta` (cuenta las ventas del mes de un cliente); usa los datos de otra clase | `cliente/ClienteDAO.java`, `contarVentasMes(int)` (l. 185–201), declarado en `cliente/IClienteDAO.java` (l. 12); lo llama `ui/JavaFXApp.java` (l. 433) | Mover método (*Move Method*) a `VentaDAO` / `IVentaDAO` | RF-07, RF-14 | Detectado | |
| MOV-04 | Lógica de negocio en la interfaz: la regla del descuento de cliente frecuente (5 % si tiene al menos una compra en el mes) y el cálculo del cambio están en el manejador del botón. `VentaController` ya tiene `aplicarDescuento` y `PagoEfectivo` ya tiene `calcularCambio`, pero no se usan aquí | `ui/JavaFXApp.java`, `createVentasPane()`, acción de `sellButton` (descuento l. 430–438, cambio l. 456). JavaFX: después de #34 | Mover método (*Move Method*): la regla a `VentaController` (usando `aplicarDescuento`) y el cambio a `PagoEfectivo.calcularCambio` | RF-06, RF-07, RF-14 | Detectado | |
| MOV-05 | Métodos en la clase equivocada: `ReporteModel` (un modelo de datos) tiene métodos estáticos que crean DAOs y hacen consultas. Repiten lo que ya hace `ReporteDAO`, y `reporteInventario()` lo hace con `ProductoDAO` y con los campos al revés | `reporte/ReporteModel.java`, `generarReporteVentas`, `reporteInventario`, `productoVendidoPorId`, `productosVendidosPorCategoria` (l. 94–122); imports de `ProductoDAO` y `ProductoModel` (l. 6–7) | Mover método (*Move Method*) a `ReporteDAO`; como ahí ya existen, después se eliminan (ver LLA-18) | RF-16, RF-17, RF-18 | Detectado | |
| MOV-06 | Clase con varias responsabilidades y campos que cambian de significado: `ReporteModel` se usa para tres reportes distintos. `periodo` guarda el nombre del producto, `categoria` guarda la etiqueta "dia/semana/mes" y `cantidadVentas` guarda el stock en el reporte de inventario | `reporte/ReporteModel.java` (campos l. 11–16); se llena en `reporte/ReporteDAO.java`, `reporteVentas()` (l. 50–54), `productoVendidoPorId()` (l. 71–76), `productosVendidosPorCategoria()` (l. 96–101) y `reporteInventario()` (l. 118–122) | Extraer clase (*Extract Class*): un modelo por reporte (ventas por periodo, producto vendido, inventario). Coordinar con Organización de datos | RF-16, RF-17, RF-18 | Detectado | |
| MOV-07 | Grupo de datos que pertenece a otra clase: `VentaModel` guarda los datos del pago (`metodoPago`, `numeroTarjeta`, `montoPagado`, `cambio`), que ya modelan `PagoEfectivo` y `PagoTarjeta` | `venta/VentaModel.java` (campos l. 16–19); se llenan en `venta/VentaView.java`, `pedirDatosVenta()` (l. 105–137) y en `ui/JavaFXApp.java`, `createVentasPane()` (l. 445–469) | Extraer clase (*Extract Class*) / Mover campo (*Move Field*): los datos del pago a una clase de pago referenciada desde `VentaModel` | RF-06, RF-08 | Detectado | |
| MOV-08 | Envidia de características: para buscar un producto por ID, quien llama trae **todos** los productos y filtra con `stream()`, aunque `BuscarProductoService.buscarPorId` ya existe | `venta/VentaView.java`, `pedirDatosVenta()` (l. 70–71); `ui/MainView.java`, `menuInventario()` (l. 127); `ui/JavaFXApp.java`, `createInventarioPane()` (l. 213) y `createVentasPane()` (l. 354). JavaFX y vistas de consola: después de #34 | Mover método (*Move Method*): la búsqueda va al DAO (`IProductoDAO.buscarPorId`) | RF-01, RF-06 | Detectado | |
| MOV-09 | Envidia de características: el código que copia un producto del inventario para meterlo al carrito con otra cantidad está repetido y usa solo datos de `ProductoModel` | `venta/VentaView.java`, `pedirDatosVenta()` (l. 81–85); `ui/JavaFXApp.java`, `createVentasPane()` (l. 370–376). JavaFX y vistas de consola: después de #34 | Mover método (*Move Method*): `ProductoModel.copiarConCantidad(int)` | RF-06 | Detectado | |
| MOV-10 | Cadena de mensajes (*Message Chains*): el cliente navega `ProductoDAO → BuscarProductoService` para buscar. `MovimientoController.getBuscador()` también expone el objeto delegado | `ui/MainView.java`, `menuInventario()` (l. 157, 162 y 167); `ui/JavaFXApp.java`, `createInventarioPane()` (l. 274); `inventario/MovimientoController.java`, `getBuscador()` (l. 57–59); `producto/IProductoDAO.java` (l. 10). JavaFX y consola: después de #34 | Ocultar delegado (*Hide Delegate*): métodos `buscarPorCodigo/Nombre/Categoria` en el DAO o controlador, que deleguen en el buscador | RF-11 | Detectado | |
| MOV-11 | Intermediario (*Middle Man*): todos los métodos de `ReporteController` solo pasan la llamada a `ReporteDAO` sin agregar nada. Lo mismo pasa con `PromocionController`, `ClienteController` y la parte CRUD de `MovimientoController` | `reporte/ReporteController.java` (l. 9–23); `promocion/PromocionController.java` (l. 13–31); `cliente/ClienteController.java` (l. 13–39); `inventario/MovimientoController.java` (l. 21–35, 47–53) | Quitar intermediario (*Remove Middle Man*) | RF-01 a RF-05, RF-12 a RF-18 | Detectado | Se registra pero **se recomienda no aplicarlo**: #34 va a hacer que las interfaces pasen por los controladores (MVC), y ahí ganan responsabilidades (MOV-04, MOV-08). Se decide con Grandes refactorizaciones |
| MOV-12 | Falta un método en una clase de biblioteca que no podemos modificar: el formato de dinero `String.format("%.2f", x)` está repetido en 12 lugares, y la lectura de un monto con coma decimal `Double.parseDouble(s.trim().replace(',', '.'))`, en 2 | Formato: `venta/VentaController.java`, `generarTicket()` (l. 58, 62, 66, 67); `ui/JavaFXApp.java` (l. 317, 526, 528, 530, 532, 535, 536, 653). Lectura de monto: `venta/VentaView.java` (l. 110); `ui/JavaFXApp.java` (l. 450). JavaFX y consola: después de #34 | Introducir método externo (*Introduce Foreign Method*): `formatearMonto(double)` y `leerMonto(String)` en `util` | RF-06, RF-07, RF-08 | Detectado | |

**Técnicas que no aplican:**

- **Clase en línea (*Inline Class*):** no se encontró ninguna clase que no haga casi nada y cuyo trabajo convenga pasar a otra. Las clases chicas (`PagoEfectivo`, `PagoTarjeta`, `PromocionModel`, `MovimientoInventarioModel`) tienen su propia responsabilidad. `PagoEfectivo` y `PagoTarjeta`, además, implementan `IMetodoPago`. Los controladores que solo delegan se registran como intermediarios (MOV-11), no como clases para eliminar.
- **Introducir extensión local (*Introduce Local Extension*):** solo hacen falta dos métodos externos sobre `String` y `double` (MOV-12). Crear una subclase o *wrapper* de una clase de biblioteca para dos métodos es más complejo que dejarlos como métodos estáticos en `util`. Si después se necesitaran más de tres métodos sobre la misma clase, se revisa otra vez.
- **Mover campo (*Move Field*) por separado:** el único caso encontrado es el de los datos del pago en `VentaModel`, que va junto con la extracción de clase de MOV-07. No hay campos sueltos que otra clase use más que la suya.

**Observaciones para otras unidades (no se registran aquí):**

- **Errores que encontré (B-XX):** `ReporteView` muestra las columnas cruzadas con lo que llena `ReporteDAO`. En el reporte de inventario (opción 2) la columna "Nombre" muestra la categoría y la columna "Categoria" muestra el nombre. En "Producto vendido por ID" (opción 3) y "por categoría" (opción 4) se muestra la categoría donde debería ir el nombre del producto. La vista se escribió para los campos de `ReporteModel.reporteInventario()` (MOV-05), que están al revés que en el DAO. El cruce del inventario ya está descrito en la ERS (RF-17); el de las opciones 3 y 4 (RF-18) no. No se corrige aquí: se abre issue `B-XX`.
- `VentaModel` guarda `cliente` (nombre) y `clienteId` por separado en vez de una referencia a `ClienteModel` (`venta/VentaModel.java`, l. 12–13). Le toca a Organización de datos (*Change Value to Reference*).
- `JavaFXApp` usa `ReporteDAO`, `ProductoDAO`, `ClienteDAO` y `PromocionDAO` directo, sin pasar por los controladores (l. 45–48, 616, 622). Es información para #34.

---

## Simplificación de las llamadas a métodos — José Armando

| ID | Code smell | Ubicación | Técnica | RF afectados | Estado | Justificación |
|---|---|---|---|---|---|---|
| LLA-01 | Método de consulta que imprime en vez de devolver datos (el DAO hace trabajo de vista) | `venta/VentaDAO.java`, `historial()` (líneas 31–41) y `historialPorCliente(int)` (líneas 48–66); declarados en `IVentaDAO` (líneas 7 y 9) | Separar consulta de modificador (*Separate Query from Modifier*): que devuelvan `List<VentaModel>` y la vista imprima | RF-10, RF-13 | Detectado | |
| LLA-02 | Dos métodos públicos con la misma responsabilidad y nombres distintos: `registrar` solo delega en `registrarVenta` | `venta/IVentaDAO.java` (líneas 6 y 11); `venta/VentaDAO.java`, `registrar()` (líneas 26–29) y `registrarVenta()` (líneas 68–127) | Renombrar método (*Rename Method*): dejar un solo nombre en la interfaz y eliminar el alias | RF-06, RF-09 | Detectado | |
| LLA-03 | Código de error como valor de retorno: `-1` significa "ID inválido" y quien llama tiene que comprobar `id > 0` | `inventario/InventarioView.java`, `pedirIdProducto()` (líneas 55–63); `cliente/ClienteView.java`, `pedirIdCliente()` (líneas 74–79); `promocion/PromocionView.java`, `pedirIdPromocion()` (líneas 39–43). Vistas de consola: se refactorizan después de #34 | Reemplazar código de error con excepción (*Replace Error Code with Exception*) | RF-01, RF-03, RF-05, RF-12, RF-15 | Detectado | |
| LLA-04 | Los errores de BD se tragan en el DAO: `catch (SQLException)` imprime y devuelve lista vacía, `null` o nada. Quien llama no distingue "no hay datos" de "falló la BD" (por ejemplo, `ProductoDAO.agregar` deja `id = 0` y la consola muestra "Producto agregado con ID: 0") | `producto/ProductoDAO.java` (todos los métodos), `producto/BuscarProductoService.java` (`buscarPorId`, `buscar`), `inventario/MovimientoDAO.java`, `cliente/ClienteDAO.java`, `venta/VentaDAO.java`, `promocion/PromocionDAO.java`, `reporte/ReporteDAO.java` | Reemplazar código de error con excepción (*Replace Error Code with Exception*) | RF-01 a RF-18 (todos pasan por un DAO); RNF-02 | Detectado | |
| LLA-05 | Se usa una excepción para controlar un caso que se puede comprobar antes: `catch (NullPointerException)` cuando la fecha de la venta es `null` | `venta/VentaDAO.java`, `listarTodos()` (líneas 140–145) | Reemplazar excepción con prueba (*Replace Exception with Test*): `if (f == null)` antes de `fmt.parse` | RF-10 | Detectado | |
| LLA-06 | Método casi igual a otro que solo cambia el tipo del parámetro: `buscarPorId` copia todo el cuerpo de `buscar` para usar `setInt` en vez de `setString` | `producto/BuscarProductoService.java`, `buscarPorId(int)` (líneas 33–61) frente a `buscar(String, String)` (líneas 63–90) | Parametrizar método (*Parameterize Method*) | RF-11 | Detectado | |
| LLA-07 | Dos búsquedas iguales que solo cambian la columna del `WHERE` (mismo mapeo de 5 campos repetido) | `cliente/ClienteDAO.java`, `buscar(int)` (líneas 42–66) y `buscarPorTelefono(String)` (líneas 158–182) | Parametrizar método (*Parameterize Method*) | RF-06, RF-12 | Detectado | |
| LLA-08 | Un parámetro de texto decide qué hace el método (`"diario"`, `"semanal"`, `"mensual"`, `"anual"`) con un `switch` interno | `reporte/ReporteDAO.java`, `reporteVentas(String tipo)` (líneas 20–61); lo llaman `ReporteController.ventasPorPeriodo` y `JavaFXApp.createReportesPane()` (línea 603) | Reemplazar parámetro con métodos explícitos (*Replace Parameter with Explicit Methods*) | RF-16 | Detectado | |
| LLA-09 | Se pasa por separado una parte del objeto que ya se recibe: `items` es lo mismo que `venta.getProductos()` | `ui/JavaFXApp.java`, `buildTicket(List<ProductoModel>, VentaModel, double)` (líneas 517–542), llamado en la línea 473. JavaFX: se refactoriza después de #34 | Preservar el objeto completo (*Preserve Whole Object*) | RF-07, RF-08 | Detectado | |
| LLA-10 | Parámetro sobrante: `descuento` ya está en `venta.getDescuento()` (se asigna antes con `venta.setDescuento(descuento)`) | `ui/JavaFXApp.java`, `buildTicket(List<ProductoModel>, VentaModel, double)` (líneas 517–542), llamado en la línea 473. JavaFX: se refactoriza después de #34 | Quitar parámetro (*Remove Parameter*) | RF-07, RF-08 | Detectado | |
| LLA-11 | Dependencia creada dentro del método en vez de recibirla: `registrarVenta` hace `new MovimientoDAO()`, así que no se puede probar sin BD | `venta/VentaController.java`, `registrarVenta()` (líneas 17–29, `new MovimientoDAO()` en la línea 20) | Agregar parámetro (*Add Parameter*): recibir `IMovimientoDAO` en el constructor | RF-06, RF-09 | Detectado | |
| LLA-12 | Constructores que obtienen la conexión por su cuenta (`ConexionDB.getInstancia()`) o crean su DAO con `new`; no se puede pasar una conexión o DAO de prueba. `BuscarProductoService` ya recibe la conexión por parámetro y es el modelo a seguir | Constructores de `producto/ProductoDAO.java` (l. 17), `inventario/MovimientoDAO.java` (l. 16), `cliente/ClienteDAO.java` (l. 17), `venta/VentaDAO.java` (l. 22), `promocion/PromocionDAO.java` (l. 15), `reporte/ReporteDAO.java` (l. 16); `promocion/PromocionController.java` (l. 9) y `reporte/ReporteController.java` (l. 7) | Agregar parámetro (*Add Parameter*) | RF-01 a RF-18 | Detectado | |
| LLA-13 | Consulta con efectos secundarios: `procesar` devuelve si el pago es válido y además imprime mensajes en consola | `metodoPago/PagoEfectivo.java`, `procesar(double)` (líneas 9–18); `metodoPago/PagoTarjeta.java`, `procesar(double)` (líneas 9–18) | Separar consulta de modificador (*Separate Query from Modifier*) | RF-06 | Detectado | |
| LLA-14 | El nombre no dice lo que hace: `verificarStock` no verifica nada, devuelve la lista de productos con cantidad menor o igual al umbral | `inventario/MovimientoController.java`, `verificarStock(int)` (línea 39) | Renombrar método (*Rename Method*) | RF-04 | Detectado | |
| LLA-15 | El nombre no dice lo que hace: `actualizarStock` solo resta 1 a la cantidad si es mayor que 0 | `producto/ProductoModel.java`, `actualizarStock()` (línea 36) | Renombrar método (*Rename Method*) | RF-09 | Detectado | |
| LLA-16 | Nombre inconsistente: `buscar(int)` es la búsqueda por ID; en producto el mismo método se llama `buscarPorId` | `cliente/ClienteDAO.java` (línea 43) e `IClienteDAO.java`, `buscar(int)`; lo llama `ClienteController.buscarCliente(int)` | Renombrar método (*Rename Method*) | RF-12 | Detectado | |
| LLA-17 | Nombre inconsistente entre capas: el controlador se llama `productoPorId` y el DAO que llama, `productoVendidoPorId` | `reporte/ReporteController.java`, `productoPorId(int)` (línea 17) | Renombrar método (*Rename Method*) | RF-18 | Detectado | |
| LLA-18 | Métodos públicos que solo se usan dentro de su propia clase o que nadie llama | `inventario/InventarioView.java`, `mostrarAlertaStock()` (l. 67; solo la usa `mostrarResumenAlertasStock`; vista de consola, después de #34); `reporte/ReporteModel.java`, métodos estáticos `generarReporteVentas`, `reporteInventario`, `productoVendidoPorId`, `productosVendidosPorCategoria` (líneas 94–122; ningún llamador) | Ocultar método (*Hide Method*) | RF-04, RF-16, RF-17, RF-18 | Detectado | |

**Técnicas que no aplican:**

- **Introducir objeto parámetro (*Introduce Parameter Object*):** no hay grupos de parámetros que se repitan en varios métodos. Los métodos reciben uno o dos valores (`id`, `umbral`, `tipo`) o un modelo completo. El único constructor con muchos parámetros, `ReporteModel(int, String, String, double, int, String)`, no se llama en ningún lugar y corresponde a Organización de datos.
- **Reemplazar parámetro con método (*Replace Parameter with Method*):** no se encontró ninguna llamada donde quien llama obtenga un valor con un método que el método llamado pueda invocar por su cuenta. El caso de `buildTicket` se registra en LLA-09 y LLA-10.
- **Eliminar método de asignación (*Remove Setting Method*):** los `set` de los modelos los usan los DAO para mapear filas y para guardar el `id` generado (`setId` después del `INSERT`), y las vistas para capturar datos. Quitarlos exige constructores con todos los campos, que es parte de Encapsulate Field de Organización de datos (Katherin). Se revisa de nuevo después de esa unidad.
- **Reemplazar constructor con método de fábrica (*Replace Constructor with Factory Method*):** no hay constructores que elijan una subclase según un código de tipo. `PagoEfectivo` y `PagoTarjeta` se crean en `VentaView` y en `JavaFXApp`, pero cada rama necesita un dato distinto (monto recibido o número de tarjeta). Una fábrica no quitaría ese condicional. `ConexionDB` ya usa `getInstancia()`.

**Observaciones para otras unidades (no se registran aquí):**

- `ConexionDB.getConexion()` (l. 42–56) devuelve la conexión y, si está cerrada, abre una nueva. Podría verse como consulta con efecto secundario, pero es el patrón normal de una conexión perezosa, así que no se registra. Si Generalización o Grandes refactorizaciones cambian `ConexionDB` (por ejemplo, para inyectar la conexión, LLA-12), se revisa ahí.
- `IVentaDAO.generarReporte(String)` siempre devuelve `null` y nadie lo llama (`VentaDAO`, l. 43–46). Le toca a Generalización (Extract Interface / Collapse Hierarchy).
- `catch` vacíos en `VentaDAO.registrarVenta()` (l. 122 y 125), `InventarioView.mostrarMenu()` (l. 154 y 156) y `JavaFXApp.createInventarioPane()` (l. 192, 193 y 218). Van contra §10 de las convenciones. Coinciden en ubicación con LLA-04, pero no los corrige ninguna técnica de esta unidad.
- `MovimientoController`, `ClienteController` y `PromocionController` no se crean en ninguna interfaz, y `InventarioView.mostrarMenu()`, `ClienteView.mostrarMenu()` y `PromocionView.mostrarMenu()` no se llaman desde `MainView`. Es información para #34.

---

## Organización de datos — Katherin

| ID | Code smell | Ubicación | Técnica | RF afectados | Estado | Justificación |
|---|---|---|---|---|---|---|
| DAT-01 | | | | | | |

**Técnicas que no aplican:**
