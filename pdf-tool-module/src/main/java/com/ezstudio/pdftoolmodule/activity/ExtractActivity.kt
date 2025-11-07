package com.ezstudio.pdftoolmodule.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.ezstudio.pdftoolmodule.PdfToolBaseActivity
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.adapter.PdfPageAdapter
import com.ezstudio.pdftoolmodule.databinding.ActivityExtractBinding
import com.ezstudio.pdftoolmodule.model.PdfPageModel
import com.ezstudio.pdftoolmodule.utils.pdftool.Thumbnail
import com.ezteam.baseproject.utils.FileSaveManager
import com.ezteam.baseproject.utils.FirebaseRemoteConfigUtil
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.ViewUtils
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
//import com.google.android.gms.ads.ez.EzAdControl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.*

class ExtractActivity : PdfToolBaseActivity<ActivityExtractBinding>(), View.OnClickListener {

    companion object {
        const val FILE_PATH = "file path"
    }

    private val filePath by lazy {
        intent?.getStringExtra(FILE_PATH) ?: ""
    }
    private val pageAdapter by lazy {
        PdfPageAdapter(this, mutableListOf())
    }

    override fun initView() {
        binding.rcvPage.adapter = pageAdapter
        loadNativeNomedia()
    }

    override fun initData() {
        lifecycleScope.launch(Dispatchers.IO) {
            Thumbnail.start(filePath, 1.0f, result = { bitmap, index ->
                lifecycleScope.launch(Dispatchers.Main) {
                    if (!isDestroyed) {
                        val page = PdfPageModel(bitmap, index)
                        pageAdapter.apply {
                            list.add(page)
                            notifyItemInserted(index)
                        }
                    }
                }
            })
        }
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
                val adView = LayoutInflater.from(this@ExtractActivity)
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

    override fun initListener() {
        super.initListener()
        binding.ivBack.setOnClickListener(this)
        binding.ivRemoveSuggest.setOnClickListener(this)
        binding.ivDone.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.iv_done -> {
                saveImage()
            }
            R.id.iv_remove_suggest -> {
                ViewUtils.collapse(binding.lnSuggest)
            }
            R.id.iv_back -> {
                onBackPressed()
            }
        }
    }

    private fun saveImage() {
        val lstImage = mutableListOf<Bitmap>()
        pageAdapter.list.forEach {
            if (it.selected) {
                lstImage.add(it.thumbnail)
            }
        }
        if (lstImage.isNullOrEmpty()) {
            toast(getString(R.string.choose_at_least))
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                toolViewModel.isLoading.postValue(true)
                val folder = "${getAppName()}/${FilenameUtils.getBaseName(filePath)}"
                    .replace(" ", "_")
                val lstImageCreated = mutableListOf<String>()
                lstImage.forEach {
                    val filePath = FileSaveManager.saveImageBitmap(
                        this@ExtractActivity,
                        folder,
                        it
                    )
                    filePath?.let {
                        lstImageCreated.add(it)
                    }
                }
                toolViewModel.isLoading.postValue(false)
                lifecycleScope.launch(Dispatchers.Main) {
                    finish()
                    openSuccessScreen(lstImageCreated)
                }
            }
        }
    }

    private fun getAppName(): String {
        val pm: PackageManager = applicationContext.packageManager
        val ai: ApplicationInfo? = try {
            pm.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        val applicationName = (if (ai != null) pm.getApplicationLabel(ai) else "(EzPdf)")
        return applicationName.toString()
    }

    override fun viewBinding(): ActivityExtractBinding {
        return ActivityExtractBinding.inflate(LayoutInflater.from(this))
    }

}