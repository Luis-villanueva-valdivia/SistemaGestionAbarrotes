# Convenciones del equipo

Reglas con las que trabajamos en este repositorio. Si algo no está aquí, se decide en el issue correspondiente o en Slack y se agrega a este archivo por PR.

**Proyecto:** refactorización del *Sistema de Gestión de Abarrotes* (Construcción de Software, Equipo 3). El sistema original no es nuestro: la versión de partida está guardada en la etiqueta `v1.0-original`. **No se agregan funciones nuevas:** el sistema debe hacer exactamente lo mismo antes y después de cada cambio (RF-01 a RF-18 de la ERS).

**Resumen rápido:**

1. Toma tu issue y muévelo a *En progreso*. Avisa en el canal de Slack que corresponda.
2. Crea tu rama desde `develop`: `tipo/iss-XX-descripcion`.
3. Haz commits con el formato `tipo: descripción (iss-XX)`. En refactorizaciones, **una técnica por commit**.
4. Antes del PR: `mvn clean compile` y `mvn test` en verde.
5. Abre un PR **hacia `develop` de este repo** con `Closes #XX` y los RF que conserva.
6. Tu revisor lo prueba y aprueba. Tú haces *Squash and merge* y borras la rama.

---

## 1. Equipo

| Integrante | GitHub | Tema de refactorización | Responsabilidad de apoyo |
|---|---|---|---|
| Luis Adrián Villanueva Valdivia | `@Luis-villanueva-valdivia` | Manejo de la generalización · coordina las grandes refactorizaciones | Git/GitHub y Slack: repositorio, ramas, revisión e integración de PR |
| Oswaldo Castillo Machado | `@oswaldo-castillo` | Simplificación de expresiones condicionales | Pruebas unitarias (JUnit 5): coordina y revisa todas las pruebas |
| Paola Lizzeth Parra Canche | `@Pao-Parra` | Movimiento de funciones entre objetos | Javadoc y diagrama de clases |
| José Armando Ontiveros Alcocer | `@Jose-Ontiveros` | Simplificación de las llamadas a métodos | Integra los documentos de cada entrega · coordina las pruebas de caja negra |
| Katherin Aolani Ordoñes Pacheco | `@KatherineOr18` | Organización de datos | Matriz de refactorizaciones y matriz de trazabilidad · vigila las fechas |

---

## 2. Entorno

| Herramienta | Versión |
|---|---|
| Java (JDK) | 17 |
| Maven | 3.9 o superior |
| JavaFX | 20.0.2 (lo baja Maven) |
| JUnit | 5 (lo baja Maven) |
| Base de datos | SQLite (`sistema-caja/data/tienda.db`) |

Comandos, desde la carpeta `sistema-caja/`:

```bash
mvn clean compile      # compilar
mvn test               # correr las pruebas
mvn javafx:run         # abrir la interfaz gráfica
```

El sistema tiene **dos interfaces**: la gráfica (`JavaFXApp`) y la de consola (`MainView` y las clases `*View`, que se abren pasando el argumento `cli` a `App`). Todo cambio se revisa en las dos.

---

## 3. Calendario de la Entrega 2 (martes 24 de noviembre)

Fechas internas: van antes de la revisión para que haya tiempo de corregir. Si no vas a llegar, avísalo en Slack **antes** de la hora, no después.

| Cuándo | Qué | Quién |
|---|---|---|
| **Sáb 21 · 23:59** | Base de pruebas: base de datos aislada, `mvn test` funcionando y un ejemplo de cada tipo de prueba (§7) | Oswaldo |
| **Dom 22 · 14:00** | Detección de *code smells* de cada tema, anotada en la matriz (§8) | Cada quien su tema |
| **Dom 22 · 18:00** | Matriz revisada e integrada | Katherin |
| **Dom 22 · 21:00** | Revisión de avance en Slack: ¿se cumple todo el plan o se ajusta? | Todos (decide el equipo) |
| **Dom 22 · 23:59** | PR de pruebas de cada módulo abierto (§7.3) | Cada quien su módulo |
| **Lun 23 · 10:00** | Pruebas revisadas y fusionadas: `develop` en verde | Oswaldo revisa |
| **Lun 23 · 19:00** | PR de la gran refactorización (cada quien su módulo) y de organización de datos y llamadas a métodos | Luis coordina · Katherin · José Armando |
| **Lun 23 · 22:00** | Se congela `develop`: lo que no esté fusionado pasa a la Entrega 3 | Luis |
| **Lun 23 · 23:59** | Diagrama de clases del sistema refactorizado | Paola |
| **Mar 24 · 08:00** | `develop` → `main` y etiqueta `v1.1.0` | Luis |
| **Mar 24 · 09:00** | Ejecución de las pruebas en un clon limpio y evidencia | Oswaldo |
| **Mar 24 · 10:00** | Documento de la Entrega 2 integrado | José Armando |

