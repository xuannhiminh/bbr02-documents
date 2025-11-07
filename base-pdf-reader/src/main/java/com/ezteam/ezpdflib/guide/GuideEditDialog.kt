package com.ezteam.ezpdflib.guide

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ezteam.baseproject.databinding.GuideEditSpotlightBinding
import com.ezteam.ezpdflib.R

class GuideEditDialog(
    context: Context,
    private val steps: List<GuideStep>
) : Dialog(context) {

    private lateinit var binding: GuideEditSpotlightBinding
    private var currentStepIndex = 0

    init {
        binding = GuideEditSpotlightBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        val insetsController = WindowCompat.getInsetsController(window!!, window!!.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        showStep(currentStepIndex)

        binding.btnGotIt.setOnClickListener {
            currentStepIndex++
            if (currentStepIndex < steps.size) {
                showStep(currentStepIndex)
            } else {
                dismiss()
                stopArrowDiagonal(binding.arrowImage)
            }
        }
    }

    private var arrowDiagonalAnim: AnimatorSet? = null
    private var shouldLoopArrow = false

    private fun startArrowDiagonal(
        arrow: ImageView,
        distanceDp: Float = 160f,
        startOffsetXDp: Float = 0f,
        startOffsetYDp: Float = 0f
    ) {
        val density = arrow.resources.displayMetrics.density
        val d = distanceDp * density
        val startXOffset = startOffsetXDp * density
        val startYOffset = startOffsetYDp * density

        shouldLoopArrow = true

        arrow.translationX = startXOffset
        arrow.translationY = startYOffset
        arrow.alpha = 0f
        arrow.visibility = View.VISIBLE

        val fadeIn = ObjectAnimator.ofFloat(arrow, View.ALPHA, 0f, 1f).apply { duration = 150 }
        val moveX =
            ObjectAnimator.ofFloat(arrow, View.TRANSLATION_X, startXOffset, startXOffset + d)
        val moveY =
            ObjectAnimator.ofFloat(arrow, View.TRANSLATION_Y, startYOffset, startYOffset + d)
        val move = AnimatorSet().apply {
            playTogether(moveX, moveY)
            duration = 700
        }
        val fadeOut = ObjectAnimator.ofFloat(arrow, View.ALPHA, 1f, 0f).apply { duration = 200 }

        arrowDiagonalAnim?.cancel()
        arrowDiagonalAnim = AnimatorSet().apply {
            playSequentially(fadeIn, move, fadeOut)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // Chỉ lặp khi còn ở bước cần hiệu ứng
                    if (isShowing && shouldLoopArrow) {
                        arrow.postDelayed({
                            if (shouldLoopArrow) {
                                startArrowDiagonal(
                                    arrow,
                                    distanceDp,
                                    startOffsetXDp,
                                    startOffsetYDp
                                )
                            }
                        }, 300)
                    }
                }
            })
            start()
        }
    }

    private fun stopArrowDiagonal(arrow: ImageView) {
        shouldLoopArrow = false
        arrowDiagonalAnim?.removeAllListeners()
        arrowDiagonalAnim?.cancel()
        arrow.clearAnimation()
        arrow.animate().cancel()
        arrow.alpha = 1f
    }

    private fun dp(dp: Float, v: View): Float = dp * v.resources.displayMetrics.density

    private fun getRelativeTopLeftOnOverlay(child: View, overlay: View): PointF {
        val childLoc = IntArray(2)
        val overlayLoc = IntArray(2)
        child.getLocationOnScreen(childLoc)
        overlay.getLocationOnScreen(overlayLoc)
        val relX = (childLoc[0] - overlayLoc[0]).toFloat()
        val relY = (childLoc[1] - overlayLoc[1]).toFloat()
        return PointF(relX, relY)
    }

    private fun showStep(index: Int) {
        val step = steps[index]
        step.targetView.post {
            binding.spotlightOverlay.setHoleAroundView(step.targetView, margin = 12)
//            binding.spotlightOverlay.startBlinkingBorder()

            val arrow = binding.arrowImage

            val topLeft = getRelativeTopLeftOnOverlay(step.targetView, binding.spotlightOverlay)
            val targetCenterX = topLeft.x + step.targetView.width / 2f
            val targetTopY    = topLeft.y
            val targetCenterY = topLeft.y + step.targetView.height / 2f

            val off = dp(24f, arrow)

            arrow.post {
                arrow.x = targetCenterX - arrow.width / 2f - off + dp(20f, arrow)
                arrow.y = targetTopY - off + step.arrowOffsetY

                if (index == 0) {
                    arrow.x = topLeft.x
                    arrow.y = topLeft.y
                    startArrowDiagonal(
                        arrow,
                        distanceDp = 80f,
                        startOffsetXDp = -220f,
                        startOffsetYDp = -50f
                    )
                } else {
                    stopArrowDiagonal(arrow)
                }

                val tvGuide = (binding.root as ViewGroup).findViewById<TextView>(R.id.tv_guide_text)
                tvGuide.text = step.titleLines.joinToString("\n")
                tvGuide.setTextColor(ContextCompat.getColor(context, R.color.text1))
                tvGuide.textSize = 14f
                tvGuide.ellipsize = null
                tvGuide.isSingleLine = false
            }
        }
    }
}

