package com.ezteam.ezpdflib.bottomsheet

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.brian.base_iap.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.brian.base_iap.utils.TemporaryStorage
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.activity.PdfDetailActivity
import com.ezteam.ezpdflib.databinding.LibBottomSheetDetailBinding
import com.ezteam.ezpdflib.extension.getFileLength
import com.ezteam.ezpdflib.util.DateUtils
import com.ezteam.ezpdflib.util.PreferencesKey
import com.ezteam.ezpdflib.util.PreferencesUtils
import com.ezteam.ezpdflib.viewmodel.DetailViewmodel
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import org.apache.commons.io.FilenameUtils
import java.io.File

class BottomSheetDetailFunction(
    var listener: ((FunctionState) -> Unit)? = null
) : BottomSheetDialogFragment() {

    enum class FunctionState {
        CONTINUOUS_PAGE, ORIENTATION, NIGHT_MODE, GO_PAGE, FAVORITE, RENAME, NOTE, PRINT, DELETE, BROWSE_FILE,
        RATE_US, FEEDBACK, SHARE_APP, PRIVACY_POLICY, CHANGE_LANGUAGE, CLEAR_RECENT, CLEAR_FAVORITE, SHARE,
        RECENT, DETAIL, ADD_PASSWORD, REMOVE_PASSWORD, BOOKMASK, THUMBNAIL, OUTLINE, SIGNATURE, TOOL, WATERMARK,
        EXTRACT_IMAGE,  ADD_IMAGE, EDIT, ADD_TEXT, SETTING
    }

    private lateinit var binding: LibBottomSheetDetailBinding
    private lateinit var detailViewmodel: DetailViewmodel
    private var isContinuous = false
    private fun logEvent(event: String) {
        try {
            FirebaseAnalytics.getInstance(requireContext()).logEvent(event, Bundle())
        } catch (e: Exception) {
            Log.e("DefaultReaderGuideDialog", "Error initializing FirebaseAnalytics $e")
        }
    }

    private var isAdLoaded = false
    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LibBottomSheetDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
        if (TemporaryStorage.isLoadAds) {
            loadNativeNomedia()
            dialog?.setCancelable(false)
            dialog?.setCanceledOnTouchOutside(false)
        } else {
            dialog?.setCancelable(true)
            dialog?.setCanceledOnTouchOutside(true)
            Log.d("DetailFileInPDFDetailDialog", "Not load Ads")
        }
    }


    @SuppressLint("SetTextI18n")
    private fun initViews() {
        detailViewmodel = ViewModelProvider(requireActivity()).get(DetailViewmodel::class.java)

        isContinuous =
            PreferencesUtils.getBoolean(PreferencesKey.KeyPress.PDF_VIEWER_CONTINOUOUS, true)
        binding.funcContinuousPage.iconResId =
            if (!isContinuous) R.drawable.lib_ic_continuous else R.drawable.lib_ic_page_by_page
        binding.funcContinuousPage.title =
            if (!isContinuous) getString(R.string.continuous) else getString(R.string.page_by_page)

    }
    private fun loadNativeNomedia() {
        if (IAPUtils.isPremium()) {
            binding.layoutNative.visibility = View.GONE
            return
        }
        val safeContext = context ?: return
        if (!SystemUtils.isInternetAvailable(safeContext)) {
            binding.layoutNative.visibility = View.GONE
            onAdLoadFinished()
            return
        }

        binding.layoutNative.visibility = View.VISIBLE
        val loadingView = LayoutInflater.from(safeContext)
            .inflate(R.layout.ads_native_loading_short, null)
        binding.layoutNative.removeAllViews()
        binding.layoutNative.addView(loadingView)

        val callback = object : NativeCallback() {
            override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                super.onNativeAdLoaded(nativeAd)
                if (!isAdded || view == null || isRemoving) return

                val adView = LayoutInflater.from(safeContext)
                    .inflate(R.layout.ads_native_bot_no_media_short, null) as NativeAdView
                binding.layoutNative.removeAllViews()
                binding.layoutNative.addView(adView)
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)

                onAdLoadFinished()
            }

            override fun onAdFailedToLoad() {
                super.onAdFailedToLoad()
                if (!isAdded || view == null || isRemoving) return
                binding.layoutNative.visibility = View.GONE

                onAdLoadFinished()
            }
        }

        Admob.getInstance().loadNativeAd(
            safeContext.applicationContext,
            getString(R.string.native_popup_all),
            callback
        )
    }

    private fun onAdLoadFinished() {
        isAdLoaded = true
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
    }
    private fun initListener() {
        binding.funcContinuousPage.setOnClickListener {
            logEvent("func_detail_continuous_page")
            PreferencesUtils.putBoolean(
                PreferencesKey.KeyPress.PDF_VIEWER_CONTINOUOUS,
                !isContinuous
            )
            listener?.invoke(FunctionState.CONTINUOUS_PAGE)
            dismiss()
        }

        binding.funcNightMode.setOnClickListener {
            listener?.invoke(FunctionState.NIGHT_MODE)
            dismiss()
        }

        binding.funcGoToPage.setOnClickListener {
            logEvent("func_detail_goto_page")
            listener?.invoke(FunctionState.GO_PAGE)
            dismiss()
        }
        binding.funcNote.setOnClickListener {
            listener?.invoke(FunctionState.NOTE)
            dismiss()
        }

        binding.funcPrint.setOnClickListener {
            logEvent("func_detail_print")
            listener?.invoke(FunctionState.PRINT)
            dismiss()
        }

        binding.funcInfo.setOnClickListener {
            logEvent("func_detail_detail")
            listener?.invoke(FunctionState.DETAIL)
            dismiss()
        }

        binding.funcDelete.setOnClickListener {
            listener?.invoke(FunctionState.DELETE)
            dismiss()
        }

        binding.funcAddFavorite.setOnClickListener {
            listener?.invoke(FunctionState.FAVORITE)
            dismiss()
        }

        binding.funcThumbnail.setOnClickListener {
            logEvent("func_detail_thumbnail")
            listener?.invoke(FunctionState.THUMBNAIL)
            dismiss()
        }

        binding.funcOutline.setOnClickListener {
            listener?.invoke(FunctionState.OUTLINE)
            dismiss()
        }
        binding.funcSignature.setOnClickListener {
            listener?.invoke(FunctionState.SIGNATURE)
            dismiss()
        }

        binding.funcShare.setOnClickListener {
            logEvent("func_detail_share")
            listener?.invoke(FunctionState.SHARE)
            dismiss()
        }

        binding.funcTools.setOnClickListener {
            logEvent("func_detail_tool")
            listener?.invoke(FunctionState.TOOL)
            dismiss()
        }
        binding.funcEdit.setOnClickListener {
            listener?.invoke(FunctionState.EDIT)
            dismiss()
        }
        binding.funcSetting.setOnClickListener {
            listener?.invoke(FunctionState.SETTING)
            dismiss()
        }
    }
}