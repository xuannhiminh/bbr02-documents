package office.pdf.document.reader.viewer.editor.screen.reloadfile

//import com.google.android.gms.ads.ez.EzAdControl
//import com.google.android.gms.ads.ez.listenner.ShowAdCallback
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.brian.base_iap.utils.FirebaseRemoteConfigUtil
import com.brian.base_iap.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.brian.base_iap.utils.TemporaryStorage
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import office.pdf.document.reader.viewer.editor.R
import office.pdf.document.reader.viewer.editor.databinding.ActivityReloadFileBinding
import office.pdf.document.reader.viewer.editor.screen.base.PdfBaseActivity
import office.pdf.document.reader.viewer.editor.screen.main.MainActivity
import java.util.concurrent.TimeUnit
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.xml.KonfettiView
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size

class ReloadFileSuccessActivity : PdfBaseActivity<ActivityReloadFileBinding>() {
    companion object {
        private const val TAG = "ReloadFileActivity"

        fun start(activity: FragmentActivity) {
            activity.intent.data?.let {
                activity.intent.apply {
                    setClass(activity, ReloadFileSuccessActivity::class.java)
                }
                activity.startActivity(activity.intent)
            } ?: kotlin.run {
                val intent = Intent(activity, ReloadFileSuccessActivity::class.java).apply {
                }
                activity.startActivity(intent)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        val konfettiView = findViewById<KonfettiView>(R.id.konfettiView)
        // Define a confetti burst party:
        val party = Party(
            speed = 0f,           // initial speed (0 to let maxSpeed handle it)
            maxSpeed = 30f,       // max random speed
            damping = 0.9f,       // deceleration
            spread = 360,         // full circle spread
            colors = listOf(      // confetti colors
                0xFFFCE18A.toInt(),  // yellow
                0xFFFF726D.toInt(),  // red/orange
                0xFFF4306D.toInt(),  // pink
                0xFFB48DEF.toInt()   // purple
            ),
            shapes = listOf(Shape.Circle, Shape.Square),
            position = Position.Relative(0.5, 0.5),
            emitter = Emitter(Long.MAX_VALUE, TimeUnit.MILLISECONDS).perSecond(20)
        )
        konfettiView.start(party)

        currentFileTypeIndex = 0
        showNextDefaultReaderDialog(true)
    }

    override fun initData() {
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
        Log.d(TAG, "loadNativeNomedia: TemporaryStorage.nativeAdPreload " + TemporaryStorage.nativeAdPreload)
        if (TemporaryStorage.nativeAdPreload != null) {
            if (isFinishing || isDestroyed) {
                TemporaryStorage.nativeAdPreload = null
                TemporaryStorage.isLoadingNativeAdsLanguage = false
                TemporaryStorage.callbackNativeAdsLanguage = {  }
                return
            }

            val adView = LayoutInflater.from(this)
                .inflate(R.layout.ads_native_bot, null) as NativeAdView
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(adView)
            Admob.getInstance().pushAdsToViewCustom(TemporaryStorage.nativeAdPreload, adView)
            TemporaryStorage.nativeAdPreload = null
            return
        } else if (TemporaryStorage.isLoadingNativeAdsLanguage) {
            Log.d(TAG, "loadNativeNomedia: TemporaryStorage.isLoadingNativeAdsLanguage " + TemporaryStorage.isLoadingNativeAdsLanguage)
            val loadingView = LayoutInflater.from(this)
                .inflate(R.layout.ads_native_bot_loading, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            TemporaryStorage.callbackNativeAdsLanguage = fun(nativeAd: NativeAd?) {
                // This return only exits the function literal, not the enclosing Activity
                if (isFinishing || isDestroyed) return

                if (nativeAd != null) {
                    Log.i("LanguageActivity", "Native ad loaded while waiting")
                    TemporaryStorage.isLoadingNativeAdsLanguage = false

                    // Inflate and bind your NativeAdView
                    val adView = LayoutInflater.from(this)
                        .inflate(R.layout.ads_native_bot, null) as NativeAdView
                    binding.layoutNative.removeAllViews()
                    binding.layoutNative.addView(adView)
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
                } else {
                    Log.e("LanguageActivity", "Native ad failed to load while waiting")
                    TemporaryStorage.isLoadingNativeAdsLanguage = false

                    // Clear the callback so it won't be invoked again
                    TemporaryStorage.callbackNativeAdsLanguage = { }

                    // Hide the ad container
                    binding.layoutNative.visibility = View.GONE
                }
            }

            return
        } else {
            val loadingView = LayoutInflater.from(this)
                .inflate(R.layout.ads_native_bot_loading, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            val callback = object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    if (isFinishing || isDestroyed) return
                    val adView = LayoutInflater.from(this@ReloadFileSuccessActivity)
                        .inflate(R.layout.ads_native_bot, null) as NativeAdView
                    binding.layoutNative.removeAllViews()
                    binding.layoutNative.addView(adView)
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
                }

                override fun onAdFailedToLoad() {
                    if (isFinishing || isDestroyed) return
                    binding.layoutNative.visibility = View.GONE
                }
            }

            Admob.getInstance().loadNativeAd(
                applicationContext,
                FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("native_reload_file_success"),
                callback
            )
        }

    }


    override fun initListener() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.ivHome.setOnClickListener { openSplashWithWhereToOpen(R.id.ivHome) }
        binding.ivRecent.setOnClickListener { openSplashWithWhereToOpen(R.id.ivRecent) }
        binding.ivBookmarks.setOnClickListener { openSplashWithWhereToOpen(R.id.ivBookmarks) }
        binding.ivEdit.setOnClickListener { openSplashWithWhereToOpen(R.id.ivEdit) }

    }
    private fun openSplashWithWhereToOpen(viewId: Int) {
        val intent = Intent(this@ReloadFileSuccessActivity, MainActivity::class.java).apply {
            putExtra("${packageName}.isFromReloadFileSuccess", true)
            putExtra("${packageName}.whereToOpen", viewId)
        }
        startActivity(intent)
        // MainActivity.start(this@ReloadFileSuccessActivity)
        finish()
    }

    override fun viewBinding(): ActivityReloadFileBinding {
        return ActivityReloadFileBinding.inflate(LayoutInflater.from(this))
    }

    override fun onResume() {
        super.onResume()
        loadNativeNomedia()
    }

    override fun onStop() {
        super.onStop()
        TemporaryStorage.nativeAdPreload = null
    }

    override fun onDestroy() {
        super.onDestroy()
        TemporaryStorage.callbackNativeAdsLanguage = {}
        TemporaryStorage.isLoadingNativeAdsLanguage = false
    }

}