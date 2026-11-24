# Convenciones del equipo

Reglas con las que trabajamos en este repositorio. Si algo no está aquí, se decide en el issue correspondiente o en Slack y se agrega a este archivo por PR.

**Proyecto:** refactorización del *Sistema de Gestión de Abarrotes* (Construcción de Software, Equipo 3). El sistema original no es nuestro: la versión de partida está guardada en la etiqueta `v1.0-original`. **No se agregan funciones nuevas:** el sistema debe hacer exactamente lo mismo antes y después de cada cambio (RF-01 a RF-18 de la ERS).

La lista de issues, las dependencias y el calendario están en [`docs/planeacion.md`](planeacion.md).

**Resumen rápido:**

1. Revisa que las dependencias de tu issue ya estén **fusionadas en `develop`**.
2. Mueve tu issue a *En progreso* y avisa en Slack.
3. Crea tu rama desde `develop`: `tipo/iss-XX-descripcion`.
4. Commits con el formato `tipo: descripción (iss-XX)`. En refactorizaciones, **una técnica por commit**.
5. Antes del PR: `mvn clean compile` y `mvn test` en verde.
6. PR **hacia `develop` de este repo** con `Closes #XX`, los RF que conserva y a quién desbloquea.
7. Tu revisor aprueba, tú haces *Squash and merge*, se borra la rama y **avisas a los issues que desbloqueas**.

---

## 1. Entorno y versiones

| Herramienta | Versión |
|---|---|
| Java (JDK) | 17 |
| Maven | 3.9 o superior |
| JavaFX | 20.0.2 (lo baja Maven) |
| JUnit | 5 (lo baja Maven) |
| Base de datos | SQLite (`sistema-caja/data/tienda.db`) |
| Codificación | UTF-8, 4 espacios, sin tabuladores |

Comandos, desde la carpeta `sistema-caja/` (iguales en Windows, macOS y Linux):

```bash
mvn clean compile      # compilar
mvn test               # correr las pruebas
mvn javafx:run         # abrir la interfaz gráfica
```

El sistema tiene **dos interfaces**: la gráfica (`JavaFXApp`) y la de consola (`MainView` y las clases `*View`, que se abren pasando el argumento `cli` a `App`). Todo cambio se revisa en las dos.

> ⚠️ El `pom.xml` fija `javafx.platform = win`. La interfaz gráfica se prueba en Windows; `mvn compile` y `mvn test` funcionan en cualquier sistema.

---

## 2. Estructura del repositorio

| Carpeta o archivo | Qué va ahí |
|---|---|
| `sistema-caja/src/main/java/com/tienda/` | Código del sistema, un paquete por módulo (`venta`, `producto`, `inventario`, `cliente`, `promocion`, `reporte`, `metodoPago`, `ui`, `util`) |
| `sistema-caja/src/test/java/com/tienda/` | Pruebas JUnit, en el mismo paquete que la clase que prueban |
| `sistema-caja/data/` | `schema.sql` y la base de datos `tienda.db` |
| `docs/convenciones.md` | Este archivo |
| `docs/planeacion.md` | Issues, responsables, dependencias y calendario |
| `docs/matriz-refactorizaciones.md` | *Code smells* detectados y técnica aplicada en cada caso |
| `docs/uml/` | Diagramas (los originales y el de clases refactorizado) |
| `docs/pruebas/` | Evidencia de la ejecución de pruebas de cada entrega |
| `.github/pull_request_template.md` | Plantilla de PR |

La versión original del sistema **no se toca**: está en la etiqueta `v1.0-original` para comparar.

---

## 3. Ramas

```
main      ← solo recibe develop en cada entrega (v1.1.0, v2.0.0)
develop   ← integración, rama predeterminada
  └── tipo/iss-XX-descripcion   ← una rama por issue
```

- **Nunca** se trabaja directo en `main` ni en `develop`. Las dos están protegidas: no aceptan push directo, no se pueden borrar y piden 1 aprobación.
- Una rama por issue, siempre desde `develop` actualizado.
- Formato: `tipo/iss-XX-descripcion`, en minúsculas, sin acentos, con guiones.

| Tipo | Uso | Ejemplo |
|---|---|---|
| `test` | Pruebas | `test/iss-29-pruebas-cliente` |
| `refactor` | Refactorización sin cambiar comportamiento | `refactor/iss-41-encapsular-producto` |
| `docs` | Matriz, diagramas, documentación | `docs/iss-27-deteccion-datos` |
| `chore` | Configuración, Maven, estructura | `chore/iss-22-base-pruebas` |
| `fix` | Corrección de un error `B-XX` (solo si el equipo lo acuerda) | `fix/b-01-stock-negativo` |

