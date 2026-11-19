package com.manish.liquidlauncher.notifications

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Powers notification badges and the music widget.
 * The user enables it once in Settings > Notification access.
 */
class LauncherNotificationListener : NotificationListenerService() {

    private var sessionManager: MediaSessionManager? = null
    private var current: MediaController? = null

    private val sessionsListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers -> pickController(controllers) }

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) = publishMedia()
        override fun onMetadataChanged(metadata: MediaMetadata?) = publishMedia()
        override fun onSessionDestroyed() = publishMedia()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationHub.connected.value = true
        refreshCounts()
        try {
            val cn = ComponentName(this, LauncherNotificationListener::class.java)
            val sm = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            sessionManager = sm
            sm.addOnActiveSessionsChangedListener(sessionsListener, cn)
            pickController(sm.getActiveSessions(cn))
        } catch (_: Exception) {
        }
    }

    override fun onListenerDisconnected() {
        NotificationHub.connected.value = false
        try {
            sessionManager?.removeOnActiveSessionsChangedListener(sessionsListener)
        } catch (_: Exception) {
        }
        current?.unregisterCallback(controllerCallback)
        current = null
        NotificationHub.controller = null
        NotificationHub.media.value = null
        NotificationHub.counts.value = emptyMap()
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) = refreshCounts()

    override fun onNotificationRemoved(sbn: StatusBarNotification?) = refreshCounts()

    private fun refreshCounts() {
        val map = HashMap<String, Int>()
        try {
            val active = activeNotifications ?: emptyArray()
            for (n in active) {
                if (n.isOngoing) continue
                if ((n.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) continue
                map[n.packageName] = (map[n.packageName] ?: 0) + 1
            }
        } catch (_: Exception) {
        }
        NotificationHub.counts.value = map
    }

    private fun pickController(list: List<MediaController>?) {
        val best = list?.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
            ?: list?.firstOrNull()
        if (best?.sessionToken != current?.sessionToken) {
            current?.unregisterCallback(controllerCallback)
            current = best
            best?.registerCallback(controllerCallback)
        }
        NotificationHub.controller = current
        publishMedia()
    }

    private fun publishMedia() {
        val c = current
        val md = c?.metadata
        val title = md?.getString(MediaMetadata.METADATA_KEY_TITLE)
        if (c == null || title.isNullOrBlank()) {
            NotificationHub.media.value = null
            return
        }
        val art = md?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: md?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        NotificationHub.media.value = MediaInfo(
            title = title,
            artist = md?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "",
            art = art?.asImageBitmap(),
            playing = c.playbackState?.state == PlaybackState.STATE_PLAYING
        )
    }
}
