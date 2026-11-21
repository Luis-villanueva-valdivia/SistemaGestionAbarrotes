<!--
Título del PR (se convierte en el commit final del squash merge):
  tipo: descripción (iss-XX)
  Ej.: refactor: encapsular campos de ProductoModel (iss-41)

⚠️ Revisa que el PR vaya a: Luis-villanueva-valdivia/SistemaGestionAbarrotes · develop
-->

Closes #XX

**Desbloquea a:** #XX, #XX <!-- o "ninguno". Al fusionar, avisa en esos issues y quítales estado:bloqueado -->

**Requisitos que conserva:** RF-XX, RF-XX <!-- Los RF de la ERS que pasan por el código tocado -->

## Qué se hizo / Técnica

<!-- Refactorización: [ID de la matriz] code smell → técnica del curso → clase/método.
     Pruebas: clases probadas y número de pruebas. -->
-

## Descripción

<!-- Por qué se hizo este cambio. Una o dos frases. -->

## Cambios

-
-

## Cómo probarlo

1. `git fetch && git switch tipo/iss-XX-descripcion`
2. `cd sistema-caja && mvn clean compile && mvn test`
3. <!-- Pantalla o menú de consola a abrir, y qué hacer -->
4. Resultado esperado:

## Resultado de pruebas

<!-- Captura o resumen de `mvn test`: pruebas corridas, fallidas (debe ser 0). -->

## Criterios de aceptación

<!-- Copiados del issue. Marca los que se cumplen. -->
- [ ]
- [ ]

## Checklist

- [ ] Las dependencias del issue ya estaban **fusionadas** en `develop`.
- [ ] La rama sale de `develop` y sigue el formato `tipo/iss-XX-descripcion`.
- [ ] El título y los commits siguen el formato `tipo: descripción (iss-XX)`.
- [ ] El PR va a `develop` de **este** repo, no al de origen.
- [ ] `mvn clean compile` termina sin errores.
- [ ] `mvn test` pasa en verde.
- [ ] El sistema hace lo mismo que antes, en la interfaz gráfica y en la de consola.
- [ ] Pruebas: solo agregué archivos en `src/test` y cada prueba indica su RF.
- [ ] Refactorizaciones: una técnica por commit y el comentario `// [ID] Técnica` arriba del código cambiado.
- [ ] La matriz de refactorizaciones está actualizada.
- [ ] Javadoc en las clases y métodos públicos que toqué.
- [ ] Revisor asignado según `docs/convenciones.md` §6, y avisé en Slack.

## Notas para el revisor

<!-- Dudas, decisiones tomadas o pendientes. Borrar si no hay. -->
