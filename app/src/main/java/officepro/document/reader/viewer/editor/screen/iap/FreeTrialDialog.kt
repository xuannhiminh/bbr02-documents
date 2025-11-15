package office.pdf.document.reader.viewer.editor.screen.iap

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import office.pdf.document.reader.viewer.editor.R
import office.pdf.document.reader.viewer.editor.databinding.FreeTrialDialogBinding

class FreeTrialDialog : DialogFragment() {
    private var _binding: FreeTrialDialogBinding? = null
    private val binding get() = _binding!!

    var positiveCallBack: (() -> Unit)? = null
    var negativeCallBack: (() -> Unit)? = null
    var priceMonthAnnual = ""
    var priceYear = ""

    private var isViewDestroyed = false
    private var isAdLoaded = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        // intercept back button
        dialog.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                dismiss()
                negativeCallBack?.invoke()
                true
            } else {
                false
            }
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun getTheme(): Int {
        return R.style.DialogStyle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            _binding = FreeTrialDialogBinding.inflate(inflater, container, false)
        } catch (e: Exception) {
            Log.e("FreeTrialDialog", "Error inflating layout: ${e.message}")
          //  throw e
            negativeCallBack?.invoke()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewDestroyed = false

        val title = createHighlightedSpannable(
            fullText = getString(R.string.get_free_trial),
            highlightTexts = listOf(getString(R.string.day_free_trial))
        )
        binding.tvTitle.text = title
        val feature1 = createHighlightedSpannable(
            fullText = getString(R.string.unlocked_premium_features),
            highlightTexts = listOf(getString(R.string.unlocked))
        )
        binding.feature1.text = feature1
        val feature2 = createHighlightedSpannable(
            fullText = getString(R.string.exclusive_tools_to_enhance),
            highlightTexts = listOf(getString(R.string.exclusive_tools))
        )
        binding.feature2.text = feature2
        val feature3 = createHighlightedSpannable(
            fullText = getString(R.string.no_payment_cancel_refund),
            highlightTexts = listOf(getString(R.string.no_payment), getString(R.string.refund))
        )
        binding.feature3.text = feature3


        val description = createHighlightedSpannable(
            fullText = getString(R.string.trial_conditions,priceMonthAnnual,priceYear),
            highlightTexts = listOf(getString(R.string.pay_for_renewal))
        )
        binding.tvDescription.text = description

        binding.btnFreeTrial.post {
            val buttonWidth = binding.btnFreeTrial.width
            val shineView = binding.shineView

            shineView.layoutParams.height = binding.btnFreeTrial.height

            val clipBounds = android.graphics.Rect(0, 0, buttonWidth, binding.btnFreeTrial.height)
            shineView.clipBounds = clipBounds

            val animator = ObjectAnimator.ofFloat(
                shineView,
                "translationX",
                -shineView.width.toFloat(),
                buttonWidth.toFloat()
            )
            animator.duration = 1250
            animator.repeatCount = ValueAnimator.INFINITE
            animator.start()
        }
        binding.btnClose.setOnClickListener {
            dismiss()
            negativeCallBack?.invoke()
        }
        binding.btnFreeTrial.setOnClickListener {
            if (isViewDestroyed) return@setOnClickListener
            positiveCallBack?.invoke()
            dismiss()
        }
    }
    fun createHighlightedSpannable(
        fullText: String,
        highlightTexts: List<String>
    ): SpannableString {
        val spannable = SpannableString(fullText)

        try {
            val highlightColor = ContextCompat.getColor(requireContext(), R.color.primaryColor)

            highlightTexts.forEach { keyword ->
                var startIndex = fullText.indexOf(keyword)
                while (startIndex >= 0) {
                    val endIndex = startIndex + keyword.length

                    // Tô màu
                    spannable.setSpan(
                        ForegroundColorSpan(highlightColor),
                        startIndex,
                        endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    // In đậm nếu cần
                        spannable.setSpan(
                            StyleSpan(Typeface.BOLD),
                            startIndex,
                            endIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                    // Tìm lần xuất hiện tiếp theo
                    startIndex = fullText.indexOf(keyword, endIndex)
                }
            }
        } catch (e: Exception) {
            Log.e("SpannableUtil", "Error creating spannable: ${e.message}")
        }

        return spannable
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
            setDimAmount(0.5f)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewDestroyed = true
        _binding = null
    }

}
