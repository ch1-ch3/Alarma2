# ESTADO — Alarma2 (nombre provisorio) · Proyecto claude.ai: Alarma2 (a confirmar) · v4 · 2026-11-24

Último retorno procesado: R1

## 1. Perfil

App Android gratis para cualquiera, pensada para impactar la primera vez. El usuario no programa: Claude construye en su compu (Windows) y él prueba con su celular (Redmi Note 10 con PixelOS 17, Android 17) y tres amigos con Android. Sin fecha límite (S3). Rigor informal, salvo que la alarma suene siempre. Repo `ch1-ch3/Alarma2`. Reglas del juego: `ENTREVISTA.md`.

## 2. Objetivo y criterio de éxito

Primera versión instalada en el celular del usuario y en los de tres amigos, con la historia de ejemplo de `ENTREVISTA.md` (punto 8) cumplida en una prueba real:
- La alarma suena a horario 5 mañanas seguidas en el celular del usuario.
- Una alarma se juega de punta a punta con los tres amigos.
- El usuario no se entera antes ni del sonido ni de quién fue.

La segunda vuelta (comunidad, reacciones, compartir, denunciar, tienda) se planifica después de F5.

## 3. Fase actual

- **F#**: F2
- **Tarea**: elegir el servicio gratuito por internet de toda la app (I2), definir el estilo visual de la app (D10) y construir con ambos las cuentas y los amigos.
- **Incluye**: comparar servicios gratuitos que no pidan tarjeta y cubran entrar con Google, datos compartidos, guardar audios de 30 s, avisos al celular y tareas a una hora fija (el cierre de votación de F3); elegir uno y anotar por qué. Entrar con Google; elegir ID único y nombre; agregar amigo por ID; aceptar o rechazar; eliminar amigo. Las alarmas se suben al servicio y los amigos las ven en una lista con la hora y cuánto falta para el cierre (15 min antes). Reglas del servicio: solo el dueño cambia sus alarmas, solo sus amigos las ven, eliminar corta el acceso. Estilo visual (D10): guía corta de colores, letras, botones y navegación, aplicada a las pantallas de F1 y a las nuevas; el usuario aprueba el estilo en su celular antes de seguir.
- **No incluye**: propuestas, votos, audios, avisos, revelación, link de invitación (D8), puntos o rachas, nombre e ícono definitivos, pantallas de F3–F4 (solo se dejan previstas en la navegación).
- **Entradas**: `app-android/` (la app de F1), `ENTREVISTA.md` puntos 3 ("Entrar y armar el grupo", "Los amigos") y 6, `diseno/referencia_stitch/` (referencia aproximada, D11), `entregables/F1_pruebas.md` solo si hace falta un dato del celular.
- **Supuestos**: S2, S5, S7.
- **Criterio de salida**:
  - Avanzar: el usuario aprobó el estilo en su celular; el celular del usuario y un segundo aparato (emulador en la compu o celular de un amigo), con dos cuentas de Google distintas, se agregan como amigos; cada uno ve las alarmas del otro; un cambio de hora aparece en el otro en menos de un minuto; al eliminar, deja de verlas; el amigo no puede cambiar la alarma ajena; y la alarma sigue sonando en modo avión.
  - Corregir: algo de lo anterior falla con arreglo dentro del servicio elegido.
  - Pivotar: ningún servicio gratis sin tarjeta cubre lo necesario. Decide el usuario.
- **Ejecuta**: Claude, chat de ejecución sobre la carpeta. El usuario inicia sesión él mismo donde haga falta su cuenta de Google y hace las pruebas en el celular.
- **Destino**: la app en el celular del usuario y en el segundo aparato; accede a la carpeta.
- **Audiencia**: el usuario, sin palabras técnicas.
- **Formato del entregable**: app actualizada en `app-android/`; `diseno/ESTILO.md` (guía del estilo, la usan F3 y F4); `entregables/F2_servicio.md` (servicios comparados, elegido y por qué, límites gratis); `entregables/F2_pruebas.md` (tabla de pruebas).
- **Indicaciones de ejecución**:
  - Nada pago ni con tarjeta (D2). Claude no escribe contraseñas: el usuario entra él mismo.
  - No romper lo de F1: repetir la prueba de pantalla bloqueada y una en modo avión (D4).
  - Verificar con el registro del celular, no solo con lo que cuenta el usuario (D9).
  - Subir a GitHub al terminar (D6).
- **Detenerse cuando**: un servicio pida tarjeta o pago; haga falta una cuenta o dato que solo el usuario puede dar; o cambiar algo de `ENTREVISTA.md`.

## 4. Plan

| F | Objetivo | Entregable | Criterio de salida | Estado |
|---|---|---|---|---|
| F1 | Que la alarma suene siempre | Alarma simple en el celular | 5 pruebas y una mañana real | Hecha; falta la mañana real (S6) |
| F2 | Servicio, cuentas y amigos | Entrar, ID, amigos, ver alarmas ajenas | Punto 3 | Actual |
| F3 | El complot | Proponer (grabar, subir, banco chico), rechazo de audios mudos, votar, cierre | Dos amigos proponen y votan; el dueño no ve nada | Pendiente |
| F4 | Sonar y revelar | Baja del ganador, posponer con el segundo, reserva, revelación, avisos | Historia de ejemplo entre dos aparatos. Resolver I4 | Pendiente |
| F5 | Prueba real | App en 4 celulares una semana y lista de arreglos | Criterio de éxito. Resolver I5 e I6 | Pendiente |

