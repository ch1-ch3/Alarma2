package com.alarma2.app

import android.app.Activity
import android.content.Context
import android.os.CancellationSignal
import android.os.Handler
import android.os.HandlerThread
import android.os.UserManager
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.Executors

/**
 * Todo lo que va por internet: cuenta, amigos y alarmas compartidas (Supabase).
 * Nada de esto participa en hacer sonar la alarma: si falla o no hay internet, la alarma suena igual.
 * Las funciones de red bloquean: llamarlas fuera del hilo principal (ver [enSegundoPlano]).
 */
object Nube {
    // Datos públicos por diseño (van dentro de la app). Las reglas del servicio protegen los datos.
    private const val SERVIDOR = "https://rfyrrgfofqauygvgjumq.supabase.co"
    private const val CLAVE = "sb_publishable_RCLH_S_erX8S_HCRqOrXfQ_2pdVYgjb"
    private const val CLIENTE_WEB_GOOGLE = "803783634555-2gq3suaii75l33lqucg59o2mrdci9o5o.apps.googleusercontent.com"
    private const val TAG = "Alarma2"

    class Falla(val codigo: Int, val detalle: String) : Exception("$codigo $detalle")

    data class Perfil(val id: String, val usuario: String, val nombre: String)
    data class Amistad(val otro: Perfil, val aceptada: Boolean, val pedidaPorMi: Boolean)
    data class AlarmaAmigo(val dueno: Perfil, val hora: Int, val minuto: Int, val dias: Int, val activa: Boolean, val proximo: Long?) {
        /** Próximo disparo: el que informó el dueño, o calculado si ese ya pasó. */
        fun proximoDisparo(ahora: Long = System.currentTimeMillis()): Long =
            proximo?.takeIf { it > ahora } ?: Alarma(0, hora, minuto, dias, 0).proximoDisparo(ahora)
    }

    private val hilos = Executors.newSingleThreadExecutor()
    fun enSegundoPlano(trabajo: () -> Unit) = hilos.execute {
        try { trabajo() } catch (e: Throwable) { Log.w(TAG, "Nube: ${e.message}") }
    }

    // ---------- Sesión ----------

    private fun prefs(ctx: Context) = ctx.applicationContext.getSharedPreferences("cuenta", Context.MODE_PRIVATE)
    private fun desbloqueado(ctx: Context) = ctx.getSystemService(UserManager::class.java)?.isUserUnlocked ?: false

    fun conCuenta(ctx: Context) = desbloqueado(ctx) && prefs(ctx).getString("refresh", null) != null
    fun yo(ctx: Context): String = prefs(ctx).getString("uid", null) ?: throw Falla(401, "sin cuenta")
    fun nombreGoogle(ctx: Context) = prefs(ctx).getString("nombreGoogle", "") ?: ""

    /** Perfil propio guardado en el celular (para no esperar a internet al dibujar). */
    fun perfilGuardado(ctx: Context): Perfil? {
        val p = prefs(ctx)
        val u = p.getString("usuario", null) ?: return null
        return Perfil(p.getString("uid", "") ?: "", u, p.getString("nombre", "") ?: "")
    }

    private fun guardarSesion(ctx: Context, j: JSONObject) {
        prefs(ctx).edit()
            .putString("acceso", j.getString("access_token"))
            .putString("refresh", j.getString("refresh_token"))
            .putLong("vence", System.currentTimeMillis() + j.optLong("expires_in", 3600) * 1000)
            .putString("uid", j.getJSONObject("user").getString("id"))
            .apply()
    }

    @Synchronized
    private fun token(ctx: Context): String {
        val p = prefs(ctx)
        if (System.currentTimeMillis() > p.getLong("vence", 0) - 60_000) refrescar(ctx)
        return p.getString("acceso", null) ?: throw Falla(401, "sin cuenta")
    }

    @Synchronized
    private fun refrescar(ctx: Context) {
        val r = prefs(ctx).getString("refresh", null) ?: throw Falla(401, "sin cuenta")
        val (c, t) = http("POST", "/auth/v1/token?grant_type=refresh_token", JSONObject().put("refresh_token", r).toString(), null)
        if (c in 400..499) { olvidarSesion(ctx); throw Falla(401, "la sesión venció") }
        if (c >= 300) throw Falla(c, t)
        guardarSesion(ctx, JSONObject(t))
    }

    private fun olvidarSesion(ctx: Context) = prefs(ctx).edit().clear().apply()

