package com.alarma2.app

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.LinearLayout
import android.widget.ScrollView

/** Interruptor tipo pastilla: verde oscuro con bolita mostaza cuando está prendido. */
class Interruptor(ctx: Context) : View(ctx) {
    var prendido = false
        set(v) { field = v; invalidate() }
    var alCambiar: ((Boolean) -> Unit)? = null
    private val pinta = Paint(Paint.ANTI_ALIAS_FLAG)
    private val w = Estilo.dp(ctx, 68)
    private val h = Estilo.dp(ctx, 40)

    init {
        setOnClickListener { prendido = !prendido; alCambiar?.invoke(prendido) }
        contentDescription = "Prender o apagar"
    }

    override fun onMeasure(a: Int, b: Int) = setMeasuredDimension(w, h + Estilo.dp(context, 4))

    override fun onDraw(c: Canvas) {
        val r = h / 2f
        val sombra = Estilo.dp(context, 4).toFloat()
        pinta.color = if (prendido) Color.parseColor("#0E3A1F") else Color.parseColor("#9A8F80")
        c.drawRoundRect(RectF(0f, sombra, w.toFloat(), h + sombra), r, r, pinta)
        pinta.color = if (prendido) Color.parseColor("#1E6B3A") else Color.parseColor("#C9BFB0")
        c.drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), r, r, pinta)
        val cx = if (prendido) w - r else r
        val m = Estilo.dp(context, 4)
        pinta.color = if (prendido) Estilo.MOSTAZA_SOMBRA else Color.parseColor("#8A7F70")
        c.drawCircle(cx, r + Estilo.dp(context, 2), r - m, pinta)
        pinta.color = if (prendido) Estilo.MOSTAZA else Estilo.LINO
        c.drawCircle(cx, r, r - m, pinta)
        if (prendido) {
            pinta.color = Estilo.TINTA
            pinta.strokeWidth = Estilo.dpf(context, 2.5f)
            pinta.style = Paint.Style.STROKE
            pinta.strokeCap = Paint.Cap.ROUND
            val s = r / 3
            c.drawLine(cx - s, r, cx - s / 4, r + s * 0.7f, pinta)
            c.drawLine(cx - s / 4, r + s * 0.7f, cx + s, r - s * 0.6f, pinta)
            pinta.style = Paint.Style.FILL
        }
    }
}

/** Ventana flotante con el estilo de la app (reemplaza los cuadros grises del sistema). */
object Hoja {
    fun abrir(act: Activity, titulo: String, armar: (LinearLayout, Dialog) -> Unit): Dialog {
        val d = Dialog(act, android.R.style.Theme_Material_Dialog_NoActionBar)
        val caja = LinearLayout(act).apply {
            orientation = LinearLayout.VERTICAL
            val p = Estilo.dp(act, 22)
            setPadding(p, p, p, p)
        }
        caja.addView(Estilo.tituloTexto(act, titulo, 24f))
        caja.addView(Estilo.espacio(act, 16))
        armar(caja, d)
        d.setContentView(ScrollView(act).apply { addView(caja) })
        d.window?.setBackgroundDrawable(Estilo.redondo(Estilo.FONDO_ALTO, Estilo.dpf(act, 28f), Estilo.dp(act, 2), Estilo.BORDE_FONDO))
        d.window?.setLayout((act.resources.displayMetrics.widthPixels * 0.92).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        d.show()
        return d
    }

    /** Pregunta sí/no. El botón de "sí" es el principal. */
    fun confirmar(act: Activity, titulo: String, mensaje: String, si: String, alSi: () -> Unit) {
        abrir(act, titulo) { caja, d ->
            caja.addView(Estilo.etiqueta(act, mensaje, 17f, Estilo.TEXTO_SUAVE))
            caja.addView(Estilo.espacio(act, 20))
            caja.addView(Estilo.boton(act, si) { d.dismiss(); alSi() })
            caja.addView(Estilo.espacio(act, 10))
            caja.addView(Estilo.boton(act, "Mejor no", Estilo.TipoBoton.SUAVE, 16f) { d.dismiss() })
        }
    }
}

/** Respeta las barras del sistema (arriba la hora, abajo los gestos). */
object Bordes {
    fun aplicar(raiz: View, arriba: View = raiz, abajo: View = raiz) {
        raiz.setOnApplyWindowInsetsListener { _, ins ->
            val (t, b) = if (Build.VERSION.SDK_INT >= 30) {
                val i = ins.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                i.top to i.bottom
            } else {
                @Suppress("DEPRECATION") ins.systemWindowInsetTop to ins.systemWindowInsetBottom
            }
            arriba.setPadding(arriba.paddingLeft, t, arriba.paddingRight, arriba.paddingBottom)
            abajo.setPadding(abajo.paddingLeft, abajo.paddingTop, abajo.paddingRight, b)
            ins
        }
    }
}

fun LinearLayout.centrado() = apply { gravity = Gravity.CENTER }
