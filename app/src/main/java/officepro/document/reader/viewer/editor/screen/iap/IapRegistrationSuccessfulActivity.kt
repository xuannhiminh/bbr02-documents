package officepro.document.reader.viewer.editor.screen.iap


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.ezteam.baseproject.extensions.hasExtraKeyContaining
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.baseproject.utils.PresKey
import officepro.document.reader.viewer.editor.databinding.ActivityIapRegistrationSuccessfulBinding
import officepro.document.reader.viewer.editor.screen.base.PdfBaseActivity
import officepro.document.reader.viewer.editor.screen.language.LanguageActivity
import officepro.document.reader.viewer.editor.screen.main.MainActivity


class IapRegistrationSuccessfulActivity : PdfBaseActivity<ActivityIapRegistrationSuccessfulBinding>() {

    companion object {
        fun start(activity: FragmentActivity) {
            val pkg = activity.packageName

            activity.intent.data?.let {
                activity.intent.apply {
                    setClass(activity, IapRegistrationSuccessfulActivity::class.java)
                }
                activity.startActivity(activity.intent)
            } ?: activity.intent.hasExtraKeyContaining(pkg).let { hasKey ->
                if (hasKey) {
                    activity.intent.apply {
                        setClass(activity, IapRegistrationSuccessfulActivity::class.java)
                       // Intent.setFlags = 0 // reset to 0 because sometime intent already has flags new task and kill activity before start
                    }
                    activity.startActivity(activity.intent)
                } else {
                    val intent = Intent(activity, IapRegistrationSuccessfulActivity::class.java)
                    activity.startActivity(intent)
                }
            }
        }
    }

    override fun viewBinding(): ActivityIapRegistrationSuccessfulBinding {
        return ActivityIapRegistrationSuccessfulBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
//        window.statusBarColor = Color.parseColor("#1F0718")
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            isFromSplash = intent.getBooleanExtra("${packageName}.isFromSplash", false)
    }

    override fun initListener() {

        binding.btnOk.setOnClickListener {
            navigateNext()
            this@IapRegistrationSuccessfulActivity.finish()
        }
    }
    private var isFromSplash = false
    private fun navigateNext() {
        if (!isFromSplash) {
            finish()    // vào từ main
            return
        }
        if (PreferencesUtils.getBoolean(PresKey.GET_START, true)) {
            LanguageActivity.start(this)    // lần đầu tiên
        } else {
            MainActivity.start(this)
        }
        finish()
    }

    override fun initData() {

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

