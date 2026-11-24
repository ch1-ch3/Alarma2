package com.alarma2.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Lo despierta el sistema a la hora de la alarma: arranca el servicio que suena. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val id = intent.getIntExtra(Programador.EXTRA_ID, -1)
        if (id < 0) return
        val i = Intent(ctx, AlarmService::class.java)
            .setAction(AlarmService.ACCION_SONAR)
            .putExtra(Programador.EXTRA_ID, id)
        ctx.startForegroundService(i)
    }
}

/** Al prender el celular, cambiar la hora o actualizar la app: vuelve a armar todo. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        Programador.reprogramarTodas(ctx)
    }
}
