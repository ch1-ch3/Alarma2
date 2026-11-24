# F1 · Pruebas de la alarma en el celular real

## Celular

- Marca y modelo: Xiaomi Redmi Note 10 (modelo M2101K7AG, código "mojito"/"sunny").
- Sistema: **PixelOS 17.0** (sistema alternativo basado en Android puro, no el MIUI/HyperOS original de Xiaomi). Build `CP2A.260605.016`, compilación del 2026-11-12.
- Versión de Android: 17 (nivel de API 37).

## Ajustes tocados a mano

- Opciones de desarrollador y depuración USB (solo para instalar desde la compu).
- Aceptar en el celular la instalación por cable.
- Permitir notificaciones de Alarma2 (lo pide la app al abrirla).
- Ajustes de batería: **ninguno**. No hizo falta sacar la app del ahorro de batería (no está en la lista de excepciones del sistema y sonó igual en las 5 pruebas).
- Durante la prueba C, Claude prendió y apagó "No molestar" desde la compu, con permiso del usuario.

## Sonido de reserva

`app-android/app/src/main/res/raw/reserva.wav`: tono sintetizado en la compu con `app-android/tools/generar_sonido.py` (ondas senoidales). No usa material de terceros; es libre de usar.

- v1 (pruebas A y B): bips agudos de 880 a 3136 Hz, ~1,6 s. El usuario dijo que era "recontra agudo" y le rompía los oídos.
- v2 (desde la prueba C): campanita grave de 220 a 440 Hz con caída suave, ~2,2 s.

## Pruebas

Evidencia: registro del celular (`adb logcat`, etiqueta `Alarma2`) más lo que contó el usuario. Volumen elegido en todas: 80 % → nivel 6 de 7 del canal de alarmas.

| # | Situación | Qué se hizo | Qué pasó | Sonó | A horario | Loop 1 s | Posponer 5 min | Apagar |
|---|---|---|---|---|---|---|---|---|
| — | Desbloqueado (paso 3) | Alarma de prueba a las 14:31 | Sonó 14:31:00.045; se apagó a los 4 s | sí | sí | — | — | sí |
| A | Pantalla bloqueada | Alarma 14:45, celular bloqueado con el botón | Sonó 14:45:00.048, apareció la pantalla sobre el bloqueo, pospuesta a los 2 s, volvió a sonar 14:50:02.4, apagada | sí | sí | 1 vuelta completa vista (se pospuso antes de la segunda) | sí (5 min 0 s) | sí |
| B | Celular en silencio | Modo silencio puesto 14:51:28 (registro del sistema: modo 0). Primer intento 14:53 se apagó a los 2 s; se repitió a las 14:56 | Sonó 14:56:00.048, 9 vueltas, pausa medida 1,005 s entre vueltas, pospuesta 14:56:24, volvió 15:01:24.3, apagada | sí | sí | sí (1,00–1,01 s) | sí (5 min 0 s) | sí |
| C | Modo "No molestar" | Primer intento 15:08: el registro mostró que "No molestar" nunca se prendió y se apagó a los 6 s (no cuenta). Con permiso del usuario, Claude lo prendió desde la compu (`cmd notification set_dnd priority` → ZEN_MODE_IMPORTANT_INTERRUPTIONS, el mismo del botón rápido) y lo apagó al terminar | Sonó 15:11:00.041 con sonido v2, 13 vueltas cada 3,5 s (2,5 s de sonido + 1 s de silencio), pospuesta 15:11:47, volvió 15:16:47.5, apagada | sí | sí | sí | sí (5 min 0 s) | sí |
| D | App cerrada del todo desde recientes + **posposición automática** | Alarma 15:20; el usuario cerró la app desde la lista de recientes y dejó el celular bloqueado sin tocarlo | Sistema mató la app a las 15:18:27 ("remove task"); el sistema la volvió a arrancar para la alarma a las 15:20:00.039; sonó sin parar; a las 15:30:00.38 "10 minutos sin apagar: se pospone sola"; volvió a sonar 15:35:00.4; apagada 15:38:13 | sí | sí | sí (vueltas con 1,0 s de pausa en lo que quedó del registro) | se pospuso sola a los 10 min y volvió a los 5 min (sin tocar el botón en esta prueba) | sí |
| E (intento 1) | Reinicio con alarma puesta | Alarma 15:41 de una vez; reinicio 15:40; sin desbloquear | **No sonó.** La app se rearmó sola al prender (15:40:49) y el sistema la despertó a las 15:41:00, pero la app se cerró con error: `FGS type mediaPlayback not allowed to start from BOOT_COMPLETED`. Como la alarma cayó 20 s después de prender, Android le aplicó la regla de "recién prendido", que prohíbe ese tipo de servicio | no | — | — | — | — |

Arreglo 1 de 2 para la falla de E: el servicio que suena pasa a usar el tipo `systemExempted` (reservado a apps de alarma, permitido recién prendido), con `mediaPlayback` como respaldo.
| E | Reinicio con alarma puesta (con el arreglo) | Alarma 15:49 de una vez; el usuario reinició enseguida | El celular terminó de prender 15:48:22; la app se rearmó sola 15:48:31; el sistema la despertó 15:49:00.05 y sonó (sin error), pospuesta 15:49:08, volvió 15:54:08.6, apagada 15:54:18 | sí | sí | sí | sí (5 min 0 s) | sí |

## Resumen

Las 5 situaciones sonaron a horario y al volumen elegido (80 % → 6 de 7) después del arreglo de E. Loop con 1 s de silencio medido en el registro (1,00–1,01 s). Posponer: 5 min exactos en A, B, C y E. Posposición automática a los 10 min: probada en D.

## Problemas encontrados

1. **Sonido demasiado agudo** (usuario, prueba B) → se cambió por una campanita grave (v2).
2. **No sonó después de reiniciar** (E intento 1): si la alarma cae en los ~20 s siguientes a que el celular termina de prender, Android 15+ prohíbe el tipo de servicio `mediaPlayback` y la app se cerraba. Arreglo: tipo `systemExempted` (reservado a apps de alarma). En el intento 2 sonó bien, pero la alarma cayó ~38 s después de prender, fuera de esa ventana: **el caso exacto de los 20 s está arreglado en el código pero no se volvió a probar** (el usuario decidió no repetirlo).
3. El celular **no tiene PIN** (se desbloquea deslizando): no se pudo probar el caso "reinicio sin desbloquear con PIN". La app está preparada (guarda las alarmas en el lugar disponible antes de desbloquear y escucha el aviso de "prendido bloqueado"), sin verificar.
4. Respuestas antes de tiempo: en B y C el usuario contestó "todo bien" sin haber hecho completa la prueba; se detectó en el registro y se repitieron. Desde ahí se comprobó todo con el registro del celular.
5. Aviso de Android 17 en el registro: `AudioHardening background playback would be muted ... usage: USAGE_ALARM`. Hoy solo avisa ("would be"), no silencia. Una versión futura de Android podría silenciar sonidos de apps en segundo plano; conviene seguirlo.
6. El volumen del canal de alarmas tiene solo 7 escalones en este celular: el porcentaje elegido se redondea (80 % → 6/7).
7. Una alarma de "una sola vez" que no llegó a sonar (por ejemplo, la del intento 1 de E) queda puesta para el día siguiente a la misma hora.
