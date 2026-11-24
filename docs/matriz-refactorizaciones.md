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
| GRA-01 | La interfaz gráfica tiene lógica de negocio y acceso a datos: los eventos de los botones llaman directo a los DAO y deciden reglas (validar el carrito y el stock, crear un cliente por teléfono, descuento de 5 %, validar el pago sin usar `IMetodoPago` y armar el ticket) | `ui/JavaFXApp.java` (655 líneas): `createClientesPane()` (l. 85–163), `createInventarioPane()` (l. 165–303), `createVentasPane()` (l. 305–515), `buildTicket()` (l. 517–542), `createPromocionesPane()` (l. 544–586), `createReportesPane()` (l. 588–636). Plan de separación abajo | Separar dominio de la presentación (*Separate Domain from Presentation*) | RF-01, RF-02, RF-03, RF-06, RF-07, RF-08, RF-09, RF-11, RF-12, RF-14, RF-15, RF-16, RF-17, RF-18 | Detectado | |
| GRA-02 | Las vistas de consola tienen lógica de negocio y acceso a datos: `MainView` usa los DAO directamente, y `VentaView.pedirDatosVenta()` crea su propio `ProductoDAO` y `ClienteDAO`, busca productos y calcula el total. Al revés, `VentaController.generarTicket()` imprime en consola desde el controlador. Además hay menús de consola paralelos que nadie llama (`mostrarMenu()` de `ClienteView`, `InventarioView` y `PromocionView`) y sus controladores nunca se crean | `ui/MainView.java`: `menuClientes()` (l. 69–102), `menuInventario()` (l. 104–181), `menuVentas()` (l. 183–202), `menuPromociones()` (l. 204–245); `venta/VentaView.java`, `pedirDatosVenta()` (l. 37–147); `venta/VentaController.java`, `generarTicket()` (l. 49–73) | Separar dominio de la presentación (*Separate Domain from Presentation*) | RF-01, RF-02, RF-03, RF-06, RF-07, RF-08, RF-10, RF-11, RF-12, RF-14, RF-15 | Detectado | |
| GRA-03 | Diseño procedimental: los modelos solo guardan datos (*getters* y *setters*) y sus reglas están repartidas en vistas, controladores y DAO. El subtotal (precio × cantidad) se calcula en 7 lugares, y el ajuste de existencias según el tipo de movimiento está dentro del DAO | Subtotal: `ui/JavaFXApp.java` (l. 317, 412, 521, 652), `venta/VentaView.java` (l. 91), `venta/VentaController.java`, `calcularTotal()` (l. 32–41), `venta/VentaDAO.java`, `registrarVenta()` (l. 76–79). Movimiento: `inventario/MovimientoDAO.java`, `registrar()` (l. 33–42 y 52). Modelos: `VentaModel`, `ProductoModel`, `MovimientoInventarioModel` | Convertir diseño procedimental en objetos (*Convert Procedural Design to Objects*): `VentaModel` calcula su subtotal y `MovimientoInventarioModel` su efecto en las existencias | RF-02, RF-06, RF-07, RF-09 | Detectado | |
| GRA-04 | El método de pago se maneja como texto (`"Efectivo"` / `"Tarjeta"`) con condicionales repetidos, aunque ya existe la jerarquía `IMetodoPago` → `PagoEfectivo` / `PagoTarjeta`. `IMetodoPago` no se usa como tipo en ninguna clase, `JavaFXApp` repite la validación del pago por su cuenta y `VentaModel` guarda campos que solo usa un tipo de pago (`numeroTarjeta` solo tarjeta, `cambio` solo efectivo) | `ui/JavaFXApp.java`, `createVentasPane()` (l. 448–470) y `buildTicket()` (l. 534–539); `venta/VentaView.java`, `pedirDatosVenta()` (l. 104–143); `venta/VentaController.java`, `generarTicket()` (l. 63–71); `venta/VentaModel.java` (l. 16–19). JavaFX y consola: se refactoriza después de #34 | Extraer jerarquía (*Extract Hierarchy*): que cada pago valide su dato, guarde sus propios datos (monto recibido y cambio, o número de tarjeta) y dé sus líneas del ticket. Depende de GEN-01 | RF-06, RF-08 | Detectado | |

