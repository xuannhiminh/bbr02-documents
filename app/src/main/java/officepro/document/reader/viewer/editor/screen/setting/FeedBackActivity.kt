package officepro.document.reader.viewer.editor.screen.search
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.brian.base_iap.utils.IAPUtils
import com.brian.base_iap.utils.PreferencesUtils
import com.google.firebase.firestore.FirebaseFirestore
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.ActivityFeedbackBinding
import officepro.document.reader.viewer.editor.model.FeedbackData
import officepro.document.reader.viewer.editor.screen.PolicyActivity
import officepro.document.reader.viewer.editor.screen.base.PdfBaseActivity
import officepro.document.reader.viewer.editor.screen.setting.FeedBackSucessDialog
import com.brian.base_iap.utils.FirebaseRemoteConfigUtil
import com.ezteam.baseproject.utils.SystemUtils

class FeedBackActivity : PdfBaseActivity<ActivityFeedbackBinding>() {

    companion object {
        private const val KEY_LAST_FEEDBACK_TIME = "LAST_FEEDBACK_TIME"
        fun start(activity: FragmentActivity) {
            val intent = Intent(activity, FeedBackActivity::class.java)
            activity.startActivity(intent)
        }
    }

    private val selectedOptions = mutableSetOf<String>()

    private fun saveFeedbackFireBase(
        message: String,
        problem: String
//        onSuccess: () -> Unit,
//        onFailure: (Exception) -> Unit
    ) {
        val feedback = FeedbackData(
            message = message,
            problem = problem,
            installTime = SystemUtils.getInstallTime(this),
            hasNotificationGranted = checkNotificationPermission())
        firebaseDb
            .collection("feedback")
            .add(feedback)
//            .addOnSuccessListener { onSuccess() }
//            .addOnFailureListener { e -> onFailure(e) }
    }

