package com.ezteam.ezpdflib.bottomsheet

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.activity.PdfDetailActivity
import com.ezteam.ezpdflib.databinding.LibBottomSheetDetailBinding
import com.ezteam.ezpdflib.databinding.LibBottomSheetEditBinding
import com.ezteam.ezpdflib.extension.getFileLength
import com.ezteam.ezpdflib.util.DateUtils
import com.ezteam.ezpdflib.util.PreferencesKey
import com.ezteam.ezpdflib.util.PreferencesUtils
import com.ezteam.ezpdflib.viewmodel.DetailViewmodel
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import org.apache.commons.io.FilenameUtils
import java.io.File

class BottomSheetEdit(
    var listener: ((BottomSheetDetailFunction.FunctionState) -> Unit)? = null
) : BottomSheetDialogFragment() {

    private lateinit var binding: LibBottomSheetEditBinding

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
        binding = LibBottomSheetEditBinding.inflate(inflater, container, false)
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
        binding.funcNote.setOnClickListener {
            listener?.invoke(BottomSheetDetailFunction.FunctionState.NOTE)
            dismiss()
        }

        binding.funcSignature.setOnClickListener {
            listener?.invoke(BottomSheetDetailFunction.FunctionState.SIGNATURE)
            dismiss()
        }
        binding.funcAddImage.setOnClickListener {
            listener?.invoke(BottomSheetDetailFunction.FunctionState.ADD_IMAGE)
            dismiss()
        }

        binding.funcAddText.setOnClickListener {
            listener?.invoke(BottomSheetDetailFunction.FunctionState.ADD_TEXT)
            dismiss()
        }
    }
}