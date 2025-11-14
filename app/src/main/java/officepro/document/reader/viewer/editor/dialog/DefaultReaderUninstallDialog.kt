package officepro.document.reader.viewer.editor.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.ezteam.baseproject.utils.SystemUtils
import com.brian.base_iap.utils.TemporaryStorage
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.DefaultReaderUninstallDialogBinding
import com.nlbn.ads.util.AppOpenManager

class DefaultReaderUninstallDialog : DialogFragment() {
    override fun getTheme(): Int {
        return R.style.DialogStyle
    }
    private var _binding: DefaultReaderUninstallDialogBinding? = null
    private val binding get() = _binding!!
    private var isViewDestroyed = false
    private var isAdLoaded = false
    var defaultViewer: ResolveInfo? = null
    var listener: Unit.() -> Unit = {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent) // Translucent background
        }
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DefaultReaderUninstallDialogBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewDestroyed = false
        if (TemporaryStorage.isLoadAds) {
            loadNativeNomedia()
        } else {
            Log.d("DefaultReaderUninstallDialog", "Not load Ads")
        }

            val pm = requireContext().packageManager
            val appIcon = defaultViewer?.loadIcon(pm)
            val packageName = defaultViewer?.activityInfo?.packageName

            binding.fileName.text = defaultViewer?.loadLabel(pm).toString()
            binding.fileIcon.setImageDrawable(appIcon)

            binding.btnReset.setOnClickListener {
                dismiss()
                try {
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                    listener.invoke(Unit) // Call the listener if needed
                    listener = {}
                     AppOpenManager.getInstance().disableAppResume()
                    TemporaryStorage.setTemporaryTurnOffNotificationOutApp()
                } catch (e: Exception) {
                    Log.e("DefaultReaderUninstallDialog", "Error opening app settings: ${e.message}")
                    // Handle the error, e.g., show a toast
                }
            }


        binding.btnLater.setOnClickListener {
            dismiss()
        }

        val appName = getString(R.string.app_name)
        val fullText = getString(R.string.set_pdf_reader_as_your_default_pdf_file_viewer, appName)
        val spannable = SpannableString(fullText)

        // Find the position of app name
        val startIndex = fullText.indexOf(appName)
        val endIndex = startIndex + appName.length

        if (startIndex != -1) {
            // Apply red color
            val redColor = ContextCompat.getColor(requireContext(), R.color.primaryColor)
            spannable.setSpan(ForegroundColorSpan(redColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.tvTitle.text = spannable
    }
    private fun loadNativeNomedia()     {
        val safeContext = context ?: return
        if (SystemUtils.isInternetAvailable(safeContext)) {
            isAdLoaded = false // reset trạng thái

            binding.layoutNative.visibility = View.VISIBLE
            val loadingView = LayoutInflater.from(safeContext)
                .inflate(R.layout.ads_native_loading_short, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            val callback = object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    super.onNativeAdLoaded(nativeAd)
                    if (isViewDestroyed || !isAdded || _binding == null) return

                    // Inflate ad view
                    val adView = LayoutInflater.from(safeContext)
                        .inflate(R.layout.ads_native_bot_no_media_short, null) as NativeAdView
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

}
