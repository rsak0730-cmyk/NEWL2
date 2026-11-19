package com.manish.liquidlauncher.model

import androidx.compose.runtime.Immutable

/** Visual treatments for the bottom dock. */
enum class DockStyle(val label: String) {
    GlassPill("Glass pill"),
    Floating("Floating"),
    Minimal("Minimal"),
    Frosted("Frosted bar"),
    Invisible("Invisible")
}

/** Grid presets (columns x max rows). */
data class GridPreset(val label: String, val columns: Int, val maxRows: Int)

val GridPresets = listOf(
    GridPreset("4 x 5", 4, 5),
    GridPreset("4 x 6", 4, 6),
    GridPreset("5 x 5", 5, 5),
    GridPreset("5 x 6", 5, 6),
    GridPreset("3 x 5", 3, 5)
)

/** What a launcher gesture does. */
enum class GestureAction(val label: String) {
    None("Nothing"),
    Drawer("Open app drawer"),
    Notifications("Notification shade"),
    ThemeEngine("Theme engine"),
    TypeStudio("Type & language"),
    IdleMode("Idle clock"),
    Lock("Lock screen"),
    PrivateSpace("Private space"),
    EditMode("Edit mode"),
    Search("Web search"),
    Carousel("3D app carousel"),
    DeviceHub("Multi-device hub")
}

/** The five configurable gestures. */
enum class GestureSlot(val label: String) {
    SwipeUp("Swipe up"),
    SwipeDown("Swipe down"),
    DoubleTap("Double tap"),
    LongPress("Long press")
}

data class GestureMap(val assignments: Map<GestureSlot, GestureAction> = emptyMap()) {
    fun actionFor(slot: GestureSlot, fallback: GestureAction): GestureAction =
        assignments[slot] ?: fallback

    fun with(slot: GestureSlot, action: GestureAction): GestureMap =
        GestureMap(assignments + (slot to action))
}

/** A saved pair of apps that launch together in split screen. */
@Immutable
data class AppPair(
    val name: String,
    val first: String,
    val second: String
)

/**
 * One home-screen item, stored by key so the grid order survives restarts.
 * Keys: `app:<package>` or `folder:<name>`.
 */
@Immutable
data class WorkspaceItem(val key: String) {
    val isFolder: Boolean get() = key.startsWith("folder:")
    val isApp: Boolean get() = key.startsWith("app:")
    val packageName: String get() = key.removePrefix("app:")
    val folderName: String get() = key.removePrefix("folder:")

    companion object {
        fun ofApp(packageName: String) = WorkspaceItem("app:$packageName")
        fun ofFolder(name: String) = WorkspaceItem("folder:$name")
    }
}

/** A logged app launch, used by the activity timeline and smart suggestions. */
@Immutable
data class LaunchEvent(val packageName: String, val atMillis: Long)
