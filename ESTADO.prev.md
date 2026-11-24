# ESTADO — Alarma2 (nombre provisorio) · Proyecto claude.ai: Alarma2 (a confirmar) · v1 · 2026-11-11

Último retorno procesado: ninguno

## 1. Perfil

- **Tipo**: app para celulares Android. Software.
- **Propósito**: publicarla gratis para cualquiera. Tiene que ser muy llamativa la primera vez; no importa que se use durante años.
- **Rigor**: informal, salvo una cosa que no admite fallas: que la alarma suene siempre.
- **Horizonte**: sin fecha límite (S3).
- **Recursos**: el usuario no programa; Claude hace todo el trabajo en la compu del usuario (Windows). Celular Android con cable. Tres amigos con Android para probar. 25 dólares para Google Play, recién al publicar.
- **Destino**: primero el celular del usuario y los de sus amigos; después Google Play.
- **Guardado**: GitHub, `https://github.com/ch1-ch3/Alarma2`.

## 2. Objetivo y criterio de éxito

Tener la primera versión instalada en el celular del usuario y en los de tres amigos, y que la historia de ejemplo de `ENTREVISTA.md` (punto 8) se cumpla tal cual en una prueba real.

Se cumple cuando:
- La alarma suena a horario 5 mañanas seguidas en el celular del usuario, sin fallar ninguna.
- Una alarma real se juega de punta a punta con los tres amigos: proponen, votan, suena el ganador, al posponer suena el segundo y aparece la revelación.
- El usuario no pudo enterarse antes ni del sonido ni de quién fue.

La segunda vuelta (banco de la comunidad, reacciones, compartir, denunciar, publicar en la tienda) queda fuera de este plan y se planifica después de F5.

## 3. Fase actual

- **F#**: F1
- **Tarea**: construir una alarma simple, sin amigos ni cuentas, e instalarla en el celular del usuario para comprobar que suena siempre.
- **Alcance, incluye**: preparar la compu para armar apps de Android; crear la app; poner una alarma con hora, días y volumen; que suene en loop con 1 segundo de silencio entre vueltas; posponer 5 minutos; apagar; un sonido de reserva que trae la app; que se vuelva a armar sola si el celular se reinicia; que a los 10 minutos sin apagarla se posponga sola.
- **Alcance, no incluye**: cuentas, amigos, propuestas, votos, avisos, grabar audios, revelación, diseño lindo.
- **Entradas**: `ENTREVISTA.md` (puntos 3, 6 y 7).
- **Supuestos**: S1, S4.
- **Criterio de salida**:
  - Avanzar: la alarma sonó en las 5 pruebas (pantalla bloqueada; celular en silencio; "no molestar"; app cerrada del todo; después de reiniciar el celular) y el usuario la usó una mañana real y sonó. La prueba de la mañana real el usuario la informa en el chat de planificación.
  - Corregir: falla alguna de las 5 pruebas pero tiene arreglo dentro de la misma forma de construir.
  - Pivotar: en el celular del usuario no hay forma de que suene de manera confiable. Se replantea la forma de construir (S1).
- **Ejecuta**: Claude, en un chat de ejecución sobre la carpeta `Alarma2`. El usuario enchufa el celular y hace las pruebas que Claude le pide.
- **Destino**: el celular del usuario, por cable. El chat de ejecución accede a la carpeta.
- **Audiencia del resultado**: el usuario, que no programa. Todo se le explica sin palabras técnicas.
- **Formato del entregable**: la app instalada en el celular, el código en la carpeta y `entregables/F1_pruebas.md` con la marca y modelo del celular, las 5 pruebas y su resultado.
- **Indicaciones de ejecución**:
  - Construir con las herramientas propias de Android (D4).
  - No instalar ni contratar nada que cueste plata (D2).
  - Anotar la marca y el modelo del celular, y cualquier ajuste de batería que haya hecho falta tocar a mano (I1).
  - Subir el trabajo a GitHub al terminar (D6).
- **Detenerse cuando**: una herramienta pida pago o tarjeta; la compu no tenga espacio para instalar lo necesario; el celular no se conecte después de dos intentos; o se necesite cambiar algo de lo decidido en la entrevista.

## 4. Plan

