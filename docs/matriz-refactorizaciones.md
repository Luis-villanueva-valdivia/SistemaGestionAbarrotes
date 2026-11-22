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

## Organización de datos — Katherin

| ID | Code smell | Ubicación | Técnica | RF afectados | Estado | Justificación |
|---|---|---|---|---|---|---|
| DAT-01 | Datos de dominio con reglas de formato (`vencimiento`, `codigo`) representados como `String` planos sin validación | `producto/ProductoModel.java` (`vencimiento`, `codigo`, líneas 8 y 12) | Replace Data Value with Object | RF-01, RF-03 | Detectado | Permite almacenar fechas inválidas o códigos mal formados sin validación centralizada ni operaciones propias del dominio |
| DAT-02 | Acceso y modificación directa al campo `this.cantidad` dentro de la misma clase en lugar de usar getters/setters | `producto/ProductoModel.java`, `actualizarStock()` (líneas 36-40) | Self Encapsulate Field | RF-01, RF-02 | Detectado | Impide centralizar reglas de validación o alertas de stock mínimo en un único método mutador |
| DAT-03 | El arreglo `productos` se devuelve y asigna directamente en `getProductos()` y `setProductos()` exponiendo el estado interno | `venta/VentaModel.java` (`productos`, líneas 11, 25, 38) | Encapsulate Collection | RF-02 | Detectado | Cualquier clase externa puede alterar el contenido del arreglo sin control ni recálculo de totales |
| DAT-04 | El tipo de pago se almacena como texto libre (`metodoPago`) junto con campos condicionales (`numeroTarjeta`) | `venta/VentaModel.java` (`metodoPago`, `numeroTarjeta`, líneas 16-17, 29-30, 42-43) | Replace Type Code with Class / Subclasses / State-Strategy | RF-02 | Detectado | Propenso a errores de escritura en cadenas y deja atributos nulos innecesarios cuando el pago es en efectivo |
| DAT-05 | Los datos del cliente asociado a la venta se guardan como primitivos sueltos (`cliente`, `clienteId`) en vez de referenciar un objeto `ClienteModel` | `venta/VentaModel.java` (`cliente`, `clienteId`, líneas 12-13, 26-27, 39-40) | Replace Data Value with Object | RF-02, RF-04 | Detectado | Duplica información del cliente y rompe la integridad referencial entre entidades del modelo |
| DAT-06 | El tipo de movimiento (`"ENTRADA"`, `"SALIDA"`, `"AJUSTE"`) se almacena como `String` libre sin restricción ni comportamiento polimórfico | `inventario/MovimientoInventarioModel.java` (`tipo`, líneas 6, 13, 20) | Replace Type Code with Class / Subclasses / State-Strategy | RF-01 | Detectado | No garantiza valores válidos en tiempo de compilación y obliga a usar condicionales repetitivos para afectar el stock |
| DAT-07 | La fecha del movimiento se maneja como `String` plano en lugar de un objeto de fecha (`LocalDate` / `LocalDateTime`) | `inventario/MovimientoInventarioModel.java` (`fecha`, líneas 5, 12, 19) | Replace Data Value with Object | RF-01 | Detectado | Dificulta el ordenamiento cronológico, filtrado por periodos en reportes y validación de formato |
| DAT-08 | Número mágico `0.05` (5 % de descuento por cliente frecuente) y umbral `1` incrustados directamente en la lógica de cobro | `ui/JavaFXApp.java`, `createVentasPane()` (líneas 434-435) | Replace Magic Number with Symbolic Constant | RF-02, RF-05 | Detectado | Dificulta actualizar el porcentaje de descuento y oculta el significado de negocio. *Nota: en `ui/JavaFXApp.java`, se refactoriza después de #34* |
| DAT-09 | Comparación directa de cadenas literales (`metodo.equals("Efectivo")`) para decidir la lógica de pago en la vista | `ui/JavaFXApp.java`, `createVentasPane()` (línea 448) | Replace Type Code with Class / Subclasses / State-Strategy | RF-02 | Detectado | Acopla las reglas de validación de pago a literales de texto en la interfaz. *Nota: en `ui/JavaFXApp.java`, se refactoriza después de #34* |
| DAT-10 | El estado del carrito de compras (`cartItems`) y el cálculo de descuentos se mantienen directamente en la clase de interfaz gráfica en lugar de un modelo observable | `ui/JavaFXApp.java`, `createVentasPane()` (líneas 431-445) | Duplicate Observed Data | RF-02, RNF-04 | Detectado | Mezcla datos de negocio con componentes visuales de JavaFX. *Nota: en `ui/JavaFXApp.java`, se refactoriza después de #34* |

**Técnicas que no aplican:**
* **Encapsulate Field*:** Todos los atributos de las clases modelo (`ProductoModel`, `VentaModel`, `ClienteModel`, `MovimientoInventarioModel`, `PromocionModel`, `ReporteModel`) ya están declarados con visibilidad `private` y cuentan con métodos `get` y `set`.
* **Replace Array with Object:** No se encontraron arreglos heterogéneos usados para simular registros o tuplas de distintos tipos de datos (el arreglo `ProductoModel[]` en `VentaModel` es homogéneo y se cubre en `DAT-03` con *Encapsulate Collection*).
* **Change Value to Reference / Change Reference to Value:** El sistema trabaja con DTOs/Modelos sencillos persistidos mediante DAO sin ciclos de vida de instancias compartidas en memoria ni objetos de valor inmutables complejos.
* **Change Unidirectional/Bidirectional Association:** Las relaciones entre entidades se resuelven mediante identificadores numéricos (`clienteId`, `productoId`) a nivel de consultas DAO, sin referencias cruzadas entre objetos en memoria.
* **Replace Record with Data Class:** El proyecto está desarrollado en Java orientado a objetos y ya utiliza clases modelo estructuradas en lugar de registros externos sin formato.