## 5. Supuestos

| S# | Supuesto | Estado | Evidencia |
|---|---|---|---|
| S1 | Con herramientas propias de Android la alarma es confiable | Validado en el celular del usuario | R1: 5 pruebas sin tocar batería |
| S2 | Un servicio gratis alcanza para las pruebas | Abierto | F2 |
| S3 | Sin fecha límite | Abierto | — |
| S4 | Audio de 30 s, posponer 5 min, se vale votarse, sin tope de amigos | Abierto (posponer validado) | R1 |
| S5 | A los amigos se les pasa la app como archivo | Abierto | F5 |
| S6 | La alarma suena en una mañana real | Abierto: lo informa el usuario. Si falla, se vuelve a F1 | — |
| S7 | Hay una segunda cuenta de Google para probar (del usuario o de un amigo) | Abierto | F2 |

## 6. Incertidumbres abiertas

| I# | Qué no se sabe | Impacto | Cómo se verifica |
|---|---|---|---|
| I2 | Qué servicio gratis cubre todo sin tarjeta | Alto | Paso 1 de F2 |
| I4 | Si baja el ganador 15 min antes con la app cerrada | Alto | F4 |
| I5 | Si es gracioso de verdad | Alto | F5 |
| I6 | Si suena en Xiaomi con su sistema original (MIUI/HyperOS), que pueden tener los amigos | Alto | Barato: pasarle la app de F1 a un amigo ya. Si no, F5 |
| I7 | Android 17 avisa que en el futuro podría silenciar sonido de apps en segundo plano | Medio | Seguirlo; revisar antes de publicar |

## 7. Decisiones

| D# | Decisión | Motivo | Fecha |
|---|---|---|---|
| D1 | Solo Android al principio | iPhone cuesta más y traba alarmas | 11-10 |
| D2 | Gratis total; nada pago | Decisión del usuario | 11-10 |
| D3 | Entrar con Google | Rápido, gratis | 11-10 |
| D4 | Herramientas propias de Android | Lo más confiable para sonar | 11-11 |
| D5 | Alarma primero, social después | Mayor riesgo, más barato de probar | 11-11 |
| D6 | Guardar en GitHub `ch1-ch3/Alarma2` | Lo pidió el usuario | 11-11 |
| D7 | Sonido de reserva grave, sintetizado | Pedido del usuario en F1 (desvío R1) | 11-24 |
| D8 | Amigos solo por ID en F2; link de invitación después | Achica F2; el link es comodidad | 11-24 |
| D9 | Las pruebas se comprueban con el registro del celular | En F1 hubo respuestas antes de terminar | 11-24 |
| D10 | Interfaz vistosa, divertida, amigable e intuitiva, inspirada en apps exitosas sin copiarlas. Colores por ahora: los de Stitch (verde oscuro, mostaza, café); reemplaza el "pastel" del 11-24 | Pedido del usuario | 11-24 |
| D11 | El diseño de Stitch es la base provisoria, no a seguir al pie de la letra; se puede mejorar | Al usuario no lo convence del todo | 11-24 |

## 8. Próxima acción

`/generar-prompt` en este chat para la F2. En paralelo, y opcional: pasarle la app de F1 a un amigo con Xiaomi original y que ponga una alarma (I6).

## 9. Temas abiertos

- Interfaz definitiva: el usuario no sabe todavía cómo la quiere. Revisarla con la app en uso, antes de F5.
- Nombre de la app: candidato "Complot" (del diseño de Stitch), sin confirmar. Antes de F5.
- Puntos y rachas (aparecen en Stitch): no están en la entrevista; fuera de v1 salvo que el usuario los pida.
- Link de invitación (D8).
- Edad mínima, denuncias, banco de 30–50 sonidos: segunda vuelta (F3 usa ~10).
- Espiar con una segunda cuenta: no se frena en v1.
- Pantalla de un usuario sin amigos.
- Sin volver a probar en F1: alarma en los 20 s después de prender; reinicio con PIN.

## 10. Historial

- 2026-11-10: entrevista. 2026-11-11: plan v1.
- 2026-11-24: R1, F1 cumple. Las 5 pruebas sonaron, con loop, posponer y auto-posponer. Arreglada la falla al reiniciar. Avanzar a F2.
- 2026-11-24: v3, el usuario sumó a F2 el estilo visual (D10, D11) y pasó un diseño de Stitch como referencia.
- 2026-11-24: v4, se dejan los colores de Stitch en vez de pastel (D10). Prompt F2_v2 reemplaza a F2_v1, que no se ejecutó.

## 11. Fuentes

- `ENTREVISTA.md`: la app, reglas, casos, historia de ejemplo.
- `app-android/`: código de la app (F1).
- `entregables/F1_pruebas.md`: celular, pruebas y fallas de F1.
- `RETORNOS.md`: R1.
- `diseno/referencia_stitch/`: 4 pantallas de Stitch (mis alarmas, complots, sala de votación, revelación) y su guía de estilo oscuro verde y mostaza. Base provisoria (D11).
