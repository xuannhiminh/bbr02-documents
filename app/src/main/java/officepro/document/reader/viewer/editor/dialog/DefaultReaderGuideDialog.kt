package officepro.document.reader.viewer.editor.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
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
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.brian.base_iap.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.DefaultReaderGuideDialogBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DefaultReaderGuideDialog : DialogFragment() {

    companion object {
        private const val ARG_FILE_TYPE = "file_type"

        fun newInstance(fileType: String): DefaultReaderGuideDialog {
            val dialog = DefaultReaderGuideDialog()
            val args = Bundle()
            args.putString(ARG_FILE_TYPE, fileType)
            dialog.arguments = args
            return dialog
        }
    }
    private val fileType: String by lazy {
        arguments?.getString(ARG_FILE_TYPE, "pdf") ?: "pdf"
    }
    override fun getTheme(): Int {
        return R.style.DialogStyle
    }
    private var _binding: DefaultReaderGuideDialogBinding? = null
    private val binding get() = _binding!!
    private var isViewDestroyed = false
    private var isAdLoaded = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent) // Translucent background
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DefaultReaderGuideDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewDestroyed = false
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        } catch (e: Exception) {
            Log.e("DefaultReaderGuideDialog", "Error initializing FirebaseAnalytics $e")
        }

        if (TemporaryStorage.isLoadAds) {
            loadNativeNomedia()
        } else {
            Log.d("Load Ads", "Not load Ads")
        }
        binding.btnSetDefault.setOnClickListener {
            dismiss()
            redirectToDefaultAppSettings()
        }
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        val appName = getString(R.string.app_name)
        val alwaysText = getString(R.string.always)
        val tvTitleText = getString(R.string.select_pdf_reader_icon_and_click_always_to_set_it_as_default, appName, alwaysText)
        var spannable = SpannableString(tvTitleText)

        // Find the position of app Name
        var startIndex = tvTitleText.indexOf(appName)
        var endIndex = startIndex + appName.length

        if (startIndex != -1) {
            // Apply red color
            val redColor = ContextCompat.getColor(requireContext(), R.color.primaryColor)
            spannable.setSpan(ForegroundColorSpan(redColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

         startIndex = tvTitleText.indexOf(alwaysText)
         endIndex = startIndex + alwaysText.length

        if (startIndex != -1) {
            // Apply red color
            val redColor = ContextCompat.getColor(requireContext(), R.color.primaryColor)
            spannable.setSpan(ForegroundColorSpan(redColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }


        binding.tvTitle.text = spannable

        val textGuide1 = getString(R.string.select_this_pdf_reader_icon_in_the_app_list, appName)
        spannable = SpannableString(textGuide1)
        startIndex = textGuide1.indexOf(appName)
        endIndex = startIndex + appName.length

        if (startIndex != -1) {
            // Apply bold style
            spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            // Apply red color
            val redColor = ContextCompat.getColor(requireContext(), R.color.primaryColor)
            spannable.setSpan(ForegroundColorSpan(redColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.textGuide1.text = spannable


        val textGuide2 = getString(R.string.click_always_to_set_it_as_the_default_reader, alwaysText)
        spannable = SpannableString(textGuide2)
        startIndex = textGuide2.indexOf(alwaysText)
        endIndex = startIndex + alwaysText.length

        if (startIndex != -1) {
            // Apply bold style
            spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            // Apply red color
            val redColor = ContextCompat.getColor(requireContext(), R.color.primaryColor)
            spannable.setSpan(ForegroundColorSpan(redColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.textGuide2.text = spannable


    }
    private fun loadNativeNomedia() {
        if (IAPUtils.isPremium()) {
            binding.layoutNative.visibility = View.GONE
            return
        }
        val safeContext = context ?: return
        if (SystemUtils.isInternetAvailable(safeContext)) {
            isAdLoaded = false // reset trạng thái

            binding.layoutNative.visibility = View.VISIBLE
            val loadingView = LayoutInflater.from(safeContext)
                .inflate(R.layout.ads_native_loading, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            val callback = object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    super.onNativeAdLoaded(nativeAd)
                    if (isViewDestroyed || !isAdded || _binding == null) return

                    // Inflate ad view
                    val adView = LayoutInflater.from(safeContext)
                        .inflate(R.layout.ads_native_bot_no_media, null) as NativeAdView
                    binding.layoutNative.removeAllViews()
                    binding.layoutNative.addView(adView)
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)

                    // Cho phép đóng dialog ngoài khi ad đã load
                    isAdLoaded = true
                    dialog?.setCancelable(true)
                    dialog?.setCanceledOnTouchOutside(true)
                }

                override fun onAdFailedToLoad() {
                    super.onAdFailedToLoad()
                    if (isViewDestroyed || !isAdded || _binding == null) return

                    // Ẩn layout ad, vẫn coi là "đã load" để không block user
                    binding.layoutNative.visibility = View.GONE

                    isAdLoaded = true
                    dialog?.setCancelable(true)
                    dialog?.setCanceledOnTouchOutside(true)
                }
            }

            Admob.getInstance().loadNativeAd(
                safeContext.applicationContext,
                getString(R.string.native_popup_all),
                callback
            )
        } else {
            // Nếu không có internet, hide ad và mở khóa dialog
            binding.layoutNative.visibility = View.GONE
            isAdLoaded = true
            dialog?.setCancelable(true)
            dialog?.setCanceledOnTouchOutside(true)
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


//    private fun showOpenWithDialog() {
//        val uri = FileProvider.getUriForFile(requireContext(),
//            "${requireContext().packageName}.provider", fileModel.file)
//
//        val intent = Intent(Intent.ACTION_VIEW).apply {
//            setDataAndType(uri, "application/pdf")
//            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        }
//
//        try {
//            requireContext().startActivity(intent)
//        } catch (e: ActivityNotFoundException) {
//            Toast.makeText(requireContext(), "No app found to open PDF", Toast.LENGTH_SHORT).show()
//        }
//    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private fun logEvent(event: String) {
        firebaseAnalytics.logEvent(event, Bundle())
    }

    private fun redirectToDefaultAppSettings() {
        try {
            logEvent("set_default_reader_${fileType}")

            val mimeType = when (fileType.lowercase()) {
                "pdf" -> "application/pdf"
                "word" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                "excel" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                "ppt" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                else -> "*/*"
            }
            // fake uri để trigger Open With
            val uri = Uri.parse("content://dummy.${fileType.lowercase()}")
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra("${context?.packageName}.isToSetDefaultReader", true)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