    private fun sendFeedbackEmail(message: String, problem: String) {
        val feedback = FeedbackData(
            message = message,
            problem = problem,
            installTime = SystemUtils.getInstallTime(this),
            hasNotificationGranted = checkNotificationPermission())

        val email = arrayOf(EMAIL_FEEDBACK)

        // Convert FeedbackData to human-readable email text
        val fullMessage = buildString {
            appendLine("${getString(R.string.your_feedback)}:")
            appendLine("====================")
            appendLine(feedback.message)
            appendLine(feedback.problem)
            appendLine()
            appendLine()
            appendLine("${getString(R.string.fix_your_issue)}:")
            appendLine("======================")
            appendLine("Version Code: ${feedback.versionCode}")
            appendLine("Premium: ${feedback.isPremium}")
            appendLine("OS API Level: ${feedback.osApiLevel}")
            appendLine("Device Model: ${feedback.deviceModel}")
            appendLine("Locale: ${feedback.locale}")
            appendLine("Install Time: ${SystemUtils.formatTimestamp(feedback.installTime?: -1L)}")
            appendLine("Notification Granted: ${feedback.hasNotificationGranted}")
            appendLine("Timestamp: ${SystemUtils.formatTimestamp(feedback.timestamp)}")
            appendLine("Time Enter App: ${feedback.timeEnterApp}")
            appendLine("Time Show Notification: ${feedback.timeShowNotification}")
            appendLine("Time Clicked Notification: ${feedback.timeClickedNotification}")
            appendLine()
            appendLine()
            appendLine("======================")
            appendLine(getString(R.string.feedback_disclaimer))
        }

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, email)
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_title))
            putExtra(Intent.EXTRA_TEXT, fullMessage)
        }

        try {
            startActivity(Intent.createChooser(intent, "Send Feedback"))
        } catch (e: Exception) {
            toast("No email app found.")
        }
    }


    private lateinit var firebaseDb: FirebaseFirestore;

    override fun initView() {
        firebaseDb =   FirebaseFirestore.getInstance();
    }



    override fun onStop() {
        super.onStop()
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    private fun canSubmitFeedback(): Boolean {
        val last = PreferencesUtils.getLong(KEY_LAST_FEEDBACK_TIME, 0L)
        val now = System.currentTimeMillis()
        val oneHour = 60 * 60 * 1000L
        return (now - last) >= oneHour
    }
    override fun onBackPressed() {
        super.onBackPressed()
        if (FirebaseRemoteConfigUtil.getInstance().isShowAdsMain()) {
            showAdsInterstitial(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_setting")){
                finish()
            }
        } else {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
    }

    override fun initData() {
    }

    override fun onResume() {
        super.onResume()
    }



    override fun initListener() {
        binding.ivBack.setOnClickListener {
            if (FirebaseRemoteConfigUtil.getInstance().isShowAdsMain()) {
                showAdsInterstitial(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_setting")){
                    finish()
                }
            } else {
                finish()
            }
        }
        binding.etDetail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0

                when {
                    length in 1..5 -> {
                        binding.tvWarning.visibility = View.VISIBLE
                        binding.btnSubmit.isEnabled = false
                        binding.btnSubmit.alpha = 0.5f // Optional: làm mờ nút
                    }
                    length >= 6 -> {
                        binding.tvWarning.visibility = View.GONE
                        binding.btnSubmit.isEnabled = true
                        binding.btnSubmit.alpha = 1f
                    }
                    else -> {
                        binding.tvWarning.visibility = View.GONE
                        binding.btnSubmit.isEnabled = false
                        binding.btnSubmit.alpha = 0.5f
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        setupOptionClick(binding.optionFile)
        setupOptionClick(binding.optionSuggestion)
        setupOptionClick(binding.optionSlow)
        setupOptionClick(binding.optionBug)
        setupOptionClick(binding.optionOther)
        setupOptionClick(binding.optionNoti)


        binding.btnSubmit.setOnClickListener {
            if (!canSubmitFeedback()) {
                toast(getString(R.string.feature_request_limit))
                return@setOnClickListener
            }

            val message = binding.etDetail.text.toString().trim()
            binding.tvWarning.visibility = View.GONE

            if (selectedOptions.isEmpty()) {
                binding.tvOptionWarning.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (message.length < 6) {
                binding.tvWarning.visibility = View.VISIBLE
                return@setOnClickListener
            }
            binding.tvOptionWarning.visibility = View.GONE
            binding.tvWarning.visibility = View.GONE
            val problems = selectedOptions.joinToString(separator = ", ")
            PreferencesUtils.putBoolean("NOT_SUBMIT_FEEDBACK", false)
            val FEEDBACK_TYPE_EMAIL = 0L
            if (FirebaseRemoteConfigUtil.getInstance().getFeedbackType() == FEEDBACK_TYPE_EMAIL && !IAPUtils.isPremium()) {
                sendFeedbackEmail(problems, message)
                PreferencesUtils.putLong(KEY_LAST_FEEDBACK_TIME, System.currentTimeMillis())
                return@setOnClickListener
            } else {
                saveFeedbackFireBase(message, problems)
                try {
                    val feedBackSuccessDialog = FeedBackSucessDialog()
                    feedBackSuccessDialog.listener = {
                        finish()
                    }
                    try {
                        feedBackSuccessDialog.show(
                            supportFragmentManager,
                            FeedBackSucessDialog::class.java.name
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("SettingActivity", "Error showing FeedBackSucessDialog: ${e.message}", e)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }



//                    , {
//                        val feedBackSuccessDialog = FeedBackSucessDialog()
//                        feedBackSuccessDialog.listener = {
//                            finish()
//                        }
//                        try {
//                            feedBackSuccessDialog.show(
//                                supportFragmentManager,
//                                FeedBackSucessDialog::class.java.name
//                            )
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    },
//                    { ex->
//                        Log.e("FeedBackActivity", "Error saving feedback", ex)
//                        try {
//                            toast(getString(R.string.error_sth_wrong))
//                            finish()
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    })
        }

    }

    private fun setupOptionClick(optionLayout: LinearLayout) {
        optionLayout.setOnClickListener {
            val textView = optionLayout.getChildAt(0) as? TextView
            val optionText = textView?.text?.toString() ?: return@setOnClickListener

            val isSelected = selectedOptions.contains(optionText)
            if (isSelected) {
                // Bỏ chọn
                selectedOptions.remove(optionText)
                optionLayout.setBackgroundResource(R.drawable.bg_button_outline)
                textView?.setTextColor(ContextCompat.getColor(this, R.color.text1))
            } else {
                // Chọn
                selectedOptions.add(optionText)
                optionLayout.setBackgroundResource(R.drawable.bg_button_selected)
                textView?.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
        }
    }


    override fun viewBinding(): ActivityFeedbackBinding {
        return ActivityFeedbackBinding.inflate(LayoutInflater.from(this))
    }

    private fun sendFeedback(fullMessage: String) {
        val email = arrayOf(EMAIL_FEEDBACK)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, email)
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_title))
            putExtra(Intent.EXTRA_TEXT, fullMessage)
        }

        try {
            startActivity(Intent.createChooser(intent, "Send Feedback"))
        } catch (e: Exception) {
            toast("No email app found.")
        }
    }
}