| F | Objetivo | Entregable | Criterio de salida | Estado |
|---|---|---|---|---|
| F1 | Comprobar que la alarma suena siempre | Alarma simple instalada en el celular | Suena en las 5 pruebas y en una mañana real | Actual |
| F2 | Cuentas y amigos | Entrar con Google, elegir ID, agregar, aceptar y eliminar amigos; ver las alarmas de los amigos | Dos celulares se agregan como amigos y cada uno ve la alarma del otro. Antes de construir, resolver I2 | Pendiente |
| F3 | El complot | Proponer sonido (grabar, subir, banco chico propio), rechazo de audios mudos, votar, cierre 15 minutos antes | Dos amigos proponen y votan sobre una alarma real y el dueño no ve ni sonido ni nombres | Pendiente |
| F4 | Sonar y revelar | Suena el ganador, posponer con el segundo, reserva si falla, revelación, avisos sin nombres | La historia de ejemplo se cumple entre dos celulares. Resolver I4 | Pendiente |
| F5 | Prueba real con amigos | La app en 4 celulares durante una semana, y una lista de lo que hay que arreglar | Se cumple el criterio de éxito del punto 2 | Pendiente |

Cada fase se hace en una sola sesión. Si al llegar a F3 o F4 resultan grandes, se dividen en dos.

## 5. Supuestos

| S# | Supuesto | Estado | Evidencia |
|---|---|---|---|
| S1 | Con las herramientas propias de Android se logra una alarma confiable; la versión de iPhone, si llega, se hace aparte | Abierto | Se prueba en F1 |
| S2 | Los servicios gratuitos alcanzan para guardar audios, cuentas y avisos durante las pruebas | Abierto | Se verifica al empezar F2 (I2) |
| S3 | No hay fecha límite | Abierto | No se habló |
| S4 | Audio de hasta 30 segundos, posponer de 5 minutos, vale votarse a uno mismo, sin tope de amigos | Abierto | `ENTREVISTA.md`, punto 9 |
| S5 | A los amigos se les pasa la app como archivo, sin tienda, para la prueba | Abierto | Se confirma en F5 |

## 6. Incertidumbres abiertas

| I# | Qué no se sabe | Impacto | Cómo se verifica | Depende |
|---|---|---|---|---|
| I1 | Si la alarma suena siempre en el celular real (algunas marcas cierran las apps para ahorrar batería) | Alto | F1, barato | S1 |
| I2 | Qué servicio gratuito guarda los audios sin pedir tarjeta | Alto | Revisar las condiciones actuales al empezar F2, barato | S2 |
| I3 | Si la compu del usuario tiene espacio y el celular se conecta bien | Medio | Primer paso de F1 | — |
| I4 | Si el celular logra bajar el sonido ganador 15 minutos antes con la app cerrada | Alto | F4 | S1, S2 |
| I5 | Si de verdad es gracioso y da ganas de mostrarlo | Alto | F5, con amigos reales | — |

## 7. Decisiones

| D# | Decisión | Motivo | Fecha |
|---|---|---|---|
| D1 | Solo Android al principio | iPhone cuesta más y traba las alarmas | 2026-11-10 |
| D2 | Gratis total, sin publicidad ni nada pago | Decisión del usuario | 2026-11-10 |
| D3 | Se entra con la cuenta de Google | Un toque, gratis, menos abandono | 2026-11-10 |
| D4 | Se construye con las herramientas propias de Android | Es lo más confiable para que la alarma suene | 2026-11-11 |
| D5 | Primero la alarma sola, después lo social | Es el riesgo más grande y el más barato de probar | 2026-11-11 |
| D6 | El proyecto se guarda en GitHub, repo `ch1-ch3/Alarma2` | Lo pasó el usuario; estaba vacío | 2026-11-11 |

Las reglas del juego (votación, propuesta definitiva, avisos sin nombres, casos raros) están en `ENTREVISTA.md` y valen como decididas.

## 8. Próxima acción

En este chat: `/generar-prompt` para la F1. Después, abrir un chat de ejecución sobre la carpeta `Alarma2` con el celular a mano y el cable.

## 9. Temas abiertos

- Nombre de la app. No frena nada hasta F5.
- Estética y tono de los textos. Se define antes de F4, que es cuando aparece la revelación.
- Edad mínima para la tienda. Segunda vuelta.
- Qué pasa con un sonido denunciado y quién lo revisa. Segunda vuelta.
- De dónde salen los 30 a 50 sonidos del banco propio. Para F3 alcanza con unos 10.
- El dueño podría espiar con una segunda cuenta. No se frena en la primera versión.
- Primera pantalla de un usuario sin amigos.

## 10. Historial

- 2026-11-10: entrevista inicial cerrada (`ENTREVISTA.md`).
- 2026-11-11: plan inicial v1.

## 11. Fuentes

- `ENTREVISTA.md`: qué es la app, reglas, casos raros, historia de ejemplo y corte de la primera versión.
- Chat del 2026-11-11: el usuario tiene Android con cable, tres amigos con Android, y pasó el repo de GitHub.
- Repo `https://github.com/ch1-ch3/Alarma2`: revisado el 2026-11-11, vacío.
