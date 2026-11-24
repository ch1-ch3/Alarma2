package com.alarma2.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

/**
 * Hace sonar la alarma. Corre "en primer plano" para que el sistema no lo cierre,
 * aunque la app esté cerrada o el celular bloqueado.
 */
class AlarmService : Service() {

    companion object {
        const val ACCION_SONAR = "com.alarma2.SONAR"
        const val ACCION_POSPONER = "com.alarma2.POSPONER"
        const val ACCION_APAGAR = "com.alarma2.APAGAR"
        const val CANAL = "alarma_sonando"
        const val NOTIF_ID = 1
        const val POSPONER_MS = 5 * 60 * 1000L
        const val AUTO_POSPONER_MS = 10 * 60 * 1000L
        const val SILENCIO_ENTRE_VUELTAS_MS = 1000L
        private const val TAG = "Alarma2"

        /** Id de la alarma que está sonando ahora (-1 si ninguna). La lee la pantalla de alarma. */
        @Volatile var sonandoId: Int = -1
        /** La pantalla de alarma se anota acá para cerrarse cuando se apaga o pospone. */
        var alTerminar: (() -> Unit)? = null
    }

    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var volumenAnterior: Int = -1
    private var wakeLock: PowerManager.WakeLock? = null
    private var vibrador: Vibrator? = null

