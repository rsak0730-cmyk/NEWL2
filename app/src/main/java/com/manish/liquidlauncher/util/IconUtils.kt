package com.manish.liquidlauncher.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color

fun drawableToBitmap(drawable: Drawable, size: Int = 176): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, size, size)
    drawable.draw(canvas)
    return bitmap
}

/** Saturation-weighted average colour so vivid icons give vivid glows (greys/whites are ignored). */
fun extractDominantColor(bitmap: Bitmap): Color {
    return try {
        val scaled = Bitmap.createScaledBitmap(bitmap, 16, 16, true)
        var rSum = 0.0
        var gSum = 0.0
        var bSum = 0.0
        var weightSum = 0.0
        for (x in 2 until 14) {
            for (y in 2 until 14) {
                val pixel = scaled.getPixel(x, y)
                val alpha = (pixel shr 24) and 0xFF
                if (alpha > 140) {
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8) and 0xFF
                    val b = pixel and 0xFF
                    if (r + g + b in 75..680) {
                        val sat = maxOf(r, g, b) - minOf(r, g, b)
                        val weight = 1.0 + sat / 24.0
                        rSum += r * weight
                        gSum += g * weight
                        bSum += b * weight
                        weightSum += weight
                    }
                }
            }
        }
        if (weightSum > 0) {
            Color(
                red = (rSum / weightSum).toInt().coerceIn(48, 255),
                green = (gSum / weightSum).toInt().coerceIn(48, 255),
                blue = (bSum / weightSum).toInt().coerceIn(48, 255)
            )
        } else {
            Color(0xFF38BDF8)
        }
    } catch (_: Exception) {
        Color(0xFF38BDF8)
    }
}
