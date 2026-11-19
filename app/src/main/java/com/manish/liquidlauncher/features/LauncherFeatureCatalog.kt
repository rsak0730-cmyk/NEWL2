package com.manish.liquidlauncher.features

/**
 * Central registry for the NEW-LAUNCHER feature set.
 *
 * Every entry now carries an implementation status so the Feature Studio tells the truth
 * about what is wired up on the device in front of the user.
 */
object LauncherFeatureCatalog {

    enum class Status(val label: String) {
        Ready("READY"),
        NeedsPermission("NEEDS PERMISSION"),
        DeviceDependent("DEVICE DEPENDENT"),
        Planned("PLANNED")
    }

    data class Feature(
        val name: String,
        val group: String,
        val description: String,
        val status: Status = Status.Ready
    )

    val groups: List<Pair<String, List<Feature>>> = listOf(
        "Dock & Workspace" to listOf(
            Feature(
                "Smart Dock Editing", "Dock & Workspace",
                "Long-press any app › Add to dock / Remove from dock. The dock keeps its own order and survives restarts."
            ),
            Feature(
                "Free Drag & Drop", "Dock & Workspace",
                "Edit mode › drag any icon across the grid. The dragged icon follows your finger and the grid re-flows on drop."
            ),
            Feature(
                "Folder Dragging", "Dock & Workspace",
                "Folders are workspace items like apps, so they can be dragged, reordered and undone."
            ),
            Feature(
                "Resize & Reposition", "Dock & Workspace",
                "Icon size, grid presets and per-item reposition are live. Free-form widget resizing needs the Android app-widget host, which a launcher cannot host outside its own workspace.",
                Status.DeviceDependent
            ),
            Feature(
                "Drag-to-Create Folder", "Dock & Workspace",
                "In edit mode, hold a dragged app over another app for a moment to merge both into a new folder."
            ),
            Feature(
                "Edge Auto-Scroll", "Dock & Workspace",
                "Dragging an icon against the left or right edge flips to the next page so items can be moved between pages."
            ),
            Feature(
                "Edit Mode", "Dock & Workspace",
                "Toggle from the Control Center tile, the Theme Engine, a long-press gesture, or the floating bubble."
            ),
            Feature(
                "Dynamic Grid Reordering", "Dock & Workspace",
                "The order is stored as a key list and re-applied on every load, so new apps append instead of reshuffling the grid."
            ),
            Feature(
                "Undo Movement", "Dock & Workspace",
                "The last move can be undone from the glass Undo chip for six seconds."
            ),
            Feature(
                "Grid Presets", "Dock & Workspace",
                "3×5, 4×5, 4×6, 5×5 and 5×6 presets in Theme Engine › Workspace."
            ),
            Feature(
                "Multiple Dock Styles", "Dock & Workspace",
                "Glass pill, floating, minimal, frosted bar and invisible docks."
            ),
            Feature(
                "Liquid Drag Physics", "Dock & Workspace",
                "Jelly squash on press, scale-up while dragging, spring settle on drop."
            )
        ),
        "App & Folder Intelligence" to listOf(
            Feature(
                "Smart Icon Engine", "App & Folder Intelligence",
                "Icon shapes (squircle, circle, drop, square), per-app dominant tint, glass rim, gloss and notification badges."
            ),
            Feature(
                "Intelligent Folders", "App & Folder Intelligence",
                "Social and Google folders are built from package rules; your own folders are stored and re-applied."
            ),
            Feature(
                "Smart App Prediction", "App & Folder Intelligence",
                "Deterministic, on-device: launch counts are bucketed by time of day and the current bucket is surfaced."
            ),
            Feature(
                "App Action Menu", "App & Folder Intelligence",
                "Open, app info, folder, dock, focus, text & language, hide and uninstall."
            ),
            Feature(
                "App Recommendation Engine", "App & Folder Intelligence",
                "Frequency ranking from local usage counts. No AI service, no network, no data leaves the device."
            ),
            Feature(
                "Icon Action Expansion", "App & Folder Intelligence",
                "Long-press opens the action menu directly from the home grid, drawer, folder and suggestions rows."
            )
        ),
        "Floating Experiences" to listOf(
            Feature(
                "Live Activity Island", "Floating Experiences",
                "Glass island under the status bar with the current track and transport controls.",
                Status.NeedsPermission
            ),
            Feature(
                "Floating App Launcher", "Floating Experiences",
                "The app drawer opens as a floating glass sheet over the dimmed, blurred workspace."
            ),
            Feature(
                "Floating Window Manager", "Floating Experiences",
                "Free-form floating windows are an OEM/Android multi-window capability. App pairs hand off to split screen where the device supports it.",
                Status.DeviceDependent
            ),
            Feature(
                "Floating Shortcut Bubbles", "Floating Experiences",
                "A draggable glass bubble that opens the drawer on tap and toggles edit mode on long-press. Theme Engine › Workspace."
            ),
            Feature(
                "Edge Gesture Dock", "Floating Experiences",
                "Gestures are fully remappable in the Gesture Studio; a swipe can open the drawer, shade, private space, type studio and more."
            ),
            Feature(
                "Edge Lighting", "Floating Experiences",
                "Animated sweep-gradient border glow while the battery is charging. Theme Engine › Workspace."
            ),
            Feature(
                "Gesture Trail", "Floating Experiences",
                "A fading ribbon of droplets follows every finger. Watched in the Initial pointer pass so it never consumes a tap or a swipe. Theme Engine › Workspace."
            ),
            Feature(
                "Liquid Touch Ripple", "Floating Experiences",
                "Every glass touch target now expands a soft double ring from the exact point pressed, on top of the jelly squash."
            )
        ),
        "Widgets & Information" to listOf(
            Feature(
                "Interactive Widget Stack", "Widgets & Information",
                "The home widget block cycles clock › weather › music › timer › world clock › countdown on tap."
            ),
            Feature(
                "Widget Stack", "Widgets & Information",
                "Same stack, also reachable as a dedicated page of widgets on the Control Center."
            ),
            Feature(
                "Interactive Weather Widget", "Widgets & Information",
                "Live current conditions from Open-Meteo (no key, no location permission). Set the city in Theme Engine › Widgets.",
                Status.NeedsPermission
            ),
            Feature(
                "Animated Battery Widget", "Widgets & Information",
                "Liquid double-sine fill, charging bolt pulse and live percentage."
            ),
            Feature(
                "Music Control Widget", "Widgets & Information",
                "Spinning artwork with previous / play-pause / next transport.",
                Status.NeedsPermission
            ),
            Feature(
                "World Clock Widget", "Widgets & Information",
                "Five zones with live times, DST-aware through the platform time zone database."
            ),
            Feature(
                "Calendar Countdown", "Widgets & Information",
                "Named countdown with days/hours/minutes remaining, set in Theme Engine › Widgets."
            ),
            Feature(
                "Timer and Stopwatch", "Widgets & Information",
                "Start, pause and reset with tenth-of-a-second precision inside the widget stack."
            ),
            Feature(
                "Device Monitor", "Widgets & Information",
                "Live RAM, storage and uptime readouts."
            ),
            Feature(
                "Quick File Access", "Widgets & Information",
                "The Files tile opens the system document picker straight into storage."
            )
        ),
        "Gestures & Navigation" to listOf(
            Feature(
                "Gesture Studio", "Gestures & Navigation",
                "Swipe up, swipe down, double tap and long press each map to any launcher action."
            ),
            Feature(
                "One-Handed Launcher Mode", "Gestures & Navigation",
                "Pulls the workspace into the lower half of the screen for thumb reach."
            ),
            Feature(
                "3D App Carousel", "Gestures & Navigation",
                "Coverflow shelf of every app: the centred card stands upright, the rest rotate and dim in 3D. Control Center › Carousel tile, or assign it to a gesture."
            ),
            Feature(
                "Advanced Task Switcher", "Gestures & Navigation",
                "Recent-task enumeration is restricted to the system recents UI on Android 10+.",
                Status.DeviceDependent
            )
        ),
        "Privacy & Connectivity" to listOf(
            Feature(
                "Private Space", "Privacy & Connectivity",
                "PIN-gated space for hidden apps. This hides apps from the launcher; it is not an OS-level isolated profile.",
                Status.Ready
            ),
            Feature(
                "Lock Screen Companion", "Privacy & Connectivity",
                "Always-on style idle clock with burn-in drift, plus double-tap-to-lock through the accessibility service.",
                Status.NeedsPermission
            ),
            Feature(
                "Multi-Device Hub", "Privacy & Connectivity",
                "Sends this device's whole layout — theme, dock, folders, grid, gestures, fonts, PIN — to another phone as one file through any share channel, and receives it back. No account, no server, no upload."
            ),
            Feature(
                "Activity Timeline", "Privacy & Connectivity",
                "Local launch history with relative times, shown as a widget on the Control Center."
            ),
            Feature(
                "App Pair", "Privacy & Connectivity",
                "Save two apps and launch them together with LAUNCH_ADJACENT where split screen is available.",
                Status.DeviceDependent
            ),
            Feature(
                "Global Media Controls", "Privacy & Connectivity",
                "Transport controls on the activity island and the music widget, driven by the media session.",
                Status.NeedsPermission
            )
        ),
        "System & Performance" to listOf(
            Feature(
                "Adaptive Performance Engine", "System & Performance",
                "The animation clock and gyro tilt pause whenever the drawer covers the home screen or power save is on."
            ),
            Feature(
                "Type & Language Studio", "System & Performance",
                "Per-folder, per-app and per-widget fonts and languages, with search by font name and on-demand download from the public Google Fonts library."
            )
        )
    )

    val all: List<Feature> = groups.flatMap { it.second }

    fun count(status: Status): Int = all.count { it.status == status }
}
