package officepro.document.reader.viewer.editor.screen.start

//import com.google.android.gms.ads.ez.EzAdControl
//import com.google.android.gms.ads.ez.listenner.ShowAdCallback
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.ezteam.baseproject.extensions.hasExtraKeyContaining
import com.brian.base_iap.utils.PreferencesUtils
import com.nlbn.ads.util.AppOpenManager
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.common.PresKey
import officepro.document.reader.viewer.editor.databinding.ActivityGetStartBinding
import officepro.document.reader.viewer.editor.screen.base.PdfBaseActivity
import officepro.document.reader.viewer.editor.screen.language.LanguageActivity
import officepro.document.reader.viewer.editor.screen.main.MainActivity
import officepro.document.reader.viewer.editor.screen.main.MainViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RequestAllFilePermissionActivity : PdfBaseActivity<ActivityGetStartBinding>() {
    companion object {
        private const val TAG = "RequestAllFilePermissionActivity"

        fun start(activity: FragmentActivity) {
            val pkg = activity.packageName

            activity.intent.data?.let {
                activity.intent.apply {
                    setClass(activity, RequestAllFilePermissionActivity::class.java)
                }
                activity.startActivity(activity.intent)
            } ?: activity.intent.hasExtraKeyContaining(pkg).let { hasKey ->
                if (hasKey) {
                    activity.intent.apply {
                        setClass(activity, RequestAllFilePermissionActivity::class.java)
                    }
                    activity.startActivity(activity.intent)
                } else {
                    val intent = Intent(activity, RequestAllFilePermissionActivity::class.java)
                    activity.startActivity(intent)
                }
            }
        }
    }

    private val viewModel by inject<MainViewModel>()

    override fun initView() {
        PreferencesUtils.putBoolean(PresKey.GET_START, false)
        val appName = getString(R.string.app_name)
        val tvTitleText = getString(R.string.title_pdf_reader, appName)
        var spannable = SpannableString(tvTitleText)

        // Find the position of app Name
        var startIndex = tvTitleText.indexOf(appName)
        var endIndex = startIndex + appName.length

        if (startIndex != -1) {


            // Apply all caps (through custom span)
            val allCapsString = appName.uppercase()
            spannable = SpannableString(tvTitleText.replace(appName, allCapsString))

            // Recalculate start and end indices after replacing with uppercase
            startIndex = spannable.toString().indexOf(allCapsString)
            endIndex = startIndex + allCapsString.length

            // Apply red color
            val redColor = ContextCompat.getColor(this, R.color.primaryColor)
            spannable.setSpan(ForegroundColorSpan(redColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Apply text size (32sp converted to pixels)
            val textSizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                32f,
                resources.displayMetrics
            ).toInt()

            spannable.setSpan(
                AbsoluteSizeSpan(textSizePx),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.textTitle.text = spannable

        val tvPermissionExplanationText = getString(R.string.permission_explanation, appName)
        spannable = SpannableString(tvPermissionExplanationText)
        startIndex = tvPermissionExplanationText.indexOf(appName)
        endIndex = startIndex + appName.length
        if (startIndex != -1) {
            // Apply red color
            val redColor = ContextCompat.getColor(this, R.color.primaryColor)
            spannable.setSpan(ForegroundColorSpan(redColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Apply bold style
            spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        }
        binding.textPermissionExplanation.text = spannable
        binding.animationView.playAnimation()
        binding.animationView.scaleX = 1.5f
        binding.animationView.scaleY = 1.5f
    }

    override fun initData() {

    }

    override fun initListener() {
        binding.buttonAllow.setOnClickListener {
            requestPermissionStorage {
                if (it) {
                    viewModel.migrateFileDataViewModelScope()
                }
                openMain()
            }
        }

        binding.buttonLater.setOnClickListener {
            openMain()
        }
    }

    private fun openMain() {
//        EzAdControl.getInstance(this).setShowAdCallback(object : ShowAdCallback() {
//            override fun onDisplay() {
//                Log.e("Show Ads", "Display: ${this.javaClass.simpleName}")
//            }
//
//            override fun onDisplayFaild() {
//                AppOpenManager.getInstance().disableAppResume()
//                val intent = Intent(this@RequestAllFilePermissionActivity, MainActivity::class.java)
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                startActivity(intent)
                if(ContextCompat.checkSelfPermission(this,
                        Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    RequestNotificationPermissionActivity.start(this@RequestAllFilePermissionActivity);
                } else {
                    MainActivity.start(this@RequestAllFilePermissionActivity);
                }
                finish()
//            }
//
//            override fun onClosed() {
//                lifecycleScope.launch {
//                    viewModel.migrateFileData()
//                }
//                MainActivity.start(this@GetStartActivity)
//                finish()
//                Log.e("Show Ads", "Close: ${this.javaClass.simpleName}")
//            }
//        }).showAdsWithoutCapping()

    }

    override fun viewBinding(): ActivityGetStartBinding {
        return ActivityGetStartBinding.inflate(LayoutInflater.from(this))
    }

}