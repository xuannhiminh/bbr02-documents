package officepro.document.reader.viewer.editor.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.RequestAllFilePermissionDialogBinding

class RequestAllFilePermissionDialog : DialogFragment() {
    override fun getTheme(): Int {
        return R.style.DialogStyle
    }
    private var _binding: RequestAllFilePermissionDialogBinding? = null
    private val binding get() = _binding!!

    private var title: String = ""
    private var message: String = ""
    private var onConfirm: (() -> Unit)? = null
    private var isViewDestroyed = false
    private var isAdLoaded = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = RequestAllFilePermissionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewDestroyed = false
        val appName = getString(R.string.app_name)
        val tvTitleText = getString(R.string.title_pdf_reader, appName)
        var spannable = SpannableString(tvTitleText)

        // Find the position of app Name
        var startIndex = tvTitleText.indexOf(appName)
        var endIndex = startIndex + appName.length

        if (startIndex != -1) {
            // Apply all caps (through custom span)
            val allCapsString = appName.uppercase()
            spannable = SpannableString(tvTitleText.replace(appName, allCapsString))

            // Recalculate start and end indices after replacing with uppercase
            startIndex = spannable.toString().indexOf(allCapsString)
            endIndex = startIndex + allCapsString.length

            // Apply red color
            val redColor = ContextCompat.getColor(requireContext(), R.color.primaryColor)
            spannable.setSpan(ForegroundColorSpan(redColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Apply text size (32sp converted to pixels)
            val textSizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                32f,
                resources.displayMetrics
            ).toInt()

            spannable.setSpan(
                AbsoluteSizeSpan(textSizePx),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        val tvPermissionExplanationText = getString(R.string.permission_explanation, appName)
        spannable = SpannableString(tvPermissionExplanationText)
        startIndex = tvPermissionExplanationText.indexOf(appName)
        endIndex = startIndex + appName.length
        if (startIndex != -1) {
            // Apply red color
            val redColor = ContextCompat.getColor(requireContext(), R.color.primaryColor)
            spannable.setSpan(ForegroundColorSpan(redColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Apply bold style
            spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        }
        binding.textPermissionExplanation.text = spannable
        binding.animationView.playAnimation()
        binding.animationView.scaleX = 1.5f
        binding.animationView.scaleY = 1.5f

        binding.buttonAllow.setOnClickListener {
            onConfirm?.invoke()
            dismiss()
        }

        binding.buttonLater.setOnClickListener {
            dismiss()
        }
    }
    override fun onCancel(dialog: DialogInterface) {
        // Chỉ cho cancel khi quảng cáo đã load
        if (!isAdLoaded) {

        } else {
            super.onCancel(dialog)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER)
            setDimAmount(0.5f)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewDestroyed = true
        _binding = null
    }

    fun setTitle(title: String): RequestAllFilePermissionDialog {
        this.title = title
        return this
    }

    fun setMessage(message: String): RequestAllFilePermissionDialog {
        this.message = message
        return this
    }

    fun setOnConfirmListener(callback: () -> Unit): RequestAllFilePermissionDialog {
        this.onConfirm = callback
        return this
    }
}
