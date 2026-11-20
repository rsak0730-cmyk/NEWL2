package com.manish.liquidlauncher.data

import android.content.Context
import com.manish.liquidlauncher.model.AppPair
import com.manish.liquidlauncher.model.CustomFolder
import com.manish.liquidlauncher.model.DockStyle
import com.manish.liquidlauncher.model.EdgePattern
import com.manish.liquidlauncher.model.FontChoice
import com.manish.liquidlauncher.model.GestureAction
import com.manish.liquidlauncher.model.GestureMap
import com.manish.liquidlauncher.model.GestureSlot
import com.manish.liquidlauncher.model.IconShapeStyle
import com.manish.liquidlauncher.model.LaunchEvent
import com.manish.liquidlauncher.model.LiquidThemeConfig
import com.manish.liquidlauncher.model.SoundTheme
import com.manish.liquidlauncher.model.ThemeMode
import com.manish.liquidlauncher.model.WallpaperTheme
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

/** SharedPreferences wrapper: theme, wallpaper, dock, hidden apps, folders, focus, usage, backup. */
class LauncherPrefs(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("liquid_launcher_prefs", Context.MODE_PRIVATE)

    fun loadTheme(): LiquidThemeConfig {
        val d = LiquidThemeConfig()
        val shapes = IconShapeStyle.values()
        return LiquidThemeConfig(
            accentHue = sp.getFloat("hue", d.accentHue),
            secondaryHue = sp.getFloat("hue2", d.secondaryHue),
            refractionOpacity = sp.getFloat("density", d.refractionOpacity),
            glowIntensity = sp.getFloat("glow", d.glowIntensity),
            cornerRadiusDp = sp.getFloat("radius", d.cornerRadiusDp),
            iconScale = sp.getFloat("iconScale", d.iconScale),
            liveAnimations = sp.getBoolean("live", d.liveAnimations),
            bubbles = sp.getBoolean("bubbles", d.bubbles),
            iconBob = sp.getBoolean("bob", d.iconBob),
            tiltEffects = sp.getBoolean("tilt", d.tiltEffects),
            showLabels = sp.getBoolean("labels", d.showLabels),
            haptics = sp.getBoolean("haptics", d.haptics),
            hapticsStrength = sp.getFloat("hapticsStrength", d.hapticsStrength).coerceIn(0.5f, 1.5f),
            blurDrawer = sp.getBoolean("blur", d.blurDrawer),
            iconShape = shapes.getOrElse(sp.getInt("shape", d.iconShape.ordinal)) { d.iconShape },
            sounds = sp.getBoolean("sounds", d.sounds),
            soundTheme = SoundTheme.values().getOrElse(sp.getInt("soundTheme", d.soundTheme.ordinal)) { d.soundTheme },
            badges = sp.getBoolean("badges", d.badges),
            themeMode = ThemeMode.values().getOrElse(sp.getInt("themeMode", d.themeMode.ordinal)) { d.themeMode },
            dockStyle = DockStyle.values().getOrElse(sp.getInt("dockStyle", d.dockStyle.ordinal)) { d.dockStyle },
            gridColumns = sp.getInt("gridColumns", d.gridColumns).coerceIn(3, 5),
            gridRows = sp.getInt("gridRows", d.gridRows).coerceIn(3, 7),
            oneHanded = sp.getBoolean("oneHanded", d.oneHanded),
            activityIsland = sp.getBoolean("island", d.activityIsland),
            edgeLighting = sp.getBoolean("edgeLight", d.edgeLighting),
            edgeLightingPattern = EdgePattern.values().getOrElse(sp.getInt("edgePattern", d.edgeLightingPattern.ordinal)) { d.edgeLightingPattern },
            gestureTrail = sp.getBoolean("trail", d.gestureTrail),
            floatingBubbles = sp.getBoolean("bubble", d.floatingBubbles),
            wallpaperTheme = WallpaperTheme.values().getOrElse(sp.getInt("wallTheme", d.wallpaperTheme.ordinal)) { d.wallpaperTheme },
            focusScheduleEnabled = sp.getBoolean("focusSched", d.focusScheduleEnabled),
            focusStartHour = sp.getInt("focusStart", d.focusStartHour).coerceIn(0, 23),
            focusEndHour = sp.getInt("focusEnd", d.focusEndHour).coerceIn(0, 23),
            focusWeekdaysOnly = sp.getBoolean("focusWeekdays", d.focusWeekdaysOnly),
            calendarWidgetEnabled = sp.getBoolean("calWidget", d.calendarWidgetEnabled),
            airQualityWidgetEnabled = sp.getBoolean("aqWidget", d.airQualityWidgetEnabled)
        )
    }

    fun saveTheme(c: LiquidThemeConfig) {
        sp.edit()
            .putFloat("hue", c.accentHue)
            .putFloat("hue2", c.secondaryHue)
            .putFloat("density", c.refractionOpacity)
            .putFloat("glow", c.glowIntensity)
            .putFloat("radius", c.cornerRadiusDp)
            .putFloat("iconScale", c.iconScale)
            .putBoolean("live", c.liveAnimations)
            .putBoolean("bubbles", c.bubbles)
            .putBoolean("bob", c.iconBob)
            .putBoolean("tilt", c.tiltEffects)
            .putBoolean("labels", c.showLabels)
            .putBoolean("haptics", c.haptics)
            .putFloat("hapticsStrength", c.hapticsStrength)
            .putBoolean("blur", c.blurDrawer)
            .putInt("shape", c.iconShape.ordinal)
            .putBoolean("sounds", c.sounds)
            .putInt("soundTheme", c.soundTheme.ordinal)
            .putBoolean("badges", c.badges)
            .putInt("themeMode", c.themeMode.ordinal)
            .putInt("dockStyle", c.dockStyle.ordinal)
            .putInt("gridColumns", c.gridColumns)
            .putInt("gridRows", c.gridRows)
            .putBoolean("oneHanded", c.oneHanded)
            .putBoolean("island", c.activityIsland)
            .putBoolean("edgeLight", c.edgeLighting)
            .putInt("edgePattern", c.edgeLightingPattern.ordinal)
            .putBoolean("trail", c.gestureTrail)
            .putBoolean("bubble", c.floatingBubbles)
            .putInt("wallTheme", c.wallpaperTheme.ordinal)
            .putBoolean("focusSched", c.focusScheduleEnabled)
            .putInt("focusStart", c.focusStartHour)
            .putInt("focusEnd", c.focusEndHour)
            .putBoolean("focusWeekdays", c.focusWeekdaysOnly)
            .putBoolean("calWidget", c.calendarWidgetEnabled)
            .putBoolean("aqWidget", c.airQualityWidgetEnabled)
            .apply()
    }

    var wallpaperUri: String?
        get() = sp.getString("wallpaper", null)
        set(value) {
            sp.edit().putString("wallpaper", value).apply()
        }

    var focusMode: Boolean
        get() = sp.getBoolean("focus", false)
        set(value) {
            sp.edit().putBoolean("focus", value).apply()
        }

    var onboarded: Boolean
        get() = sp.getBoolean("onboarded", false)
        set(value) {
            sp.edit().putBoolean("onboarded", value).apply()
        }

    /** null = never customised (use smart defaults). */
    fun loadDock(): List<String>? {
        val raw = sp.getString("dock", null) ?: return null
        return raw.split(",").filter { it.isNotBlank() }
    }

    fun saveDock(packages: List<String>) {
        sp.edit().putString("dock", packages.joinToString(",")).apply()
    }

    fun loadHidden(): Set<String> = sp.getStringSet("hidden", emptySet())?.toSet() ?: emptySet()

    fun saveHidden(hidden: Set<String>) {
        sp.edit().putStringSet("hidden", HashSet(hidden)).apply()
    }

    fun loadDistracting(): Set<String> = sp.getStringSet("distracting", emptySet())?.toSet() ?: emptySet()

    fun saveDistracting(set: Set<String>) {
        sp.edit().putStringSet("distracting", HashSet(set)).apply()
    }

    // ---- Custom folders (stored as JSON) ---------------------------------------------------
    fun loadFolders(): List<CustomFolder> {
        val raw = sp.getString("folders", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val pk = o.getJSONArray("apps")
                CustomFolder(o.getString("name"), (0 until pk.length()).map { pk.getString(it) })
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveFolders(folders: List<CustomFolder>) {
        val arr = JSONArray()
        folders.forEach { f ->
            arr.put(JSONObject().put("name", f.name).put("apps", JSONArray(f.packages)))
        }
        sp.edit().putString("folders", arr.toString()).apply()
    }

    // ---- Usage (overall + time-of-day buckets for smart suggestions) -----------------------
    fun currentBucket(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) / 6

    fun launchCounts(): Map<String, Int> {
        val out = HashMap<String, Int>()
        for ((key, value) in sp.all) {
            if (key.startsWith("launch_") && value is Int) {
                out[key.removePrefix("launch_")] = value
            }
        }
        return out
    }

    fun bucketCounts(bucket: Int): Map<String, Int> {
        val prefix = "tb_${bucket}_"
        val out = HashMap<String, Int>()
        for ((key, value) in sp.all) {
            if (key.startsWith(prefix) && value is Int) {
                out[key.removePrefix(prefix)] = value
            }
        }
        return out
    }

    fun bumpLaunch(packageName: String) {
        val key = "launch_$packageName"
        val bucketKey = "tb_${currentBucket()}_$packageName"
        sp.edit()
            .putInt(key, sp.getInt(key, 0) + 1)
            .putInt(bucketKey, sp.getInt(bucketKey, 0) + 1)
            .apply()
    }

    // ---- Per-item type (font + language) ---------------------------------------------------
    fun loadTypeMap(): Map<String, FontChoice> {
        val raw = sp.getString("typeMap", null) ?: return emptyMap()
        return try {
            val arr = JSONArray(raw)
            val out = HashMap<String, FontChoice>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val target = o.optString("target")
                val id = o.optString("id")
                if (target.isBlank() || id.isBlank()) continue
                out[target] = FontChoice(
                    fontId = id,
                    familyName = o.optString("family", id),
                    languageTag = o.optString("lang", ""),
                    script = o.optString("script", "")
                )
            }
            out
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun saveTypeMap(map: Map<String, FontChoice>) {
        val arr = JSONArray()
        map.forEach { (target, choice) ->
            arr.put(
                JSONObject()
                    .put("target", target)
                    .put("id", choice.fontId)
                    .put("family", choice.familyName)
                    .put("lang", choice.languageTag)
                    .put("script", choice.script)
            )
        }
        sp.edit().putString("typeMap", arr.toString()).apply()
    }

    // ---- Home workspace order ---------------------------------------------------------------
    fun loadOrder(): List<String>? {
        val raw = sp.getString("order", null) ?: return null
        return raw.split("|").filter { it.isNotBlank() }
    }

    fun saveOrder(keys: List<String>) {
        sp.edit().putString("order", keys.joinToString("|")).apply()
    }

    // ---- Gestures --------------------------------------------------------------------------
    fun loadGestures(): GestureMap {
        val out = HashMap<GestureSlot, GestureAction>()
        for (slot in GestureSlot.values()) {
            val stored = sp.getInt("gesture_" + slot.name, -1)
            if (stored >= 0) {
                out[slot] = GestureAction.values().getOrElse(stored) { GestureAction.None }
            }
        }
        return GestureMap(out)
    }

    fun saveGestures(map: GestureMap) {
        val edit = sp.edit()
        for (slot in GestureSlot.values()) {
            val action = map.assignments[slot]
            if (action == null) edit.remove("gesture_" + slot.name)
            else edit.putInt("gesture_" + slot.name, action.ordinal)
        }
        edit.apply()
    }

    // ---- App pairs -------------------------------------------------------------------------
    fun loadPairs(): List<AppPair> {
        val raw = sp.getString("pairs", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val a = o.optString("a")
                val b = o.optString("b")
                if (a.isBlank() || b.isBlank()) null
                else AppPair(o.optString("name", "Pair"), a, b)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun savePairs(pairs: List<AppPair>) {
        val arr = JSONArray()
        pairs.forEach { p ->
            arr.put(JSONObject().put("name", p.name).put("a", p.first).put("b", p.second))
        }
        sp.edit().putString("pairs", arr.toString()).apply()
    }

    // ---- Private space ---------------------------------------------------------------------
    var privatePin: String?
        get() = sp.getString("privatePin", null)
        set(value) {
            sp.edit().putString("privatePin", value).apply()
        }

    // ---- Launch history (activity timeline) -------------------------------------------------
    fun loadHistory(): List<LaunchEvent> {
        val raw = sp.getString("history", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val pkg = o.optString("p")
                if (pkg.isBlank()) null else LaunchEvent(pkg, o.optLong("t", 0L))
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun pushHistory(packageName: String) {
        val list = ArrayList(loadHistory())
        list.add(0, LaunchEvent(packageName, System.currentTimeMillis()))
        val trimmed = list.take(60)
        val arr = JSONArray()
        trimmed.forEach { e -> arr.put(JSONObject().put("p", e.packageName).put("t", e.atMillis)) }
        sp.edit().putString("history", arr.toString()).apply()
    }

    // ---- Weather ----------------------------------------------------------------------------
    var weatherCity: String?
        get() = sp.getString("weatherCity", null)
        set(value) {
            sp.edit().putString("weatherCity", value).apply()
        }

    var weatherCache: String?
        get() = sp.getString("weatherCache", null)
        set(value) {
            sp.edit().putString("weatherCache", value).apply()
        }

    // ---- Countdown ---------------------------------------------------------------------------
    var countdownTarget: Long
        get() = sp.getLong("countdown", 0L)
        set(value) {
            sp.edit().putLong("countdown", value).apply()
        }

    var countdownLabel: String?
        get() = sp.getString("countdownLabel", null)
        set(value) {
            sp.edit().putString("countdownLabel", value).apply()
        }

    // ---- Backup / restore (everything except the wallpaper URI) ----------------------------
    fun exportJson(): String {
        val root = JSONObject()
        for ((k, v) in sp.all) {
            if (k == "wallpaper") continue
            val o = JSONObject()
            when (v) {
                is Boolean -> o.put("t", "b").put("v", v)
                is Int -> o.put("t", "i").put("v", v)
                is Float -> o.put("t", "f").put("v", v.toDouble())
                is String -> o.put("t", "s").put("v", v)
                is Set<*> -> o.put("t", "ss").put("v", JSONArray(v.map { it.toString() }))
                else -> continue
            }
            root.put(k, o)
        }
        return root.toString()
    }

    fun importJson(json: String): Boolean {
        return try {
            val root = JSONObject(json)
            val edit = sp.edit()
            val keys = root.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val o = root.getJSONObject(k)
                when (o.getString("t")) {
                    "b" -> edit.putBoolean(k, o.getBoolean("v"))
                    "i" -> edit.putInt(k, o.getInt("v"))
                    "f" -> edit.putFloat(k, o.getDouble("v").toFloat())
                    "s" -> edit.putString(k, o.getString("v"))
                    "ss" -> {
                        val arr = o.getJSONArray("v")
                        edit.putStringSet(k, (0 until arr.length()).map { arr.getString(it) }.toSet())
                    }
                }
            }
            edit.apply()
            true
        } catch (_: Exception) {
            false
        }
    }
}
