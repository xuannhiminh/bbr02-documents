package office.pdf.document.reader.viewer.editor.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import office.pdf.document.reader.viewer.editor.databinding.GuideSpotlightBinding

class ReloadFileGuideDialog(
    context: Context,
    private val targetView: View? = null
) : Dialog(context) {

    private var binding: GuideSpotlightBinding =
        GuideSpotlightBinding.inflate(LayoutInflater.from(context))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // bỏ title
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        // làm nền dialog trong suốt
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        }
        binding.root.setOnClickListener {
            dismiss()
        }
        startAnimations()
    }

    private fun startAnimations() {
        // animation cho tay
        binding.handIcon.post {
            val distance = binding.arrowIcon.height.toFloat() - binding.handIcon.height.toFloat()

            val handAnim = TranslateAnimation(0f, 0f, 0f, distance).apply {
                duration = 1000
                repeatCount = 0
                fillAfter = true
            }

            handAnim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    binding.handIcon.visibility = View.INVISIBLE
                    binding.handIcon.postDelayed({
                        binding.handIcon.clearAnimation()
                        binding.handIcon.translationY = 0f
                        binding.handIcon.visibility = View.VISIBLE
                        binding.handIcon.startAnimation(handAnim)
                    }, 300)
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })

            binding.handIcon.startAnimation(handAnim)
        }

        // animation cho arrow (nhấp nháy)
        val arrowAnim = AlphaAnimation(0.3f, 1.0f).apply {
            duration = 600
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }
        binding.arrowIcon.startAnimation(arrowAnim)
    }

    fun stopAnimations() {
        binding.handIcon.clearAnimation()
        binding.arrowIcon.clearAnimation()
    }
}
