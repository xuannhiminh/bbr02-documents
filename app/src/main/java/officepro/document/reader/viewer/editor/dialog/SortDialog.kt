package office.pdf.document.reader.viewer.editor.dialog

import android.app.Dialog
import android.content.DialogInterface
import office.pdf.document.reader.viewer.editor.common.PresKey
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.brian.base_iap.utils.IAPUtils
import com.brian.base_iap.utils.PreferencesUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.brian.base_iap.utils.TemporaryStorage
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import office.pdf.document.reader.viewer.editor.R
import office.pdf.document.reader.viewer.editor.databinding.DefaultReaderSortDialogBinding

class SortDialog : DialogFragment() {
    override fun getTheme(): Int {
        return R.style.DialogStyle
    }
    private var _binding: DefaultReaderSortDialogBinding? = null
    private val binding get() = _binding!!

    private var callBack: ((Int) -> Unit)? = null // Thêm biến lưu callback
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DefaultReaderSortDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewDestroyed = false
        val sortState = PreferencesUtils.getInteger(PresKey.SORT_STATE, 4)
        when (sortState) {
            3 -> binding.radioGroupSort.check(R.id.radio_date)
            4 -> binding.radioGroupSort.check(R.id.radio_date_desc)
            1 -> binding.radioGroupSort.check(R.id.radio_name_asc)
            2 -> binding.radioGroupSort.check(R.id.radio_name_desc)
            5 -> binding.radioGroupSort.check(R.id.radio_size_asc)
            6 -> binding.radioGroupSort.check(R.id.radio_size_desc)
        }
        if (TemporaryStorage.isLoadAds) {
            loadNativeNomedia()
        } else {
            Log.d("SortDialog", "Not load Ads")
        }

        binding.btnOk.setOnClickListener {
            val selectedId = when (binding.radioGroupSort.checkedRadioButtonId) {
                R.id.radio_date -> 3
                R.id.radio_date_desc -> 4
                R.id.radio_name_asc -> 1
                R.id.radio_name_desc -> 2
                R.id.radio_size_asc -> 5
                R.id.radio_size_desc -> 6
                else -> 3
            }
            callBack?.invoke(selectedId) // Gọi callback nếu không null
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.ivClose.setOnClickListener {
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
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
            setDimAmount(0.5f)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewDestroyed = false
        _binding = null
    }

    // Hàm thiết lập callback
    fun setOnSortSelectedListener(callback: (Int) -> Unit) {
        this.callBack = callback
    }
}

