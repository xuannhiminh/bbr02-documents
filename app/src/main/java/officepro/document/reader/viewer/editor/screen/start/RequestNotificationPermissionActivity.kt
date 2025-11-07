package officepro.document.reader.viewer.editor.screen.start

//import com.google.android.gms.ads.ez.EzAdControl
//import com.google.android.gms.ads.ez.listenner.ShowAdCallback
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ezteam.baseproject.extensions.hasExtraKeyContaining
import com.ezteam.baseproject.utils.PreferencesUtils
import com.nlbn.ads.util.AppOpenManager
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.common.PresKey
import officepro.document.reader.viewer.editor.databinding.ActivityNotificationBinding
import officepro.document.reader.viewer.editor.screen.base.PdfBaseActivity
import officepro.document.reader.viewer.editor.screen.main.MainActivity
import officepro.document.reader.viewer.editor.screen.main.MainViewModel
import officepro.document.reader.viewer.editor.service.NotificationForegroundService
import org.koin.android.ext.android.inject

class RequestNotificationPermissionActivity : PdfBaseActivity<ActivityNotificationBinding>() {
    companion object {
        private const val TAG = "RequestNotificationPermissionActivity"

        fun start(activity: FragmentActivity) {
            val pkg = activity.packageName

            activity.intent.data?.let {
                activity.intent.apply {
                    setClass(activity, RequestNotificationPermissionActivity::class.java)
                }
                activity.startActivity(activity.intent)
            } ?: activity.intent.hasExtraKeyContaining(pkg).let { hasKey ->
                if (hasKey) {
                    activity.intent.apply {
                        setClass(activity, RequestNotificationPermissionActivity::class.java)
                    }
                    activity.startActivity(activity.intent)
                } else {
                    val intent = Intent(activity, RequestNotificationPermissionActivity::class.java)
                    activity.startActivity(intent)
                }
            }
        }
    }

    private val viewModel by inject<MainViewModel>()

    override fun initView() {
        PreferencesUtils.putBoolean(PresKey.GET_START, false)
        val appName = getString(R.string.app_name)
        val tvTitleText = getString(R.string.noti_title, appName)
        var spannable = SpannableString(tvTitleText)

        // Find the position of app Name
        var startIndex = tvTitleText.indexOf(appName)
        var endIndex = startIndex + appName.length

        if (startIndex != -1) {
            // Recalculate start and end indices after replacing with uppercase
            startIndex = spannable.toString().indexOf(appName)
            endIndex = startIndex + appName.length

            // Apply red color
            val redColor = ContextCompat.getColor(this, R.color.primaryColor)
            spannable.setSpan(ForegroundColorSpan(redColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Apply text size (32sp converted to pixels)
            val textSizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                20f,
                resources.displayMetrics
            ).toInt()

            spannable.setSpan(
                AbsoluteSizeSpan(textSizePx),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.descTitle.text = spannable

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

    }

    override fun initData() {

    }

    override fun initListener() {
        binding.btnAllow.setOnClickListener {
            requestNotificationPermission()
        }

        binding.buttonLater.setOnClickListener {
            openMain()
        }

        binding.root.setOnClickListener {
            requestNotificationPermission()
        }

        binding.buttonLater.setOnTouchListener { v, event ->
            v.performClick()
            true
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                onNotificationPermissionGranted()
            } else {
                onNotificationPermissionDenied()
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermissionFlow() {
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Already have it
                PreferencesUtils.putBoolean("NOTIFICATION", true)
                PreferencesUtils.putInteger("NOTIF_DENIAL_COUNT", 0)
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> {
                // Show rationale, then request
                requestNotificationPermissionLauncher.launch(
                    android.Manifest.permission.POST_NOTIFICATIONS
                )

            }

            else -> {
                // First-time request
                requestNotificationPermissionLauncher.launch(
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }
    private fun requestNotificationPermission() {
        Log.e("GetStartActivity", "Request notification permission SDK: ${Build.VERSION.SDK_INT}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val denialCount = PreferencesUtils.getInteger("NOTIF_DENIAL_COUNT", 0)
            if (denialCount >= 2) {
                requestPermissionNotificationGoToSetting {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                }
                waitingNotificationResult = true
                AppOpenManager.getInstance().disableAppResume()
            } else {
                requestNotificationPermissionFlow()
            }
        } else {
            Log.d("MainActivity", "Notification permission not required for SDK < 33")
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionNotificationGoToSetting {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                }
                waitingNotificationResult = true
                AppOpenManager.getInstance().disableAppResume()
            } else {
                onNotificationPermissionGranted()
            }
        }
    }


    private var waitingNotificationResult = false


    override fun onResume() {
        super.onResume()
        if(waitingNotificationResult) {
            waitingNotificationResult = false
                val granted = ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                if (granted && !PreferencesUtils.getBoolean("NOTIFICATION", false)) {
                    // User enabled in Settings
                    onNotificationPermissionGranted()
                } else if (!granted && PreferencesUtils.getBoolean("NOTIFICATION", false)) {
                    // User disabled in Settings
                    onNotificationPermissionDenied()
                }
        }

    }



    private fun onNotificationPermissionGranted() {
        PreferencesUtils.putBoolean("NOTIFICATION", true)
        // Proceed with showing notifications
        //Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        Log.d("MainActivity", "Notification permission granted")
        try {
            startForegroundService( Intent(this, NotificationForegroundService::class.java))
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error starting service: ${e.message}")
        }
        openMain()
    }

    private fun onNotificationPermissionDenied() {
        // Show a message or guide the user to settings
        //Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        PreferencesUtils.putBoolean("NOTIFICATION", false)
        PreferencesUtils.putInteger("NOTIF_DENIAL_COUNT",
            PreferencesUtils.getInteger("NOTIF_DENIAL_COUNT", 0) + 1
        )
        try {
            startForegroundService( Intent(this, NotificationForegroundService::class.java))
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error starting service: ${e.message}")
        }
        Log.e("MainActivity", "Notification permission denied")
        openMain()
    }

    private fun openMain() {
        AppOpenManager.getInstance().disableAppResume()
        MainActivity.start(this@RequestNotificationPermissionActivity);
        finish()
    }

    override fun viewBinding(): ActivityNotificationBinding {
        return ActivityNotificationBinding.inflate(LayoutInflater.from(this))
    }

}