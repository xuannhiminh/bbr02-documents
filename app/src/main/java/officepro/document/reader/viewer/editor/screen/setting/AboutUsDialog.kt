package officepro.document.reader.viewer.editor.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.ezteam.baseproject.utils.SystemUtils
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.AboutUsDialogBinding
import officepro.document.reader.viewer.editor.databinding.DefaultReaderRequestDialogBinding

class AboutUsDialog : DialogFragment() {
    override fun getTheme(): Int {
        return R.style.DialogStyle
    }
    private var _binding: AboutUsDialogBinding? = null
    private val binding get() = _binding!!
    private var isViewDestroyed = false
    private var isAdLoaded = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent) // Translucent background
        }
//        dialog.setCancelable(false)
//        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = AboutUsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewDestroyed = false
        //loadNativeNomedia()
        binding.tvTitle.text = handleAppNameSpannable()
        val versionName = SystemUtils.getVersionName(requireContext())
        binding.content.text = getString(R.string.app_version, versionName)
    }
    private fun handleAppNameSpannable(): SpannableString {
        try {
            val appName = getString(R.string.app_name)
            val spannable = SpannableString(appName)

            // Find the position of app name
            val startIndex = 0
            val endIndex = appName.indexOf(' ', appName.indexOf(' ') + 1)

            // Apply red color
            val redColor = ContextCompat.getColor(requireContext(), R.color.primaryColor)
            spannable.setSpan(
                ForegroundColorSpan(redColor),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Apply bold style
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannable
        } catch (e: Exception) {
            Log.e("AboutUsDialog", "Error creating spannable app name: ${e.message}")
            return SpannableString(getString(R.string.app_name)) // Fallback to plain text
        }
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) // Dynamic height
            setGravity(Gravity.BOTTOM) // Align bottom
            setDimAmount(0.5f) // Dim background
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewDestroyed = false
        _binding = null // Prevent memory leaks
    }


}
