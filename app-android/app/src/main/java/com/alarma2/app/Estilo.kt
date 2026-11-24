package com.alarma2.app

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Estilo de la app (ver diseno/ESTILO.md): colores, letras y piezas reutilizables.
 * Todo se arma en código para no depender de librerías.
 */
object Estilo {
    // Colores
    val FONDO = Color.parseColor("#15291D")          // verde bosque oscuro
    val FONDO_ALTO = Color.parseColor("#1F3A29")     // tarjetas sobre el fondo
    val BORDE_FONDO = Color.parseColor("#2E4D38")
    val MOSTAZA = Color.parseColor("#F4B731")        // lo principal
    val MOSTAZA_SOMBRA = Color.parseColor("#B07D12")
    val CAFE = Color.parseColor("#4A2C1C")           // secundario, bordes
    val CAFE_SOMBRA = Color.parseColor("#2A170D")
    val LINO = Color.parseColor("#F4EFE6")           // tarjetas claras
    val LINO_APAGADO = Color.parseColor("#D9D2C5")
    val LINO_SOMBRA = Color.parseColor("#3B2416")
    val TINTA = Color.parseColor("#23160E")          // texto sobre lino y mostaza
    val TINTA_SUAVE = Color.parseColor("#5C4A3D")
    val TEXTO = Color.parseColor("#F4EFE6")          // texto sobre el fondo
    val TEXTO_SUAVE = Color.parseColor("#BAC8B3")
    val VERDE_OK = Color.parseColor("#1E6B3A")
    val VERDE_OK_FONDO = Color.parseColor("#D3EBD5")
    val PICANTE = Color.parseColor("#B23A0E")        // naranja-rojo de "algo pasa"
    val CAJA_LINO = Color.parseColor("#E8E1D4")      // recuadro dentro de la tarjeta

    private var titulos: Typeface? = null
    private var titulosExtra: Typeface? = null
    private var cuerpo: Typeface? = null
    private var cuerpoFuerte: Typeface? = null

    private fun fuente(ctx: Context, archivo: String, peso: Int): Typeface =
        try {
            Typeface.Builder(ctx.assets, "fonts/$archivo")
                .setFontVariationSettings("'wght' $peso").build()
        } catch (e: Exception) { Typeface.DEFAULT_BOLD }

    fun titulo(ctx: Context) = titulos ?: fuente(ctx, "epilogue.ttf", 700).also { titulos = it }
    fun tituloExtra(ctx: Context) = titulosExtra ?: fuente(ctx, "epilogue.ttf", 800).also { titulosExtra = it }
    fun texto(ctx: Context) = cuerpo ?: fuente(ctx, "jakarta.ttf", 500).also { cuerpo = it }
    fun textoFuerte(ctx: Context) = cuerpoFuerte ?: fuente(ctx, "jakarta.ttf", 700).also { cuerpoFuerte = it }

    fun dp(ctx: Context, v: Int) = (v * ctx.resources.displayMetrics.density).toInt()
    fun dpf(ctx: Context, v: Float) = v * ctx.resources.displayMetrics.density

