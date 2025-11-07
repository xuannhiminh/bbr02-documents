package com.ezteam.ezpdflib.bottomsheet

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.KeyEvent
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.adapter.ColorAdapter
import com.ezteam.ezpdflib.adapter.FontAdapter
import com.ezteam.ezpdflib.databinding.LibBottomsheetAddTextBinding
import com.ezteam.ezpdflib.util.Config
import com.ezteam.ezpdflib.viewmodel.DetailViewmodel
import com.ezteam.ezpdflib.widget.stickerView.TextSticker
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob

class BottomSheetAddText() : BottomSheetDialogFragment() {

    private var handlerSticker: TextSticker? = null

    constructor(handlerSticker: TextSticker) : this() {
        this.handlerSticker = handlerSticker
    }

    private lateinit var binding: LibBottomsheetAddTextBinding
    private lateinit var detailViewmodel: DetailViewmodel
    var showDialogColor: ((String) -> Unit)? = null
    var colorSelectListener: ((String) -> Unit)? = null
    var fontSelectListener: ((String) -> Unit)? = null
    var textChangeListener: ((String) -> Unit)? = null

    private val colorAdapter by lazy {
        val dataColor = arrayListOf("")
        dataColor.addAll(Config.colorHexString)
        ColorAdapter(dataColor)
    }

    private lateinit var fontAdapter: FontAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (handlerSticker == null) {
            dismissAllowingStateLoss()
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
        binding = LibBottomsheetAddTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        detailViewmodel = ViewModelProvider(requireActivity()).get(DetailViewmodel::class.java)
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
        binding.rcvColor.adapter = colorAdapter
        loadListFont()
        handlerSticker?.let {
            updateUi(it)
        } ?: dismissAllowingStateLoss()
    }

    private fun loadListFont() {
        fontAdapter = FontAdapter(requireActivity(), mutableListOf())
        fontAdapter.fontSelectListener = fontSelectListener
        context?.assets?.let {
            val files: Array<String> = it.list("font") as Array<String>
            fontAdapter.clear()
            fontAdapter.addAll(files.toMutableList())
        }
        binding.rcvFont.adapter = fontAdapter
    }

    private fun initListener() {
        colorAdapter.itemSelectListener = {
            if (TextUtils.isEmpty(it)) {
                showDialogColor?.invoke(colorAdapter.colorSelect)
            } else {
                colorSelectListener?.invoke(it)
            }
        }
        binding.edtText.addTextChangedListener {
            textChangeListener?.invoke(it.toString())
        }
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

    private fun updateUi(handlerSticker: TextSticker) {
        binding.edtText.setText(handlerSticker.text.toString())
        colorAdapter.colorSelect = handlerSticker.currentTextColor
        val indexColor = Config.colorHexString.indexOfFirst {
            it == colorAdapter.colorSelect
        }
        binding.rcvColor.smoothScrollToPosition(if (indexColor == -1) 0 else indexColor + 1)

        fontAdapter.fontSelected = handlerSticker.fontName

        val indexFont = fontAdapter.list.indexOfFirst {
            it == handlerSticker.fontName
        }
        binding.rcvFont.smoothScrollToPosition(if (indexFont == -1) 0 else indexFont)
    }
}