**Técnicas que no aplican:**

- **Separar herencia (*Tease Apart Inheritance*):** no hay ninguna jerarquía que haga dos trabajos a la vez. La única jerarquía del sistema (`IMetodoPago` con `PagoEfectivo` y `PagoTarjeta`) tiene un solo nivel y una sola responsabilidad.

**Plan de separación para #34 (qué sale de cada pantalla y a dónde va):**

| Pantalla | Lógica que sale | Destino |
|---|---|---|
| JavaFX · Clientes | Llamadas directas a `ClienteDAO`: listar, registrar, buscar, actualizar y eliminar (l. 97–147) | `ClienteController` (ya existe y no se usa) |
| JavaFX · Inventario | CRUD con `ProductoDAO`; búsqueda del producto a editar con `listarTodos().stream().filter` (l. 213); normalizar AJUSTE → ENTRADA y validar el tipo (l. 244–248); armar el movimiento con la fecha y `new MovimientoDAO()` (l. 252–257); búsqueda con `BuscarProductoService` (l. 270–292) | `MovimientoController` (ya existe y no se usa) |
| JavaFX · Ventas | Validar producto, cantidad y stock al agregar al carrito (l. 350–377); subtotal (l. 317, 412, 652); buscar o crear el cliente por teléfono (l. 415–428); descuento de 5 % (l. 430–437); validar el pago y calcular el cambio (l. 448–470); armar el ticket (`buildTicket()`, l. 517–542) | `VentaController` (métodos nuevos), la jerarquía de pagos (GRA-04) y `ClienteController` para el cliente por teléfono |
| JavaFX · Promociones | Llamadas directas a `PromocionDAO` (l. 554–573) | `PromocionController` (ya existe y no se usa) |
| JavaFX · Reportes | Llamadas directas a `ReporteDAO`, incluidos dos `new ReporteDAO()` (l. 616 y 622) | `ReporteController` (ya lo usa la consola) |
| Consola · `MainView` | Lo mismo por módulo: clientes (l. 82, 89, 97), inventario (l. 116–177, con `new MovimientoDAO()` en la l. 175), ventas (`ventaDAO.listarTodos()`, l. 199) y promociones (l. 214–241) | Los mismos controladores que la interfaz gráfica |
| Consola · `VentaView` | `new ProductoDAO()` y `new ClienteDAO()` (l. 39 y 48), búsqueda de producto (l. 70–71) y total (l. 91) | `VentaController`; la vista solo pide y muestra datos |

- Al terminar, `JavaFXApp` solo arma la ventana y las pestañas, y cada pestaña pasa a su propia clase de vista en `ui/` (`ClientesPane`, `InventarioPane`, `VentasPane`, `PromocionesPane`, `ReportesPane`), como pide RNF-03.
- **No se conectan** los `mostrarMenu()` de `ClienteView`, `InventarioView` y `PromocionView`. Agregarían funciones que hoy no existen en consola, como editar y eliminar clientes (RF-12) o el historial de movimientos (RF-05). Se dejan para la decisión de código sin uso.
- Se conservan las diferencias entre las dos interfaces que describe la ERS. Por ejemplo, el descuento de 5 % solo existe en la gráfica (RF-07), la consola no valida stock y no asocia el cliente a la venta (RF-06), y cada interfaz tiene su propio formato de ticket (Apéndice C).

---

## Manejo de la generalización — Luis

