package com.alarma2.app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import java.util.Calendar

/** Pantalla principal: lista de alarmas para crear, editar, prender/apagar y borrar. */
class MainActivity : Activity() {

    private lateinit var lista: LinearLayout
    private lateinit var avisos: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }
        raiz.addView(TextView(this).apply { text = "Alarma2"; textSize = 30f })
        avisos = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        raiz.addView(avisos)
        raiz.addView(Button(this).apply {
            text = "+ Agregar alarma"
            textSize = 20f
            setOnClickListener { editar(null) }
        })
        raiz.addView(Button(this).apply {
            text = "Alarma de prueba en 2 minutos"
            setOnClickListener { alarmaDePrueba() }
        })
        lista = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        raiz.addView(ScrollView(this).apply { addView(lista) })
        setContentView(raiz)

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    override fun onResume() {
        super.onResume()
        Programador.reprogramarTodas(this)
        mostrarAvisos()
        refrescar()
    }

    /** Si falta algún permiso que hace falta para sonar, muestra un botón para darlo. */
    private fun mostrarAvisos() {
        avisos.removeAllViews()
        val nm = getSystemService(NotificationManager::class.java)
        if (!nm.areNotificationsEnabled()) {
            agregarAviso("Falta permitir los avisos: sin eso no aparece la alarma.") {
                startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
            }
        }
        if (Build.VERSION.SDK_INT >= 34 && !nm.canUseFullScreenIntent()) {
            agregarAviso("Falta permitir la pantalla completa: es la que aparece sobre el bloqueo.") {
                startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:$packageName")))
            }
        }
    }

    private fun agregarAviso(texto: String, accion: () -> Unit) {
        avisos.addView(Button(this).apply {
            text = "⚠ $texto Tocá acá."
            setTextColor(Color.rgb(183, 28, 28))
            setOnClickListener { accion() }
        })
    }

    private fun refrescar() {
        lista.removeAllViews()
        val alarmas = Almacen.todas(this)
        if (alarmas.isEmpty()) {
            lista.addView(TextView(this).apply { text = "No hay alarmas todavía."; setPadding(0, 32, 0, 0) })
        }
        for (a in alarmas) lista.addView(fila(a))
    }

    private fun fila(a: Alarma): View {
        val f = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 24, 0, 24)
        }
        val textos = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { editar(a) }
        }
        textos.addView(TextView(this).apply {
            text = a.textoHora(); textSize = 36f
            if (!a.activa) setTextColor(Color.GRAY)
        })
        var detalle = "${a.textoDias()} · volumen ${a.volumen}%"
        if (a.pospuestaHasta > System.currentTimeMillis()) {
            val c = Calendar.getInstance().apply { timeInMillis = a.pospuestaHasta }
            detalle += " · pospuesta hasta %02d:%02d".format(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
        }
        textos.addView(TextView(this).apply { text = detalle })
        f.addView(textos)
        f.addView(Switch(this).apply {
            isChecked = a.activa
            setOnCheckedChangeListener { _, on ->
                a.activa = on
                a.pospuestaHasta = 0
                Almacen.guardar(this@MainActivity, a)
                Programador.programar(this@MainActivity, a)
                refrescar()
            }
        })
        f.addView(Button(this).apply {
            text = "Borrar"
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage("¿Borrar la alarma de las ${a.textoHora()}?")
                    .setPositiveButton("Borrar") { _, _ ->
                        Programador.cancelar(this@MainActivity, a.id)
                        Almacen.borrar(this@MainActivity, a.id)
                        refrescar()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        })
        return f
    }

    private fun editar(existente: Alarma?) {
        val caja = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
        }
        val reloj = TimePicker(this).apply {
            setIs24HourView(true)
            if (existente != null) { hour = existente.hora; minute = existente.minuto }
        }
        caja.addView(reloj)

        caja.addView(TextView(this).apply { text = "Se repite (sin marcar = una sola vez):" })
        val filaDias = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val nombres = listOf("L", "M", "M", "J", "V", "S", "D")
        val marcas = nombres.mapIndexed { i, n ->
            CheckBox(this).apply {
                text = n
                isChecked = existente != null && existente.dias and (1 shl i) != 0
                filaDias.addView(this)
            }
        }
        caja.addView(filaDias)

        val etiquetaVol = TextView(this)
        val vol = SeekBar(this).apply {
            max = 100
            progress = existente?.volumen ?: 80
        }
        fun actualizarEtiqueta() { etiquetaVol.text = "Volumen: ${vol.progress}%" }
        actualizarEtiqueta()
        vol.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) {
                if (p < 5) s?.progress = 5 // nunca muda
                actualizarEtiqueta()
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        caja.addView(etiquetaVol)
        caja.addView(vol)

        val dialogo = AlertDialog.Builder(this)
            .setTitle(if (existente == null) "Nueva alarma" else "Editar alarma")
            .setView(ScrollView(this).apply { addView(caja) })
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()
        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                var dias = 0
                marcas.forEachIndexed { i, m -> if (m.isChecked) dias = dias or (1 shl i) }
                val ok = guardar(existente, reloj.hour, reloj.minute, dias, vol.progress.coerceAtLeast(5))
                if (ok) dialogo.dismiss()
            }
        }
        dialogo.show()
    }

    /** Guarda y arma la alarma. Devuelve false si ya hay otra a esa hora. */
    private fun guardar(existente: Alarma?, hora: Int, minuto: Int, dias: Int, volumen: Int): Boolean {
        val choca = Almacen.todas(this).any { it.id != existente?.id && it.hora == hora && it.minuto == minuto }
        if (choca) {
            Toast.makeText(this, "Ya tenés una alarma a las %02d:%02d".format(hora, minuto), Toast.LENGTH_LONG).show()
            return false
        }
        val a = existente ?: Alarma(Almacen.nuevoId(this), hora, minuto, dias, volumen)
        a.hora = hora; a.minuto = minuto; a.dias = dias; a.volumen = volumen
        a.activa = true; a.pospuestaHasta = 0
        Almacen.guardar(this, a)
        Programador.programar(this, a)
        refrescar()
        val falta = (a.proximoDisparo() - System.currentTimeMillis()) / 60000
        Toast.makeText(this, "Suena en ${falta / 60} h ${falta % 60 + 1} min", Toast.LENGTH_LONG).show()
        return true
    }

    private fun alarmaDePrueba() {
        val c = Calendar.getInstance().apply { add(Calendar.MINUTE, 2) }
        var h = c.get(Calendar.HOUR_OF_DAY); var m = c.get(Calendar.MINUTE)
        // Si ya hay una a esa hora, corre un minuto más.
        val todas = Almacen.todas(this)
        while (todas.any { it.hora == h && it.minuto == m }) {
            c.add(Calendar.MINUTE, 1); h = c.get(Calendar.HOUR_OF_DAY); m = c.get(Calendar.MINUTE)
        }
        guardar(null, h, m, 0, 80)
    }
}
