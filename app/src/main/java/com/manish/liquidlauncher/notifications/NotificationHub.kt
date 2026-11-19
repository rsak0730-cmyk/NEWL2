package com.manish.liquidlauncher.notifications

import android.media.session.MediaController
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap

@Immutable
data class MediaInfo(
    val title: String,
    val artist: String,
    val art: ImageBitmap?,
    val playing: Boolean
)

/** Process-wide live state fed by [LauncherNotificationListener] and read by Compose. */
object NotificationHub {
    val counts = mutableStateOf<Map<String, Int>>(emptyMap())
    val media = mutableStateOf<MediaInfo?>(null)
    val connected = mutableStateOf(false)

    @Volatile
    var controller: MediaController? = null
}
