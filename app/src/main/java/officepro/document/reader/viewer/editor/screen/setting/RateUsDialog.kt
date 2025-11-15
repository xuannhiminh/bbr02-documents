package office.pdf.document.reader.viewer.editor.screen.setting

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.DialogFragment
import com.ezteam.baseproject.animation.AnimationUtils
import com.brian.base_iap.utils.PreferencesUtils
import com.brian.base_iap.utils.TemporaryStorage
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.google.firebase.analytics.FirebaseAnalytics
import office.pdf.document.reader.viewer.editor.R
import office.pdf.document.reader.viewer.editor.databinding.RateUsDialogBinding
import office.pdf.document.reader.viewer.editor.screen.search.FeedBackActivity

class RateUsDialog : DialogFragment() {
    override fun getTheme(): Int {
        return R.style.DialogStyle
    }
    private var _binding: RateUsDialogBinding? = null
    private val binding get() = _binding!!
    private var selectedRating = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(false)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = RateUsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private fun logEvent(firebaseAnalytic: FirebaseAnalytics, event: String) {
        firebaseAnalytic.logEvent(event, Bundle().apply {
            putString("screen", "RateUsDialog")
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRate.isEnabled = false
        binding.btnRate.alpha = 0.5f

        setupStarRating()
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RateUsDialog", "Error showing RateUsDialog: ${e.message}", e)
        }
            binding.btnRate.setOnClickListener {
            if (selectedRating == 0) return@setOnClickListener
            logEvent(firebaseAnalytics, "selected_rating_${selectedRating}")
            if (selectedRating < 5) {
                FeedBackActivity.start(requireActivity())
                dismiss()
            }  else {
                PreferencesUtils.putBoolean("SHOW_SATISFIED_DIALOG", false)
                TemporaryStorage.isRateFullStar = true
                launchInAppReview()
            }
        }
    }

    private fun launchInAppReview() {
        try {
            val manager = ReviewManagerFactory.create(requireContext())
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // We got the ReviewInfo object
                    val reviewInfo = task.result
                    val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                    flow.addOnCompleteListener { _ ->
                        showSuccessDialog()
                        Log.d("RateUsDialog", "launchInAppReview: success")
                    }
                } else {
                    showSuccessDialog()
                    @ReviewErrorCode val reviewErrorCode =
                        (task.exception as ReviewException).errorCode
                    Log.d("RateUsDialog", "launchInAppReview: $reviewErrorCode")
                }
            }
        } catch (e: Exception) {
            showSuccessDialog()
            Log.e("RateUsDialog", "Error launching in-app review: ${e.message}", e)
        }
    }
    private fun showSuccessDialog() {
        val dialog = FeedBackSucessDialog()
        dialog.listener = {
            dismiss()
        }
        try {
            dialog.show(this.parentFragmentManager, "FeedBackSuccessDialog")
        } catch (e : Exception)
        {
            Log.e("DefaultReaderRequestDialog", "Error showing FeedBackSuccessDialog: ${e.message}")
        }
    }
    private fun setupStarRating() {
        val stars = listOf(
            binding.star1,
            binding.star2,
            binding.star3,
            binding.star4,
            binding.star5
        )

        stars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                selectedRating = index + 1
                updateStarUI(selectedRating)

                binding.btnRate.isEnabled = true
                binding.btnRate.alpha = 1f
            }
        }
    }

    private fun updateStarUI(rating: Int) {
        val stars = listOf(
            binding.star1,
            binding.star2,
            binding.star3,
            binding.star4,
            binding.star5
        )

        val bounce = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)

        stars.forEachIndexed { index, imageView ->
            if (index < rating) {
                imageView.setImageResource(R.drawable.icon_favourite)
                imageView.startAnimation(bounce)
            } else {
                imageView.setImageResource(R.drawable.icon_rate)
            }
        }

        when (rating) {
            1 -> {
                binding.emojiHeader.text = "😢"
                updateTexts(R.string.we_sorry, R.string.your_feedback_is_welcome, R.string.send_feedback)
            }
            2 -> {
                binding.emojiHeader.text = "😞"
                updateTexts(R.string.we_sorry, R.string.your_feedback_is_welcome, R.string.send_feedback)
            }
            3 -> {
                binding.emojiHeader.text = "😮"
                updateTexts(R.string.we_sorry, R.string.your_feedback_is_welcome, R.string.send_feedback)
            }
            4 -> {
                binding.emojiHeader.text = "😊"
                updateTexts(R.string.much_appreciated, R.string.your_support_is_motivation, R.string.send_feedback)
            }
            5 -> {
                binding.emojiHeader.text = "🥰"
                updateTexts(R.string.much_appreciated, R.string.your_support_is_motivation, R.string.rate)
            }
        }
    }

    private fun updateTexts(titleResId: Int, subtitleResId: Int, btnTextResId: Int) {
        binding.titleText.setText(titleResId)
        binding.subtitleText.setText(subtitleResId)
        binding.btnRate.setText(btnTextResId)
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
        _binding = null
    }
}
