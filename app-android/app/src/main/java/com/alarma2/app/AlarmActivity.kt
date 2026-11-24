package com.alarma2.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** Pantalla que aparece cuando suena, aunque el celular esté bloqueado. */
class AlarmActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val hora = Almacen.buscar(this, AlarmService.sonandoId)?.textoHora() ?: ""
        val caja = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }
        caja.addView(TextView(this).apply {
            text = hora
            textSize = 72f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        })
        caja.addView(TextView(this).apply {
            text = "¡Arriba!"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 96)
        })
        caja.addView(boton("POSPONER 5 MIN", AlarmService.ACCION_POSPONER))
        caja.addView(boton("APAGAR", AlarmService.ACCION_APAGAR))
        setContentView(caja)
    }

    private fun boton(texto: String, accion: String) = Button(this).apply {
        text = texto
        textSize = 24f
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 220,
        ).apply { setMargins(0, 24, 0, 24) }
        setOnClickListener {
            startService(Intent(this@AlarmActivity, AlarmService::class.java).setAction(accion))
            finish()
        }
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
