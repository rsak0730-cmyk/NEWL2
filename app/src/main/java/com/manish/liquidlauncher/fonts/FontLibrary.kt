package com.manish.liquidlauncher.fonts

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily
import com.manish.liquidlauncher.model.FontChoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Outcome of a font install, with a message the UI can show as-is. */
data class DownloadResult(val ok: Boolean, val message: String?)

/**
 * Downloads and caches fonts.
 *
 * Fonts come from the public google/fonts repository (the same files Google Fonts serves),
 * so any family that Google publishes — including every language subset — can be installed.
 * Files are stored in the launcher's private storage and loaded with [Typeface.createFromFile],
 * which means they keep working offline afterwards.
 */
object FontLibrary {

    private const val PREFS = "liquid_type_fonts"
    private const val KEY_INSTALLED = "installed"
    private const val DIR = "fonts"
    private const val USER_AGENT = "LiquidLauncher/3.1 (Android)"

    private var context: Context? = null
    private val familyCache = HashMap<String, FontFamily>()
    private var installed: List<InstalledFont> = emptyList()
    private var loaded = false

    fun init(ctx: Context) {
        if (loaded) return
        context = ctx.applicationContext
        installed = readInstalled()
        loaded = true
    }

    // ---- Catalogue queries ---------------------------------------------------------------

    /** System families plus every downloaded file. */
    fun installedFonts(): List<InstalledFont> {
        val custom = installed.sortedBy { it.family.lowercase() }
        return FontCatalog.system + custom
    }

    fun isInstalled(id: String): Boolean =
        id.startsWith("sys:") || installed.any { it.id == id }

    fun record(id: String): InstalledFont? = installed.firstOrNull { it.id == id }

    // ---- Resolution ----------------------------------------------------------------------

    fun familyFor(choice: FontChoice?): FontFamily {
        if (choice == null || choice.fontId.isBlank()) return FontFamily.SansSerif
        return familyForId(choice.fontId)
    }

    fun familyForId(id: String): FontFamily {
        familyCache[id]?.let { return it }
        val family: FontFamily = when {
            id == "sys:serif" -> FontFamily.Serif
            id == "sys:mono" -> FontFamily.Monospace
            id == "sys:cursive" -> FontFamily.Cursive
            id == "sys:condensed" -> safeTypeface("sans-serif-condensed")
            id.startsWith("dl:") -> {
                val rec = installed.firstOrNull { it.id == id }
                val ctx = context
                if (rec != null && ctx != null) {
                    val file = File(File(ctx.filesDir, DIR), rec.fileName)
                    if (file.exists()) {
                        try {
                            FontFamily(Typeface.createFromFile(file))
                        } catch (_: Throwable) {
                            FontFamily.SansSerif
                        }
                    } else {
                        FontFamily.SansSerif
                    }
                } else {
                    FontFamily.SansSerif
                }
            }
            else -> FontFamily.SansSerif
        }
        familyCache[id] = family
        return family
    }

    private fun safeTypeface(name: String): FontFamily = try {
        FontFamily(Typeface.create(name, Typeface.NORMAL))
    } catch (_: Throwable) {
        FontFamily.SansSerif
    }

    // ---- Install / remove ----------------------------------------------------------------

    suspend fun download(font: CatalogFont): DownloadResult = withContext(Dispatchers.IO) {
        val ctx = context ?: return@withContext DownloadResult(false, "Launcher not ready")
        try {
            val remote = findFile(font.slug)
                ?: return@withContext DownloadResult(false, "No font file found online for ${font.family}")
            val bytes = httpGet(remote.second, null)
                ?: return@withContext DownloadResult(false, "Download failed — check your connection")
            if (bytes.size < 2048) {
                return@withContext DownloadResult(false, "The downloaded file is not a usable font")
            }
            val dir = File(ctx.filesDir, DIR).apply { mkdirs() }
            val file = File(dir, safeName(font.id) + ".ttf")
            file.writeBytes(bytes)
            // Fail fast if the file cannot be parsed as a font.
            Typeface.createFromFile(file)
            val record = InstalledFont(font.id, font.family, font.script, font.languageTag, file.name)
            installed = installed.filterNot { it.id == font.id } + record
            writeInstalled(installed)
            familyCache.remove(font.id)
            DownloadResult(true, null)
        } catch (t: Throwable) {
            DownloadResult(false, t.message ?: "Could not install this font")
        }
    }

    fun delete(id: String): Boolean {
        val ctx = context ?: return false
        val rec = installed.firstOrNull { it.id == id } ?: return false
        try {
            File(File(ctx.filesDir, DIR), rec.fileName).delete()
        } catch (_: Throwable) {
        }
        installed = installed.filterNot { it.id == id }
        writeInstalled(installed)
        familyCache.remove(id)
        return true
    }

    // ---- Online search -------------------------------------------------------------------

