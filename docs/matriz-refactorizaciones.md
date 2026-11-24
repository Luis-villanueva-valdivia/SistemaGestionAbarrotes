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
| CON-01 | Condicionales anidados al resolver un cliente opcional durante una venta | `sistema-caja/src/main/java/com/tienda/venta/VentaView.java`, `pedirDatosVenta()` (líneas 45–59; vista de consola, se reorganiza después de #34) | Sustituir condicionales anidados por cláusulas de guarda (*Replace Nested Conditional with Guard Clauses*) | RF-06, RF-08 | Detectado | |
| CON-02 | Condicionales anidados en el flujo de actualización de clientes | `sistema-caja/src/main/java/com/tienda/cliente/ClienteView.java`, `mostrarMenu()`, caso `"4"` (líneas 109–125) | Sustituir condicionales anidados por cláusulas de guarda (*Replace Nested Conditional with Guard Clauses*) | RF-12 | Descartado | `ClienteView.mostrarMenu()` no se llama desde `MainView` (ver GRA-02) y la consola no permite editar clientes (RF-12). Depende de la decisión de código sin uso en #34. |
| CON-03 | Condicionales anidados en la actualización de productos desde la consola | `sistema-caja/src/main/java/com/tienda/ui/MainView.java`, `menuInventario()`, caso `"3"` (líneas 124–141; vista de consola, se reorganiza después de #34). El mismo patrón está en `InventarioView.mostrarMenu()` (líneas 145–158), pero ese menú no se llama (ver GRA-02). | Sustituir condicionales anidados por cláusulas de guarda (*Replace Nested Conditional with Guard Clauses*) | RF-01, RF-02 | Detectado | |
| CON-04 | Resolución del cliente y cálculo del descuento mezclados dentro del evento que registra la venta | `sistema-caja/src/main/java/com/tienda/ui/JavaFXApp.java`, `createVentasPane()`, evento de `sellButton` (líneas 415–438; interfaz JavaFX, se reorganiza después de #34) | Descomponer condicional (*Decompose Conditional*) | RF-06, RF-07, RF-08, RF-14 | Detectado | |
| CON-05 | Selección del método de pago y validación de efectivo/tarjeta concentradas en un mismo flujo | `sistema-caja/src/main/java/com/tienda/venta/VentaView.java`, `pedirDatosVenta()` (líneas 99–144; vista de consola, se reorganiza después de #34) | Descomponer condicional (*Decompose Conditional*) | RF-06 | Detectado | |
| CON-06 | Validación y captura de datos de pago dentro del evento de venta | `sistema-caja/src/main/java/com/tienda/ui/JavaFXApp.java`, `createVentasPane()`, evento de `sellButton` (líneas 448–470; interfaz JavaFX, se reorganiza después de #34) | Descomponer condicional (*Decompose Conditional*) | RF-06 | Detectado | |
| CON-07 | Uso de una variable booleana de control para terminar los ciclos de los menús | `MainView.mostrarMenu()` (líneas 39–57); `ReporteView.mostrarMenu()` (líneas 9–10 y 59). Son menús de consola y se reorganizan después de #34. El mismo patrón está en `ClienteView`, `InventarioView` y `PromocionView`, pero esos menús no se llaman (ver GRA-02). | Eliminar la variable de control (*Remove Control Flag*) | RNF-02 | Detectado | |

**Técnicas que no aplican:**

- **Consolidar expresiones condicionales (*Consolidate Conditional Expression*):** no se identificó un conjunto adicional de condiciones independientes que produzcan el mismo resultado y pueda unificarse sin alterar su significado. Las expresiones encontradas que comparten resultado ya están consolidadas, por ejemplo la validación `numTarjeta == null || numTarjeta.isEmpty()` de `PagoTarjeta` y la comprobación de tipo normalizado en `JavaFXApp`.
- **Consolidar fragmentos condicionales duplicados (*Consolidate Duplicate Conditional Fragments*):** no se identificaron bloques duplicados en ramas opuestas cuya consolidación sea segura y relevante. En el flujo de venta, el registro y la actualización posterior se ejecutan después de seleccionar y validar el pago, no repetidos dentro de ambas ramas.
- **Reemplazar condicional con polimorfismo (*Replace Conditional with Polymorphism*):** no se registra un caso adicional en esta unidad. El procesamiento del dominio de pago ya cuenta con `IMetodoPago`, `PagoEfectivo` y `PagoTarjeta`; los condicionales restantes de las vistas recopilan datos de entrada diferentes (monto recibido o número de tarjeta). Cualquier cambio de jerarquía o distribución de responsabilidades debe coordinarse con la unidad de generalización para evitar duplicar trabajo.
- **Introducir objeto nulo (*Introduce Null Object*):** no se identificó un valor nulo que pudiera sustituirse por un objeto neutro sin perder significado. En los flujos revisados, `null` distingue estados como cliente no encontrado, producto no encontrado o ausencia de cliente asociado, y cada uno produce una respuesta diferente.
- **Introducir aserción (*Introduce Assertion*):** las condiciones revisadas corresponden a validaciones de entradas del usuario y decisiones normales del flujo. No se identificaron invariantes internas cuya comprobación debiera expresarse mediante aserciones.

**Pistas preliminares revisadas que no se registran como code smell:** el condicional de `JavaFXApp` que cambia el texto de ayuda del campo de pago (alrededor de la línea 342) solo selecciona un texto de interfaz entre dos opciones; la normalización del tipo de movimiento (`JavaFXApp`, alrededor de las líneas 242–247) realiza una conversión breve y valida el resultado con una expresión combinada. Ninguno de estos fragmentos, por sí solo, presenta complejidad o duplicación suficiente para justificar una fila adicional.

**Notas de trazabilidad:**

- CON-02 queda descartado porque `ClienteView.mostrarMenu()` no se ejecuta; si #34 decide conectarlo, se vuelve a evaluar.
- CON-03 corresponde a RF-01 y RF-02: el flujo edita datos generales del producto y también su cantidad/existencia.
- CON-04 incluye RF-14 porque el código identifica clientes frecuentes y aplica el descuento asociado, además de calcular el total (RF-07) dentro del flujo de venta (RF-06) que genera el recibo (RF-08).
- CON-07 se relaciona con RNF-02 porque el ciclo mantiene el menú operativo ante opciones inválidas.
- CON-04 se cruza con MOV-04 (descuento en el botón de venta) y CON-05/CON-06 con GRA-04 (método de pago como texto). Se coordinan al refactorizar para no aplicar dos cambios sobre las mismas líneas.

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
| DAT-01 | | | | | | |

**Técnicas que no aplican:**