| ID | Code smell | Ubicación | Técnica | RF afectados | Estado | Justificación |
|---|---|---|---|---|---|---|
| GEN-01 | Dos métodos de subclases hermanas siguen los mismos pasos en el mismo orden (validar su dato → si falla, imprimir el error y devolver `false` → imprimir "Pago … procesado" → devolver `true`); solo cambian la validación y los mensajes | `metodoPago/PagoEfectivo.java`, `procesar(double)` (l. 10–18); `metodoPago/PagoTarjeta.java`, `procesar(double)` (l. 10–18) | Formar método plantilla (*Form Template Method*) | RF-06 | Detectado | |
| GEN-02 | Seis clases sin padre común repiten el mismo campo, el mismo constructor y la misma forma de validar la conexión | `producto/ProductoDAO.java`, `inventario/MovimientoDAO.java`, `cliente/ClienteDAO.java`, `venta/VentaDAO.java`, `promocion/PromocionDAO.java`, `reporte/ReporteDAO.java`. `BuscarProductoService` también tiene su `conn` (l. 12–16) | Extraer superclase (*Extract Superclass*): `DAOBase` | RF-01 a RF-18 (todos pasan por un DAO) | Detectado | |
| GEN-03 | El mismo atributo `private final Connection conn` está en las seis clases de GEN-02 | `ProductoDAO` (l. 15), `MovimientoDAO` (l. 14), `ClienteDAO` (l. 15), `VentaDAO` (l. 19), `PromocionDAO` (l. 13), `ReporteDAO` (l. 14) | Subir atributo (*Pull Up Field*) a `DAOBase`. Depende de GEN-02 | RF-01 a RF-18 | Detectado | |
| GEN-04 | Los seis constructores tienen el mismo cuerpo: `conn = ConexionDB.getInstancia().getConexion()` | `ProductoDAO()` (l. 17–19), `MovimientoDAO()` (l. 16–18), `ClienteDAO()` (l. 17–19), `VentaDAO()` (l. 22–24), `PromocionDAO()` (l. 15–17), `ReporteDAO()` (l. 16–18) | Subir cuerpo del constructor (*Pull Up Constructor Body*): constructor de `DAOBase` llamado con `super(...)`. Depende de GEN-02 | RF-01 a RF-18 | Detectado | |
| GEN-05 | La guarda `if (conn == null) { imprimir; return; }` está copiada en casi todos los métodos de los DAO (26 veces); solo cambia el mensaje | `ClienteDAO` (8), `PromocionDAO` (5), `ProductoDAO` (4), `ReporteDAO` (4), `VentaDAO` (3), `MovimientoDAO` (2) | Subir método (*Pull Up Method*): se extrae a `hayConexion(String mensaje)`, que queda idéntico en las seis clases, y se sube a `DAOBase`. Depende de GEN-02 | RF-01 a RF-18 | Detectado | |
| GEN-06 | Tres métodos iguales en clases hermanas: validan la conexión, ejecutan `DELETE … WHERE id = ?` e imprimen el error; solo cambian la tabla y los mensajes | `cliente/ClienteDAO.java`, `eliminar(int)` (l. 87–99); `producto/ProductoDAO.java`, `eliminar(int)` (l. 72–84); `promocion/PromocionDAO.java`, `eliminar(int)` (l. 61–73) | Subir método (*Pull Up Method*): `eliminarPorId(...)` en `DAOBase`, con la tabla y los mensajes como parámetros. Depende de GEN-02 | RF-03, RF-12, RF-15 | Detectado | |
| GEN-07 | Cinco modelos repiten el mismo atributo `id` con su `getId()` y `setId()`, y lo usan igual: los DAO lo llenan con la llave generada después del `INSERT` | `cliente/ClienteModel.java` (l. 4, 11, 18), `producto/ProductoModel.java` (l. 4, 15, 26), `promocion/PromocionModel.java` (l. 4, 9, 14), `inventario/MovimientoInventarioModel.java` (l. 4, 11, 18), `venta/VentaModel.java` (l. 8, 22, 35) | Extraer superclase (*Extract Superclass*): `EntidadBase` con `id`, `getId()` y `setId()`. Las tablas de JavaFX siguen funcionando porque `PropertyValueFactory("id")` encuentra el *getter* heredado | RF-01, RF-06, RF-12, RF-15 | Detectado | |
| GEN-08 | Tres vistas de consola sin padre común comparten métodos con el mismo cuerpo (ver GEN-09 y GEN-10) | `cliente/ClienteView.java`, `inventario/InventarioView.java`, `promocion/PromocionView.java`. Vistas de consola: se refactoriza después de #34 | Extraer superclase (*Extract Superclass*): `VistaConsola` | RF-01, RF-03, RF-12, RF-15 | Detectado | |
| GEN-09 | Método idéntico en dos clases: `mostrarMensaje(String)` imprime `"[INFO] " + mensaje` | `cliente/ClienteView.java` (l. 61–63); `inventario/InventarioView.java` (l. 117–119). Vistas de consola: después de #34 | Subir método (*Pull Up Method*) a `VistaConsola`. Depende de GEN-08 | RF-01, RF-12 | Detectado | |
| GEN-10 | Tres métodos con el mismo cuerpo que solo cambian el texto que piden: leen un número, si no es válido imprimen "ID inválido" y devuelven `-1` | `cliente/ClienteView.java`, `pedirIdCliente()` (l. 74–79); `inventario/InventarioView.java`, `pedirIdProducto()` (l. 55–63); `promocion/PromocionView.java`, `pedirIdPromocion()` (l. 39–43). Vistas de consola: después de #34 | Subir método (*Pull Up Method*): `pedirId(Scanner, String etiqueta)` en `VistaConsola`. Depende de GEN-08 | RF-01, RF-03, RF-12, RF-15 | Detectado | |
| GEN-11 | `ReporteDAO` es el único DAO sin interfaz. `ReporteController`, `JavaFXApp` y `ReporteModel` dependen de la clase concreta, así que no se puede probar con un DAO falso | `reporte/ReporteDAO.java`; usado en `reporte/ReporteController.java` (l. 7), `ui/JavaFXApp.java` (l. 48, 616, 622), `reporte/ReporteModel.java` (l. 95, 115, 120) | Extraer interfaz (*Extract Interface*): `IReporteDAO` | RF-16, RF-17, RF-18 | Detectado | |
| GEN-12 | Los clientes de la interfaz solo usan un subconjunto, y uno que usan no está en ella: declara `generarReporte` (siempre devuelve `null` y nadie lo llama), `historial` e `historialPorCliente` (nadie los llama) y `registrar` (alias de `registrarVenta`), pero le falta `listarTodos()`, que `MainView` (l. 199) y `JavaFXApp` (l. 639) usan con la clase concreta. Por eso `VentaDAOFalso` de las pruebas implementa métodos de relleno | `venta/IVentaDAO.java` (l. 6–11); `venta/VentaDAO.java` (l. 26–66 y 130) | Extraer interfaz (*Extract Interface*): rehacer `IVentaDAO` con `registrarVenta` y `listarTodos` | RF-06, RF-10, RF-13 | Detectado | |
| GEN-13 | Una clase con atributos que solo usan algunas instancias: sirve para tres tipos de reporte, y sus campos cambian de significado según quién la llena (`periodo` guarda el periodo o el nombre del producto, `categoria` la etiqueta del periodo o la categoría, `cantidadVentas` el stock en el reporte de inventario); `fechaGeneracion` no la usa ninguno | `reporte/ReporteModel.java`; se llena en `reporte/ReporteDAO.java` (l. 50–55, 71–76, 96–101, 118–122) y en sus métodos estáticos (l. 94–122) | Extraer subclase (*Extract Subclass*): un modelo por tipo de reporte (ventas por periodo, producto vendido, inventario). Las columnas de la tabla de reportes en JavaFX usan estos nombres de propiedad: se aplica después de #34 | RF-16, RF-17, RF-18 | Detectado | |

