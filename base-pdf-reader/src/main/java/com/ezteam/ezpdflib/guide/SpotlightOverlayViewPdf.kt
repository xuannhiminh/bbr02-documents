package com.ezteam.ezpdflib.guide

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.animation.ValueAnimator
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout

class SpotlightOverlayViewPdf @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.TRANSPARENT
        strokeWidth = 0f * resources.displayMetrics.density
        isAntiAlias = true
    }

    private var holeRect: RectF? = null
    private val cornerRadius = 20f * resources.displayMetrics.density

    private var blinkAnimator: ValueAnimator? = null
    init {
        setWillNotDraw(false)
    }

    fun setHoleAroundView(targetView: View, margin: Int = 16) {
        val location = IntArray(2)
        targetView.getLocationInWindow(location)

        val x = location[0].toFloat() - margin
        val y = location[1].toFloat() - margin
        val w = targetView.width + margin * 2
        val h = targetView.height + margin * 2

        holeRect = RectF(x, y, x + w, y + h)
        invalidate()
    }

    fun startBlinkingBorder(
        baseColor: Int = Color.TRANSPARENT,
        duration: Long = 1000L
    ) {
        blinkAnimator?.cancel()

        blinkAnimator = ValueAnimator.ofInt(255, 0, 255).apply {
            this.duration = duration
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animator ->
                val alpha = animator.animatedValue as Int
                val newColor = baseColor and 0x00FFFFFF or (alpha shl 24)
                borderPaint.color = newColor
                invalidate()
            }
            start()
        }
    }


    fun stopBlinkingAndClearHole() {
        blinkAnimator?.cancel()
        blinkAnimator = null
        borderPaint.color = Color.TRANSPARENT
        holeRect = null // Không vẽ nữa
        invalidate()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        holeRect?.let {
            canvas.drawRoundRect(it, cornerRadius, cornerRadius, borderPaint)
        }
    }
}