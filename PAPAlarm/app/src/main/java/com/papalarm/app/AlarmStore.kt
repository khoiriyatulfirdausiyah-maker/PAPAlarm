package com.papalarm.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object AlarmStore {
    private const val PREF = "papalarm_prefs"
    private const val KEY = "alarms"

    fun load(context: Context): MutableList<AlarmItem> {
        val raw = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY, "[]") ?: "[]"
        val out = mutableListOf<AlarmItem>()
        runCatching {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val d = o.optJSONArray("days") ?: JSONArray()
                val days = mutableSetOf<Int>()
                for (j in 0 until d.length()) days.add(d.getInt(j))
                out.add(AlarmItem(
                    id = o.getInt("id"),
                    hour = o.getInt("hour"),
                    minute = o.getInt("minute"),
                    label = o.optString("label", "Rutinitas"),
                    category = o.optString("category", "Lainnya"),
                    days = days,
                    enabled = o.optBoolean("enabled", true),
                    audioUri = o.optString("audioUri").takeIf { it.isNotBlank() && it != "null" },
                    vibrate = o.optBoolean("vibrate", true)
                ))
            }
        }
        return out
    }

    fun save(context: Context, alarms: List<AlarmItem>) {
        val arr = JSONArray()
        alarms.forEach { a ->
            arr.put(JSONObject().apply {
                put("id", a.id); put("hour", a.hour); put("minute", a.minute)
                put("label", a.label); put("category", a.category); put("enabled", a.enabled)
                put("audioUri", a.audioUri ?: JSONObject.NULL); put("vibrate", a.vibrate)
                put("days", JSONArray().apply { a.days.sorted().forEach { put(it) } })
            })
        }
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply()
    }

    fun get(context: Context, id: Int) = load(context).firstOrNull { it.id == id }

    fun updateEnabled(context: Context, id: Int, enabled: Boolean) {
        val list = load(context)
        list.firstOrNull { it.id == id }?.enabled = enabled
        save(context, list)
    }
}
