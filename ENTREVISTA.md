# Entrevista inicial: app de alarma con sonido sorpresa

Fecha: 10 de noviembre de 2026. Todavía no tiene nombre.

## 1. Objetivo

Una app de alarma donde el sonido no lo elegís vos: tus amigos se complotan, proponen sonidos y votan con cuál te vas a despertar. No sabés qué va a sonar ni quién fue hasta que la apagás.

No busca que la gente la use durante años. Busca que sea muy llamativa y graciosa la primera vez, y que dé ganas de mostrársela a otros.

## 2. Para quién es

- Para cualquiera: se publica en la tienda.
- Arranca solo en Android (Google Play, 25 dólares una sola vez). iPhone queda para más adelante si la app prende (99 dólares por año y más trabas para las alarmas).
- Gratis total, sin publicidad y sin nada pago.

## 3. Cómo funciona, paso a paso

**Entrar y armar el grupo**

1. La persona entra con su cuenta de Google, en un toque.
2. Elige su ID único (tipo `@nacho`) y su nombre.
3. Agrega amigos por ID o con un link de invitación. El otro tiene que aceptar.

**El dueño de la alarma**

4. Pone una alarma: hora, días en que se repite y volumen. Puede tener varias, a cualquier hora.
5. Toda alarma puesta en la app está abierta al complot. No existe la alarma "normal".
6. Ve que ya hay un sonido puesto, pero nunca cuál ni de quién.
7. Le llegan avisos sin nombres: "Alguien propuso un sonido para tu alarma de las 7:00", "Cambió el sonido que va ganando", "Ya hay 3 votos".

**Los amigos**

8. Les llega un aviso cuando el dueño pone una alarma: "Nacho puso una alarma para las 7:00. Elegile el sonido".
9. En su pantalla principal ven las alarmas de todos sus amigos, con cuánto falta para que cierre cada votación.
10. Al entrar a una alarma ven todas las propuestas, las pueden escuchar, ven quién propuso cada una y cuántos votos tiene.
11. Pueden proponer un sonido (uno solo por alarma, y una vez mandado no se puede cambiar ni borrar): grabando un audio, subiendo uno propio o eligiendo del banco.
12. Votan un solo sonido y pueden cambiar el voto.

**La votación**

13. Cierra 15 minutos antes de que suene la alarma, sea la hora que sea.
14. Gana el sonido con más votos. Si hay empate, gana el que se propuso primero.
15. Si hay una sola propuesta y nadie votó, suena esa igual.

**Cuando suena**

16. Suena el ganador en loop, con 1 segundo de silencio entre vuelta y vuelta, al volumen que eligió el dueño.
17. Si pospone (5 minutos), suena el segundo más votado, después el tercero y así. Si no hay otro, se repite el mismo.
18. Al apagarla del todo aparece la revelación (al posponer, no).

**La revelación**

19. Quién propuso el ganador y cómo se llama el sonido.
20. Quiénes lo votaron (los cómplices).
21. Los sonidos que perdieron, para escucharlos.
22. Botón para volver a escuchar el ganador.
23. Reaccionar o contestarle a los amigos.
24. Compartir afuera (WhatsApp, historias).
25. A los amigos les llega el resultado: "Nacho se despertó con tu sonido", más la reacción.

**Se repite**

26. En las alarmas que se repiten, cada día arranca de cero con votación nueva. Apenas suena la de hoy, se abre la de mañana.

## 4. Qué necesita para funcionar

- Cuenta de Google de cada usuario.
- Micrófono, para grabar audios.
- Acceso a archivos, para subir audios propios.
- Permiso de avisos y de alarmas en el celular.
- Internet, para amigos, propuestas, votos y para bajar el sonido ganador.
- Un banco propio de 30 a 50 sonidos libres de usar, elegidos a mano.
- Un sonido de reserva por usuario (y uno que trae la app por defecto).

## 5. Qué produce

- Una alarma que suena siempre, con un sonido sorpresa elegido por los amigos.
- La pantalla de revelación.
- Avisos para el dueño (sin nombres) y para los amigos.
- Un banco de sonidos: el propio más el de la comunidad (sonidos que la gente sube y marca como públicos, con los más usados arriba).

## 6. Reglas principales