No hay ramas `feat`: en este proyecto no se agregan funciones.

```bash
git switch develop
git pull
git switch -c test/iss-29-pruebas-cliente
```

---

## 4. Commits

Formato: **`tipo: descripción en imperativo (iss-XX)`**

- `tipo` en minúsculas: `test`, `refactor`, `docs`, `chore`, `fix`.
- Descripción en español, de 72 caracteres o menos, sin punto final.
- **Un paso, un commit que compila.** En refactorizaciones, **una técnica por commit y con su nombre del curso**: el historial es la evidencia de la matriz.
- Se fusiona con **squash**, así que el título del PR sigue el mismo formato.

```
test: agrega pruebas de ClienteController con DAO falso (iss-29)
refactor: encapsular campos de ProductoModel (iss-41)
refactor: reemplazar numero magico del descuento por constante (iss-41)
refactor: extraer la pantalla de ventas de JavaFXApp (iss-45)
docs: agrega code smells de organizacion de datos a la matriz (iss-27)
```

---

## 5. Flujo de trabajo y pull requests

> [!CAUTION]
> **Dependencias:** muchos issues no se pueden empezar hasta que otro esté **fusionado en `develop`**. No basta con que tenga PR abierto. Cada issue lo dice arriba, en el recuadro rojo.

1. **Revisa las dependencias** del issue. Si alguna no está fusionada, no empieces: el issue está en *Bloqueado*.
2. Mueve el issue a **En progreso** y avisa en el canal de Slack de tu tema.
3. Crea tu rama desde `develop` actualizado.
4. Haz **cambios pequeños**, un commit por paso.
5. Corre las pruebas: `mvn clean compile` y `mvn test`.
6. Trae lo último de `develop` y resuelve conflictos en tu rama:
   ```bash
   git fetch origin
   git merge origin/develop
   ```
7. Abre el **PR hacia `develop` de este repo**:
   > ⚠️ **Este repo es un fork.** GitHub propone por defecto el repo de origen. Revisa siempre que diga **base repository: `Luis-villanueva-valdivia/SistemaGestionAbarrotes`** · **base: `develop`**.
   - Título con el formato de los commits.
   - Llena la plantilla: `Closes #XX`, los RF que conserva y **Desbloquea a**.
   - Asigna a tu revisor (§6), mueve el issue a *En revisión* y avisa en Slack.
8. **Revisión.** El revisor baja la rama, compila, corre las pruebas y revisa los criterios de aceptación. En refactorizaciones revisa también que la interfaz gráfica y la de consola hagan lo mismo que antes. Aprueba o pide cambios; si pide cambios, el autor corrige en la misma rama y pide revisión otra vez.
9. Con la aprobación, **el autor fusiona** con **Squash and merge**. La rama se borra sola.
10. **Avisa en los issues que desbloqueas**: comenta "Ya está fusionado #XX", quítales `estado:bloqueado` y muévelos a *Por hacer*. Avísales también en Slack.

---

## 6. Revisión cruzada

Nadie aprueba su propio PR. En círculo:

| PR de | Lo revisa |
|---|---|
| Luis | Oswaldo |
| Oswaldo | Paola |
| Paola | José Armando |
| José Armando | Katherin |
| Katherin | Luis |

Excepciones:
- **PR de pruebas:** los revisa Oswaldo, que coordina las pruebas (los de Oswaldo, Luis).
- **PR de la matriz:** los revisa Katherin, que la integra (los de Katherin, Luis).
- Si el revisor no contesta en **4 horas**, puede aprobar cualquier otro integrante. Pídelo en Slack.

---

## 7. Lo que se pide sobre el código

### 7.1 Comentario de cada refactorización

Cada refactorización aplicada deja **un comentario de una línea** justo arriba de la clase o método que cambió, con el ID de la matriz y la técnica. Es lo que une el código con la matriz y con el commit:

```java
// [DAT-03] Encapsulate Field: los atributos pasan a privados con getters y setters
public class ProductoModel {
```

```java
// [CON-02] Replace Nested Conditional with Guard Clauses
public double calcularTotal(VentaModel v) {
```

### 7.2 Javadoc

Toda clase y método público **que se toque** lleva Javadoc: qué hace, parámetros, retorno y excepciones. El formato lo revisa Paola.

