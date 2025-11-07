package com.yourpackage

import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.GuideEditSpotlightBinding
import officepro.document.reader.viewer.editor.databinding.GuideSpotlightBinding
import officepro.document.reader.viewer.editor.dialog.GuideStep
import officepro.document.reader.viewer.editor.screen.language.PreferencesHelper.getString

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
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
            }
        }
    }

    private fun showStep(index: Int) {
        val step = steps[index]
        step.targetView.post {
            binding.spotlightOverlay.setHoleAroundView(step.targetView, margin = 12)
//            binding.spotlightOverlay.startBlinkingBorder()

            // Di chuyển mũi tên
            val loc = IntArray(2)
            step.targetView.getLocationInWindow(loc)
            val targetX = loc[0]
            val targetY = loc[1]

            val arrow = binding.arrowImage
            arrow.post {
                arrow.x = targetX + step.targetView.width / 2f - arrow.width / 2f
                arrow.y = targetY - arrow.height - 12f + step.arrowOffsetY
            }

            // Cập nhật nội dung text
            val container = (binding.root as ViewGroup).findViewById<LinearLayout>(R.id.content_box)
            container.removeViews(0, container.childCount - 1) // Xoá các TextView (giữ lại nút)

            step.titleLines.forEach { text ->
                val tv = TextView(context).apply {
                    this.text = text
                    setTextColor(Color.BLACK)
                    textSize = 14f
                    setPadding(0, 4, 0, 0)
                }
                container.addView(tv, container.childCount - 1)
            }
        }
    }
}