    fun redondo(color: Int, radio: Float, borde: Int = 0, colorBorde: Int = 0) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radio
        if (borde > 0) setStroke(borde, colorBorde)
    }

    /** Pieza con "sombra sólida" abajo: se ve como algo físico que se puede apretar. */
    fun conRelieve(ctx: Context, cara: Int, sombra: Int, radio: Float, alto: Int, borde: Int = 0, colorBorde: Int = 0, hundido: Boolean = false): Drawable {
        val capas = LayerDrawable(arrayOf(redondo(sombra, radio), redondo(cara, radio, borde, colorBorde)))
        val arriba = if (hundido) alto - dp(ctx, 2) else 0
        val abajo = if (hundido) dp(ctx, 2) else alto
        capas.setLayerInset(0, 0, arriba, 0, 0)
        capas.setLayerInset(1, 0, arriba, 0, abajo)
        return capas
    }

    fun etiqueta(ctx: Context, txt: String, tam: Float = 16f, color: Int = TEXTO, fuerte: Boolean = false) =
        TextView(ctx).apply {
            text = txt
            textSize = tam
            setTextColor(color)
            typeface = if (fuerte) textoFuerte(ctx) else texto(ctx)
            setLineSpacing(0f, 1.15f)
        }

    fun tituloTexto(ctx: Context, txt: String, tam: Float = 26f, color: Int = TEXTO, extra: Boolean = false) =
        TextView(ctx).apply {
            text = txt
            textSize = tam
            setTextColor(color)
            typeface = if (extra) tituloExtra(ctx) else titulo(ctx)
            letterSpacing = -0.01f
            includeFontPadding = false
        }

    /** Ícono liso (Material Symbols) pintado del color pedido. */
    fun icono(ctx: Context, res: Int, color: Int, tam: Int = 24) = ImageView(ctx).apply {
        setImageResource(res)
        setColorFilter(color)
        layoutParams = LinearLayout.LayoutParams(dp(ctx, tam), dp(ctx, tam))
    }

    /** Pone un ícono a la izquierda del texto, del mismo color que el texto. */
    fun <T : TextView> conIcono(tv: T, res: Int, tam: Int = 20, color: Int = tv.currentTextColor): T {
        val d = tv.context.getDrawable(res)!!.mutate()
        d.setTint(color)
        d.setBounds(0, 0, dp(tv.context, tam), dp(tv.context, tam))
        tv.setCompoundDrawablesRelative(d, null, null, null)
        tv.compoundDrawablePadding = dp(tv.context, 8)
        return tv
    }

    /** Chip: pastilla chica con texto. */
    fun chip(ctx: Context, txt: String, fondo: Int, color: Int, borde: Int = 0, icono: Int = 0) = TextView(ctx).apply {
        text = txt
        textSize = 14f
        setTextColor(color)
        typeface = textoFuerte(ctx)
        maxLines = 1
        background = redondo(fondo, dpf(ctx, 999f), if (borde != 0) dp(ctx, 2) else 0, borde)
        setPadding(dp(ctx, 12), dp(ctx, 5), dp(ctx, 12), dp(ctx, 5))
        gravity = Gravity.CENTER_VERTICAL
        if (icono != 0) conIcono(this, icono, 16)
    }

    /** Tarjeta clara (lino) con relieve café, como las de alarma. */
    fun tarjeta(ctx: Context, apagada: Boolean = false) = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        val p = dp(ctx, 20)
        setPadding(p, p, p, p + dp(ctx, 6))
        background = conRelieve(ctx, if (apagada) LINO_APAGADO else LINO, LINO_SOMBRA, dpf(ctx, 28f), dp(ctx, 6))
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            .apply { setMargins(0, 0, 0, dp(ctx, 16)) }
    }

    /** Tarjeta oscura (sobre el fondo verde), para las alarmas de los amigos. */
    fun tarjetaOscura(ctx: Context) = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        val p = dp(ctx, 18)
        setPadding(p, p, p, p + dp(ctx, 4))
        background = conRelieve(ctx, FONDO_ALTO, Color.parseColor("#0C1810"), dpf(ctx, 28f), dp(ctx, 4), dp(ctx, 2), BORDE_FONDO)
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            .apply { setMargins(0, 0, 0, dp(ctx, 16)) }
    }

    enum class TipoBoton { PRINCIPAL, SECUNDARIO, SUAVE }

    /** Botón grueso que se hunde al tocarlo. */
    fun boton(ctx: Context, txt: String, tipo: TipoBoton = TipoBoton.PRINCIPAL, tam: Float = 18f, icono: Int = 0, alClick: () -> Unit) =
        BotonApretable(ctx, tipo).apply {
            textSize = tam
            text = if (icono == 0) txt else {
                // El ícono va dentro del texto para quedar pegado a la palabra, centrado.
                isAllCaps = false
                val d = ctx.getDrawable(icono)!!.mutate()
                d.setTint(currentTextColor)
                val t = dp(ctx, (tam * 1.3f).toInt())
                d.setBounds(0, 0, t, t)
                android.text.SpannableString("  " + txt.uppercase()).apply {
                    setSpan(android.text.style.ImageSpan(d, android.text.style.DynamicDrawableSpan.ALIGN_CENTER), 0, 1, 0)
                }
            }
            setOnClickListener { alClick() }
        }

    fun fila(ctx: Context) = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    fun espacio(ctx: Context, alto: Int) = View(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(ctx, alto))
    }

    fun ancho(peso: Float = 1f) = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, peso)
}

/** Botón con relieve: el texto baja junto con la cara cuando se aprieta. */
class BotonApretable(ctx: Context, tipo: Estilo.TipoBoton) : TextView(ctx) {
    private val alto = Estilo.dp(ctx, 6)
    private val base = Estilo.dp(ctx, 16)

    init {
        val (cara, sombra, color, borde) = when (tipo) {
            Estilo.TipoBoton.PRINCIPAL -> listOf(Estilo.MOSTAZA, Estilo.MOSTAZA_SOMBRA, Estilo.TINTA, 0)
            Estilo.TipoBoton.SECUNDARIO -> listOf(Estilo.CAFE, Estilo.CAFE_SOMBRA, Estilo.LINO, 0)
            Estilo.TipoBoton.SUAVE -> listOf(Estilo.FONDO_ALTO, Color.parseColor("#0C1810"), Estilo.TEXTO, Estilo.BORDE_FONDO)
        }
        val r = Estilo.dpf(ctx, 999f)
        val bordeAncho = if (borde != 0) Estilo.dp(ctx, 2) else 0
        background = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), Estilo.conRelieve(ctx, cara, sombra, r, alto, bordeAncho, borde, hundido = true))
            addState(intArrayOf(), Estilo.conRelieve(ctx, cara, sombra, r, alto, bordeAncho, borde))
        }
        setTextColor(color)
        typeface = Estilo.textoFuerte(ctx)
        letterSpacing = 0.03f
        gravity = Gravity.CENTER
        isAllCaps = true
        minHeight = Estilo.dp(ctx, 60)
        setPadding(Estilo.dp(ctx, 20), base, Estilo.dp(ctx, 20), base + alto)
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val d = if (isPressed) alto - Estilo.dp(context, 2) else 0
        setPadding(paddingLeft, base + d, paddingRight, base + alto - d)
    }
}
