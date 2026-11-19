package com.manish.liquidlauncher.model

import androidx.compose.runtime.Immutable

/**
 * A chosen typeface + language for one piece of launcher text.
 *
 * [fontId] uses these schemes:
 *  - `sys:<name>`   a font family that ships with Android (sans, serif, mono, cursive, condensed)
 *  - `dl:<family>`  a font downloaded by the user into the launcher's private storage
 *  - `""`           follow the launcher default
 */
@Immutable
data class FontChoice(
    val fontId: String,
    val familyName: String,
    val languageTag: String = "",
    val script: String = ""
) {
    val isEmpty: Boolean get() = fontId.isBlank()
}

/**
 * Every piece of launcher text that can carry its own font + language.
 *
 * Targets are plain strings so they can be persisted in SharedPreferences:
 *  - fixed targets: `home`, `dock`, `drawer`, `clock`, `battery`, `weather`, `music`, `timeline`
 *  - per folder: `folder:<folder name>`
 *  - per app: `app:<package name>`
 */
object TypeTargets {
    const val HOME = "home"
    const val DOCK = "dock"
    const val DRAWER = "drawer"
    const val CLOCK = "clock"
    const val BATTERY = "battery"
    const val WEATHER = "weather"
    const val MUSIC = "music"
    const val TIMELINE = "timeline"

    private const val FOLDER = "folder:"
    private const val APP = "app:"

    fun folder(name: String): String = FOLDER + name
    fun app(packageName: String): String = APP + packageName

    fun isFolder(target: String): Boolean = target.startsWith(FOLDER)
    fun isApp(target: String): Boolean = target.startsWith(APP)
    fun folderName(target: String): String = target.removePrefix(FOLDER)
    fun packageName(target: String): String = target.removePrefix(APP)

    /** Human label used by the Type Studio chips. */
    fun label(target: String): String = when {
        target == HOME -> "Home labels"
        target == DOCK -> "Dock labels"
        target == DRAWER -> "App drawer"
        target == CLOCK -> "Clock"
        target == BATTERY -> "Battery"
        target == WEATHER -> "Weather"
        target == MUSIC -> "Music"
        target == TIMELINE -> "Activity"
        isFolder(target) -> "Folder: " + folderName(target)
        isApp(target) -> "App: " + packageName(target).substringAfterLast('.')
        else -> target
    }
}

/** Result of a font download, kept small so the UI can show progress per row. */
enum class FontState { NotInstalled, Downloading, Installed, Failed }