    /** Abre "Entrar con Google" y, si sale bien, abre la sesión en el servicio. Avisa en el hilo principal. */
    fun entrarConGoogle(act: Activity, alTerminar: (String?) -> Unit) {
        val crudo = UUID.randomUUID().toString()
        val cifrado = MessageDigest.getInstance("SHA-256").digest(crudo.toByteArray()).joinToString("") { "%02x".format(it) }
        val opcion = GetSignInWithGoogleOption.Builder(CLIENTE_WEB_GOOGLE).setNonce(cifrado).build()
        val pedido = GetCredentialRequest.Builder().addCredentialOption(opcion).build()
        CredentialManager.create(act).getCredentialAsync(act, pedido, CancellationSignal(), act.mainExecutor,
            object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                override fun onResult(result: GetCredentialResponse) {
                    val cred = result.credential
                    if (cred !is CustomCredential || cred.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        alTerminar("Google devolvió algo inesperado."); return
                    }
                    val google = GoogleIdTokenCredential.createFrom(cred.data)
                    enSegundoPlano {
                        val error = try {
                            val cuerpo = JSONObject().put("provider", "google").put("id_token", google.idToken).put("nonce", crudo)
                            val (c, t) = http("POST", "/auth/v1/token?grant_type=id_token", cuerpo.toString(), null)
                            if (c >= 300) throw Falla(c, t)
                            guardarSesion(act, JSONObject(t))
                            prefs(act).edit().putString("nombreGoogle", google.displayName ?: "").apply()
                            miPerfil(act)
                            Log.i(TAG, "Entró con Google: ${yo(act)}")
                            null
                        } catch (e: Exception) {
                            Log.w(TAG, "No pudo entrar", e); "No se pudo entrar. ¿Hay internet?"
                        }
                        act.runOnUiThread { alTerminar(error) }
                    }
                }

                override fun onError(e: GetCredentialException) {
                    Log.w(TAG, "Entrar con Google: ${e.type} ${e.message}")
                    alTerminar(if (e is GetCredentialCancellationException) null else "Google no dejó entrar: ${e.message}")
                }
            })
    }

    fun salir(ctx: Context) {
        try { http("POST", "/auth/v1/logout", "{}", token(ctx)) } catch (_: Exception) {}
        olvidarSesion(ctx)
        Log.i(TAG, "Salió de la cuenta")
    }

    // ---------- Pedidos ----------

    private fun http(metodo: String, ruta: String, cuerpo: String?, token: String?, extra: Map<String, String> = emptyMap()): Pair<Int, String> {
        val c = URL(SERVIDOR + ruta).openConnection() as HttpURLConnection
        try {
            c.requestMethod = metodo
            c.connectTimeout = 10_000
            c.readTimeout = 15_000
            c.setRequestProperty("apikey", CLAVE)
            c.setRequestProperty("Content-Type", "application/json")
            if (token != null) c.setRequestProperty("Authorization", "Bearer $token")
            extra.forEach { (k, v) -> c.setRequestProperty(k, v) }
            if (cuerpo != null) { c.doOutput = true; c.outputStream.use { it.write(cuerpo.toByteArray()) } }
            val codigo = c.responseCode
            val txt = (if (codigo < 400) c.inputStream else c.errorStream)?.bufferedReader()?.use { it.readText() } ?: ""
            return codigo to txt
        } finally { c.disconnect() }
    }

    private fun rest(ctx: Context, metodo: String, ruta: String, cuerpo: String? = null, extra: Map<String, String> = emptyMap()): String {
        var (c, t) = http(metodo, "/rest/v1/$ruta", cuerpo, token(ctx), extra)
        if (c == 401) { refrescar(ctx); val r = http(metodo, "/rest/v1/$ruta", cuerpo, token(ctx), extra); c = r.first; t = r.second }
        if (c >= 300) throw Falla(c, t)
        return t
    }

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
    private fun perfil(o: JSONObject) = Perfil(o.getString("id"), o.getString("usuario"), o.getString("nombre"))

    // ---------- Perfil ----------

    /** Trae el perfil propio (null si todavía no eligió su @ID) y lo guarda en el celular. */
    fun miPerfil(ctx: Context): Perfil? {
        val arr = JSONArray(rest(ctx, "GET", "perfiles?id=eq.${yo(ctx)}&select=id,usuario,nombre"))
        val p = if (arr.length() > 0) perfil(arr.getJSONObject(0)) else null
        prefs(ctx).edit().putString("usuario", p?.usuario).putString("nombre", p?.nombre).apply()
        return p
    }

    /** Crea el perfil. Devuelve un texto de error o null si salió bien. */
    fun crearPerfil(ctx: Context, usuario: String, nombre: String): String? {
        return try {
            rest(ctx, "POST", "perfiles", JSONObject().put("id", yo(ctx)).put("usuario", usuario).put("nombre", nombre).toString())
            miPerfil(ctx)
            Log.i(TAG, "Perfil creado: @$usuario")
            null
        } catch (e: Falla) {
            if (e.detalle.contains("23505")) "Ese @ID ya lo tiene otra persona. Probá con otro." else "No se pudo guardar (${e.codigo})."
        }
    }

    fun buscar(ctx: Context, usuario: String): Perfil? {
        val arr = JSONArray(rest(ctx, "GET", "perfiles?usuario=eq.${enc(usuario)}&select=id,usuario,nombre"))
        return if (arr.length() > 0) perfil(arr.getJSONObject(0)) else null
    }

    // ---------- Amigos ----------

    fun amistades(ctx: Context): List<Amistad> {
        val me = yo(ctx)
        val arr = JSONArray(rest(ctx, "GET",
            "amistades?select=de,para,estado,pd:perfiles!amistades_de_fkey(id,usuario,nombre),pp:perfiles!amistades_para_fkey(id,usuario,nombre)&order=creado"))
        return List(arr.length()) {
            val o = arr.getJSONObject(it)
            val mia = o.getString("de") == me
            Amistad(perfil(o.getJSONObject(if (mia) "pp" else "pd")), o.getString("estado") == "aceptada", mia)
        }
    }

    fun pedirAmistad(ctx: Context, otro: String) {
        rest(ctx, "POST", "amistades", JSONObject().put("de", yo(ctx)).put("para", otro).toString())
        Log.i(TAG, "Solicitud enviada a $otro")
    }

    fun aceptar(ctx: Context, otro: String) {
        rest(ctx, "POST", "rpc/aceptar_amistad", JSONObject().put("otro", otro).toString())
        Log.i(TAG, "Solicitud de $otro aceptada")
    }

    /** Rechazar, cancelar o sacar a un amigo: borra la fila del par. */
    fun borrarAmistad(ctx: Context, otro: String) {
        val me = yo(ctx)
        rest(ctx, "DELETE", "amistades?or=(and(de.eq.$me,para.eq.$otro),and(de.eq.$otro,para.eq.$me))")
        Log.i(TAG, "Amistad con $otro borrada")
    }

    // ---------- Alarmas ----------

    /** Sube la copia de mis alarmas y borra las que ya no están. */
    fun subirAlarmas(ctx: Context, lista: List<Alarma>) {
        val me = yo(ctx)
        val ahora = System.currentTimeMillis()
        if (lista.isNotEmpty()) {
            val arr = JSONArray()
            for (a in lista) arr.put(JSONObject()
                .put("dueno", me).put("local_id", a.id).put("hora", a.hora).put("minuto", a.minuto)
                .put("dias", a.dias).put("activa", a.activa)
                .put("proximo", if (a.activa) Instant.ofEpochMilli(a.proximoDisparo(ahora)).toString() else JSONObject.NULL)
                .put("actualizado", Instant.ofEpochMilli(ahora).toString()))
            rest(ctx, "POST", "alarmas?on_conflict=dueno,local_id", arr.toString(),
                mapOf("Prefer" to "resolution=merge-duplicates,return=minimal"))
        }
        val quedan = if (lista.isEmpty()) "" else "&local_id=not.in.(${lista.joinToString(",") { it.id.toString() }})"
        rest(ctx, "DELETE", "alarmas?dueno=eq.$me$quedan")
        Log.i(TAG, "Subí ${lista.size} alarmas")
    }

    /** Alarmas de mis amigos. Las reglas del servicio ya filtran: solo llegan las de amigos aceptados. */
    fun alarmasDeAmigos(ctx: Context): List<AlarmaAmigo> {
        val arr = JSONArray(rest(ctx, "GET", "alarmas?dueno=neq.${yo(ctx)}&select=hora,minuto,dias,activa,proximo,perfiles(id,usuario,nombre)"))
        return List(arr.length()) {
            val o = arr.getJSONObject(it)
            val prox = if (o.isNull("proximo")) null else OffsetDateTime.parse(o.getString("proximo")).toInstant().toEpochMilli()
            AlarmaAmigo(perfil(o.getJSONObject("perfiles")), o.getInt("hora"), o.getInt("minuto"), o.getInt("dias"), o.getBoolean("activa"), prox)
        }
    }
}

/**
 * Sube las alarmas un ratito después de cada cambio, en otro hilo.
 * Si no hay cuenta, internet o el celular sigue bloqueado tras reiniciar, no hace nada.
 */
object Sincro {
    private val hilo = HandlerThread("sincro").apply { start() }
    private val manejador = Handler(hilo.looper)
    private var app: Context? = null
    private val tarea = Runnable {
        val ctx = app ?: return@Runnable
        try {
            if (Nube.conCuenta(ctx) && Nube.perfilGuardado(ctx) != null) Nube.subirAlarmas(ctx, Almacen.todas(ctx))
        } catch (e: Throwable) {
            Log.w("Alarma2", "No se pudieron subir las alarmas: ${e.message}")
        }
    }

    fun avisar(ctx: Context) {
        try {
            app = ctx.applicationContext
            manejador.removeCallbacks(tarea)
            manejador.postDelayed(tarea, 1500)
        } catch (_: Throwable) {}
    }
}
