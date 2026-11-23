package cz.klavir.app.data

import android.content.Context
import cz.klavir.jadro.Profil
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Ukládá profily žáků do SharedPreferences jako JSON.
 * Vše zůstává jen v tabletu, nic se neposílá na internet.
 */
class UlozisteProfilu(context: Context) {
    private val prefs = context.getSharedPreferences("profily", Context.MODE_PRIVATE)

    fun nacti(): List<Profil> {
        val json = prefs.getString("seznam", null) ?: return emptyList()
        return try {
            val pole = JSONArray(json)
            (0 until pole.length()).map { zJson(pole.getJSONObject(it)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun uloz(profily: List<Profil>) {
        val pole = JSONArray()
        profily.forEach { pole.put(doJson(it)) }
        prefs.edit()?.putString("seznam", pole.toString())?.apply()
    }

    var posledniProfil: String?
        get() = prefs.getString("posledni", null)
        set(v) {
            prefs.edit()?.putString("posledni", v)?.apply()
        }

    fun novy(jmeno: String, zvire: String) = Profil(UUID.randomUUID().toString(), jmeno.trim(), zvire)

    private fun doJson(p: Profil) = JSONObject().apply {
        put("id", p.id)
        put("jmeno", p.jmeno)
        put("zvire", p.zvire)
        put("body", p.body)
        put("hvezdyLekci", JSONObject(p.hvezdyLekci as Map<*, *>))
        put("hvezdyPisnicek", JSONObject(p.hvezdyPisnicek as Map<*, *>))
        put("nejdelsiSerie", p.nejdelsiSerie)
        put("spravnychNot", p.spravnychNot)
        put("notZMikrofonu", p.notZMikrofonu)
        put("pisnickyBezChyby", JSONArray(p.pisnickyBezChyby.toList()))
        put("odznaky", JSONArray(p.odznaky.toList()))
        put("dnuVRade", p.dnuVRade)
        put("posledniDen", p.posledniDen)
        put("predcitatAutomaticky", p.predcitatAutomaticky)
    }

    private fun zJson(o: JSONObject): Profil {
        fun mapa(klic: String): Map<String, Int> {
            val m = o.optJSONObject(klic) ?: return emptyMap()
            return m.keys().asSequence().associateWith { m.getInt(it) }
        }
        fun mnozina(klic: String): Set<String> {
            val a = o.optJSONArray(klic) ?: return emptySet()
            return (0 until a.length()).map { a.getString(it) }.toSet()
        }
        return Profil(
            id = o.getString("id"),
            jmeno = o.getString("jmeno"),
            zvire = o.optString("zvire", "🦊"),
            body = o.optInt("body"),
            hvezdyLekci = mapa("hvezdyLekci"),
            hvezdyPisnicek = mapa("hvezdyPisnicek"),
            nejdelsiSerie = o.optInt("nejdelsiSerie"),
            spravnychNot = o.optInt("spravnychNot"),
            notZMikrofonu = o.optInt("notZMikrofonu"),
            pisnickyBezChyby = mnozina("pisnickyBezChyby"),
            odznaky = mnozina("odznaky"),
            dnuVRade = o.optInt("dnuVRade"),
            posledniDen = o.optLong("posledniDen", -1),
            predcitatAutomaticky = o.optBoolean("predcitatAutomaticky", false),
        )
    }
}
