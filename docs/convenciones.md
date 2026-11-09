# Convenciones del equipo

Reglas con las que trabajamos en este repositorio. Si algo no está aquí, se decide en el issue correspondiente y se agrega a este archivo por PR.

**Resumen rápido:**

1. Toma un issue y muévelo a *En progreso*.
2. Crea tu rama desde `develop`: `tipo/iss-XX-descripcion`.
3. Haz commits con el formato `tipo: descripción (iss-XX)`.
4. Abre un PR **hacia `develop` de este repo** con `Closes #N`.
5. Tu revisor lo prueba y aprueba. Tú haces *Squash and merge* y borras la rama.

---

## 1. Entorno

| Herramienta | Versión |
|---|---|
| Java (JDK) | 17 |
| Maven | 3.9 o superior |
| JavaFX | 20.0.2 (lo baja Maven) |
| Base de datos | SQLite (`sistema-caja/data/tienda.db`) |

Comandos básicos, desde la carpeta `sistema-caja/`:

```bash
mvn clean compile   # compilar
mvn test            # correr las pruebas
```

---

## 2. Issues

### 2.1 Título

Formato: **`ISS-XX · verbo en infinitivo + objeto`**

- `XX` a dos dígitos.
- Se usa el punto medio `·` entre el número y el texto.
- El texto dice **qué se va a lograr**, no cómo.

Ejemplos:

```
ISS-04 · Refactorizar la capa de ventas
ISS-08 · Crear el núcleo del broker
ISS-14 · Levantar el broker como servidor TCP con hilos
```

Los errores que aparezcan durante el trabajo se registran como **`B-XX · descripción del problema`**, con la etiqueta `tipo:bug`.

### 2.2 Cuerpo

Todo issue tiene estas secciones, en este orden:

```markdown
**Dependencias:** ISS-03, ISS-08   (o "ninguna")

**Descripción.** Por qué existe este issue y qué problema resuelve. Una o dos frases.

**Entrega esperada.**
- Qué clases, archivos o cambios deben existir al terminar.
- Para refactorizaciones: el mal olor, dónde está y la técnica del curso.

**Limitaciones.** (opcional) Lo que queda fuera de alcance.

**Criterios de aceptación.**
- Condiciones comprobables. El revisor las usa para aprobar el PR.
```

Un issue no se empieza si alguna de sus **dependencias** no está cerrada, salvo que se acuerde en el issue.

### 2.3 Número ISS y número de GitHub

El número ISS es el del plan de trabajo; el `#` lo pone GitHub al crear el issue. **No siempre coinciden**:

| ISS | # | ISS | # | ISS | # |
|---|---|---|---|---|---|
| 01–07 | #1–#7 | 09 | #8 | 11 | #10 |
| 08 | #11 | 10 | #9 | 12–21 | #12–#21 |

- En **ramas y commits** se usa el número ISS: `feature/iss-08-nucleo-broker`, `(iss-08)`.
- En el **PR** se cierra con el número de GitHub: `Closes #11`.

### 2.4 Etiquetas

Cada issue lleva **un tipo, una o más áreas y una prioridad**.

| Grupo | Etiquetas |
|---|---|
| Tipo | `tipo:config`, `tipo:refactor`, `tipo:feature`, `tipo:test`, `tipo:docs`, `tipo:bug` |
| Área | `area:repo`, `area:broker`, `area:venta`, `area:producto`, `area:cliente`, `area:inventario`, `area:ui` |
| Prioridad | `prioridad:bloqueante` (frena a otros, va primero), `prioridad:alta`, `prioridad:media` |

### 2.5 Milestones

| Milestone | Fecha | Contenido |
|---|---|---|
| Entrega 1 — Broker | sábado 14 de noviembre | Broker nivel 1 y 2, y las refactorizaciones que lo habilitan |
| Entrega 2 — Refactorización | por confirmar | Refactorizaciones que no bloquean al broker y el catálogo |

### 2.6 Tablero