**Técnicas que no aplican:**

- **Bajar método y bajar atributo (*Push Down Method / Push Down Field*):** no hay nada en un tipo padre que use solo uno de sus hijos. `IMetodoPago` declara únicamente `procesar`, que implementan los dos, y lo exclusivo de cada pago (`calcularCambio`, `montoRecibido`, `numTarjeta`) ya está en su subclase. Las superclases nuevas (GEN-02, GEN-07, GEN-08) solo reciben lo que comparten todas sus hijas.
- **Colapsar jerarquía (*Collapse Hierarchy*):** cada `IXxxDAO` tiene una sola implementación, pero las interfaces sirven para probar con DAO falsos (`VentaDAOFalso` en `EjemplosBasePruebasTest`, y las pruebas de #28 a #32). Colapsarlas quitaría eso. `PagoEfectivo` y `PagoTarjeta` son diferentes entre sí.
- **Reemplazar herencia con delegación (*Replace Inheritance with Delegation*):** la única herencia de clase es `JavaFXApp extends Application`, y JavaFX la exige para `Application.launch`. `JavaFXApp` usa lo que necesita de `Application` (`start`).
- **Reemplazar delegación con herencia (*Replace Delegation with Inheritance*):** `ClienteController` delega 7 de los 8 métodos de `IClienteDAO` y `PromocionController` los 5 de `IPromocionDAO`, pero un controlador no *es* un DAO. Heredar ataría el controlador a la base de datos (sin DAO falso en las pruebas) y expondría la persistencia a las vistas, en contra de GRA-01 y GRA-02. Además, después de #34 los controladores tendrán lógica propia. El reenvío simple corresponde a *Remove Middle Man* (Movimiento de funciones).

**Revisado y descartado:**

- *Extract Subclass* en `VentaModel` (`numeroTarjeta` solo para tarjeta, `cambio` solo para efectivo): una venta no es un tipo de pago. Esos datos pasan al objeto de pago en GRA-04.
- *Form Template Method* en los métodos que listan datos en consola (`mostrarProductos`, `mostrarClientesFrecuentes`, `mostrarPromociones`, `mostrarHistorial`): siguen pasos parecidos, pero cada vista tiene varias listas con formatos distintos y forzar una plantilla complica más de lo que quita. Los ciclos de los menús ya están en CON-07.

**Coordinación con otras unidades:**

- GEN-02 a GEN-06 conviene aplicarlas **antes de LLA-12**: así el constructor de `DAOBase` recibe la `Connection` y LLA-12 se hace en un solo lugar.
- GEN-01 toca los mismos métodos que **LLA-13**. Si primero se quitan los `println`, la plantilla queda solo con la validación. Hay que acordar el orden con José Armando.
- GEN-10 toca los mismos métodos que **LLA-03**: primero se sube `pedirId` y después se cambia el `-1` por una excepción, así se cambia en un solo lugar.
- GEN-12 toca los mismos métodos que **LLA-01 y LLA-02**.
- GEN-13 se coordina con **Organización de datos** (Katherin) si allí se renombran los campos de `ReporteModel`.

**Observaciones (posibles B-XX, no se corrigen al refactorizar):**

- En consola, "Producto vendido por ID" muestra la categoría en lugar del nombre del producto (`reporte/ReporteView.java`, l. 44), porque `ReporteDAO.productoVendidoPorId()` guarda el nombre en `periodo`. La ERS no lo menciona en RF-18.
- `MainView.menuPromociones()`, opción 3 (l. 225), llama `buscarPorProducto(id)` con el ID de la promoción y no usa el resultado.
- `BuscarProductoService` (l. 44–53 y 74–83) repite el mismo mapeo de 9 campos que `ProductoDAO.mapRow()` (l. 110–122). No es de esta unidad; se avisa a Movimiento de funciones y a Llamadas a métodos (LLA-06).
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
