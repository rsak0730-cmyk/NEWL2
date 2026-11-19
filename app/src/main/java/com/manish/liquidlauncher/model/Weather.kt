package com.manish.liquidlauncher.model

import androidx.compose.runtime.Immutable
import org.json.JSONObject

/** Current conditions from Open-Meteo (free, keyless). */
@Immutable
data class WeatherInfo(
    val city: String,
    val temperatureC: Float,
    val code: Int,
    val isDay: Boolean,
    val updatedAt: Long
) {
    /** Short human label for the WMO weather code. */
    val label: String
        get() = when (code) {
            0 -> "Clear"
            1 -> "Mostly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45, 48 -> "Fog"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing drizzle"
            61, 63, 65 -> "Rain"
            66, 67 -> "Freezing rain"
            71, 73, 75 -> "Snow"
            77 -> "Snow grains"
            80, 81, 82 -> "Showers"
            85, 86 -> "Snow showers"
            95 -> "Thunderstorm"
            96, 99 -> "Storm with hail"
            else -> "—"
        }

    fun toJson(): String = JSONObject()
        .put("city", city)
        .put("t", temperatureC.toDouble())
        .put("code", code)
        .put("day", isDay)
        .put("at", updatedAt)
        .toString()

    companion object {
        fun fromJson(raw: String): WeatherInfo? = try {
            val o = JSONObject(raw)
            WeatherInfo(
                city = o.optString("city", ""),
                temperatureC = o.optDouble("t", 0.0).toFloat(),
                code = o.optInt("code", -1),
                isDay = o.optBoolean("day", true),
                updatedAt = o.optLong("at", 0L)
            )
        } catch (_: Throwable) {
            null
        }
    }
}