GitHub Projects con cuatro columnas: **Backlog → En progreso → En revisión → Hecho**.
Cada quien mueve sus propios issues. Si algo te bloquea más de unas horas, coméntalo en el issue y avisa en el grupo.

---

## 3. Ramas

```
main      ← solo recibe develop en cada entrega (etiquetas v1.0.0, v2.0.0)
develop   ← rama de integración; aquí llegan todos los PR
  └── tipo/iss-XX-descripcion   ← una rama por issue
```

- **Nunca** se trabaja directo en `main` ni en `develop`. Ambas están protegidas: no aceptan push directo y piden 1 aprobación.
- Una rama por issue, siempre desde `develop` actualizado.
- Formato: `tipo/iss-XX-descripcion`, en minúsculas, sin acentos, con guiones.

| Tipo de rama | Uso | Ejemplo |
|---|---|---|
| `feature` | Código nuevo | `feature/iss-08-nucleo-broker` |
| `refactor` | Refactorización sin cambiar comportamiento | `refactor/iss-04-capa-ventas` |
| `fix` | Corrección de un error | `fix/b-01-stock-negativo` |
| `test` | Pruebas | `test/iss-03-caracterizacion` |
| `docs` | Documentación y diagramas | `docs/iss-17-diagramas` |
| `chore` | Configuración, Maven, estructura | `chore/iss-02-compilacion` |

```bash
git switch develop
git pull
git switch -c refactor/iss-04-capa-ventas
```

---

## 4. Commits

Formato: **`tipo: descripción (iss-XX)`**

- `tipo` en minúsculas, de la tabla de abajo.
- Descripción en español, en imperativo, sin punto final, de 72 caracteres o menos.
- Siempre termina con el issue entre paréntesis.

| Tipo | Uso |
|---|---|
| `feat` | Funcionalidad nueva |
| `refactor` | Cambio interno sin cambiar el comportamiento |
| `fix` | Corrección de un error |
| `test` | Pruebas |
| `docs` | Solo documentación |
| `chore` | Configuración, `pom.xml`, `.gitignore`, estructura |

**En las refactorizaciones, una técnica por commit y con su nombre.** Así el historial sirve de evidencia para el catálogo de refactorizaciones:

```
refactor: separar consulta de modificador en historial de ventas (iss-04)
refactor: mover calcularTotal a VentaModel (iss-04)
refactor: reemplazar condicional de pago por polimorfismo (iss-05)
feat: agrega RegistroServicios con registro y sustitucion (iss-08)
test: agrega pruebas de caracterizacion de ProductoDAO (iss-03)
```

---

## 5. Pull requests

### 5.1 Antes de abrirlo

```bash
git fetch origin
git merge origin/develop     # traer lo último y resolver conflictos en tu rama
mvn clean compile
mvn test
```

### 5.2 Al abrirlo

> ⚠️ **Este repo es un fork.** Al abrir un PR, GitHub propone por defecto el repo original. Revisa siempre que diga:
> **base repository: `Luis-villanueva-valdivia/SistemaGestionAbarrotes`** · **base: `develop`**

- **Título** con el mismo formato que los commits: `tipo: descripción (iss-XX)`. Se convierte en el mensaje del commit final.
- Llena la **plantilla** que aparece sola (`.github/pull_request_template.md`), incluido `Closes #N`.
- Asigna a tu **revisor** (§6) y mueve el issue a *En revisión*.

### 5.3 Revisión y fusión

1. El revisor baja la rama, la compila, corre las pruebas y revisa los criterios de aceptación del issue.
2. **Aprueba** o **pide cambios**. Si pide cambios, el autor corrige en la misma rama, contesta los comentarios y pide revisión otra vez (*Re-request review*).
3. Con la aprobación, **el autor fusiona su propio PR** con **Squash and merge**. Así cada issue queda como un solo commit en `develop` y el trabajo de cada quien queda a su nombre.
4. Se **borra la rama** (botón *Delete branch* y `git branch -d` en local).
5. El issue se cierra solo por el `Closes #N`.

