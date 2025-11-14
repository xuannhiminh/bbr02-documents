package com.ezstudio.pdftoolmodule.activity

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.ezstudio.pdftoolmodule.PdfToolBaseActivity
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.adapter.FileSuccessAdapter
import com.ezstudio.pdftoolmodule.databinding.ActivityToolSuccessBinding
import com.ezstudio.pdftoolmodule.model.FileModel
import com.ezstudio.pdftoolmodule.utils.FileUtils
import com.ezteam.baseproject.extensions.launchActivity
import com.ezteam.baseproject.extensions.openFile
import com.ezteam.baseproject.extensions.share
import com.ezteam.baseproject.utils.DateUtils
import com.brian.base_iap.utils.FirebaseRemoteConfigUtil
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.baseproject.utils.PresKey
import com.ezteam.baseproject.utils.SystemUtils
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.ConsentHelper
//import com.google.android.gms.ads.ez.EzAdControl
//import com.google.android.gms.ads.ez.listenner.NativeAdListener
//import com.google.android.gms.ads.ez.nativead.AdmobNativeAdView
import org.apache.commons.io.FilenameUtils
import java.io.File


class SuccessActivity : PdfToolBaseActivity<ActivityToolSuccessBinding>(), View.OnClickListener {

    companion object {
        fun start(context: Context) {
            context.launchActivity<SuccessActivity> {}
        }
    }

    private val adapter by lazy {
        FileSuccessAdapter(this, toolViewModel.lstFileSuccess)
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
//        EzAdControl.getInstance(this).showAdsWithoutCapping()
        toolViewModel.getAllFileInDevice()
        if (toolViewModel.lstFileSuccess.isNotEmpty()){
            binding.tvPath.text =
                "${getString(R.string.file_created_at)}: ${File(toolViewModel.lstFileSuccess[0]).parentFile.path}"
        }
        loadNativeNomedia()
    }
    private fun loadNativeNomedia() {
        if (IAPUtils.isPremium()) {
            binding.layoutNative.visibility = View.GONE
            return
        }

        if (!SystemUtils.isInternetAvailable(this)) {
            binding.layoutNative.visibility = View.GONE
            return
        }

        binding.layoutNative.visibility = View.VISIBLE
        val loadingView = LayoutInflater.from(this)
            .inflate(R.layout.ads_native_loading_short, null)
        binding.layoutNative.removeAllViews()
        binding.layoutNative.addView(loadingView)

        val callback = object : NativeCallback() {
            override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                super.onNativeAdLoaded(nativeAd)

                // Kiểm tra an toàn trước khi update UI
                if (isFinishing || isDestroyed || binding.layoutNative == null) return

                val layoutRes = R.layout.ads_native_bot_no_media_short
                val adView = LayoutInflater.from(this@SuccessActivity)
                    .inflate(layoutRes, null) as NativeAdView

                binding.layoutNative.removeAllViews()
                binding.layoutNative.addView(adView)

                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
            }

            override fun onAdFailedToLoad() {
                super.onAdFailedToLoad()
                if (isFinishing || isDestroyed || binding.layoutNative == null) return
                binding.layoutNative.visibility = View.GONE
            }
        }

        Admob.getInstance().loadNativeAd(
            applicationContext,
            FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("native_filedetail"),
            callback
        )
    }
    override fun initData() {
        adapter.list.forEach {
            FileUtils.scanFile(this@SuccessActivity, it, null)
        }
        binding.rcvFile.adapter = adapter
        if (adapter.list.size == 1 && FilenameUtils.getExtension(adapter.list[0]) == "pdf") {
            fillDataPdfFile()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fillDataPdfFile() {
        binding.cardFileInfo.visibility = View.VISIBLE
        binding.rcvFile.visibility = View.GONE
        val pdfFile = File(adapter.list[0])
        binding.tvFileTitle.text = FilenameUtils.getBaseName(pdfFile.path)
        binding.tvFileCreateDate.text =
            "${
                DateUtils.longToDateString(
                    pdfFile.lastModified(),
                    DateUtils.DATE_FORMAT_7
                )
            } | ${FileModel.getFileLength(pdfFile.length().toDouble())}"
        binding.cardFileInfo.visibility = View.VISIBLE
    }

    override fun initListener() {
        adapter.apply {
            shareListener = {
                val shareType = if (FilenameUtils.getExtension(it) == "pdf") {
                    "application/pdf"
                } else {
                    "image/png"
                }
                File(it).share(this@SuccessActivity, shareType)
            }

            itemClickListener = {
                if (FilenameUtils.getExtension(it) == "pdf") {
                    toolViewModel.openFile.value = it
                } else {
                    File(it).openFile(this@SuccessActivity)
                }
            }
        }
        binding.ivClose.setOnClickListener(this)
        binding.cardFileInfo.setOnClickListener(this)
        binding.ivShare.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_share -> {
                File(adapter.list[0]).share(this, "application/pdf")
            }
            R.id.card_file_info -> {
                toolViewModel.openFile.value = adapter.list[0]
            }
            R.id.iv_close -> {
                finish()

            }
        }
    }
    override fun viewBinding(): ActivityToolSuccessBinding {
        return ActivityToolSuccessBinding.inflate(LayoutInflater.from(this))
    }


}