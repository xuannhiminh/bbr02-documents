package officepro.document.reader.viewer.editor.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.DialogFragment
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.InputNameDialogBinding
import officepro.document.reader.viewer.editor.screen.setting.FeedBackSucessDialog

class RenameDialog : DialogFragment() {
    override fun getTheme(): Int {
        return R.style.DialogStyle
    }
    private var _binding: InputNameDialogBinding? = null
    private val binding get() = _binding!!

    private var fileName: String = ""
    private var callBack: ((String) -> Unit)? = null

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

    override fun onCancel(dialog: DialogInterface) {
        // Chỉ cho cancel khi quảng cáo đã load
        if (!isAdLoaded) {

        } else {
            super.onCancel(dialog)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = InputNameDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewDestroyed = false
        binding.editInputName.setText(fileName)
        binding.editInputName.selectAll()
        if (TemporaryStorage.isLoadAds) {
            loadNativeNomedia()
        } else {
            Log.d("RenameDialog", "Not load Ads")
        }
        binding.btnOk.setOnClickListener {
            val newName = binding.editInputName.text.toString().trim()
            if (newName.isNotEmpty()) {
                callBack?.invoke(newName)
                dismiss()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
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
            setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER)
            setDimAmount(0.5f)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewDestroyed = true
        _binding = null
    }

    fun setFileName(fileName: String): RenameDialog {
        this.fileName = fileName
        return this
    }

    fun setOnRenameListener(callback: (String) -> Unit): RenameDialog {
        this.callBack = callback
        return this
    }
}