**Entrega 3 (3 de diciembre):** refactorizaciones de movimiento de funciones, condicionales y generalización; pruebas de caja negra; matriz de trazabilidad; Javadoc; matriz de refactorizaciones final; versión final (`v2.0.0`).

---

## 4. Issues

### 4.1 Título

Formato: **`ISS-XX · verbo en infinitivo + objeto`**

- `XX` es el mismo número que le pone GitHub (`#XX`), a dos dígitos.
- Se usa el punto medio `·` entre el número y el texto.
- El texto dice **qué se va a lograr**, no cómo.

```
ISS-24 · Detectar los code smells de organización de datos
ISS-30 · Escribir las pruebas del módulo de clientes
ISS-41 · Encapsular los campos de ProductoModel
```

Los errores del sistema que aparezcan se registran como **`B-XX · descripción del problema`**, con la etiqueta `tipo:bug`. **No se corrigen dentro de una refactorización ni de una prueba**: se anotan y se decide aparte.

### 4.2 Cuerpo

```markdown
**Dependencias:** #XX, #XX   (o "ninguna")

**Requisitos que conserva:** RF-XX, RF-XX

**Descripción.** Por qué existe este issue. Una o dos frases.

**Entrega esperada.**
- Qué clases, archivos o cambios deben existir al terminar.
- En refactorizaciones: code smell → técnica del curso → clase/método.

**Criterios de aceptación.**
- Condiciones comprobables. El revisor las usa para aprobar el PR.
- En refactorizaciones siempre va: "Las pruebas pasan igual que antes del cambio".
```

Un issue no se empieza si alguna de sus dependencias no está cerrada, salvo que se acuerde en el issue.

### 4.3 Flujo: detectar → refactorizar

1. **Detección.** Cada quien tiene un issue para detectar los *code smells* de su tema en **todo** el sistema. Se cierra con un PR que agrega las filas a la matriz (§8).
2. **Refactorización.** Con lo detectado, cada quien crea sus issues de refactorización como **sub-issues** de su issue de detección. Se juntan los *code smells* de **la misma clase y el mismo tema** en un solo issue (lo normal son 4 a 6 por persona).
3. Cada issue de refactorización queda **bloqueado por** las pruebas del módulo que toca, y por la gran refactorización si el código está en `JavaFXApp` o en una vista de consola.

### 4.4 Etiquetas

Cada issue lleva **un tipo, una o más áreas y una prioridad**. Las refactorizaciones y detecciones llevan además su **tema**.

| Grupo | Etiquetas |
|---|---|
| Tipo | `tipo:config`, `tipo:test`, `tipo:refactor`, `tipo:docs`, `tipo:bug` |
| Área | `area:venta`, `area:producto`, `area:inventario`, `area:cliente`, `area:promocion`, `area:reporte`, `area:ui`, `area:repo` |
| Tema | `tema:generalizacion`, `tema:condicionales`, `tema:movimiento`, `tema:llamadas`, `tema:datos`, `tema:grandes` |
| Prioridad | `prioridad:bloqueante` (frena a otros, va primero), `prioridad:alta`, `prioridad:media` |

### 4.5 Milestones

| Milestone | Fecha |
|---|---|
| Entrega 2 — Pruebas y primeras refactorizaciones | martes 24 de noviembre |
| Entrega 3 — Versión final | jueves 3 de diciembre |

### 4.6 Tablero

GitHub Projects con cuatro columnas: **Backlog → En progreso → En revisión → Hecho**.
Cada quien mueve sus propios issues. Si algo te bloquea más de **2 horas**, coméntalo en el issue y avisa en Slack.

---

## 5. Ramas

```
main      ← solo recibe develop en cada entrega (v1.1.0, v2.0.0)
develop   ← rama de integración; aquí llegan todos los PR
  └── tipo/iss-XX-descripcion   ← una rama por issue
```

- **Nunca** se trabaja directo en `main` ni en `develop`. Ambas están protegidas: no aceptan push directo y piden 1 aprobación.
- Una rama por issue, siempre desde `develop` actualizado.
- Formato: `tipo/iss-XX-descripcion`, en minúsculas, sin acentos, con guiones.

