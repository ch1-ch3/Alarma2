# Estilo de la app (D10, D11)

Base: la paleta de Stitch (`diseno/referencia_stitch/warm_canopy/DESIGN.md`), con tonos ajustados para que se lea bien con sueño. Vistosa, pícara y fácil: cada pantalla tiene una sola acción principal, en mostaza.

En código: `app-android/app/src/main/java/com/alarma2/app/Estilo.kt` (colores, letras, botones, tarjetas, chips) y `Piezas.kt` (interruptor, ventanas, bordes de pantalla).

## Colores

| Nombre | Código | Para qué |
|---|---|---|
| Fondo | `#15291D` | Verde bosque oscuro: fondo de todas las pantallas, también la de alarma sonando |
| Fondo alto | `#1F3A29` | Tarjetas oscuras (alarmas de amigos), ventanas |
| Borde fondo | `#2E4D38` | Bordes finos sobre el fondo |
| Mostaza | `#F4B731` | Lo principal: botón de la acción, barra de pestañas, días elegidos |
| Sombra mostaza | `#B07D12` | Relieve de los botones mostaza |
| Café | `#4A2C1C` | Botones secundarios, pestaña activa, chip "Cierra en" |
| Sombra café | `#2A170D` | Relieve de lo café |
| Lino | `#F4EFE6` | Tarjetas de mis alarmas, texto claro |
| Lino apagado | `#D9D2C5` | Tarjeta de una alarma apagada |
| Caja lino | `#E8E1D4` | Recuadro dentro de la tarjeta |
| Tinta | `#23160E` | Texto sobre lino y mostaza |
| Tinta suave | `#5C4A3D` | Texto secundario sobre lino |
| Texto | `#F4EFE6` | Texto sobre el fondo verde (nunca blanco puro) |
| Texto suave | `#BAC8B3` | Texto secundario sobre el fondo verde |
| Verde ok | `#1E6B3A` sobre `#D3EBD5` | Chip "Activa", interruptor prendido |
| Picante | `#B23A0E` | Avisos importantes ("falta un permiso") |

Contraste: todo texto chico va en tinta sobre claro o en lino/texto suave sobre el verde; ningún texto gris sobre verde.

## Letras

Incluidas en la app (`assets/fonts/`), gratuitas de Google Fonts (licencia OFL).

| Uso | Letra | Peso | Tamaño |
|---|---|---|---|
| Hora de la alarma | Epilogue | 800 | 56 (tarjeta), 96 (sonando) |
| Títulos | Epilogue | 700 | 24–30 |
| Nombre de la app | Epilogue | 800, espaciada, mostaza | 26 |
| Texto | Plus Jakarta Sans | 500 | 15–17 |
| Botones, chips, pestañas | Plus Jakarta Sans | 700 | 13–20, botones en mayúsculas |

## Piezas

- **Botón principal**: pastilla mostaza con texto tinta en mayúsculas, alto mínimo 60, con "sombra sólida" de 6 abajo. Al tocarlo se hunde (la cara y el texto bajan). Uno por pantalla.
- **Botón secundario**: igual, café con texto lino.
- **Botón suave**: verde alto con borde, para "Cancelar", "Mejor no", "Probar".
- **Tarjeta de alarma (mía)**: lino, esquinas de 28, relieve café. Hora enorme, interruptor a la derecha, días + volumen + chip "⚡ Activa". Abajo un recuadro con "🔒 Sonido secreto hasta que suenes" y el chip café "⏳ Cierra en 42 min". Apagada: lino apagado y "🛡️ Zafaste".
- **Tarjeta de alarma (de un amigo)**: verde alto con borde, hora en lino, nombre y @ID en mostaza, chip "Cierra en". Nada se corta: los chips van en su propio renglón, no encimados.
- **Chips**: pastillas con texto en negrita, un solo renglón.
- **Interruptor**: pastilla con relieve; prendido verde con bolita mostaza y tilde, apagado beige.
- **Ventanas** (nueva alarma, confirmar): verde alto con borde, esquinas de 28, botón principal arriba de "Cancelar". Las preguntas de borrar dicen qué se pierde.
- **Elegir hora**: ruedas (más fáciles que las agujas). Días: 7 círculos, mostaza cuando están elegidos.

## Navegación

Barra mostaza abajo, a todo lo ancho, con 4 pestañas (ícono + nombre): reloj Alarmas · máscaras Complots · megáfono Audios · personitas Amigos. La activa es una pastilla café con ícono y texto lino. Arriba: cuadradito mostaza con el reloj, "ALARMA2" (nombre provisorio) y el subtítulo de la pestaña. Audios queda "próximamente" hasta F3.

## Pantalla de alarma sonando

Fondo verde, ícono de reloj sonando en mostaza, la hora en 96 lino, "¡Arriba, dormilón!" en mostaza. Abajo dos botones enormes (22 % del alto cada uno): "Posponer 5 min" café y "Apagar" mostaza, cada uno con su ícono. Nada más.

## Tono de los textos

Pícaro, cómplice, en rioplatense, cortito. **Sin emojis** (pedido del usuario): los dibujos van como íconos. Nunca revela al dueño quién ni qué sonido.

Ejemplos:
1. "Tus 3 amigos ya están maquinando algo."
2. "Sonido secreto hasta que suenes" (con candado)
3. "Zafaste: esta alarma está apagada." (con escudo)
4. "No tenés alarmas. Dormí tranquilo… por ahora."
5. "¿Borrar la alarma? La de las 07:00 se va para siempre." → "Sí, borrarla" / "Mejor no"

## Íconos

Lisos, de un solo color (el del texto que acompañan), en `res/drawable/ic_*.xml`. Salvo el de Complots, son de Material Symbols Rounded (Google, licencia Apache 2.0, gratis). Complots es un dibujo propio: dos máscaras de teatro, alegre adelante y triste atrás, sin cinta (`ic_complot.xml`, lo genera `app-android/tools/icono_mascaras.py`). Íconos dentro de botones van pegados al texto; en etiquetas y chips, a la izquierda.

## Inspiración (patrones, no copias)

Botones gruesos que se hunden y textos con humor, como las apps de aprender con mascota; lista simple de amigos como las apps sociales chicas; hora gigante y botones grandes como los despertadores del sistema. Sin marcas, logos, personajes ni pantallas de otras apps. Sin puntos, rachas ni seguidores.

## Cambios pedidos por el usuario

- Ronda 1 (2026-11-24): le gustaron botones y colores; pidió sacar los emojis y usar íconos. Hecho.
- Ronda 2 (2026-11-24): pidió para Complots unas máscaras de teatro como las de una imagen de referencia, sin la cinta (dibujo propio), y un ícono de Audios más amigable: eligió el megáfono (opción C de `diseno/opciones_icono_audios.png`). Aprobó el estilo.
