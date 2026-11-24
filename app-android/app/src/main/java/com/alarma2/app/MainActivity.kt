package com.alarma2.app

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import java.util.Calendar

/** Pantalla principal: arriba el título, en el medio la pestaña elegida, abajo las 4 pestañas. */
class MainActivity : Activity() {

    enum class Pestana(val icono: Int, val nombre: String, val subtitulo: String) {
        ALARMAS(R.drawable.ic_alarm, "Alarmas", "Mis alarmas"),
        COMPLOTS(R.drawable.ic_complot, "Complots", "Las alarmas de tus amigos"),
        AUDIOS(R.drawable.ic_campaign, "Audios", "Tu arsenal de sonidos"),
        AMIGOS(R.drawable.ic_group, "Amigos", "Tus cómplices"),
    }

    private var actual = Pestana.ALARMAS
    private lateinit var contenido: LinearLayout
    private lateinit var subtitulo: TextView
    private lateinit var barra: LinearLayout
    private val reloj = Handler(Looper.getMainLooper())
    private val refrescoPeriodico = object : Runnable {
        override fun run() { mostrar(); reloj.postDelayed(this, 30_000) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Estilo.FONDO)
        }
        raiz.addView(encabezado())
        contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val p = Estilo.dp(this@MainActivity, 18)
            setPadding(p, Estilo.dp(this@MainActivity, 8), p, Estilo.dp(this@MainActivity, 24))
        }
        raiz.addView(ScrollView(this).apply {
            addView(contenido)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        })
        barra = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Estilo.MOSTAZA)
        }
        val contenedorBarra = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Estilo.MOSTAZA)
            addView(View(this@MainActivity).apply {
                setBackgroundColor(Estilo.MOSTAZA_SOMBRA)
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Estilo.dp(this@MainActivity, 3))
            })
            addView(barra)
        }
        raiz.addView(contenedorBarra)
        setContentView(raiz)
        Bordes.aplicar(raiz, arriba = raiz, abajo = contenedorBarra)

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    override fun onResume() {
        super.onResume()
        Programador.reprogramarTodas(this)
        reloj.post(refrescoPeriodico)
    }

    override fun onPause() {
        super.onPause()
        reloj.removeCallbacks(refrescoPeriodico)
    }

    private fun encabezado(): View {
        val f = Estilo.fila(this)
        val p = Estilo.dp(this, 18)
        f.setPadding(p, Estilo.dp(this, 14), p, Estilo.dp(this, 10))
        f.addView(Estilo.icono(this, R.drawable.ic_alarm, Estilo.TINTA, 30).apply {
            scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
            background = Estilo.conRelieve(this@MainActivity, Estilo.MOSTAZA, Estilo.MOSTAZA_SOMBRA, Estilo.dpf(this@MainActivity, 18f), Estilo.dp(this@MainActivity, 4))
            layoutParams = LinearLayout.LayoutParams(Estilo.dp(this@MainActivity, 52), Estilo.dp(this@MainActivity, 56))
            setPadding(Estilo.dp(this@MainActivity, 11), Estilo.dp(this@MainActivity, 11), Estilo.dp(this@MainActivity, 11), Estilo.dp(this@MainActivity, 15))
        })
        val textos = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Estilo.dp(this@MainActivity, 14), 0, 0, 0)
        }
        textos.addView(Estilo.tituloTexto(this, "ALARMA2", 26f, Estilo.MOSTAZA, extra = true).apply { letterSpacing = 0.06f })
        subtitulo = Estilo.etiqueta(this, "", 15f, Estilo.TEXTO_SUAVE)
        textos.addView(subtitulo)
        f.addView(textos)
        return f
    }

    private fun armarBarra() {
        barra.removeAllViews()
        for (p in Pestana.values()) {
            val activa = p == actual
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                val m = Estilo.dp(this@MainActivity, 6)
                layoutParams = LinearLayout.LayoutParams(0, Estilo.dp(this@MainActivity, 70), 1f).apply { setMargins(m, m + 2, m, m) }
                if (activa) background = Estilo.conRelieve(this@MainActivity, Estilo.CAFE, Estilo.CAFE_SOMBRA, Estilo.dpf(this@MainActivity, 22f), Estilo.dp(this@MainActivity, 3))
                setOnClickListener { actual = p; mostrar() }
                contentDescription = p.nombre
            }
            item.addView(Estilo.icono(this, p.icono, if (activa) Estilo.LINO else Estilo.TINTA, 28))
            item.addView(Estilo.etiqueta(this, p.nombre, 13f, if (activa) Estilo.LINO else Estilo.TINTA, fuerte = true).apply { gravity = Gravity.CENTER })
            barra.addView(item)
        }
    }

    /** Dibuja la pestaña elegida. */
    fun mostrar() {
        subtitulo.text = actual.subtitulo
        armarBarra()
        contenido.removeAllViews()
        when (actual) {
            Pestana.ALARMAS -> pestanaAlarmas()
            Pestana.COMPLOTS -> proximamente(R.drawable.ic_complot, "Acá vas a ver las alarmas de tus amigos", "Cuando sumes cómplices, vas a ver a qué hora suena cada uno y cuánto falta para que cierre el complot.")
            Pestana.AUDIOS -> proximamente(R.drawable.ic_campaign, "Próximamente", "Acá vas a grabar y guardar los sonidos más crueles para despertar a tus amigos.")
            Pestana.AMIGOS -> proximamente(R.drawable.ic_group, "Tus cómplices", "Muy pronto vas a poder sumar amigos por su ID.")
        }
    }

    private fun proximamente(icono: Int, titulo: String, texto: String) {
        val t = Estilo.tarjetaOscura(this).apply { gravity = Gravity.CENTER_HORIZONTAL }
        t.addView(Estilo.icono(this, icono, Estilo.MOSTAZA, 64))
        t.addView(Estilo.espacio(this, 8))
        t.addView(Estilo.tituloTexto(this, titulo, 24f).apply { gravity = Gravity.CENTER })
        t.addView(Estilo.espacio(this, 8))
        t.addView(Estilo.etiqueta(this, texto, 16f, Estilo.TEXTO_SUAVE).apply { gravity = Gravity.CENTER })
        contenido.addView(Estilo.espacio(this, 24))
        contenido.addView(t)
    }

    // ---------- Pestaña Alarmas ----------

    private fun pestanaAlarmas() {
        mostrarAvisos()
        contenido.addView(globo("Todavía no tenés cómplices. Cuando sumes amigos, ellos van a elegir con qué despertarte."))
        val alarmas = Almacen.todas(this)
        if (alarmas.isEmpty()) {
            contenido.addView(Estilo.etiqueta(this, "No tenés alarmas. Dormí tranquilo… por ahora.", 17f, Estilo.TEXTO_SUAVE).apply {
                gravity = Gravity.CENTER
                setPadding(0, Estilo.dp(this@MainActivity, 24), 0, Estilo.dp(this@MainActivity, 24))
            })
        }
        for (a in alarmas) contenido.addView(tarjetaAlarma(a))
        contenido.addView(Estilo.espacio(this, 4))
        contenido.addView(Estilo.boton(this, "Nueva alarma", tam = 20f, icono = R.drawable.ic_add) { editar(null) })
        contenido.addView(Estilo.espacio(this, 14))
        contenido.addView(Estilo.boton(this, "Probar: alarma en 2 minutos", Estilo.TipoBoton.SUAVE, 14f) { alarmaDePrueba() }.apply {
            minHeight = Estilo.dp(this@MainActivity, 48)
        })
    }

    /** Globo de diálogo con un mensaje pícaro. */
    private fun globo(txt: String): View {
        val f = Estilo.fila(this).apply { setPadding(0, 0, 0, Estilo.dp(this@MainActivity, 18)) }
        f.addView(Estilo.icono(this, R.drawable.ic_complot, Estilo.MOSTAZA).apply {
            val p = Estilo.dp(this@MainActivity, 14)
            setPadding(p, p, p, p)
            background = Estilo.redondo(Estilo.FONDO_ALTO, Estilo.dpf(this@MainActivity, 20f), Estilo.dp(this@MainActivity, 2), Estilo.BORDE_FONDO)
            layoutParams = LinearLayout.LayoutParams(Estilo.dp(this@MainActivity, 60), Estilo.dp(this@MainActivity, 60))
        })
        f.addView(Estilo.etiqueta(this, txt, 15f, Estilo.TINTA, fuerte = true).apply {
            background = Estilo.redondo(Estilo.LINO, Estilo.dpf(this@MainActivity, 20f))
            val p = Estilo.dp(this@MainActivity, 14)
            setPadding(p, p, p, p)
            layoutParams = Estilo.ancho().apply { setMargins(Estilo.dp(this@MainActivity, 10), 0, 0, 0) }
        })
        return f
    }

    private fun mostrarAvisos() {
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
        contenido.addView(Estilo.conIcono(Estilo.etiqueta(this, "$texto Tocá acá.", 16f, Estilo.LINO, fuerte = true), R.drawable.ic_warning, 24).apply {
            background = Estilo.redondo(Estilo.PICANTE, Estilo.dpf(this@MainActivity, 18f))
            val p = Estilo.dp(this@MainActivity, 14)
            setPadding(p, p, p, p)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply { setMargins(0, 0, 0, Estilo.dp(this@MainActivity, 14)) }
            setOnClickListener { accion() }
        })
    }

    private fun tarjetaAlarma(a: Alarma): View {
        val t = Estilo.tarjeta(this, apagada = !a.activa)
        t.setOnClickListener { editar(a) }
        val colorTexto = if (a.activa) Estilo.TINTA else Estilo.TINTA_SUAVE

        val arriba = Estilo.fila(this)
        arriba.addView(Estilo.tituloTexto(this, a.textoHora(), 56f, colorTexto, extra = true).apply { layoutParams = Estilo.ancho() })
        arriba.addView(Interruptor(this).apply {
            prendido = a.activa
            alCambiar = { on ->
                a.activa = on
                a.pospuestaHasta = 0
                Almacen.guardar(this@MainActivity, a)
                Programador.programar(this@MainActivity, a)
                Toast.makeText(this@MainActivity, if (on) "Prendida. Que tiemblen." else "Apagada. Zafaste.", Toast.LENGTH_SHORT).show()
                mostrar()
            }
        })
        t.addView(arriba)
        t.addView(Estilo.espacio(this, 8))

        val dias = Estilo.fila(this)
        dias.addView(Estilo.etiqueta(this, a.textoDias(), 16f, colorTexto, fuerte = true))
        dias.addView(Estilo.etiqueta(this, "   ", 15f))
        dias.addView(Estilo.conIcono(Estilo.etiqueta(this, "${a.volumen}%", 15f, Estilo.TINTA_SUAVE), R.drawable.ic_volume_up, 18))
        t.addView(dias)

        if (a.activa) {
            t.addView(Estilo.espacio(this, 14))
            val caja = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = Estilo.redondo(Estilo.CAJA_LINO, Estilo.dpf(this@MainActivity, 20f))
                val p = Estilo.dp(this@MainActivity, 14)
                setPadding(p, p, p, p)
            }
            val cierre = Estilo.fila(this)
            cierre.addView(Estilo.conIcono(Estilo.etiqueta(this, "Sonido secreto hasta que suenes", 15f, Estilo.TINTA, fuerte = true), R.drawable.ic_lock, 20).apply { layoutParams = Estilo.ancho() })
            caja.addView(cierre)
            caja.addView(Estilo.espacio(this, 10))
            val chips = Estilo.fila(this)
            chips.addView(chipCierre(this, a.proximoDisparo() - 15 * 60_000L - System.currentTimeMillis()))
            if (a.pospuestaHasta > System.currentTimeMillis()) {
                val c = Calendar.getInstance().apply { timeInMillis = a.pospuestaHasta }
                chips.addView(Estilo.espacio(this, 1).apply { layoutParams = LinearLayout.LayoutParams(Estilo.dp(this@MainActivity, 8), 1) })
                chips.addView(Estilo.chip(this, "Pospuesta hasta %02d:%02d".format(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)), Estilo.LINO, Estilo.TINTA, Estilo.CAFE, R.drawable.ic_snooze))
            }
            caja.addView(chips)
            t.addView(caja)
        } else {
            t.addView(Estilo.espacio(this, 12))
            t.addView(Estilo.conIcono(Estilo.etiqueta(this, "Zafaste: esta alarma está apagada.", 15f, Estilo.TINTA_SUAVE), R.drawable.ic_shield, 20))
        }
        return t
    }

    // ---------- Editar ----------

    private fun editar(existente: Alarma?) {
        Hoja.abrir(this, if (existente == null) "Nueva alarma" else "Editar alarma") { caja, d ->
            val reloj = TimePicker(this, null, 0, R.style.RelojRueda).apply {
                setIs24HourView(true)
                if (existente != null) { hour = existente.hora; minute = existente.minuto }
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            agrandarRuedas(reloj)
            caja.addView(reloj)
            caja.addView(Estilo.espacio(this, 8))
            caja.addView(Estilo.etiqueta(this, "¿Qué días? (ninguno = una sola vez)", 15f, Estilo.TEXTO_SUAVE, fuerte = true))
            caja.addView(Estilo.espacio(this, 8))

            val filaDias = Estilo.fila(this)
            val nombres = listOf("L", "M", "M", "J", "V", "S", "D")
            val marcados = BooleanArray(7) { existente != null && existente.dias and (1 shl it) != 0 }
            nombres.forEachIndexed { i, n ->
                val tam = Estilo.dp(this, 40)
                val b = TextView(this).apply {
                    text = n; textSize = 16f; gravity = Gravity.CENTER
                    typeface = Estilo.textoFuerte(this@MainActivity)
                    layoutParams = LinearLayout.LayoutParams(0, tam, 1f).apply { setMargins(3, 0, 3, 0) }
                }
                fun pintar() {
                    b.background = Estilo.redondo(if (marcados[i]) Estilo.MOSTAZA else Estilo.FONDO, Estilo.dpf(this, 999f), Estilo.dp(this, 2), if (marcados[i]) Estilo.MOSTAZA else Estilo.BORDE_FONDO)
                    b.setTextColor(if (marcados[i]) Estilo.TINTA else Estilo.TEXTO_SUAVE)
                }
                pintar()
                b.setOnClickListener { marcados[i] = !marcados[i]; pintar() }
                filaDias.addView(b)
            }
            caja.addView(filaDias)
            caja.addView(Estilo.espacio(this, 18))

            val etiquetaVol = Estilo.etiqueta(this, "", 15f, Estilo.TEXTO_SUAVE, fuerte = true)
            val vol = SeekBar(this).apply {
                max = 100
                progress = existente?.volumen ?: 80
                progressTintList = ColorStateList.valueOf(Estilo.MOSTAZA)
                thumbTintList = ColorStateList.valueOf(Estilo.MOSTAZA)
                progressBackgroundTintList = ColorStateList.valueOf(Estilo.BORDE_FONDO)
            }
            Estilo.conIcono(etiquetaVol, R.drawable.ic_volume_up, 20)
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
            caja.addView(Estilo.espacio(this, 22))

            caja.addView(Estilo.boton(this, "Guardar") {
                var dias = 0
                marcados.forEachIndexed { i, m -> if (m) dias = dias or (1 shl i) }
                if (guardar(existente, reloj.hour, reloj.minute, dias, vol.progress.coerceAtLeast(5))) d.dismiss()
            })
            caja.addView(Estilo.espacio(this, 10))
            caja.addView(Estilo.boton(this, "Cancelar", Estilo.TipoBoton.SUAVE, 16f) { d.dismiss() })
            if (existente != null) {
                caja.addView(Estilo.espacio(this, 14))
                caja.addView(Estilo.conIcono(Estilo.etiqueta(this, "Borrar esta alarma", 16f, Color_BORRAR, fuerte = true), R.drawable.ic_delete, 20).apply {
                    gravity = Gravity.CENTER
                    setPadding(0, Estilo.dp(this@MainActivity, 10), 0, Estilo.dp(this@MainActivity, 10))
                    setOnClickListener {
                        Hoja.confirmar(this@MainActivity, "¿Borrar la alarma?", "La de las ${existente.textoHora()} se va para siempre.", "Sí, borrarla") {
                            Programador.cancelar(this@MainActivity, existente.id)
                            Almacen.borrar(this@MainActivity, existente.id)
                            d.dismiss()
                            mostrar()
                        }
                    }
                })
            }
        }
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
        mostrar()
        val falta = (a.proximoDisparo() - System.currentTimeMillis() + 59_999) / 60000
        Toast.makeText(this, "Suena en ${falta / 60} h ${falta % 60} min", Toast.LENGTH_LONG).show()
        return true
    }

    /** Números de la rueda grandes y en mostaza, para leerlos con sueño. */
    private fun agrandarRuedas(v: View) {
        if (v is android.widget.NumberPicker && Build.VERSION.SDK_INT >= 29) {
            v.textSize = Estilo.dpf(this, 34f)
            v.textColor = Estilo.LINO
            v.selectionDividerHeight = Estilo.dp(this, 2)
            for (i in 0 until v.childCount) (v.getChildAt(i) as? TextView)?.apply { textSize = 34f; setTextColor(Estilo.MOSTAZA); typeface = Estilo.tituloExtra(this@MainActivity) }
        } else if (v is ViewGroup) {
            for (i in 0 until v.childCount) {
                val h = v.getChildAt(i)
                if (h is TextView && h !is android.widget.EditText) { h.textSize = 30f; h.setTextColor(Estilo.LINO) }
                agrandarRuedas(h)
            }
        }
    }

    private fun alarmaDePrueba() {
        val c = Calendar.getInstance().apply { add(Calendar.MINUTE, 2) }
        var h = c.get(Calendar.HOUR_OF_DAY); var m = c.get(Calendar.MINUTE)
        val todas = Almacen.todas(this)
        while (todas.any { it.hora == h && it.minuto == m }) {
            c.add(Calendar.MINUTE, 1); h = c.get(Calendar.HOUR_OF_DAY); m = c.get(Calendar.MINUTE)
        }
        guardar(null, h, m, 0, 80)
    }

    companion object {
        private val Color_BORRAR = android.graphics.Color.parseColor("#FF9E80")

        /** Chip café "Cierra en 42 min": la votación cierra 15 minutos antes de que suene. */
        fun chipCierre(ctx: android.content.Context, faltaMs: Long) =
            Estilo.chip(ctx, textoCierre(faltaMs), Estilo.CAFE, Estilo.LINO, icono = if (faltaMs <= 0) R.drawable.ic_lock else R.drawable.ic_hourglass_top)

        fun textoCierre(faltaMs: Long): String {
            if (faltaMs <= 0) return "Complot cerrado"
            val min = (faltaMs + 59_999) / 60_000
            return when {
                min < 60 -> "Cierra en $min min"
                min < 24 * 60 -> "Cierra en ${min / 60} h ${min % 60} min"
                else -> "Cierra en ${min / (24 * 60)} d ${(min % (24 * 60)) / 60} h"
            }
        }
    }
}
