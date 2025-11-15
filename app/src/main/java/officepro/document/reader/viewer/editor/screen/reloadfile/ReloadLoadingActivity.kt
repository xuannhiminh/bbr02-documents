package office.pdf.document.reader.viewer.editor.screen.reloadfile


import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieDrawable
import com.ezteam.baseproject.activity.BaseActivity
import com.brian.base_iap.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.brian.base_iap.utils.TemporaryStorage
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import office.pdf.document.reader.viewer.editor.R
import office.pdf.document.reader.viewer.editor.databinding.ActivityReloadingBinding
import office.pdf.document.reader.viewer.editor.screen.main.MainViewModel
import com.brian.base_iap.utils.FirebaseRemoteConfigUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.android.ext.android.inject
import kotlin.coroutines.resume

class ReloadLoadingActivity : BaseActivity<ActivityReloadingBinding>() {
    private val viewModel by inject<MainViewModel>()
    companion object {

        const val TAG = "ReloadLoadingActivity"
        fun start(activity: FragmentActivity) {
            activity.intent.data?.let {
                activity.intent.apply {
                    setClass(activity, ReloadLoadingActivity::class.java)
                }
                activity.startActivity(activity.intent)
            } ?: kotlin.run {
                val intent = Intent(activity, ReloadLoadingActivity::class.java)
                activity.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        binding.animationView.playAnimation()
        binding.animationView.apply {
            setAnimation(R.raw.loading_file)
            repeatCount = LottieDrawable.INFINITE
            speed = 2.0f
            playAnimation()
        }
        startPercentageCounter()
    }

    private var percentAnimator: ValueAnimator? = null

    private fun startPercentageCounter() {
        percentAnimator = ValueAnimator.ofInt(0, 99).apply {
            duration = FirebaseRemoteConfigUtil.getInstance().getDurationReloadingFile()
            addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                binding.percentText.text = "$value%"
            }
            start()
        }
    }

    private fun preloadLanguageNativeAd() {
        if (IAPUtils.isPremium()) {
            Log.w(TAG, "Skipping native ad preload for reload file success due to premium")
            return
        }
        Log.i(TAG, "starting native ad preload for reload success screen")
        TemporaryStorage.nativeAdPreload = null
        TemporaryStorage.isLoadingNativeAdsLanguage = true
        val startupTime = System.currentTimeMillis()
        val callBack = object : NativeCallback() {
            override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                super.onNativeAdLoaded(nativeAd)
                Log.i(TAG, "Native ad loaded for reload success screen loaded in ${System.currentTimeMillis() - startupTime} ms")
                TemporaryStorage.isLoadingNativeAdsLanguage = false
                TemporaryStorage.nativeAdPreload = nativeAd
                TemporaryStorage.callbackNativeAdsLanguage.invoke(nativeAd)
            }

            override fun onAdFailedToLoad() {
                super.onAdFailedToLoad()
                Log.e(TAG, "Failed to load native ad for reload success screen")
                TemporaryStorage.isLoadingNativeAdsLanguage = false
                TemporaryStorage.nativeAdPreload = null
                TemporaryStorage.callbackNativeAdsLanguage.invoke(null)
            }
        }

        Admob.getInstance().loadNativeAd(
            applicationContext,
            FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("native_language"),
            callBack
        )
    }


    override fun initData() {
        lifecycleScope.launch {
            val startTime = System.currentTimeMillis()

            val loadFileDeferred = async { migrateFileDataAndHandleIntentOpeningFile() }
            val loadAdsDeferred = async {
                if (FirebaseRemoteConfigUtil.getInstance().isShowAdsReloadFileInter()) {
                    loadInterstitialAd(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_reload_file_success"))
                } else null
            }

            loadFileDeferred.await()
            val interAd = loadAdsDeferred.await()
            val elapsed = System.currentTimeMillis() - startTime
            val remain = 2000L - elapsed  // còn thiếu bao nhiêu ms để đủ 2s

            if (remain > 0) {
                delay(remain)
            }
            // Cancel animation và set 100%
            percentAnimator?.cancel()
            percentAnimator = null
            binding.percentText.text = "100%"

            showAdsInterstitial(interAd)
        }
    }

    private suspend fun migrateFileDataAndHandleIntentOpeningFile() {
        if (isAcceptManagerStorage()) {
            viewModel.migrateFileData()
        } else {
            viewModel.addSameFilesInternal()
            Log.w(TAG, "Skipping migration, no storage permission")
        }
    }

    @Deprecated("Deprecated in Android 13, but still needed for API < 33")
    override fun onBackPressed() {
        // Block back button
        // If you want to allow in some cases, put condition here
    }

    private suspend fun loadInterstitialAd(idAds: String): InterstitialAd? =
        withTimeoutOrNull(FirebaseRemoteConfigUtil.getInstance().getDurationReloadingFile()+3000L) {
            suspendCancellableCoroutine<InterstitialAd?> { cont ->
                val startTime = System.currentTimeMillis()

                if (IAPUtils.isPremium()) {
                    Log.w(TAG, "Skipping open ads load due to premium or no consent")
                    if (cont.isActive) cont.resume(null)
                    return@suspendCancellableCoroutine
                }

                Log.i(TAG, "Loading interstitial started at $startTime")
                if (!SystemUtils.isInternetAvailable(this@ReloadLoadingActivity)) {
                    Log.e(TAG, "No internet, skipping ad load")
                    if (cont.isActive) cont.resume(null)
                    return@suspendCancellableCoroutine
                }

                val interCallback = object : AdCallback() {
                    override fun onInterstitialLoad(ad: InterstitialAd?) {
                        Log.i(TAG, "Inter Ad loaded in ${System.currentTimeMillis() - startTime} ms")
                        if (cont.isActive) cont.resume(ad)
                    }

                    override fun onInterstitialLoadFaild() {
                        Log.e(TAG, "Inter Ad failed to load")
                        if (cont.isActive) cont.resume(null)
                    }
                    override fun onAdFailedToLoad(var1: LoadAdError?) {
                        Log.e(TAG, "Inter Ad failed to load")
                        if (cont.isActive) cont.resume(null)
                    }
                }
                Admob.getInstance().setOpenActivityAfterShowInterAds(false)
                Admob.getInstance().loadInterAds(this@ReloadLoadingActivity, idAds, interCallback)


                // Cleanup if coroutine is cancelled by timeout or otherwise
                cont.invokeOnCancellation {
                    Log.w(TAG, "Inter Ad load coroutine cancelled or timed out")
                    // If you have any explicit unregister API, call it here
                }
            }
        }?.also {
            // If we got here with non-null, it’s a real ad.
            Log.i(TAG, "loadInterstitialAd returned a valid ad")
        } ?: run {
            // Timeout (or failure) branch
            Log.w(TAG, "Inter Ad load timed out (15s) or failed")
            null
        }


    private fun showAdsInterstitial(interstitialAd: InterstitialAd?) {
        Log.i(TAG, "showAdsInterstitial called with ad: $interstitialAd")
        Admob.getInstance().showInterAds(this, interstitialAd, object : AdCallback() {
            override fun onNextAction() {
                super.onNextAction()
                navigateToNextScreen()
            }
        })
    }



    private fun navigateToNextScreen() {
        ReloadFileSuccessActivity.start(this@ReloadLoadingActivity)
        finish()
    }

    override fun initListener() {}
    override fun viewBinding(): ActivityReloadingBinding =
        ActivityReloadingBinding.inflate(LayoutInflater.from(this))
}