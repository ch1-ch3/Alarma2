# F2 — Servicio por internet (I2)

Consultado en las páginas oficiales el 2026-11-24.

## Qué necesita la app

Entrar con Google · datos compartidos con reglas de quién lee y quién escribe · guardar audios de hasta 30 s (F3) · avisos al celular (F4) · una tarea a hora fija para cerrar la votación 15 min antes (F3).

## Comparación

| Servicio (plan gratis) | Google | Datos con reglas | Audios | Avisos al celular | Tarea a hora fija | Pide tarjeta | Límites gratis | Fuente |
|---|---|---|---|---|---|---|---|---|
| **Supabase Free** | Sí (incluido) | Sí: base de datos con reglas por fila | Sí: 1 GB, hasta 50 MB por archivo | No propio: se mandan con FCM desde una función | Sí: Supabase Cron, de cada segundo a una vez al año | No | 50.000 usuarios/mes, 500 MB de datos, 5 GB de tráfico, 500.000 llamadas a funciones, 2 proyectos. **Se pausa tras 1 semana sin uso** | supabase.com/pricing · supabase.com/docs/guides/cron · supabase.com/docs/guides/auth/social-login/auth-google |
| Appwrite Cloud Free | Sí | Sí: permisos por documento | Sí: 2 GB, 1 "bucket" | Sí, pero 1.000 mensajes/mes | Sí: funciones con horario | No | 75.000 usuarios/mes, 750.000 ejecuciones, 5 GB de tráfico, 2 funciones, 1 base. **Se pausa tras 1 semana sin uso** | appwrite.io/pricing |
| Firebase Spark | Sí | Sí (Firestore) | **No**: guardar archivos exige el plan Blaze (con tarjeta) desde 2024 | Sí (FCM, gratis) | **No**: las funciones exigen Blaze | Spark no; Blaze sí | — | firebase.google.com/pricing · firebase.google.com/docs/storage/faqs-storage-changes-announced-sept-2024 |

## Elegido: Supabase Free + FCM (de Firebase Spark, solo para avisos)

Por qué:
- Cubre todo menos los avisos sin tarjeta, y los avisos se resuelven con FCM, que es gratis y no pide tarjeta.
- Las reglas ("solo el dueño cambia sus alarmas, solo sus amigos las ven") se escriben en la misma base, fila por fila, y las controla el servicio: aunque alguien lo intente por fuera de la app, se rechaza.
- La tarea de cierre (F3) corre dentro del servicio con Supabase Cron, sin otro proveedor.
- Appwrite también servía, pero solo da 2 funciones y 1.000 avisos al mes; Supabase deja más margen.

## Riesgos anotados

- **Pausa por inactividad**: si nadie usa el proyecto una semana, se pausa y hay que reactivarlo desde el panel (gratis). Con la app en uso diario no debería pasar. La alarma no se ve afectada: suena con lo guardado en el celular.
- **Avisos (F4)**: necesitan un proyecto de Firebase en plan Spark solo para FCM, y una función de Supabase que los mande. Se arma en F4.
- **Cierre a la hora justa (F3)**: Cron corre como mucho cada segundo; para muchas alarmas se haría una tarea cada minuto que cierre las que corresponden.
- Límite de 2 proyectos gratis por cuenta: alcanza (uno solo).

## Claves

La dirección del proyecto y la clave pública ("anon"/"publishable") van dentro de la app: son públicas por diseño y la seguridad la ponen las reglas. La clave secreta ("service_role") **no** va al repo ni a la app.