| Tipo de rama | Uso | Ejemplo |
|---|---|---|
| `test` | Pruebas | `test/iss-30-pruebas-cliente` |
| `refactor` | Refactorización sin cambiar comportamiento | `refactor/iss-41-encapsular-producto` |
| `docs` | Matriz, diagramas, documentación | `docs/iss-24-deteccion-datos` |
| `chore` | Configuración, Maven, estructura | `chore/iss-22-base-pruebas` |
| `fix` | Corrección de un error `B-XX` (solo si el equipo lo acuerda) | `fix/b-01-stock-negativo` |

```bash
git switch develop
git pull
git switch -c test/iss-30-pruebas-cliente
```

---

## 6. Commits

Formato: **`tipo: descripción (iss-XX)`**

- `tipo` en minúsculas: `test`, `refactor`, `docs`, `chore`, `fix`.
- Descripción en español, en imperativo, sin punto final, de 72 caracteres o menos.
- Siempre termina con el issue entre paréntesis.

**En las refactorizaciones, una técnica por commit y con su nombre del curso.** El historial es la evidencia de la matriz:

```
test: agrega pruebas de ClienteController con DAO falso (iss-30)
refactor: encapsular campos de ProductoModel (iss-41)
refactor: reemplazar numero magico del descuento por constante (iss-41)
refactor: extraer VentaPane de JavaFXApp (iss-35)
docs: agrega code smells de organizacion de datos a la matriz (iss-24)
```

---

## 7. Pruebas

### 7.1 Qué se prueba

Son **pruebas de caracterización**: registran lo que el sistema **hace hoy**, no lo que debería hacer. Su trabajo es avisar si una refactorización cambió el comportamiento.

- Si encuentras algo que funciona mal, **la prueba lo deja tal cual** con un comentario `// B-XX: ...` y abres el issue del error. No lo corrijas.
- Los PR de pruebas **solo agregan archivos en `src/test`**. Nada de `src/main`: si cambias el código, ya no estás probando el sistema original.
- Cada prueba indica el requisito que cubre: `// RF-07`. Con eso se arma la matriz de trazabilidad de la Entrega 3.

### 7.2 Cómo se prueba

- **Nunca contra la base de datos real.** `ConexionDB` apunta a `data/tienda.db`; la base de pruebas (issue de Oswaldo) hace que `mvn test` trabaje sobre una copia. Si una prueba deja datos, los limpia al terminar.
- **Controladores:** se les pasa un DAO falso por el constructor (por ejemplo, `new VentaController(daoFalso)`). No necesitan base de datos.
- **DAO y servicios:** contra la base de pruebas.
- **Métodos que solo imprimen** (por ejemplo `historial()`): se captura `System.out` y se compara el texto.
- **Vistas de consola** (`MainView`, `*View`): se simula la entrada con `System.setIn` y se captura la salida.
- **Nombres:** la clase es `ClaseProbadaTest`, en el mismo paquete; los métodos, `metodo_condicion_resultado`, por ejemplo `calcularTotal_conDosProductos_sumaSubtotales`.

### 7.3 Reparto de las pruebas

| Módulo | Clases | Responsable |
|---|---|---|
| Venta y método de pago, menú de consola | `VentaController`, `VentaDAO`, `VentaView`, `PagoEfectivo`, `PagoTarjeta`, `MainView` | Luis |
| Cliente | `ClienteController`, `ClienteDAO`, `ClienteView` | Katherin |
| Producto e inventario | `BuscarProductoService`, `ProductoDAO`, `MovimientoController`, `MovimientoDAO`, `InventarioView` | José Armando |
| Reporte | `ReporteController`, `ReporteDAO`, `ReporteModel`, `ReporteView` | Paola |
| Promoción | `PromocionController`, `PromocionDAO`, `PromocionView` | Oswaldo |

**Oswaldo revisa todos los PR de pruebas.** Los PR de pruebas de Oswaldo los revisa Luis.

---

## 8. Matriz de refactorizaciones

Vive en `docs/matriz-refactorizaciones.md`, con **una sección por tema**. Cada quien escribe solo en la suya, así no hay conflictos. Katherin la revisa e integra.

