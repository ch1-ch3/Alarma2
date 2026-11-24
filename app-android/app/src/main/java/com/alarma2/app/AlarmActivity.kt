package com.alarma2.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout

/**
 * Pantalla que aparece cuando suena, aunque el celular esté bloqueado.
 * Prioridad absoluta: posponer y apagar enormes, fáciles de tocar dormido.
 */
class AlarmActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val hora = Almacen.buscar(this, AlarmService.sonandoId)?.textoHora() ?: ""
        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Estilo.FONDO)
        }
        val caja = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            val p = Estilo.dp(this@AlarmActivity, 22)
            setPadding(p, p, p, p)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        raiz.addView(caja)

        val arriba = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        }
        arriba.addView(Estilo.icono(this, R.drawable.ic_alarm_on, Estilo.MOSTAZA, 80))
        arriba.addView(Estilo.tituloTexto(this, hora, 96f, Estilo.LINO, extra = true).apply { gravity = Gravity.CENTER })
        arriba.addView(Estilo.espacio(this, 8))
        arriba.addView(Estilo.tituloTexto(this, "¡Arriba, dormilón!", 30f, Estilo.MOSTAZA).apply { gravity = Gravity.CENTER })
        caja.addView(arriba)

        caja.addView(boton("Posponer 5 min", AlarmService.ACCION_POSPONER, Estilo.TipoBoton.SECUNDARIO, R.drawable.ic_snooze))
        caja.addView(Estilo.espacio(this, 18))
        caja.addView(boton("Apagar", AlarmService.ACCION_APAGAR, Estilo.TipoBoton.PRINCIPAL, R.drawable.ic_alarm_on))

        setContentView(raiz)
        Bordes.aplicar(raiz)
    }

    private fun boton(texto: String, accion: String, tipo: Estilo.TipoBoton, icono: Int) =
        Estilo.boton(this, texto, tipo, 26f, icono) {
            startService(Intent(this@AlarmActivity, AlarmService::class.java).setAction(accion))
            finish()
        }.apply {
            // Cada botón ocupa casi un cuarto de la pantalla.
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (resources.displayMetrics.heightPixels * 0.22).toInt())
        }

    override fun onResume() {
        super.onResume()
        if (AlarmService.sonandoId < 0) { finish(); return }
        AlarmService.alTerminar = { runOnUiThread { finish() } }
    }

    override fun onPause() {
        super.onPause()
        AlarmService.alTerminar = null
    }

    @Deprecated("Atrás no apaga la alarma")
    override fun onBackPressed() { /* a propósito: hay que elegir posponer o apagar */ }
}
