package com.ezteam.baseproject.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.animation.ValueAnimator
import android.animation.ArgbEvaluator
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.AttributeSet
import android.view.*
import android.widget.Button
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.ezteam.baseproject.animation.AnimationUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob

class SpotlightOverlayView @JvmOverloads constructor(
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
        // Bảo đảm target đã layout xong
        targetView.post {
            val density = resources.displayMetrics.density
            val m = margin * density

            // Lấy toạ độ theo SCREEN cho cả target và overlay
            val targetLoc = IntArray(2)
            val overlayLoc = IntArray(2)
            targetView.getLocationOnScreen(targetLoc)
            this.getLocationOnScreen(overlayLoc) // SpotlightOverlayView

            val x = targetLoc[0] - overlayLoc[0] - m
            val y = targetLoc[1] - overlayLoc[1] - m
            val w = targetView.width.toFloat() + 2 * m
            val h = targetView.height.toFloat() + 2 * m

            holeRect = RectF(x, y, x + w, y + h)
            invalidate()
        }
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