- Hora, días y volumen solo los cambia el dueño. Siempre, incluso con la votación cerrada.
- El dueño nunca puede escuchar ni vetar el sonido antes de que suene.
- El dueño no ve nombres hasta apagar la alarma del todo.
- Entre amigos sí se ve quién propuso qué.
- Amistad todo o nada: un amigo ve todas las alarmas y puede proponer en todas. No se puede dejar a alguien afuera de una alarma puntual.
- Eliminar a un amigo le quita todo: no ve más las alarmas ni puede proponer.
- Una propuesta por amigo por alarma, y es definitiva: no se cambia ni se borra. Antes de mandarla, la app pide confirmar.
- Un voto por amigo, que sí se puede cambiar hasta el cierre.
- Audios grabados o subidos: máximo 30 segundos.
- El humor zarpado entre amigos está permitido.
- La alarma nunca depende de internet para sonar.
- Ningún sonido puede dejar la alarma muda: no se aceptan audios en silencio ni inaudibles.
- No se copian sonidos de MyInstants: muchos tienen dueño y la tienda puede bajar la app.

## 7. Casos especiales

| Qué pasa | Qué hace la app |
|---|---|
| El dueño cambia la hora | Propuestas y votos se mantienen, se corre el cierre. Aviso a los amigos. |
| El dueño apaga o borra la alarma | Se cancela el complot de ese día y se pierden las propuestas. Aviso a los amigos ("canceló la alarma, cobarde"). |
| Nadie propuso nada | Suena el sonido de reserva. Al apagarla dice "nadie se complotó hoy". |
| Sin internet para bajar el ganador | Suena el sonido de reserva. |
| El audio ganador está roto | Pasa al segundo más votado y, si no hay, a la reserva. |
| Un amigo propone un audio en silencio o casi inaudible (a propósito o no) | La app lo rechaza al proponer: "Este audio no se escucha, grabá otro". |
| Audio con silencio largo al principio o al final | Se recorta solo, para que suene apenas arranca y el loop no tenga huecos. |
| Audio grabado muy bajito | Hay un mínimo de fuerza: si no llega, se rechaza al proponer. No hay máximo ni se emparejan los audios: un audio de gritos suena más fuerte que uno tranquilo, a propósito. |
| Un amigo quiere cambiar su sonido después de mandarlo | No se puede: la propuesta es definitiva. Así nadie junta votos con un sonido y lo cambia por otro, ni obliga a todos a votar de nuevo. |
| El dueño elimina a un amigo que ya propuso o votó | Se borran su propuesta y sus votos de las alarmas pendientes. |
| El sonido propuesto deja de existir (lo borraron del banco o el amigo borró su cuenta) | Esa propuesta se cae y sus votos se liberan. |
| Dos amigos quieren proponer el mismo sonido del banco | Al segundo le dice "ya lo propusieron" y le ofrece votarlo. |
| El dueño cambia la hora y quedan menos de 15 minutos | La votación cierra en el momento, con lo que haya. |
| La votación ya cerró y el dueño corre la alarma para más tarde | Se reabre, y vuelve a cerrar 15 minutos antes de la hora nueva. |
| Alguien se hace amigo con la votación ya cerrada | Ve la alarma pero no puede proponer ni votar hasta la próxima. |
| El dueño quiere poner dos alarmas a la misma hora | No se puede: una sola por horario. |
| El dueño pospone más veces que sonidos hay | Al terminar la lista vuelve al ganador y sigue dando la vuelta. |
| Amigos en otro país u otra zona horaria | La alarma suena a la hora del celular del dueño. Los amigos ven sobre todo "cierra en 3 h 20 min", no una hora que confunda. |
| Auriculares o parlante bluetooth conectados | Suena también por el parlante del celular. |
| Suena durante una llamada | Vibra y muestra la pantalla de alarma; el sonido arranca al cortar. |
| El celular se reinicia o se actualiza | Las alarmas se vuelven a armar solas al prender. |
| El celular está apagado o sin batería | No suena: ninguna app puede. Al prenderlo avisa "te perdiste la alarma" y muestra la revelación. |
| El dueño la apaga desde el aviso o la pantalla bloqueada | La revelación queda esperando y aparece al abrir la app. |
| El dueño cierra sesión o desinstala la app | Sus alarmas desaparecen para los amigos y se cancelan los complots. |
| El volumen de alarmas del celular está en cero | Se usa el volumen elegido en la app, no el del celular. |
| El ganador igual resulta inaudible al sonar | Pasa al segundo más votado y, si no hay, a la reserva. |
| Celular en silencio o "no molestar" | Suena igual, al volumen elegido. |
| No la apaga nadie | Suena hasta 10 minutos y después se pospone sola. |
| Nunca eligió sonido de reserva | La app trae uno por defecto. |
| Alarma puesta con menos de 15 minutos | La votación ya nace cerrada: suena la reserva. |

