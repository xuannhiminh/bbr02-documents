package com.ezteam.ezpdflib.bottomsheet

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.activity.Mode
import com.ezteam.ezpdflib.adapter.ColorAdapter
import com.ezteam.ezpdflib.databinding.LibBottomsheetAnnotationBinding
import com.ezteam.ezpdflib.model.AnnotationValue
import com.ezteam.ezpdflib.util.Config
import com.ezteam.ezpdflib.util.PreferencesUtils
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.bottomsheet.BottomSheetDialog

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob


class BottomSheetAnnotation(var mode: Mode, var showDilogColor: (String) -> Unit) :
    BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: LibBottomsheetAnnotationBinding
    private lateinit var valueAnnotation: AnnotationValue
    private val colorAdapter by lazy {
        val dataColor = arrayListOf("")
        dataColor.addAll(Config.colorHexString)
        ColorAdapter(dataColor)
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
        binding = LibBottomsheetAnnotationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
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

    private fun initViews() {
        binding.rcvColor.adapter = colorAdapter
        try {
            valueAnnotation = PreferencesUtils.getAnnotation(Config.getPreferencesKeyByMode(mode))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        when (mode) {
            Mode.Ink -> {
                valueAnnotation.let {
                    binding.tvValueThickness.text = (it.thickness + 5f).toString() + "pt"
                    binding.tvValueTrasnparency.text = it.transparency.toString() + "pt"
                    binding.sbThickness.progress = it.thickness
                    binding.sbTrasnparency.progress = it.transparency
                }
            }
            Mode.Strikeout, Mode.Unline, Mode.HighLight -> {
                binding.llThickness.visibility = View.GONE
            }

            else -> {}
        }

        colorAdapter.colorSelect = valueAnnotation.color
        val indexSelect = Config.colorHexString.indexOfFirst {
            it == colorAdapter.colorSelect
        }
        binding.rcvColor.smoothScrollToPosition(if (indexSelect == -1) 0 else indexSelect)

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
        colorAdapter.itemSelectListener = {
            if (TextUtils.isEmpty(it)) {
                showDilogColor.invoke(colorAdapter.colorSelect)
                dismiss()
            } else {
                valueAnnotation.color = it
                PreferencesUtils.setAnnotation(
                    valueAnnotation,
                    Config.getPreferencesKeyByMode(mode)
                )
            }
        }

        binding.sbThickness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvValueThickness.text = (progress + 5f).toString() + "pt"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    valueAnnotation.thickness = it.progress + 5
                    PreferencesUtils.setAnnotation(
                        valueAnnotation,
                        Config.getPreferencesKeyByMode(mode)
                    )
                }
            }

        })

        binding.sbTrasnparency.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvValueTrasnparency.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    valueAnnotation.transparency = it.progress
                    PreferencesUtils.setAnnotation(
                        valueAnnotation,
                        Config.getPreferencesKeyByMode(mode)
                    )
                }
            }

        })
    }

    override fun onClick(view: View) {

    }

}