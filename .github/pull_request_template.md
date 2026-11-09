<!--
Título del PR (se convierte en el commit final del squash merge):
  tipo: descripción (iss-XX)
  Ej.: refactor: separar consulta de modificador en historial de ventas (iss-04)

⚠️ Revisa que el PR vaya a: Luis-villanueva-valdivia/SistemaGestionAbarrotes · develop
-->

Closes #N

## Descripción

<!-- Qué resuelve este PR y por qué. Una o dos frases. -->

## Cambios

<!-- Qué se agregó, modificó o eliminó. En refactorizaciones: mal olor → técnica aplicada. -->
-
-

## Cómo probarlo

1. `git fetch && git switch tipo/iss-XX-descripcion`
2. `cd sistema-caja && mvn clean compile && mvn test`
3. <!-- Clase main o pantalla a abrir, y qué hacer -->
4. Resultado esperado:

## Criterios de aceptación

<!-- Copiados del issue. Marca los que se cumplen. -->
- [ ]
- [ ]

## Evidencia

<!-- Capturas de consola o de la app. Si no aplica, escribe "No aplica". -->

## Checklist

- [ ] La rama sale de `develop` y sigue el formato `tipo/iss-XX-descripcion`.
- [ ] El título y los commits siguen el formato `tipo: descripción (iss-XX)`.
- [ ] El PR va a `develop` de **este** repo, no al original.
- [ ] `mvn clean compile` termina sin errores.
- [ ] `mvn test` pasa en verde.
- [ ] Refactorizaciones: una técnica por commit y sin cambiar el comportamiento.
- [ ] Clases y métodos públicos con Javadoc.
- [ ] Sin números mágicos, sin código comentado ni `System.out` de depuración.
- [ ] Solo se agregó al final de archivos ajenos, con comentario de sección.
- [ ] Revisor asignado según la revisión cruzada de `docs/convenciones.md`.

## Notas para el revisor

<!-- Dudas, decisiones tomadas o pendientes. Borrar si no hay. -->
