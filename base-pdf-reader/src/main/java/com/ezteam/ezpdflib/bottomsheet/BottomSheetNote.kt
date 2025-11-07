package com.ezteam.ezpdflib.bottomsheet

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.baseproject.utils.PresKey
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.activity.Mode
import com.ezteam.ezpdflib.databinding.LibBottomsheetNoteBinding
import com.ezteam.ezpdflib.guide.GuideStep
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob

class BottomSheetNote(
    var listener: (Mode) -> Unit,
    private val onShowGuide: ((List<GuideStep>) -> Unit)? = null,
    private val pdfContentView: View?,
    private val acceptButton: View?
) : BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: LibBottomsheetNoteBinding

    private var isAdLoaded = false
    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LibBottomsheetNoteBinding.inflate(inflater, container, false)
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

    private fun initViews() {
        binding.funcDelete.visibility = View.GONE

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
        binding.funcBrush.setOnClickListener(this)
        binding.funcUnline.setOnClickListener(this)
        binding.funcHightlight.setOnClickListener(this)
        binding.funcStricke.setOnClickListener(this)
        binding.funcCopy.setOnClickListener(this)
        binding.funcDelete.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        var guideKey: String? = null
        var mode = Mode.Normal

        when (view.id) {
            R.id.func_brush -> {
                mode = Mode.Ink
            }
            R.id.func_unline -> {
                mode = Mode.Unline
                guideKey = PresKey.FIRST_TIME_UNDER_LINE
            }
            R.id.func_hightlight -> {
                mode = Mode.HighLight
                guideKey = PresKey.FIRST_TIME_HIGHLIGHT
            }
            R.id.func_stricke -> {
                mode = Mode.Strikeout
                guideKey = PresKey.FIRST_TIME_STRIKEOUT
            }
            R.id.func_copy -> {
                mode = Mode.CopyText
            }
            R.id.func_delete -> {
                mode = Mode.Delete
            }
        }
        showEditGuide(guideKey)
        listener.invoke(mode)
        dismiss()
    }

    private fun showEditGuide(guideKey: String?) {
        if (guideKey.isNullOrEmpty()) return

        if (PreferencesUtils.getBoolean(guideKey, false)) return

        val steps = mutableListOf<GuideStep>()
        val chooseWord = resources.getString(R.string.choose_word)
        val completeOperation = resources.getString(R.string.complete_the_operation)

        pdfContentView?.let { v ->
            steps.add(
                GuideStep(
                    targetView = v,
                    titleLines = listOf(chooseWord)
                )
            )
        }

        acceptButton?.let { btn ->
            steps.add(
                GuideStep(
                    targetView = btn,
                    titleLines = listOf(completeOperation),
                    160f
                )
            )
        }

        onShowGuide?.invoke(steps)

        PreferencesUtils.putBoolean(guideKey, true)
    }


}