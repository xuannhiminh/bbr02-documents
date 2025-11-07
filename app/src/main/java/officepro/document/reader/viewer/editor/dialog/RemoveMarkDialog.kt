package officepro.document.reader.viewer.editor.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.RemoveMarkDialogBinding

class RemoveMarkDialog : DialogFragment() {
    override fun getTheme(): Int {
        return R.style.DialogStyle
    }
    private var _binding: RemoveMarkDialogBinding? = null
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
        _binding = RemoveMarkDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewDestroyed = false
        binding.tvTitle.text = title
        binding.tvMessage.text = message
        // loadNativeNomedia()
        binding.btnOk.setOnClickListener {
            onConfirm?.invoke()
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }
//    private fun loadNativeNomedia() {
//        val safeContext = context ?: return
//        if (SystemUtils.isInternetAvailable(safeContext)) {
//            isAdLoaded = false // reset trạng thái
//
//            binding.layoutNative.visibility = View.VISIBLE
//            val loadingView = LayoutInflater.from(safeContext)
//                .inflate(R.layout.ads_native_loading_short, null)
//            binding.layoutNative.removeAllViews()
//            binding.layoutNative.addView(loadingView)
//
//            val callback = object : NativeCallback() {
//                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
//                    super.onNativeAdLoaded(nativeAd)
//                    if (isViewDestroyed || !isAdded || _binding == null) return
//
//                    // Inflate ad view
//                    val adView = LayoutInflater.from(safeContext)
//                        .inflate(R.layout.ads_native_bot_no_media_short, null) as NativeAdView
//                    binding.layoutNative.removeAllViews()
//                    binding.layoutNative.addView(adView)
//                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
//
//                    // Cho phép đóng dialog ngoài khi ad đã load
//                    isAdLoaded = true
//                    dialog?.setCancelable(true)
//                    dialog?.setCanceledOnTouchOutside(true)
//                }
//
//                override fun onAdFailedToLoad() {
//                    super.onAdFailedToLoad()
//                    if (isViewDestroyed || !isAdded || _binding == null) return
//
//                    // Ẩn layout ad, vẫn coi là "đã load" để không block user
//                    binding.layoutNative.visibility = View.GONE
//
//                    isAdLoaded = true
//                    dialog?.setCancelable(true)
//                    dialog?.setCanceledOnTouchOutside(true)
//                }
//            }
//
//            Admob.getInstance().loadNativeAd(
//                safeContext.applicationContext,
//                getString(R.string.native_popup_all),
//                callback
//            )
//        } else {
//            // Nếu không có internet, hide ad và mở khóa dialog
//            binding.layoutNative.visibility = View.GONE
//            isAdLoaded = true
//            dialog?.setCancelable(true)
//            dialog?.setCanceledOnTouchOutside(true)
//        }
//    }
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

    fun setTitle(title: String): RemoveMarkDialog {
        this.title = title
        return this
    }

    fun setMessage(message: String): RemoveMarkDialog {
        this.message = message
        return this
    }

    fun setOnConfirmListener(callback: () -> Unit): RemoveMarkDialog {
        this.onConfirm = callback
        return this
    }
}
