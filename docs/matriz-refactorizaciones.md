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
| MOV-01 | | | | | | |

**Técnicas que no aplican:**

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