```java
/**
 * Calcula el total de una venta sumando el subtotal de cada producto.
 *
 * @param venta venta con sus productos; no debe ser {@code null}
 * @return total de la venta, sin descuentos
 */
public double calcularTotal(VentaModel venta) {
```

### 7.3 Pruebas

Toda clase refactorizada tiene sus pruebas en `src/test/java`, y deben pasar **igual que antes** del cambio (§11).

---

## 8. Orden de trabajo y dueños de archivo

El orden completo, con el diagrama de dependencias, está en [`docs/planeacion.md`](planeacion.md). En corto:

**Base de pruebas → (detección ‖ pruebas por módulo) → gran refactorización y refactorizaciones de datos y llamadas → diagrama → entrega**

| Unidad | Responsable | Archivos que toca | Depende de |
|---|---|---|---|
| Base de pruebas | Oswaldo | `pom.xml`, `src/test` | — |
| Detección (las 5 unidades) | Cada quien su unidad | `docs/matriz-refactorizaciones.md` (solo su sección) | — |
| Pruebas por módulo | Cada quien su módulo (§11.3) | `src/test` de su módulo | Base de pruebas |
| Gran refactorización | Luis coordina; cada quien su módulo | `JavaFXApp`, `MainView`, `*View` | Pruebas de todos los módulos |
| Organización de datos | Katherin | Modelos (`*Model`) | Su detección y las pruebas |
| Llamadas a métodos | José Armando | DAO y controladores (firmas de métodos) | Su detección y las pruebas |
| Diagrama de clases | Paola | `docs/uml/` | Gran refactorización |

- En el código heredado, **el dueño de cada clase es quien tiene el issue que la está cambiando.** Si necesitas tocar una clase que está en el issue de otra persona, avísale en su issue antes.
- Organización de datos y llamadas a métodos trabajan primero en **modelos y DAO**, que la gran refactorización no mueve, para no chocar.
- Si dos issues tocan el mismo método, se ponen como dependencia uno del otro.

---

## 9. Nombres en Java

| Elemento | Convención | Ejemplo |
|---|---|---|
| Paquetes | minúsculas, sin guiones | `com.tienda.venta` |
| Clases e interfaces | `PascalCase`, sustantivo | `VentaController`, `IMetodoPago` |
| Interfaces de DAO | prefijo `I` (como el código existente) | `IVentaDAO` |
| Métodos | `camelCase`, empiezan con verbo | `buscarPorId()`, `registrarVenta()` |
| Variables y atributos | `camelCase`, descriptivos | `productosConStockBajo` |
| Constantes | `MAYUSCULAS_CON_GUION_BAJO` | `DESCUENTO_CLIENTE_FRECUENTE` |
| Pruebas | clase probada + `Test` | `VentaControllerTest` |
| Métodos de prueba | `metodo_condicion_resultado` | `calcularTotal_conDosProductos_sumaSubtotales` |

En español, sin acentos ni ñ (`anio`, `contrasenia`). Los términos técnicos sin buena traducción se quedan en inglés (`dao`, `view`).

---

## 10. Estándares de código y reglas del proyecto

**Reglas que no se negocian:**
- **El comportamiento no cambia.** Mismas entradas, mismas salidas, mismos mensajes y mismas reglas de negocio de RF-01 a RF-18, en las dos interfaces.
- **No se agregan funciones nuevas.**
- **Primero las pruebas, después la refactorización.** No se refactoriza un módulo sin sus pruebas fusionadas.
- **Antes de aplicar una técnica, el *code smell* está anotado en la matriz.**
- **Los errores del sistema no se arreglan dentro de una refactorización ni de una prueba.** Se registran como `B-XX` y se decide aparte.

**Lo que buscamos al refactorizar** (y lo que revisa el revisor):
- Sin números mágicos: umbrales, porcentajes y textos repetidos van en constantes con nombre.
- Métodos pequeños, con una sola responsabilidad. Si pasa de ~30 líneas, probablemente hace demasiado.
- Retorno temprano en lugar de `if` anidados.
- No repetir código: si algo aparece en dos lugares, se extrae.
- Los DAO devuelven datos, no imprimen. Imprimir es trabajo de las vistas.
- Nunca un `catch` vacío.
- Nada de código comentado ni `System.out` de depuración en lo que se fusiona.

---

## 11. Pruebas

### 11.1 Qué se prueba

Son **pruebas de caracterización**: registran lo que el sistema **hace hoy**, no lo que debería hacer. Su trabajo es avisar si una refactorización cambió el comportamiento.