| Columna | Qué va |
|---|---|
| ID | `GEN-01`, `CON-01`, `MOV-01`, `LLA-01`, `DAT-01`, `GRA-01` |
| Code smell | Nombre del curso (Long Method, Data Class, Switch Statements…) |
| Ubicación | Clase y método |
| Técnica | Nombre del curso (Encapsulate Field, Extract Method…) |
| RF afectados | RF-XX |
| Estado | Detectado · En progreso · Aplicada (#PR) · No aplica |
| Justificación | Solo si no se aplica: por qué |

Al final del tema se agregan las técnicas que **no aplican** en el sistema, con su porqué (por ejemplo: "Collapse Hierarchy: no hay jerarquías de clases").

---

## 9. Pull requests

### 9.1 Antes de abrirlo

```bash
git fetch origin
git merge origin/develop     # traer lo último y resolver conflictos en tu rama
mvn clean compile
mvn test
```

### 9.2 Al abrirlo

> ⚠️ **Este repo es un fork.** Al abrir un PR, GitHub propone por defecto el repo original. Revisa siempre que diga:
> **base repository: `Luis-villanueva-valdivia/SistemaGestionAbarrotes`** · **base: `develop`**

- **Título** con el mismo formato que los commits. Se convierte en el mensaje del commit final.
- Llena la **plantilla** que aparece sola, incluidos `Closes #XX` y los RF que conserva.
- Asigna a tu **revisor** (§10), mueve el issue a *En revisión* y avisa en Slack.

### 9.3 Revisión y fusión

1. El revisor baja la rama, la compila, corre las pruebas y revisa los criterios de aceptación. En refactorizaciones, revisa también que la interfaz gráfica y la de consola sigan haciendo lo mismo.
2. **Aprueba** o **pide cambios**. Si pide cambios, el autor corrige en la misma rama y pide revisión otra vez (*Re-request review*).
3. Con la aprobación, **el autor fusiona su propio PR** con **Squash and merge**.
4. Se **borra la rama**.
5. El issue se cierra solo por el `Closes #XX`.

---

## 10. Revisión cruzada

Nadie aprueba su propio PR.

| PR de | Lo revisa |
|---|---|
| Luis | Oswaldo |
| Oswaldo | Paola |
| Paola | José Armando |
| José Armando | Katherin |
| Katherin | Luis |

Excepciones:
- **PR de pruebas:** los revisa Oswaldo (los suyos, Luis).
- **PR de la matriz:** los revisa Katherin (los suyos, Luis).
- Si el revisor asignado no contesta en **4 horas**, puede aprobar cualquier otro integrante. Pídelo en Slack.

---

## 11. Dueños de archivo

En el código heredado, el dueño de cada clase es quien tiene el issue que la está cambiando. Si necesitas tocar una clase que está en el issue de otra persona, **avísale en su issue antes**, para no pisarse.

Para evitar conflictos en la Entrega 2:
- La **gran refactorización** (separar `JavaFXApp` y las vistas de consola de la lógica) se reparte por módulo y la coordina Luis.
- **Organización de datos** y **llamadas a métodos** trabajan primero en **modelos y DAO**, que la gran refactorización no mueve.

---

## 12. Nombres en Java

| Elemento | Convención | Ejemplo |
|---|---|---|
| Paquetes | minúsculas, sin guiones | `com.tienda.venta` |
| Clases e interfaces | `PascalCase`, sustantivo | `VentaController`, `IMetodoPago` |
| Interfaces de DAO | prefijo `I` (como el código existente) | `IVentaDAO` |
| Métodos | `camelCase`, empiezan con verbo | `buscarPorId()`, `registrarVenta()` |
| Variables y atributos | `camelCase`, descriptivos | `productosConStockBajo` |
| Constantes | `MAYUSCULAS_CON_GUION_BAJO` | `DESCUENTO_CLIENTE_FRECUENTE` |
| Pruebas | clase probada + `Test` | `VentaControllerTest` |

Los nombres van en español, sin acentos ni ñ (`anio`, `contrasenia`). Términos técnicos sin buena traducción se quedan en inglés (`dao`, `view`).

---

## 13. Estándares de código

Lo que buscamos al refactorizar (y lo que revisa el revisor):

- **Indentación de 4 espacios**, sin tabuladores. UTF-8.
- **Sin números mágicos:** umbrales, porcentajes y textos repetidos van en constantes con nombre.
- **Métodos pequeños, con una sola responsabilidad.** Si pasa de ~30 líneas, probablemente hace demasiado.
- **Retorno temprano** en lugar de `if` anidados.
- **No repetir código:** si algo aparece en dos lugares, se extrae.
- **Los DAO devuelven datos, no imprimen.** Imprimir es trabajo de las vistas.
- **Nunca un `catch` vacío.**
- **Javadoc** en toda clase y método público que se toque: qué hace, parámetros, retorno y excepciones.
- **Nada de código comentado ni `System.out` de depuración** en lo que se fusiona.
