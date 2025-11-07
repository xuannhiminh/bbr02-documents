package officepro.document.reader.viewer.editor.screen.uninstall

//import com.google.android.gms.ads.ez.EzAdControl
//import com.google.android.gms.ads.ez.listenner.ShowAdCallback
import IssueOptionAdapter
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezteam.baseproject.utils.SystemUtils
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.ActivityUninstallReasonBinding
import officepro.document.reader.viewer.editor.screen.base.PdfBaseActivity
import officepro.document.reader.viewer.editor.screen.start.SplashActivity
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.google.firebase.firestore.FirebaseFirestore
import officepro.document.reader.viewer.editor.model.FeedbackData
import com.ezteam.baseproject.utils.FirebaseRemoteConfigUtil

class UninstallSurveyActivity : PdfBaseActivity<ActivityUninstallReasonBinding>() {
    private lateinit var adapter: IssueOptionAdapter
    private val uninstallReasons = listOf(
        R.string.uninstall_reason_2,
        R.string.uninstall_reason_3,
        R.string.uninstall_reason_4,
        R.string.others
    )
    private val uninstallReasonsEn = listOf(
        "Too many ads",
        "I no longer need this app",
        "Too many notifications",
        "Others"
    )
    private val DEFAULT_RESULT = 1
    private var selectedReasonIndex = DEFAULT_RESULT
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    companion object {
        private const val TAG = "UninstallSurveyActivity"

        fun start(activity: FragmentActivity) {
            activity.intent.data?.let {
                activity.intent.apply {
                    setClass(activity, UninstallSurveyActivity::class.java)
                }
                activity.startActivity(activity.intent)
            } ?:   activity.intent.extras?.let {
                activity.intent.apply {
                    setClass(activity, UninstallSurveyActivity::class.java)
                }
                activity.startActivity(activity.intent)
            } ?: kotlin.run {
                val intent = Intent(activity, UninstallSurveyActivity::class.java).apply {
                }
                activity.startActivity(intent)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
    }

    override fun initView() {
        firebaseDb =   FirebaseFirestore.getInstance();

        updateOkButtonState(false)

        adapter = IssueOptionAdapter(uninstallReasons, -1) { index ->
            selectedReasonIndex = index
            updateOkButtonState(true)

//            if (index >= 0) {
//                binding.etOtherReason.visibility = View.VISIBLE
//            } else {
//                binding.etOtherReason.visibility = View.GONE
//            }
        }

        binding.rcvData.layoutManager = LinearLayoutManager(this)
        binding.rcvData.adapter = adapter
    }
    override fun initData() {

    }
    override fun onResume() {
        super.onResume()
        loadNativeNomedia()
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
                    val adView = LayoutInflater.from(this@UninstallSurveyActivity)
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
                FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("native_survey_user"),
                callback
            )
        } else {
            binding.layoutNative.visibility = View.GONE
        }
    }
    private fun updateOkButtonState(enabled: Boolean) {
        binding.btnOk.isEnabled = enabled
        binding.btnOk.alpha = if (enabled) 1.0f else 0.5f
    }
    override fun initListener() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        binding.btnOk.setOnClickListener {
            if (selectedReasonIndex < 0) {
                Toast.makeText(this, getString(R.string.select_reason_first), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reasonText = if (selectedReasonIndex != DEFAULT_RESULT) {
                val input = binding.etOtherReason.text.toString().trim()
                if (input.length < 6) {
                    Toast.makeText(this, getString(R.string.feedback_warning), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                input
            } else {
                ""
            }

            firebaseAnalytics.logEvent("uninstall_reason_selected", Bundle().apply {
                putString("reason_text", reasonText)
                putString("reason_index", uninstallReasonsEn[selectedReasonIndex])
            })
            val FEEDBACK_TYPE_FIREBASE = 1L
            if (FirebaseRemoteConfigUtil.getInstance().getFeedbackType() == FEEDBACK_TYPE_FIREBASE) {
                Log.i(TAG, "Saving feedback to Firebase: $reasonText")
                saveFeedbackFireBase(
                    message = reasonText,
                    problem =  uninstallReasonsEn[selectedReasonIndex]
                )
            }


            Log.i("UninstallReason", "Selected: ${uninstallReasonsEn[selectedReasonIndex]}")
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:${packageName}".toUri()
                }
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error opening app settings", e)
            }
            finish()
        }

        binding.btnCancel.setOnClickListener {
            firebaseAnalytics.logEvent("uninstall_survey_cancel", Bundle().apply {
                putString("button_action", "uninstall_survey_cancel")
                putString("screen", TAG)
            })
            val intent = Intent(this, SplashActivity::class.java)
            intent.putExtra("${packageName}.isFromUninstall", true)
            startActivity(intent)
            finish()
        }
        binding.ivBack.setOnClickListener {
            UninstallActivity.start(this@UninstallSurveyActivity);
            finish()
        }
    }

    override fun viewBinding(): ActivityUninstallReasonBinding {
        return ActivityUninstallReasonBinding.inflate(LayoutInflater.from(this))
    }

    private lateinit var firebaseDb: FirebaseFirestore;

    private fun saveFeedbackFireBase(
        message: String,
        problem: String
    ) {
        val feedback = FeedbackData(
            message = message,
            problem = problem,
            type = "uninstall"
        )
        firebaseDb
            .collection("feedback")
            .add(feedback)
    }

}