package com.ezteam.ezpdflib.model

import android.graphics.Color
import java.io.Serializable

data class AnnotationValue(
    var color: String,
    var thickness: Int = 10,
    var transparency: Int = 0
) : Serializable {

    fun getRgbFromHex(): IntArray {
        val initColor = Color.parseColor(color)
        val r = Color.red(initColor)
        val g = Color.green(initColor)
        val b = Color.blue(initColor)
        return intArrayOf(r, g, b)
    }

    fun getColorPushNative(): FloatArray {
        val initColor = Color.parseColor(color)
        val r = Color.red(initColor).toFloat() / 255.0f
        val g = Color.green(initColor).toFloat() / 255.0f
        val b = Color.blue(initColor).toFloat() / 255.0f
        return floatArrayOf(r, g, b)
    }

    fun getColorSetting(): Int {
        val arrColor = getRgbFromHex()
        val alpha = 255 - transparency * 2.55
        return Color.argb(alpha.toInt(), arrColor[0], arrColor[1], arrColor[2])
    }

    fun getAlpha(): Float {
        return 1.0f - (transparency.toFloat() / 100.0f)
    }

    fun getThicknessPushNative(): Float {
        return thickness.toFloat() * 5.0f / 10.0f
    }
}
