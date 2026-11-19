package com.manish.liquidlauncher.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import kotlin.math.abs

/** Picks a primary + secondary hue from a wallpaper so the glass matches it. */
object ThemeFromImage {

    fun decode(context: Context, uri: Uri): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (bounds.outWidth / sample > 256 && bounds.outHeight / sample > 256) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
        } catch (_: Exception) {
            null
        }
    }

    fun extractHues(bitmap: Bitmap): Pair<Float, Float> {
        val small = Bitmap.createScaledBitmap(bitmap, 48, 48, true)
        val bins = FloatArray(36)
        val hsv = FloatArray(3)
        for (x in 0 until 48) {
            for (y in 0 until 48) {
                Color.colorToHSV(small.getPixel(x, y), hsv)
                if (hsv[1] > 0.15f && hsv[2] > 0.2f) {
                    bins[(hsv[0] / 10f).toInt().coerceIn(0, 35)] += hsv[1] * hsv[2]
                }
            }
        }
        var best = 0
        for (i in bins.indices) if (bins[i] > bins[best]) best = i
        if (bins[best] <= 0f) return 195f to 245f

        var second = -1
        for (i in bins.indices) {
            val dist = minOf(abs(i - best), 36 - abs(i - best))
            if (dist >= 4 && (second == -1 || bins[i] > bins[second])) second = i
        }
        val primary = best * 10f + 5f
        val secondary = if (second == -1 || bins[second] < bins[best] * 0.25f) {
            (primary + 55f) % 360f
        } else {
            second * 10f + 5f
        }
        return primary to secondary
    }
}
