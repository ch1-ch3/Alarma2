package com.alarma2.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

/**
 * Una alarma. dias: bit 0 = lunes ... bit 6 = domingo. 0 = suena una sola vez.
 * pospuestaHasta: si no es 0, la próxima vez que suena es esa (posponer).
 */
data class Alarma(
    val id: Int,
    var hora: Int,
    var minuto: Int,
    var dias: Int,
    var volumen: Int,
    var activa: Boolean = true,
    var pospuestaHasta: Long = 0L,
) {
    fun textoHora() = "%02d:%02d".format(hora, minuto)

    fun textoDias(): String {
        if (dias == 0) return "Una sola vez"
        if (dias == 0x7F) return "Todos los días"
        val nombres = listOf("Lu", "Ma", "Mi", "Ju", "Vi", "Sá", "Do")
        return nombres.filterIndexed { i, _ -> dias and (1 shl i) != 0 }.joinToString(" ")
    }

    /** Próximo momento en que tiene que sonar, en milisegundos. */
    fun proximoDisparo(ahora: Long = System.currentTimeMillis()): Long {
        if (pospuestaHasta > ahora) return pospuestaHasta
        val c = Calendar.getInstance().apply {
            timeInMillis = ahora
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (c.timeInMillis <= ahora) c.add(Calendar.DAY_OF_YEAR, 1)
        if (dias == 0) return c.timeInMillis
        repeat(8) {
            // Calendar: domingo = 1 ... sábado = 7. Lo pasamos a lunes = 0 ... domingo = 6.
            val d = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7
            if (dias and (1 shl d) != 0) return c.timeInMillis
            c.add(Calendar.DAY_OF_YEAR, 1)
        }
        return c.timeInMillis
    }

    fun aJson() = JSONObject().apply {
        put("id", id); put("hora", hora); put("minuto", minuto); put("dias", dias)
        put("volumen", volumen); put("activa", activa); put("pospuestaHasta", pospuestaHasta)
    }

    companion object {
        fun deJson(o: JSONObject) = Alarma(
            o.getInt("id"), o.getInt("hora"), o.getInt("minuto"), o.getInt("dias"),
            o.getInt("volumen"), o.optBoolean("activa", true), o.optLong("pospuestaHasta", 0L),
        )
    }
}

/**
 * Guarda las alarmas en el celular. Usa el almacenamiento que está disponible
 * incluso antes de desbloquear el celular después de un reinicio.
 */
object Almacen {
    private fun prefs(ctx: Context) =
        ctx.createDeviceProtectedStorageContext().getSharedPreferences("alarmas", Context.MODE_PRIVATE)

    @Synchronized
    fun todas(ctx: Context): MutableList<Alarma> {
        val txt = prefs(ctx).getString("lista", "[]") ?: "[]"
        val arr = JSONArray(txt)
        return MutableList(arr.length()) { Alarma.deJson(arr.getJSONObject(it)) }
            .sortedWith(compareBy({ it.hora }, { it.minuto })).toMutableList()
    }

    @Synchronized
    fun guardarTodas(ctx: Context, lista: List<Alarma>) {
        val arr = JSONArray()
        lista.forEach { arr.put(it.aJson()) }
        prefs(ctx).edit().putString("lista", arr.toString()).commit()
        // Copia para los amigos, en otro hilo y después de guardar: nunca frena ni cambia la alarma.
        Sincro.avisar(ctx)
    }

    fun buscar(ctx: Context, id: Int) = todas(ctx).firstOrNull { it.id == id }

    @Synchronized
    fun guardar(ctx: Context, a: Alarma) {
        val lista = todas(ctx).filter { it.id != a.id }.toMutableList()
        lista.add(a)
        guardarTodas(ctx, lista)
    }

    @Synchronized
    fun borrar(ctx: Context, id: Int) = guardarTodas(ctx, todas(ctx).filter { it.id != id })

    fun nuevoId(ctx: Context) = (todas(ctx).maxOfOrNull { it.id } ?: 0) + 1
}

/** Arma las alarmas con el mecanismo de "alarma de reloj" del sistema. */
object Programador {
    const val EXTRA_ID = "alarma_id"

    private fun intentDisparo(ctx: Context, id: Int): PendingIntent {
        val i = Intent(ctx, AlarmReceiver::class.java).putExtra(EXTRA_ID, id)
        return PendingIntent.getBroadcast(
            ctx, id, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun intentMostrar(ctx: Context): PendingIntent {
        val i = Intent(ctx, MainActivity::class.java)
        return PendingIntent.getActivity(
            ctx, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun programar(ctx: Context, a: Alarma) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        if (!a.activa) {
            am.cancel(intentDisparo(ctx, a.id))
            return
        }
        val cuando = a.proximoDisparo()
        val info = AlarmManager.AlarmClockInfo(cuando, intentMostrar(ctx))
        am.setAlarmClock(info, intentDisparo(ctx, a.id))
    }

    fun cancelar(ctx: Context, id: Int) {
        ctx.getSystemService(AlarmManager::class.java).cancel(intentDisparo(ctx, id))
    }

    /** Vuelve a armar todas (al prender el celular, al cambiar la hora, al abrir la app). */
    fun reprogramarTodas(ctx: Context) {
        val ahora = System.currentTimeMillis()
        val lista = Almacen.todas(ctx)
        var cambio = false
        for (a in lista) {
            // Una posposición que quedó en el pasado (por ejemplo, el celular estuvo apagado) se descarta.
            if (a.pospuestaHasta in 1..ahora) { a.pospuestaHasta = 0; cambio = true }
            programar(ctx, a)
        }
        if (cambio) Almacen.guardarTodas(ctx, lista)
    }
}
