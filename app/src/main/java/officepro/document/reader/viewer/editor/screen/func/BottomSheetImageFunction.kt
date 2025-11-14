package officepro.document.reader.viewer.editor.screen.func

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.common.FunctionState
import com.ezteam.baseproject.listener.EzItemListener
import com.brian.base_iap.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.brian.base_iap.utils.TemporaryStorage
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import officepro.document.reader.viewer.editor.databinding.SelectImageDialogBinding
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BottomSheetImageFunction(
    var imagePath: String,
    var listener: EzItemListener<FunctionState>
) : DialogFragment() {
    private lateinit var binding: SelectImageDialogBinding
    private var isViewDestroyed = false
    private var isAdLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SelectImageDialogBinding.inflate(inflater, container, false)
        return binding.root
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

    override fun getTheme(): Int {
        return R.style.DialogStyle
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
        isViewDestroyed = false
        if (TemporaryStorage.isLoadAds) {
            loadNativeNomedia()
        } else {
            Log.d("BottomSheetFileFunction", "Not load Ads")
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
                    if (isViewDestroyed || !isAdded || binding == null) return

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
                    if (isViewDestroyed || !isAdded || binding == null) return

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
    private fun getImageInfo(imagePath: String): Triple<String, String, String>? {
        try {
            val file = File(imagePath)
            if (!file.exists()) return null

            // ✅ Tên file
            val name = file.name

            // ✅ Dung lượng (làm tròn, không có số thập phân)
            val sizeInBytes = file.length()
            val sizeText = when {
                sizeInBytes >= 1024 * 1024 -> String.format(Locale.getDefault(), "%.0f MB", sizeInBytes / (1024.0 * 1024))
                else -> String.format(Locale.getDefault(), "%.0f KB", sizeInBytes / 1024.0)
            }

            // ✅ Ngày tạo (dùng metadata hệ thống)
            val path = file.toPath()
            val attr = Files.readAttributes(path, BasicFileAttributes::class.java)
            val creationTime = Date(attr.creationTime().toMillis())
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val createdDate = dateFormat.format(creationTime)

            return Triple(name, sizeText, createdDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        imagePath?.let { path ->
            val info = getImageInfo(path)
            if (info != null) {
                val (name, size, date) = info

                binding.tvTitle.text = name
                binding.tvFileInfo.text = "$size | $date"
            } else {
                binding.tvTitle.text = getString(R.string.image)
                binding.tvFileInfo.text = ""
            }
        } ?: run {
            binding.tvTitle.text = getString(R.string.image)
            binding.tvFileInfo.text = ""
        }

        if (Locale.getDefault().language == "ar") {
            binding.tvTitle.gravity = Gravity.END or Gravity.CENTER_VERTICAL
        } else {
            binding.tvTitle.gravity = Gravity.START or Gravity.CENTER_VERTICAL
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
    }


    private fun initListener() {


        binding.funcRename.setOnClickListener {
            listener.onListener(FunctionState.RENAME)
            dismiss()
        }


        binding.funcDelete.setOnClickListener {
            listener.onListener(FunctionState.DELETE)
            dismiss()
        }

        binding.funcDetailFile.setOnClickListener {
            listener.onListener(FunctionState.DETAIL)
            dismiss()
        }

        binding.funcPrint.setOnClickListener {
            listener.onListener(FunctionState.PRINT)
            dismiss()
        }
    }
}