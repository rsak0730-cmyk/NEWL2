package com.manish.liquidlauncher.fonts

/**
 * A font the launcher can install. [slug] is the folder name inside the
 * google/fonts repository (under ofl/ or apache/), which is how the downloader
 * finds the actual .ttf file.
 */
data class CatalogFont(
    val id: String,
    val family: String,
    val script: String,
    val languageTag: String,
    val slug: String,
    val category: String,
    val bundled: Boolean = true
)

/** A font that is already usable: either a system family or a downloaded file. */
data class InstalledFont(
    val id: String,
    val family: String,
    val script: String,
    val languageTag: String,
    val fileName: String = ""
) {
    val isSystem: Boolean get() = id.startsWith("sys:")
}

object FontCatalog {

    /** Families that ship with Android — always available, no download. */
    val system: List<InstalledFont> = listOf(
        InstalledFont("sys:sans", "System sans", "Latin", "en"),
        InstalledFont("sys:serif", "System serif", "Latin", "en"),
        InstalledFont("sys:mono", "System mono", "Latin", "en"),
        InstalledFont("sys:cursive", "System cursive", "Latin", "en"),
        InstalledFont("sys:condensed", "System condensed", "Latin", "en")
    )

    /**
     * Offline starter catalogue: a spread of Google Fonts families across the scripts
     * the launcher is likely to render. The online search reaches the full library
     * (thousands of families, every language Google publishes).
     */
    val bundled: List<CatalogFont> = listOf(
        // Latin / display
        f("Roboto", "Latin", "en", "roboto", "Sans"),
        f("Roboto Slab", "Latin", "en", "robotoslab", "Slab"),
        f("Roboto Mono", "Latin", "en", "robotomono", "Mono"),
        f("Open Sans", "Latin", "en", "opensans", "Sans"),
        f("Lato", "Latin", "en", "lato", "Sans"),
        f("Montserrat", "Latin", "en", "montserrat", "Sans"),
        f("Poppins", "Latin", "en", "poppins", "Geometric"),
        f("Inter", "Latin", "en", "inter", "Sans"),
        f("Oswald", "Latin", "en", "oswald", "Condensed"),
        f("Raleway", "Latin", "en", "raleway", "Sans"),
        f("Rubik", "Latin", "en", "rubik", "Sans"),
        f("Work Sans", "Latin", "en", "worksans", "Sans"),
        f("DM Sans", "Latin", "en", "dmsans", "Sans"),
        f("Space Grotesk", "Latin", "en", "spacegrotesk", "Display"),
        f("Outfit", "Latin", "en", "outfit", "Display"),
        f("Nunito", "Latin", "en", "nunito", "Rounded"),
        f("Quicksand", "Latin", "en", "quicksand", "Rounded"),
        f("Playfair Display", "Latin", "en", "playfairdisplay", "Serif"),
        f("Merriweather", "Latin", "en", "merriweather", "Serif"),
        f("Bebas Neue", "Latin", "en", "bebasneue", "Display"),
        f("Caveat", "Latin", "en", "caveat", "Handwriting"),
        f("Pacifico", "Latin", "en", "pacifico", "Handwriting"),
        f("Orbitron", "Latin", "en", "orbitron", "Sci-fi"),
        f("Righteous", "Latin", "en", "righteous", "Display"),
        f("Cinzel", "Latin", "en", "cinzel", "Serif"),
        f("Abril Fatface", "Latin", "en", "abrilfatface", "Display"),

        // Indian scripts
        f("Noto Sans Devanagari", "Devanagari", "hi", "notosansdevanagari", "Sans"),
        f("Noto Serif Devanagari", "Devanagari", "hi", "notoserifdevanagari", "Serif"),
        f("Noto Sans Bengali", "Bengali", "bn", "notosansbengali", "Sans"),
        f("Noto Serif Bengali", "Bengali", "bn", "notoserifbengali", "Serif"),
        f("Noto Sans Gurmukhi", "Gurmukhi", "pa", "notosansgurmukhi", "Sans"),
        f("Noto Sans Gujarati", "Gujarati", "gu", "notosansgujarati", "Sans"),
        f("Noto Sans Oriya", "Oriya", "or", "notosansoriya", "Sans"),
        f("Noto Sans Tamil", "Tamil", "ta", "notosanstamil", "Sans"),
        f("Noto Serif Tamil", "Tamil", "ta", "notoseriftamil", "Serif"),
        f("Noto Sans Telugu", "Telugu", "te", "notosanstelugu", "Sans"),
        f("Noto Sans Kannada", "Kannada", "kn", "notosanskannada", "Sans"),
        f("Noto Sans Malayalam", "Malayalam", "ml", "notosansmalayalam", "Sans"),
        f("Noto Sans Sinhala", "Sinhala", "si", "notosanssinhala", "Sans"),

        // World scripts
        f("Noto Sans Arabic", "Arabic", "ar", "notosansarabic", "Sans"),
        f("Noto Naskh Arabic", "Arabic", "ar", "notonaskharabic", "Serif"),
        f("Noto Sans Hebrew", "Hebrew", "he", "notosanshebrew", "Sans"),
        f("Noto Sans Thai", "Thai", "th", "notosansthai", "Sans"),
        f("Noto Sans Lao", "Lao", "lo", "notosanslao", "Sans"),
        f("Noto Sans Khmer", "Khmer", "km", "notosanskhmer", "Sans"),
        f("Noto Sans Myanmar", "Myanmar", "my", "notosansmyanmar", "Sans"),
        f("Noto Serif Tibetan", "Tibetan", "bo", "notoseriftibetan", "Serif"),
        f("Noto Sans Ethiopic", "Ethiopic", "am", "notosansethiopic", "Sans"),
        f("Noto Sans Armenian", "Armenian", "hy", "notosansarmenian", "Sans"),
        f("Noto Sans Georgian", "Georgian", "ka", "notosansgeorgian", "Sans"),
        f("Noto Sans", "Cyrillic", "ru", "notosans", "Sans"),
        f("Noto Serif", "Cyrillic", "ru", "notoserif", "Serif"),
        f("Noto Sans SC", "Han", "zh", "notosanssc", "Sans"),
        f("Noto Sans JP", "Japanese", "ja", "notosansjp", "Sans"),
        f("Noto Sans KR", "Korean", "ko", "notosanskr", "Sans"),
        f("Noto Sans Cherokee", "Cherokee", "chr", "notosanscherokee", "Sans"),
        f("Noto Sans Syriac", "Syriac", "syr", "notosanssyriac", "Sans"),
        f("Noto Sans Thaana", "Thaana", "dv", "notosansthaana", "Sans"),
        f("Noto Sans Tifinagh", "Tifinagh", "ber", "notosanstifinagh", "Sans"),
        f("Noto Sans Adlam", "Adlam", "ff", "notosansadlam", "Sans")
    )

    private fun f(family: String, script: String, tag: String, slug: String, category: String) =
        CatalogFont(id = "dl:$slug", family = family, script = script, languageTag = tag, slug = slug, category = category)

    /** Bundled families that can render [script] (or everything when blank). */
    fun forScript(script: String): List<CatalogFont> =
        if (script.isBlank()) bundled else bundled.filter { it.script.equals(script, ignoreCase = true) }

    fun searchOffline(query: String, script: String = ""): List<CatalogFont> {
        val base = forScript(script)
        if (query.isBlank()) return base
        return base.filter { it.family.contains(query, ignoreCase = true) }
    }
}
