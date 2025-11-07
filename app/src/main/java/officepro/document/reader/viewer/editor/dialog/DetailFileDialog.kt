package officepro.document.reader.viewer.editor.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.DialogFragment
import com.ezteam.baseproject.listener.EzItemListener
import com.ezteam.baseproject.utils.DateUtils
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.common.FunctionState
import officepro.document.reader.viewer.editor.databinding.DetailPageDialogBinding
import officepro.document.reader.viewer.editor.model.FileModel
import officepro.document.reader.viewer.editor.screen.main.MainViewModel
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.Date
import java.util.Locale

class DetailFileDialog(
    private val fileModel: FileModel,
    var viewModel: MainViewModel) : DialogFragment() {
    override fun getTheme(): Int {
        return R.style.DialogStyle
    }
    private var _binding: DetailPageDialogBinding? = null
    private val binding get() = _binding!!
    private var onConfirm: (() -> Unit)? = null
    private var isViewDestroyed = false
    private var isAdLoaded = false

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
        _binding = DetailPageDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        isViewDestroyed = false
        if (TemporaryStorage.isLoadAds) {
            loadNativeNomedia()
        } else {
            Log.d("DetailFileDialog", "Not load Ads")
        }
        binding.tvFilename.text = fileModel.name
        binding.tvFilename.isSelected = true
        binding.tvPath.text = fileModel.path
        binding.tvPath.isSelected = true
        binding.tvViewed.text = fileModel.timeRecent ?: "-"
        binding.tvSize.text = fileModel.sizeString
        binding.tvModified.text =  DateUtils.longToDateString(
            fileModel.date, DateUtils.DATE_FORMAT_5
        )
        binding.title.text = fileModel.name
        binding.title.isSelected = true
        @SuppressLint("SetTextI18n")
        val sizeParts = fileModel.sizeString.split(" ")
        val sizeValue = sizeParts.getOrNull(0)?.toDoubleOrNull()
        val sizeUnit = sizeParts.getOrNull(1) ?: ""
        val roundedSize = if (sizeValue != null) {
            sizeValue.toInt().toString()
        } else {
            fileModel.sizeString
        }
        binding.tvFileInfo.text = "${DateUtils.longToDateString(fileModel.date, DateUtils.DATE_FORMAT_7)} | ${"$roundedSize $sizeUnit".uppercase(Locale.ROOT)}"
        val favoriteIcon = if (fileModel.isFavorite) {
            R.drawable.icon_favourite_3
        } else {
            R.drawable.icon_favourite_2
        }
        binding.starIcon.setImageResource(favoriteIcon)
        if (Locale.getDefault().language == "ar") {
            binding.tvTitle.gravity = Gravity.END or Gravity.CENTER_VERTICAL
        } else {
            binding.tvTitle.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        val fileIconRes = when {
            fileModel.path.lowercase().endsWith(".pdf") -> R.drawable.icon_main_pdf
            fileModel.path.lowercase().endsWith(".txt") -> R.drawable.icon_main_txt
            fileModel.path.lowercase().endsWith(".ppt") || fileModel.path.lowercase().endsWith(".pptx") -> R.drawable.icon_main_ppt
            fileModel.path.lowercase().endsWith(".doc") || fileModel.path.lowercase().endsWith(".docx") -> R.drawable.icon_main_word
            fileModel.path.lowercase().endsWith(".xls") || fileModel.path.lowercase().endsWith(".xlsx") || fileModel.path.lowercase().endsWith(".xlsm") -> R.drawable.icon_main_excel
            else -> R.drawable.icon_main_pdf
        }
        binding.fileIcon.setImageResource(fileIconRes)
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
    private fun initListener() {
        binding.starIcon.setOnClickListener {
            Log.d("DetailFileDialog", "binding.starIcon.setOnClickListener")
            fileModel.isFavorite = !fileModel.isFavorite
            viewModel.reactFavorite(fileModel)
            val favoriteIcon = if (fileModel.isFavorite) {
                R.drawable.icon_favourite_3
            } else {
                R.drawable.icon_favourite_2
            }
            binding.starIcon.setImageResource(favoriteIcon)
        }
    }


    fun setOnConfirmListener(callback: () -> Unit): DetailFileDialog {
        this.onConfirm = callback
        return this
    }
}