- Si encuentras algo que funciona mal, **la prueba lo deja tal cual** con un comentario `// B-XX: ...` y abres el issue del error. No lo corrijas.
- Los PR de pruebas **solo agregan archivos en `src/test`** (y el `pom.xml`, en la base de pruebas). Nada de `src/main`: si cambias el código, ya no estás probando el sistema original.
- Cada prueba indica el requisito que cubre, `// RF-07`. Con eso se arma la matriz de trazabilidad de la Entrega 3.

### 11.2 Cómo se prueba

- **Nunca contra la base de datos real.** `ConexionDB` apunta a `data/tienda.db`; la base de pruebas hace que `mvn test` trabaje sobre una copia. Si una prueba deja datos, los limpia al terminar.
- **Controladores:** se les pasa un DAO falso por el constructor (por ejemplo, `new VentaController(daoFalso)`). No necesitan base de datos.
- **DAO y servicios:** contra la base de pruebas.
- **Métodos que solo imprimen** (por ejemplo, `historial()`): se captura `System.out` y se compara el texto.
- **Vistas de consola** (`MainView`, `*View`): se simula la entrada con `System.setIn` y se captura la salida.

### 11.3 Reparto

| Módulo | Clases | Responsable |
|---|---|---|
| Venta, método de pago y menú de consola | `VentaController`, `VentaDAO`, `VentaView`, `PagoEfectivo`, `PagoTarjeta`, `MainView` | Luis |
| Cliente | `ClienteController`, `ClienteDAO`, `ClienteView` | Katherin |
| Producto e inventario | `BuscarProductoService`, `ProductoDAO`, `MovimientoController`, `MovimientoDAO`, `InventarioView` | José Armando |
| Reporte | `ReporteController`, `ReporteDAO`, `ReporteModel`, `ReporteView` | Paola |
| Promoción | `PromocionController`, `PromocionDAO`, `PromocionView` | Oswaldo |

### 11.4 Dónde se registran

- En cada PR: captura de `mvn test` en verde.
- En cada entrega: el reporte de `target/surefire-reports` y la captura de la ejecución en un clon limpio van a `docs/pruebas/entrega-N/`.

---

## 12. Otras reglas

### 12.1 Issues

**Título:** `ISS-NN · <Unidad> · <Qué> en <dónde>`. `NN` es el mismo número que le pone GitHub (`#NN`).

```
ISS-27 · Organización de datos · Detectar code smells en todo el sistema
ISS-29 · Pruebas · Caracterizar el módulo de clientes
ISS-41 · Organización de datos · Encapsular campos en ProductoModel
```

Los errores del sistema: **`B-XX · descripción del problema`**, con `tipo:bug`.

Cada issue lleva **milestone, responsable, un tipo, una o más áreas, su unidad y una prioridad**. Si tiene dependencias sin fusionar, lleva además `estado:bloqueado`.

**Detectar → refactorizar.** Cada quien tiene un issue para detectar los *code smells* de su unidad en todo el sistema. Lo cierra un PR que agrega las filas a la matriz. Con lo detectado se crean los issues de refactorización como **sub-issues** del de detección, juntando los de **la misma clase y la misma unidad** (lo normal son 4 a 6 por persona).

### 12.2 Etiquetas

| Grupo | Etiquetas |
|---|---|
| Tipo | `tipo:refactor`, `tipo:test`, `tipo:docs`, `tipo:config`, `tipo:bug` |
| Área | `area:venta`, `area:producto`, `area:inventario`, `area:cliente`, `area:promocion`, `area:reporte`, `area:ui`, `area:pruebas`, `area:repo` |
| Unidad | `unidad:generalizacion`, `unidad:condicionales`, `unidad:movimiento`, `unidad:llamadas`, `unidad:datos`, `unidad:grandes` |
| Prioridad | `prioridad:bloqueante`, `prioridad:alta`, `prioridad:media` |
| Estado | `estado:bloqueado`: espera a que otro issue se fusione |

### 12.3 Milestones

| Milestone | Fecha |
|---|---|
| Entrega 2 — Pruebas y primeras refactorizaciones | martes 24 de noviembre |
| Entrega 3 — Versión final | jueves 3 de diciembre |

### 12.4 Tablero

GitHub Projects: **Bloqueado → Por hacer → En progreso → En revisión → Hecho**. Cada quien mueve sus propios issues. Si algo te frena más de **2 horas**, coméntalo en el issue y avisa en Slack.

### 12.5 Protección de ramas

`main` y `develop`: sin push directo, sin force push, no se pueden borrar y piden 1 aprobación. Solo se fusiona con *squash* y las ramas se borran solas al fusionar.