---

## 6. Revisión cruzada

Nadie aprueba su propio PR. Cada integrante revisa al siguiente:

| PR de | Lo revisa |
|---|---|
| P1 (@Luis-villanueva-valdivia) | P5 |
| P2 | P1 |
| P3 | P2 |
| P4 | P3 |
| P5 | P4 |

- El dueño del repo (P1) puede aprobar cualquier PR.
- Si el revisor asignado no contesta en **12 horas**, puede aprobar cualquier otro integrante (la entrega es en una semana).

---

## 7. Dueños de archivo

Quien crea un archivo es su dueño. Los demás **no reescriben** lo que ya existe sin avisar en el issue; si necesitan agregar algo, lo hacen **al final**, con un comentario de sección:

```java
// ===== ISS-10 · Proveedor de ventas (P3) =====
```

En el código heredado del sistema, el dueño de cada clase es quien tiene el issue que la refactoriza.

---

## 8. Nombres en Java

| Elemento | Convención | Ejemplo |
|---|---|---|
| Paquetes | minúsculas, sin guiones | `com.tienda.broker.nucleo` |
| Clases e interfaces | `PascalCase`, sustantivo | `RegistroServicios`, `ProveedorServicio` |
| Interfaces de DAO | prefijo `I` (como el código existente) | `IVentaDAO` |
| Métodos | `camelCase`, empiezan con verbo | `buscarPorId()`, `registrarVenta()` |
| Variables y atributos | `camelCase`, descriptivos | `idSolicitud`, `productosConStockBajo` |
| Constantes | `MAYUSCULAS_CON_GUION_BAJO` | `PUERTO_BROKER`, `TIMEOUT_MS` |
| Enums | `PascalCase`; valores en mayúsculas | `EstadoRespuesta.PARAMETRO_INVALIDO` |
| Pruebas | clase probada + `Test` | `RegistroServiciosTest` |

Los nombres van en español, sin acentos ni ñ (`anio`, `contrasenia`). Términos técnicos sin buena traducción se quedan en inglés (`socket`, `json`, `dao`).

### 8.1 Estructura de paquetes

```
com.tienda
├── cliente, producto, venta, inventario, promocion, reporte   ← sistema original
├── metodoPago, util, ui
└── broker
    ├── nucleo/        Solicitud, Respuesta, EstadoRespuesta, ProveedorServicio, RegistroServicios, Broker
    ├── proveedores/   ServicioProductos, ServicioVentas, ServicioClientes
    ├── cliente/       ClienteBroker, DemoBrokerLocal
    └── red/           MarshallerJson, BrokerServidor, NodoProveedores, ProveedorRemoto, ClienteRemoto
```

---

## 9. Estándares de código

- **Indentación de 4 espacios**, sin tabuladores. UTF-8 y salto de línea al final del archivo.
- **Sin números mágicos.** Puertos, tiempos, umbrales y textos repetidos van en constantes con nombre.
- **Métodos pequeños, con una sola responsabilidad.** Si pasa de ~30 líneas, probablemente hace demasiado.
- **Retorno temprano** en lugar de `if` anidados.
- **No repetir código:** si algo aparece en dos lugares, se extrae.
- **Los DAO devuelven datos, no imprimen.** La impresión es trabajo de las vistas.
- **Dependencias por constructor**, no `new XxxDAO()` dentro de las clases.
- **Excepciones:** nunca un `catch` vacío. El broker nunca deja escapar una excepción hacia el cliente: la convierte en una `Respuesta` de error.
- **Javadoc** en toda clase y método público: qué hace, parámetros, retorno y excepciones. Dentro del código se comenta el **porqué**, no el qué.
- **Nada de código comentado ni `System.out` de depuración** en lo que se fusiona (la bitácora del broker sí va).
- **Toda clase nueva del broker lleva sus pruebas** en `src/test/java` con el mismo paquete.
