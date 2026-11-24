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
| LLA-01 | | | | | | |

**Técnicas que no aplican:**

---

## Organización de datos - Katherin

| ID | Code smell | Ubicación | Técnica | RF afectados | Estado | Justificación |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| DAT-01 | Datos de dominio con reglas de formato (`vencimiento`, `codigo`) representados como `String` planos sin validación centralizada | `producto/ProductoModel.java` (`vencimiento`, `codigo`, líneas 8 y 12) | Reemplazar valor de dato con objeto (Replace Data Value with Object) | RF-01, RF-03 | Detectado | |
| DAT-02 | El arreglo `productos` se devuelve y asigna directamente en `getProductos()` y `setProductos()`, permitiendo alterar el arreglo interno sin recálculo de totales | `venta/VentaModel.java` (`productos`, líneas 11, 25, 38) | Encapsular colección (Encapsulate Collection) | RF-06, RF-08 | Detectado | |
| DAT-03 | El tipo de pago se maneja como texto libre (`"Efectivo"` / `"Tarjeta"`) con campos condicionales (`numeroTarjeta`) y comparaciones literales repetidas en lugar de reutilizar las clases `PagoEfectivo` y `PagoTarjeta` | `venta/VentaModel.java` (`metodoPago`, `numeroTarjeta`, líneas 16-17, 29-30, 42-43); `venta/VentaController.java` (líneas 65-68); `venta/VentaView.java` (líneas 105, 125); `ui/JavaFXApp.java` (líneas 327-342, 448, 534) | Reemplazar código de tipo con subclases (Replace Type Code with Subclasses) | RF-06, RF-08 | Detectado | |
| DAT-04 | Los datos del cliente asociado a la venta se guardan como valores sueltos (`cliente`, `clienteId`), duplicando datos en vez de referenciar la instancia de `ClienteModel` | `venta/VentaModel.java` (`cliente`, `clienteId`, líneas 12-13, 26-27, 39-40) | Cambiar valor a referencia (Change Value to Reference) | RF-06, RF-14, RF-15 | Detectado | |
| DAT-05 | El tipo de movimiento (`"ENTRADA"`, `"SALIDA"`, `"AJUSTE"`) se almacena como `String` libre y se evalúa con condicionales repetidos (nota: actualmente `"AJUSTE"` se guarda como `"ENTRADA"`, no cambiar este comportamiento al refactorizar) | `inventario/MovimientoInventarioModel.java` (`tipo`, líneas 6, 13, 20); `inventario/MovimientoDAO.java` (líneas 35-37); `venta/VentaController.java` (línea 24); `ui/JavaFXApp.java` (líneas 244-245) | Reemplazar código de tipo con clase (Replace Type Code with Class) | RF-02, RF-05, RF-09 | Detectado | |
| DAT-06 | La fecha del movimiento se maneja como `String` plano en lugar de un objeto de fecha (`LocalDate` / `LocalDateTime`), dificultando ordenamiento y filtros por periodo | `inventario/MovimientoInventarioModel.java` (`fecha`, líneas 5, 12, 19) | Reemplazar valor de dato con objeto (Replace Data Value with Object) | RF-05 | Detectado | |
| DAT-07 | Número mágico `0.05` (5 % de descuento por cliente frecuente) y umbral `1` incrustados directamente en la lógica de cobro (`ui/JavaFXApp.java`: se refactoriza después de #34) | `ui/JavaFXApp.java`, `createVentasPane()` (líneas 434-435) | Reemplazar número mágico con constante simbólica (Replace Magic Number with Symbolic Constant) | RF-07, RF-14 | Detectado | |
| DAT-08 | El estado del carrito de compras (`cartItems`) y el cálculo de descuentos se mantienen directamente en la interfaz gráfica en lugar de un modelo observable (`ui/JavaFXApp.java`: se refactoriza después de #34) | `ui/JavaFXApp.java`, `createVentasPane()` (líneas 431-445) | Duplicar datos observados (Duplicate Observed Data) | RF-06, RF-07 | Detectado | |

**Técnicas que no aplican:**
* **Auto-encapsular campo (Self Encapsulate Field):** Aunque `ProductoModel.actualizarStock()` (líneas 36-40) accede directamente a `this.cantidad`, dicho método no se llama desde ninguna parte del programa (el stock se descuenta desde otras clases), por lo que refactorizarlo no tendría efecto real en el sistema.
* **Encapsular campo (Encapsulate Field):** Todos los atributos de las clases modelo (`ProductoModel`, `VentaModel`, `ClienteModel`, `MovimientoInventarioModel`, `PromocionModel`, `ReporteModel`) ya están declarados con visibilidad `private` y cuentan con métodos `get` y `set`.
* **Reemplazar arreglo con objeto (Replace Array with Object):** No se encontraron arreglos heterogéneos usados para simular registros o tuplas de distintos tipos de datos (el arreglo `ProductoModel[]` en `VentaModel` es homogéneo y se cubre en `DAT-02` con *Encapsular colección*).
* **Cambiar referencia a valor (Change Reference to Value):** El sistema no utiliza objetos de valor inmutables compartidos en memoria que requieran convertirse a copias por valor.
* **Cambiar asociación unidireccional a bidireccional / bidireccional a unidireccional (Change Unidirectional/Bidirectional Association):** Las relaciones entre entidades se resuelven mediante identificadores numéricos (`clienteId`, `productoId`) a nivel de consultas DAO, sin referencias cruzadas entre objetos en memoria.
* **Reemplazar registro con clase de datos (Replace Record with Data Class):** El proyecto está desarrollado en Java orientado a objetos y ya utiliza clases modelo estructuradas en lugar de registros externos sin formato.
