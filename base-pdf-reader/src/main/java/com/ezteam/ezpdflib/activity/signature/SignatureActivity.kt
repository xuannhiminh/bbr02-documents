package com.ezteam.ezpdflib.activity.signature

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.ezteam.baseproject.utils.FirebaseRemoteConfigUtil
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.activity.BasePdfViewerActivity
import com.ezteam.ezpdflib.adapter.SignatureAdapter
import com.ezteam.ezpdflib.databinding.LibActivitySignatureBinding
import com.ezteam.ezpdflib.extension.launchActivity
import com.ezteam.ezpdflib.util.PreferencesKey
import com.ezteam.ezpdflib.util.PreferencesUtils
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
//import com.google.android.gms.ads.ez.EzAdControl
import java.io.File

class SignatureActivity : BasePdfViewerActivity() {

    private val binding: LibActivitySignatureBinding by lazy {
        LibActivitySignatureBinding.inflate(LayoutInflater.from(this))
    }

    private var signatureAdapter: SignatureAdapter? = null
    private var lstSignature = mutableListOf<File>()

    companion object {
        const val CREATE_SIGNATURE = 12345
        const val SIGNATURE_SELECT = "signature select"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        initListener()
        loadNativeNomedia()
    }

    override fun initView() {
        super.initView()
        signatureAdapter = SignatureAdapter(this, lstSignature)
        when (resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                signatureAdapter?.isNightMode = true
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                signatureAdapter?.isNightMode = false
            }
        }
        binding.rcvSignature.adapter = signatureAdapter
        initAdapter()
    }
    private fun loadNativeNomedia() {
        if (IAPUtils.isPremium()) {
            binding.layoutNative.visibility = View.GONE
            return
        }
        if (SystemUtils.isInternetAvailable(this)) {
            binding.layoutNative.visibility = View.VISIBLE
            val loadingView = LayoutInflater.from(this)
                .inflate(R.layout.ads_native_loading_short, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            val callback = object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    super.onNativeAdLoaded(nativeAd)

                    val layoutRes = R.layout.ads_native_bot_no_media_short
                    val adView = LayoutInflater.from(this@SignatureActivity)
                        .inflate(layoutRes, null) as NativeAdView

                    binding.layoutNative.removeAllViews()
                    binding.layoutNative.addView(adView)

                    // Gán dữ liệu quảng cáo vào view
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
                }

                override fun onAdFailedToLoad() {
                    super.onAdFailedToLoad()
                    binding.layoutNative.visibility = View.GONE
                }
            }

            Admob.getInstance().loadNativeAd(
                applicationContext,
                FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("native_filedetail"),
                callback
            )
        } else {
            binding.layoutNative.visibility = View.GONE
        }
    }
    private fun initAdapter() {
        val myDir = File("${this.filesDir}/signature")
        val files = myDir.listFiles()?.let {
            it.sortedByDescending { it.lastModified() }
        }
        if (!files.isNullOrEmpty()) {
            binding.lnNoSignature.visibility = View.GONE
        } else {
            binding.lnNoSignature.visibility = View.VISIBLE
        }
        lstSignature.apply {
            clear()
            addAll(files?.toMutableList() ?: mutableListOf())
        }
        signatureAdapter?.notifyDataSetChanged()
    }

    private fun initListener() {
        signatureAdapter?.apply {
            itemSelected = {
                val intent = Intent().apply {
                    putExtra(SIGNATURE_SELECT, it.path)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

            itemDelete = { file, position ->
                showDialogConfirm(
                    getString(R.string.app_name),
                    getString(R.string.delete_signature),
                    onConfirm = {
                        if (it) {
                            if (file.exists()) {
                                file.delete()
                            }
                            lstSignature.remove(file)
                            signatureAdapter?.notifyItemRemoved(position)
                            if (!lstSignature.isNullOrEmpty()) {
                                binding.lnNoSignature.visibility = View.GONE
                            } else {
                                binding.lnNoSignature.visibility = View.VISIBLE
                            }
                        }
                    }
                )
            }
        }
        binding.imBack.setOnClickListener {
            finish()
        }

        binding.btnAdd.setOnClickListener {
            launchActivity<CreateSignatureActivity>(CREATE_SIGNATURE) {

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_SIGNATURE) {
            if (resultCode == Activity.RESULT_OK) {
                initAdapter()
            }
        }
    }

}