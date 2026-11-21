<!--
Título del PR (se convierte en el commit final del squash merge):
  tipo: descripción (iss-XX)
  Ej.: refactor: encapsular campos de ProductoModel (iss-41)

⚠️ Revisa que el PR vaya a: Luis-villanueva-valdivia/SistemaGestionAbarrotes · develop
-->

Closes #XX

**Requisitos que conserva:** RF-XX, RF-XX <!-- Los RF de la ERS que pasan por el código tocado -->

## Descripción

<!-- Qué resuelve este PR y por qué. Una o dos frases. -->

## Cambios

<!-- Refactorizaciones: code smell → técnica aplicada → clase/método (ID de la matriz).
     Pruebas: clases probadas y número de pruebas. -->
-
-

## Cómo probarlo

1. `git fetch && git switch tipo/iss-XX-descripcion`
2. `cd sistema-caja && mvn clean compile && mvn test`
3. <!-- Pantalla o menú de consola a abrir, y qué hacer -->
4. Resultado esperado:

## Criterios de aceptación

<!-- Copiados del issue. Marca los que se cumplen. -->
- [ ]
- [ ]

## Evidencia

<!-- Captura de `mvn test` en verde y, si aplica, de la app. -->

## Checklist

- [ ] La rama sale de `develop` y sigue el formato `tipo/iss-XX-descripcion`.
- [ ] El título y los commits siguen el formato `tipo: descripción (iss-XX)`.
- [ ] El PR va a `develop` de **este** repo, no al original.
- [ ] `mvn clean compile` termina sin errores y `mvn test` pasa en verde.
- [ ] Pruebas: solo agregué archivos en `src/test` y cada prueba indica su RF.
- [ ] Refactorizaciones: una técnica por commit, con su nombre del curso.
- [ ] Refactorizaciones: el sistema hace lo mismo que antes, en la interfaz gráfica y en la de consola.
- [ ] Las técnicas aplicadas están anotadas en la matriz de refactorizaciones.
- [ ] Clases y métodos públicos que toqué tienen Javadoc.
- [ ] Sin números mágicos, sin código comentado ni `System.out` de depuración.
- [ ] Si toqué una clase de otro issue, lo avisé en ese issue.
- [ ] Revisor asignado según `docs/convenciones.md` §10, y avisé en Slack.

## Notas para el revisor

<!-- Dudas, decisiones tomadas o pendientes. Borrar si no hay. -->
