package officepro.document.reader.viewer.editor.screen.uninstall

//import com.google.android.gms.ads.ez.EzAdControl
//import com.google.android.gms.ads.ez.listenner.ShowAdCallback
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ezteam.baseproject.utils.FirebaseRemoteConfigUtil
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import com.nlbn.ads.util.ConsentHelper
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.ActivityUninstallBinding
import officepro.document.reader.viewer.editor.screen.base.PdfBaseActivity
import officepro.document.reader.viewer.editor.screen.main.MainViewModel
import officepro.document.reader.viewer.editor.screen.start.SplashActivity
import org.koin.android.ext.android.inject

class UninstallActivity : PdfBaseActivity<ActivityUninstallBinding>() {
    companion object {
        private const val TAG = "UninstallActivity"

        fun start(activity: FragmentActivity) {
            activity.intent.data?.let {
                activity.intent.apply {
                    setClass(activity, UninstallActivity::class.java)
                }
                activity.startActivity(activity.intent)
            } ?:   activity.intent.extras?.let {
                activity.intent.apply {
                    setClass(activity, UninstallActivity::class.java)
                }
                activity.startActivity(activity.intent)
            } ?: kotlin.run {
                val intent = Intent(activity, UninstallActivity::class.java).apply {
                }
                activity.startActivity(intent)
            }
        }
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        AppOpenManager.getInstance().disableAppResume()
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        val highlight = getString(R.string.uninstall_highlight)
        val tvTitleText = getString(R.string.uninstall_title, highlight)
        var spannable = SpannableString(tvTitleText)

        // Find the position of app Name
        var startIndex = tvTitleText.indexOf(highlight)
        var endIndex = startIndex + highlight.length

        if (startIndex != -1) {


            // Apply all caps (through custom span)

            spannable = SpannableString(tvTitleText.replace(highlight, highlight))

            // Recalculate start and end indices after replacing with uppercase
            startIndex = spannable.toString().indexOf(highlight)
            endIndex = startIndex + highlight.length

            // Apply red color
            val redColor = ContextCompat.getColor(this, R.color.primaryColor)
            spannable.setSpan(ForegroundColorSpan(redColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Apply text size (32sp converted to pixels)
            val textSizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                16f,
                resources.displayMetrics
            ).toInt()

            spannable.setSpan(
                AbsoluteSizeSpan(textSizePx),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.title.text = spannable

        val tvPermissionExplanationText = getString(R.string.permission_explanation, highlight)
        spannable = SpannableString(tvPermissionExplanationText)
        startIndex = tvPermissionExplanationText.indexOf(highlight)
        endIndex = startIndex + highlight.length
        if (startIndex != -1) {
            // Apply red color
            val redColor = ContextCompat.getColor(this, R.color.primaryColor)
            spannable.setSpan(ForegroundColorSpan(redColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Apply bold style
            spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        }

    }

    override fun initData() {

    }
    private fun loadNativeNomedia() {
        if (IAPUtils.isPremium()) {
            binding.layoutNative.visibility = View.GONE
            return
        }
        Log.d("Load Ads", "Start load Ads")
        if (SystemUtils.isInternetAvailable(this)) {
            binding.layoutNative.visibility = View.VISIBLE
            val loadingView = LayoutInflater.from(this)
                .inflate(R.layout.ads_native_bot_loading, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            val callback = object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    super.onNativeAdLoaded(nativeAd)

                    val layoutRes = R.layout.ads_native_bot
                    val adView = LayoutInflater.from(this@UninstallActivity)
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
                FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("native_keep_user"),
                callback
            )
        } else {
            binding.layoutNative.visibility = View.GONE
        }
    }
    private var interCallback: AdCallback? = null;

    override fun initListener() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        binding.btnAgree.setOnClickListener {
            binding.btnAgree.isEnabled = false
            firebaseAnalytics.logEvent("uninstall_stay_clicked", Bundle().apply {
                putString("button_action", "stay_clicked")
                putString("screen", "UninstallActivity")
            })

            val intent = Intent(this, SplashActivity::class.java)
            intent.putExtra("${packageName}.isFromUninstall", true)
            startActivity(intent)
            finish()
        }

        binding.tvUninstall.setOnClickListener {
            binding.tvUninstall.isEnabled = false
            firebaseAnalytics.logEvent("uninstall_continue", Bundle().apply {
                putString("button_action", "uninstall_continue")
                putString("screen", "UninstallActivity")
            })
            UninstallSurveyActivity.start(this@UninstallActivity);
            finish()
        }
    }


    override fun viewBinding(): ActivityUninstallBinding {
        return ActivityUninstallBinding.inflate(LayoutInflater.from(this))
    }

    override fun onResume() {
        super.onResume()
        loadNativeNomedia()

        // Re-enable app resume after a short delay
        object : CountDownTimer(500, 500) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                AppOpenManager.getInstance().enableAppResume()
            }
        }.start()
    }

    override fun onStop() {
        super.onStop()
    }

}