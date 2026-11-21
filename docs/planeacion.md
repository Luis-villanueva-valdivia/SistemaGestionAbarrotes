# Planeación — Entrega 2

Issues, responsables, dependencias y calendario hasta la **Entrega 2 (martes 24 de noviembre)**. Las reglas de trabajo están en [`convenciones.md`](convenciones.md).

**Qué pide la Entrega 2:** diagrama de clases y ejecución de las pruebas unitarias. Además, el plan de trabajo de la propuesta compromete para esta entrega la matriz de *code smells*, las pruebas del comportamiento actual de las dos interfaces, la separación de dominio y presentación, y las refactorizaciones de organización de datos y de llamadas a métodos.

> Los números `#22` en adelante son los que tendrán los issues en GitHub (del #1 al #21 son del plan anterior y están cerrados).

---

## 1. Issues

### Milestone: Entrega 2 — Pruebas y primeras refactorizaciones

| # | Issue | Responsable | Depende de | Para cuándo |
|---|---|---|---|---|
| #22 | ISS-22 · Pruebas · Preparar la base de pruebas aislada | Oswaldo | — | Sáb 21 · 23:59 |
| #23 | ISS-23 · Generalización y grandes refactorizaciones · Detectar code smells en todo el sistema | Luis | — | Dom 22 · 14:00 |
| #24 | ISS-24 · Condicionales · Detectar code smells en todo el sistema | Oswaldo | — | Dom 22 · 14:00 |
| #25 | ISS-25 · Movimiento de funciones · Detectar code smells en todo el sistema | Paola | — | Dom 22 · 14:00 |
| #26 | ISS-26 · Llamadas a métodos · Detectar code smells en todo el sistema | José Armando | — | Dom 22 · 14:00 |
| #27 | ISS-27 · Organización de datos · Detectar code smells en todo el sistema | Katherin | — | Dom 22 · 14:00 |
| #28 | ISS-28 · Pruebas · Caracterizar venta, método de pago y el menú de consola | Luis | #22 | Dom 22 · 23:59 |
| #29 | ISS-29 · Pruebas · Caracterizar el módulo de clientes | Katherin | #22 | Dom 22 · 23:59 |
| #30 | ISS-30 · Pruebas · Caracterizar producto e inventario | José Armando | #22 | Dom 22 · 23:59 |
| #31 | ISS-31 · Pruebas · Caracterizar el módulo de reportes | Paola | #22 | Dom 22 · 23:59 |
| #32 | ISS-32 · Pruebas · Caracterizar el módulo de promociones | Oswaldo | #22 | Dom 22 · 23:59 |
| #33 | ISS-33 · Matriz · Integrar y revisar la detección de las cinco unidades | Katherin | #23 a #27 | Dom 22 · 18:00 |
| #34 | ISS-34 · Grandes refactorizaciones · Separar la interfaz de la lógica en JavaFXApp y la consola | Luis coordina; cada quien su módulo (sub-issues) | #23, #28 a #32 | Lun 23 · 19:00 |
| #35 | ISS-35 · Diagrama · Hacer el diagrama de clases del sistema refactorizado | Paola | #34 | Lun 23 · 23:59 |
| #36 | ISS-36 · Entrega 2 · Integrar develop en main y publicar v1.1.0 | Luis | #34, #35 y los sub-issues de #26 y #27 | Mar 24 · 08:00 |
| #37 | ISS-37 · Pruebas · Ejecutar las pruebas y reunir la evidencia de la entrega | Oswaldo | #36 | Mar 24 · 09:00 |
| #38 | ISS-38 · Entrega 2 · Integrar el documento de la entrega | José Armando | #35, #37 | Mar 24 · 10:00 |

**Issues que se crean después de la detección** (domingo en la tarde, como sub-issues):

| Sub-issues de | Qué | Responsable | Depende de | Para cuándo |
|---|---|---|---|---|
| #34 | Uno por módulo: sacar su pantalla de `JavaFXApp` y su vista de consola | Cada quien el módulo de sus pruebas | Las pruebas de ese módulo | Lun 23 · 19:00 |
| #27 | Refactorizaciones de organización de datos en modelos | Katherin | #27, #33 y las pruebas del módulo | Lun 23 · 19:00 |
| #26 | Refactorizaciones de llamadas a métodos en DAO y controladores | José Armando | #26, #33 y las pruebas del módulo | Lun 23 · 19:00 |

Las refactorizaciones de **movimiento de funciones, condicionales y generalización** van en la Entrega 3: sus sub-issues se crean con la detección, pero en el milestone *Entrega 3 — Versión final*.

### Carga por persona (Entrega 2)

| Integrante | Issues |
|---|---|
| Luis | #23 detección, #28 pruebas, #34 coordinar la gran refactorización (y su sub-issue), #36 integración |
| Oswaldo | #22 base de pruebas, #24 detección, #32 pruebas, #37 ejecución y evidencia · revisa todos los PR de pruebas |
| Paola | #25 detección, #31 pruebas, #35 diagrama de clases (y su sub-issue de #34) |
| José Armando | #26 detección, #30 pruebas, sub-issues de llamadas a métodos, #38 documento (y su sub-issue de #34) |
| Katherin | #27 detección, #29 pruebas, #33 matriz, sub-issues de organización de datos (y su sub-issue de #34) · revisa los PR de la matriz |

---

## 2. Dependencias

```mermaid
flowchart LR
    I22["#22 Base de pruebas<br/>Oswaldo"]:::empieza
    I23["#23 Detección GEN/GRA<br/>Luis"]:::empieza
    I24["#24 Detección CON<br/>Oswaldo"]:::empieza
    I25["#25 Detección MOV<br/>Paola"]:::empieza
    I26["#26 Detección LLA<br/>José Armando"]:::empieza
    I27["#27 Detección DAT<br/>Katherin"]:::empieza

    I28["#28 Pruebas venta y consola<br/>Luis"]:::espera
    I29["#29 Pruebas cliente<br/>Katherin"]:::espera
    I30["#30 Pruebas producto e inventario<br/>José Armando"]:::espera
    I31["#31 Pruebas reporte<br/>Paola"]:::espera
    I32["#32 Pruebas promoción<br/>Oswaldo"]:::espera
    I33["#33 Matriz integrada<br/>Katherin"]:::espera

    I34["#34 Separar interfaz y lógica<br/>Luis coordina"]:::espera
    RDAT["Sub-issues datos<br/>Katherin"]:::espera
    RLLA["Sub-issues llamadas<br/>José Armando"]:::espera

    I35["#35 Diagrama de clases<br/>Paola"]:::final
    I36["#36 develop → main v1.1.0<br/>Luis"]:::final
    I37["#37 Ejecución y evidencia<br/>Oswaldo"]:::final
    I38["#38 Documento de la entrega<br/>José Armando"]:::final

    I22 --> I28 & I29 & I30 & I31 & I32
    I23 & I24 & I25 & I26 & I27 --> I33
    I23 --> I34
    I28 & I29 & I30 & I31 & I32 --> I34
    I27 & I33 --> RDAT
    I26 & I33 --> RLLA
    I28 & I29 & I30 & I31 & I32 --> RDAT & RLLA
    I34 --> I35
    I34 & I35 & RDAT & RLLA --> I36
    I36 --> I37
    I35 & I37 --> I38

    classDef empieza fill:#2da44e,color:#fff,stroke:#1a7f37
    classDef espera fill:#d4a72c,color:#000,stroke:#9a6700
    classDef final fill:#cf222e,color:#fff,stroke:#a40e26
```

🟩 puede empezar ya · 🟨 espera a otro issue · 🟥 va al final

### Por qué cada dependencia

| Dependencia | Por qué |
|---|---|
| #22 → pruebas por módulo | `ConexionDB` tiene fija la ruta `data/tienda.db`. Sin la base aislada, las pruebas de DAO escriben en la base real. |
| Pruebas → cualquier refactorización | Las pruebas son las que demuestran que el sistema hace lo mismo después del cambio. Refactorizar sin ellas no se puede comprobar. |
| #23 → #34 | La detección de grandes refactorizaciones define qué se saca de `JavaFXApp` (655 líneas) y de las vistas de consola, y en qué clases. |
| Detección y #33 → sub-issues de datos y llamadas | No se aplica una técnica que no esté anotada en la matriz. |
| #34 → #35 | El diagrama es del sistema **refactorizado**: se dibuja cuando la separación ya está fusionada. Paola puede empezar el domingo con el diagrama actual. |
| Todo → #36 | `main` solo recibe `develop` cuando todo lo de la entrega está fusionado y en verde. |
| #36 → #37 | La evidencia se toma de un clon limpio de `main`, que es lo que se entrega. |
| #35 y #37 → #38 | El documento lleva el diagrama y el reporte de las pruebas. |

### Riesgos

- **`JavaFXApp` y `MainView` los tocan todos en #34.** Por eso se reparte por módulo y cada quien saca solo su pantalla. Luis integra al final.
- **Datos y llamadas pueden chocar con #34.** Por eso trabajan primero en modelos y DAO, que la separación no mueve.
- **El lunes es el día cargado.** Si el domingo a las 21:00 las pruebas no están casi listas, el equipo decide en Slack qué refactorizaciones pasan a la Entrega 3.

---

## 3. Calendario

| Cuándo | Qué | Quién |
|---|---|---|
| **Sáb 21 · 23:59** | #22 base de pruebas fusionada | Oswaldo |
| **Dom 22 · 14:00** | #23 a #27 detección fusionada en la matriz | Todos |
| **Dom 22 · 18:00** | #33 matriz integrada; se crean los sub-issues | Katherin; cada quien los suyos |
| **Dom 22 · 21:00** | Revisión de avance en Slack: ¿se cumple todo o se ajusta? | Todos |
| **Dom 22 · 23:59** | #28 a #32 PR de pruebas abiertos | Cada quien su módulo |
| **Lun 23 · 10:00** | Pruebas revisadas y fusionadas: `develop` en verde | Oswaldo revisa |
| **Lun 23 · 19:00** | PR de #34 (por módulo) y de los sub-issues de datos y llamadas | Todos |
| **Lun 23 · 22:00** | Se congela `develop`: lo que no esté fusionado pasa a la Entrega 3 | Luis |
| **Lun 23 · 23:59** | #35 diagrama de clases fusionado | Paola |
| **Mar 24 · 08:00** | #36 `develop` → `main` y etiqueta `v1.1.0` | Luis |
| **Mar 24 · 09:00** | #37 pruebas ejecutadas y evidencia en `docs/pruebas/entrega-2/` | Oswaldo |
| **Mar 24 · 10:00** | #38 documento de la entrega | José Armando |

**Entrega 3 (jueves 3 de diciembre):** refactorizaciones de movimiento de funciones, condicionales y generalización (30 nov), pruebas de caja negra (1 dic, José Armando coordina), matriz de trazabilidad y Javadoc (2 dic), versión final `v2.0.0` (3 dic).

---

## 4. Tablero

| Columna | Significa |
|---|---|
| **Bloqueado** | Tiene una dependencia que todavía no está fusionada en `develop`. No se empieza. |
| **Por hacer** | Ya se puede empezar. |
| **En progreso** | Alguien está trabajando en su rama. |
| **En revisión** | Tiene PR abierto y revisor asignado. |
| **Hecho** | El PR se fusionó y el issue está cerrado. |