    /**
     * Searches the full Google Fonts library. [languageTag] filters by language subset,
     * so the user can pick "any language available online" and only see fonts that can
     * actually render it.
     */
    suspend fun searchOnline(query: String, languageTag: String = ""): List<CatalogFont> =
        withContext(Dispatchers.IO) {
            val raw = httpGet(METADATA_URL, "application/json") ?: return@withContext emptyList()
            val text = raw.toString(Charsets.UTF_8)
            val start = text.indexOf('{')
            if (start < 0) return@withContext emptyList()
            val body = text.substring(start).trim()
            if (body.isEmpty()) return@withContext emptyList()
            try {
                val root = JSONObject(body)
                val list = root.optJSONArray("familyMetadataList") ?: return@withContext emptyList()
                val wantedSubset = Languages.byTag(languageTag)?.subset ?: ""
                val out = ArrayList<CatalogFont>(64)
                for (i in 0 until list.length()) {
                    if (out.size >= 120) break
                    val o = list.optJSONObject(i) ?: continue
                    val family = o.optString("family")
                    if (family.isBlank()) continue
                    if (query.isNotBlank() && !family.contains(query, ignoreCase = true)) continue
                    val subsets = o.optJSONArray("subsets")
                    val subsetList = if (subsets == null) emptyList() else
                        (0 until subsets.length()).map { subsets.optString(it) }
                    if (wantedSubset.isNotEmpty() && wantedSubset !in subsetList) continue
                    val script = scriptOf(subsetList)
                    val tag = languageTag.ifBlank {
                        Languages.bySubset(subsetList.firstOrNull { it != "latin" && it != "latin-ext" } ?: "latin")?.tag ?: "en"
                    }
                    out.add(
                        CatalogFont(
                            id = "dl:" + slugOf(family),
                            family = family,
                            script = script,
                            languageTag = tag,
                            slug = slugOf(family),
                            category = o.optString("category", "Sans"),
                            bundled = false
                        )
                    )
                }
                out
            } catch (_: Throwable) {
                emptyList()
            }
        }

    private fun scriptOf(subsets: List<String>): String {
        for (s in subsets) {
            if (s == "latin" || s == "latin-ext" || s == "vietnamese") continue
            Languages.bySubset(s)?.let { return it.script }
        }
        return "Latin"
    }

    private fun slugOf(family: String): String =
        family.lowercase().filter { it.isLetterOrDigit() }

    // ---- Networking ----------------------------------------------------------------------

    private const val METADATA_URL = "https://fonts.google.com/metadata/fonts"

    private fun httpGet(url: String, accept: String?): ByteArray? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 25000
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", USER_AGENT)
                if (accept != null) setRequestProperty("Accept", accept)
            }
            if (conn.responseCode !in 200..299) {
                null
            } else {
                conn.inputStream.use { it.readBytes() }
            }
        } catch (_: Throwable) {
            null
        } finally {
            try {
                conn?.disconnect()
            } catch (_: Throwable) {
            }
        }
    }

    /** Finds a usable .ttf inside the google/fonts repository for this family slug. */
    private fun findFile(slug: String): Pair<String, String>? {
        for (dir in listOf("ofl", "apache", "ufl")) {
            val json = httpGet("https://api.github.com/repos/google/fonts/contents/$dir/$slug", GITHUB_ACCEPT)
                ?: continue
            val arr = try {
                JSONArray(json.toString(Charsets.UTF_8))
            } catch (_: Throwable) {
                continue
            }
            val candidates = ArrayList<Pair<String, String>>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val name = o.optString("name")
                val url = o.optString("download_url")
                if (!name.endsWith(".ttf", ignoreCase = true) || url.isBlank()) continue
                candidates.add(name to url)
            }
            if (candidates.isEmpty()) continue
            val best = candidates.firstOrNull { it.first.contains("-Regular", ignoreCase = true) }
                ?: candidates.firstOrNull { it.first.contains("[wght]", ignoreCase = true) }
                ?: candidates.firstOrNull {
                    !it.first.contains("Italic", ignoreCase = true) &&
                        !it.first.contains("Bold", ignoreCase = true)
                }
                ?: candidates.first()
            return best
        }
        return null
    }

    private const val GITHUB_ACCEPT = "application/vnd.github+json"

    // ---- Persistence ---------------------------------------------------------------------

    private fun safeName(id: String): String =
        id.replace(Regex("[^A-Za-z0-9]+"), "_").take(80)

    private fun readInstalled(): List<InstalledFont> {
        val ctx = context ?: return emptyList()
        val sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = sp.getString(KEY_INSTALLED, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = o.optString("id")
                val file = o.optString("file")
                if (id.isBlank() || file.isBlank()) null
                else InstalledFont(
                    id = id,
                    family = o.optString("family", id),
                    script = o.optString("script", ""),
                    languageTag = o.optString("lang", ""),
                    fileName = file
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun writeInstalled(list: List<InstalledFont>) {
        val ctx = context ?: return
        val arr = JSONArray()
        list.forEach { f ->
            arr.put(
                JSONObject()
                    .put("id", f.id)
                    .put("family", f.family)
                    .put("script", f.script)
                    .put("lang", f.languageTag)
                    .put("file", f.fileName)
            )
        }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_INSTALLED, arr.toString()).apply()
    }
}