## 8. Cómo saber si salió bien

- La alarma suena siempre, a horario y al volumen elegido. Si falla una vez, la gente la desinstala.
- La primera vez que alguien la usa, se ríe y se la muestra a otro.
- El dueño no puede enterarse antes de tiempo ni del sonido ni de quién fue.
- Entrar, agregar un amigo y poner la primera alarma lleva muy pocos pasos.
- La historia de ejemplo de abajo se cumple tal cual.

**Historia de ejemplo (aprobada)**

Domingo a la noche. Nacho pone una alarma para el lunes a las 7:00. A Juan, Maru y Tomi les llega "Nacho puso una alarma para las 7:00. Elegile el sonido".

Juan graba un audio gritando "¡NACHO, SE TE QUEMA LA CASA!". Maru entra, lo escucha, se ríe y lo vota. Tomi propone un sonido de gallo del banco y se vota a sí mismo. A Nacho le van llegando avisos: "Alguien propuso un sonido", "Ya hay 2 votos", "Cambió el sonido que va ganando". No sabe quién ni qué.

A las 6:45 cierra la votación. Gana Juan con 2 votos contra 1.

A las 7:00 suena el grito de Juan en loop. Nacho pospone. A las 7:05 suena el gallo de Tomi. Nacho la apaga del todo.

Aparece la pantalla: "Fue Juan. Cómplice: Maru. Perdió: el gallo de Tomi". Nacho vuelve a escuchar el grito. A Juan le llega "Nacho se despertó con tu sonido".

Como la alarma es de lunes a viernes, ya se abre la votación del martes, de cero.

## 9. Pendientes y riesgos

- **Nombre de la app**: sin definir.
- **Estética y tono de los textos**: sin definir.
- **Que suene siempre en Android**: es lo más delicado. Algunas marcas de celular cierran las apps para ahorrar batería; hay que probarlo en celulares reales.
- **Botón de denunciar**: las tiendas lo exigen en apps donde la gente sube audios. Va chiquito en la revelación, para casos graves. Falta definir qué pasa con un sonido denunciado y quién lo revisa.
- **Banco de la comunidad**: lo que sube la gente puede tener dueño. Hace falta un texto de condiciones que deje la responsabilidad en quien sube.
- **De dónde salen los 30 a 50 sonidos propios**: hay que elegirlos de sitios con sonidos libres.
- **Costos**: los servicios gratuitos alcanzan para arrancar; se paga recién si la app crece mucho.
- **Supuestos que no se confirmaron uno por uno**: máximo de 30 segundos por audio, posponer de 5 minutos, si un amigo puede votarse a sí mismo (en la historia aprobada, sí), y si hay un tope de amigos.
- **Edad mínima**: como hay audios zarpados entre usuarios, la tienda va a pedir una clasificación por edad. Sin definir.
- **Trampa del dueño**: podría crearse una segunda cuenta, hacerse amigo de sí mismo y escuchar las propuestas. No se frena en la primera versión.
- **Usuario sin amigos**: la app no tiene gracia hasta que agrega al menos uno. Hay que pensar bien esa primera pantalla.

## 10. Siguiente paso

**Primera versión (para probar con amigos de verdad)**

- Entrar con Google, ID y amigos.
- Alarmas con hora, días y volumen.
- Proponer sonido grabando, subiendo o eligiendo del banco propio.
- Votación con cierre 15 minutos antes.
- Que suene bien: loop, posponer y reserva.
- Revelación con quién fue, cómplices, perdedores y repetir.
- Avisos sin nombres.

**Segunda vuelta (antes de publicar en la tienda)**

- Banco de la comunidad.
- Reacciones y respuestas.
- Compartir afuera.
- Botón de denunciar.

**Acción recomendada:** en el chat de planificación de este proyecto, sobre la carpeta `Alarma2`: `/planificar-proyecto`.