    private val autoPosponer = Runnable { Log.i(TAG, "10 minutos sin apagar: se pospone sola"); posponer() }
    private val otraVuelta = Runnable {
        player?.let { it.seekTo(0); it.start(); Log.i(TAG, "Otra vuelta del sonido") }
    }
    private val esperarFinDeLlamada = object : Runnable {
        override fun run() {
            if (enLlamada()) handler.postDelayed(this, 2000) else { pararVibracion(); arrancarSonido() }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getIntExtra(Programador.EXTRA_ID, -1) ?: -1
        when (intent?.action) {
            ACCION_SONAR -> sonar(id)
            ACCION_POSPONER -> posponer()
            ACCION_APAGAR -> apagar()
            else -> if (sonandoId < 0) stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun sonar(id: Int) {
        val alarma = Almacen.buscar(this, id)
        // Hay que pasar a primer plano sí o sí y rápido, aunque la alarma ya no exista.
        crearCanal()
        val notif = armarAviso(alarma?.textoHora() ?: "")
        pasarAPrimerPlano(notif)
        if (alarma == null || !alarma.activa) {
            Log.w(TAG, "Alarma $id no existe o está apagada")
            if (sonandoId < 0) terminar()
            return
        }
        // Si ya estaba sonando otra, esa se pospone.
        if (sonandoId >= 0 && sonandoId != id) posponerAlarma(sonandoId)
        pararSonido()

        sonandoId = id
        Log.i(TAG, "Suena la alarma $id (${alarma.textoHora()}) al ${alarma.volumen}%")

        // Dejamos armada la próxima vez desde ya, por si algo cierra este servicio.
        alarma.pospuestaHasta = 0
        Almacen.guardar(this, alarma)
        if (alarma.dias != 0) Programador.programar(this, alarma)

        if (wakeLock == null) {
            wakeLock = getSystemService(PowerManager::class.java)
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Alarma2:sonando")
                .apply { acquire(AUTO_POSPONER_MS + 60_000) }
        }

        // Abrimos la pantalla de alarma (si el celular está en uso, queda el aviso arriba con los botones).
        try {
            startActivity(
                Intent(this, AlarmActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION),
            )
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo abrir la pantalla directo; queda el aviso", e)
        }

        ponerVolumen(alarma.volumen)
        if (enLlamada()) {
            vibrar()
            handler.postDelayed(esperarFinDeLlamada, 2000)
        } else {
            arrancarSonido()
        }
        handler.removeCallbacks(autoPosponer)
        handler.postDelayed(autoPosponer, AUTO_POSPONER_MS)
    }

    /**
     * Primer plano con el tipo reservado a las apps de alarma ("systemExempted").
     * El tipo "mediaPlayback" no sirve: Android 15+ lo prohíbe en los primeros segundos
     * después de prender el celular, y ahí la app se cerraba (falla de la prueba E).
     */
    private fun pasarAPrimerPlano(notif: Notification) {
        if (Build.VERSION.SDK_INT >= 34) {
            try {
                startForeground(NOTIF_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED)
                return
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo con systemExempted; pruebo mediaPlayback", e)
            }
        }
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIF_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIF_ID, notif)
        }
    }

    private fun arrancarSonido() {
        pararSonido()
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val mp = MediaPlayer()
        try {
            mp.setAudioAttributes(attrs)
            resources.openRawResourceFd(R.raw.reserva).use { fd ->
                mp.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
            }
            mp.setVolume(1f, 1f)
            mp.setOnCompletionListener {
                Log.i(TAG, "Terminó una vuelta; 1 s de silencio")
                handler.postDelayed(otraVuelta, SILENCIO_ENTRE_VUELTAS_MS)
            }
            mp.prepare()
            mp.start()
            player = mp
        } catch (e: Exception) {
            Log.e(TAG, "No pudo sonar el audio; vibra como último recurso", e)
            mp.release()
            vibrar()
        }
    }

    private fun ponerVolumen(porcentaje: Int) {
        val am = getSystemService(AudioManager::class.java)
        try {
            val max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            if (volumenAnterior < 0) volumenAnterior = am.getStreamVolume(AudioManager.STREAM_ALARM)
            val nivel = Math.round(max * porcentaje / 100f).coerceIn(1, max)
            am.setStreamVolume(AudioManager.STREAM_ALARM, nivel, 0)
            Log.i(TAG, "Volumen de alarma: $nivel de $max (antes $volumenAnterior)")
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo cambiar el volumen", e)
        }
    }

    private fun devolverVolumen() {
        if (volumenAnterior < 0) return
        try {
            getSystemService(AudioManager::class.java)
                .setStreamVolume(AudioManager.STREAM_ALARM, volumenAnterior, 0)
        } catch (_: Exception) {}
        volumenAnterior = -1
    }

    private fun enLlamada(): Boolean {
        val modo = getSystemService(AudioManager::class.java).mode
        return modo == AudioManager.MODE_IN_CALL || modo == AudioManager.MODE_IN_COMMUNICATION
    }

    private fun vibrar() {
        val v = getSystemService(Vibrator::class.java) ?: return
        vibrador = v
        val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
        @Suppress("DEPRECATION")
        v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 800, 600), 0), attrs)
    }

    private fun pararVibracion() { vibrador?.cancel(); vibrador = null }

    private fun pararSonido() {
        handler.removeCallbacks(otraVuelta)
        player?.let { try { it.stop() } catch (_: Exception) {}; it.release() }
        player = null
    }

    private fun posponerAlarma(id: Int) {
        val a = Almacen.buscar(this, id) ?: return
        a.pospuestaHasta = System.currentTimeMillis() + POSPONER_MS
        Almacen.guardar(this, a)
        Programador.programar(this, a)
        Log.i(TAG, "Alarma $id pospuesta 5 minutos")
    }

    private fun posponer() {
        if (sonandoId >= 0) posponerAlarma(sonandoId)
        terminar()
    }

    private fun apagar() {
        val id = sonandoId
        if (id >= 0) {
            Almacen.buscar(this, id)?.let { a ->
                a.pospuestaHasta = 0
                if (a.dias == 0) { a.activa = false; Programador.cancelar(this, id) }
                Almacen.guardar(this, a)
                if (a.activa) Programador.programar(this, a)
            }
            Log.i(TAG, "Alarma $id apagada")
        }
        terminar()
    }

    private fun terminar() {
        handler.removeCallbacksAndMessages(null)
        pararSonido()
        pararVibracion()
        devolverVolumen()
        sonandoId = -1
        alTerminar?.invoke()
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        pararSonido()
        pararVibracion()
        devolverVolumen()
        wakeLock?.let { if (it.isHeld) it.release() }
        super.onDestroy()
    }

    private fun crearCanal() {
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CANAL) != null) return
        val canal = NotificationChannel(CANAL, "Alarma sonando", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Muestra la alarma cuando suena"
            setSound(null, null) // el sonido lo pone la app, por el canal de alarmas
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setBypassDnd(true)
        }
        nm.createNotificationChannel(canal)
    }

    private fun pendiente(accion: String, codigo: Int): PendingIntent =
        PendingIntent.getService(
            this, codigo, Intent(this, AlarmService::class.java).setAction(accion),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun armarAviso(hora: String): Notification {
        val pantalla = PendingIntent.getActivity(
            this, 10, Intent(this, AlarmActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return Notification.Builder(this, CANAL)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarma $hora")
            .setContentText("¡Arriba!")
            .setCategory(Notification.CATEGORY_ALARM)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setContentIntent(pantalla)
            .setFullScreenIntent(pantalla, true)
            .addAction(Notification.Action.Builder(null, "Posponer 5 min", pendiente(ACCION_POSPONER, 11)).build())
            .addAction(Notification.Action.Builder(null, "Apagar", pendiente(ACCION_APAGAR, 12)).build())
            .build()
    }
}
