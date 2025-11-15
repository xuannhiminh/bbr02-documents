package office.pdf.document.reader.viewer.editor.dialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.google.firebase.analytics.FirebaseAnalytics
import office.pdf.document.reader.viewer.editor.R
import office.pdf.document.reader.viewer.editor.databinding.SatisfactionDialogBinding
import office.pdf.document.reader.viewer.editor.screen.search.FeedBackActivity
import office.pdf.document.reader.viewer.editor.screen.setting.RateUsDialog

class SatisfactionDialog : DialogFragment() {
    override fun getTheme(): Int {
        return R.style.DialogStyle
    }
    private var _binding: SatisfactionDialogBinding? = null
    private val binding get() = _binding!!

    private var title: String = ""
    private var message: String = ""
    private var onConfirm: (() -> Unit)? = null
    private var isViewDestroyed = false

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private fun logEvent(firebaseAnalytic: FirebaseAnalytics, event: String) {
        firebaseAnalytic.logEvent(event, Bundle().apply {
            putString("screen", "SatisficationDialog")
        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SatisfactionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewDestroyed = false
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

            binding.btnFeedback.setOnClickListener {
                logEvent(firebaseAnalytics, "Satisfy_not_really")
                FeedBackActivity.start(requireActivity())
                dismiss()
            }

            binding.btnRateUs.setOnClickListener {
                logEvent(firebaseAnalytics, "Satisfy_love_it")
                dismiss()
                try {
                    RateUsDialog().show(parentFragmentManager, "RateUsDialog")
                } catch (e : Exception) {
                    Log.e("DefaultReaderRequestDialog", "Error showing RateUsDialog: ${e.message}")
                }
            }
            binding.ivClose.setOnClickListener {
                dismiss()
            }

        } catch (e: Exception) {

        }
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

    fun setTitle(title: String): SatisfactionDialog {
        this.title = title
        return this
    }

    fun setMessage(message: String): SatisfactionDialog {
        this.message = message
        return this
    }

    fun setOnConfirmListener(callback: () -> Unit): SatisfactionDialog {
        this.onConfirm = callback
        return this
    }
}
