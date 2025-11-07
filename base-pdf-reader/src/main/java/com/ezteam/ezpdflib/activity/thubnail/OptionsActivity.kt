package com.ezteam.ezpdflib.activity.thubnail

//import com.google.android.gms.ads.ez.EzAdControl;
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.activity.BasePdfViewerActivity
import com.ezteam.ezpdflib.databinding.DeletePageBinding
import com.ezteam.ezpdflib.util.Config
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import io.reactivex.rxjava3.disposables.CompositeDisposable

class OptionsActivity : BasePdfViewerActivity() {
    protected var disposable: CompositeDisposable = CompositeDisposable()
    private var binding: DeletePageBinding? = null
    private var mapUriPage: HashMap<Int, Uri>? = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DeletePageBinding.inflate(LayoutInflater.from(this))
        setContentView(binding!!.root)
        //        EzAdControl.getInstance(this).showAds();
        initView2()// change name to 2 to avoid conflict with init view of BasePdfViewerActivity
        initListener2()
        //loadNativeNomedia()
    }
//    private fun loadNativeNomedia() {
//        if (SystemUtils.isInternetAvailable(this)) {
//            binding?.layoutNative?.visibility = View.VISIBLE
//            val loadingView = LayoutInflater.from(this)
//                .inflate(R.layout.ads_native_loading_short, null)
//            binding?.layoutNative?.removeAllViews()
//            binding?.layoutNative?.addView(loadingView)
//
//            val callback = object : NativeCallback() {
//                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
//                    super.onNativeAdLoaded(nativeAd)
//
//                    val layoutRes = R.layout.ads_native_bot_no_media_short
//                    val adView = LayoutInflater.from(this@OptionsActivity)
//                        .inflate(layoutRes, null) as NativeAdView
//
//                    binding?.layoutNative?.removeAllViews()
//                    binding?.layoutNative?.addView(adView)
//
//                    // Gán dữ liệu quảng cáo vào view
//                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
//                }
//
//                override fun onAdFailedToLoad() {
//                    super.onAdFailedToLoad()
//                    binding?.layoutNative?.visibility = View.GONE
//                }
//            }
//
//            Admob.getInstance().loadNativeAd(
//                applicationContext,
//                getString(R.string.native_options),
//                callback
//            )
//        } else {
//            binding?.layoutNative?.visibility = View.GONE
//        }
//    }
    private fun initView2() {
        if (intent != null) {
            mapUriPage =
                intent.getSerializableExtra(Config.Constant.DATA_MU_PDF_CORE) as HashMap<Int, Uri>?
        }
        if (mapUriPage == null) return
        val datas = ArrayList(
            mapUriPage!!.values
        )
        binding?.titleText?.text =  handleAppNameSpannable()

    }
    private fun handleAppNameSpannable(): SpannableString {
        try {
            val appName = getString(R.string.check_page)
            val spannable = SpannableString(appName)

            val spaceIndex = appName.indexOf(' ')
            val endIndex = if (spaceIndex != -1) appName.length else appName.length // fallback: tô hết

            val redColor = ContextCompat.getColor(this, R.color.primaryColor)
            spannable.setSpan(
                ForegroundColorSpan(redColor),
                0,
                spaceIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            return spannable
        } catch (e: Exception) {
            e.printStackTrace()
            return SpannableString(getString(R.string.check_page))
        }

    }
    private fun initListener2() {
        binding!!.icBack.setOnClickListener { v ->
            finish()
        }
    }
}