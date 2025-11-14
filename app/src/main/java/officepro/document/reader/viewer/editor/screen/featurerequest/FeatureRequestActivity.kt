package officepro.document.reader.viewer.editor.screen.reloadfile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.brian.base_iap.utils.PreferencesUtils
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.ActivityFeatureRequestBinding
import officepro.document.reader.viewer.editor.model.FeedbackData
import officepro.document.reader.viewer.editor.screen.base.PdfBaseActivity
import officepro.document.reader.viewer.editor.screen.featurerequest.FeatureRequestSuccessDialog
import com.brian.base_iap.utils.FirebaseRemoteConfigUtil

class FeatureRequestActivity : PdfBaseActivity<ActivityFeatureRequestBinding>() {

    companion object {
        private const val TAG = "FeatureRequestActivity"

        fun start(activity: FragmentActivity) {
            activity.intent.data?.let {
                activity.intent.apply { setClass(activity, FeatureRequestActivity::class.java) }
                activity.startActivity(activity.intent)
            } ?: run {
                val intent = Intent(activity, FeatureRequestActivity::class.java)
                activity.startActivity(intent)
            }
        }
    }

    private val strokeSelected by lazy { ContextCompat.getColor(this, R.color.stroke_selected) }
    private val bgSelected by lazy { ContextCompat.getColor(this, R.color.bg_selected) }
    private val strokeUnselected by lazy { ContextCompat.getColor(this, R.color.stroke_unselected) }
    private val bgUnselected by lazy { ContextCompat.getColor(this, R.color.bg_unselected) }

    // Danh sách ID card
    private val cardIds = listOf(
        R.id.card_edit_files,
        R.id.card_merge_split,
        R.id.card_convert_files,
        R.id.card_compress_files,
        R.id.card_view_setting,
        R.id.card_chat_doc,
        R.id.card_ai_summary,
        R.id.card_translate
    )

    private val featureNames = mapOf(
        R.id.card_edit_files to "Edit Files",
        R.id.card_merge_split to "Merge & Split",
        R.id.card_convert_files to "Convert Files",
        R.id.card_compress_files to "Compress Files",
        R.id.card_view_setting to "View Setting",
        R.id.card_chat_doc to "Chat with Document",
        R.id.card_ai_summary to "AI Document Summary",
        R.id.card_translate to "Translate Document"
    )

    private val selectedCardIds = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun viewBinding(): ActivityFeatureRequestBinding {
        return ActivityFeatureRequestBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        val backView = binding.root.findViewById<View>(R.id.btn_cancel)
        backView?.setOnClickListener { finish() }

        cardIds.forEach { id ->
            val card = binding.root.findViewById<MaterialCardView>(id)
            card?.let { setupToggleCard(it) }
        }

        binding.etOther.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSubmitState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        applySubmitEnabled(false)
    }

    override fun initData() {
        // no-op
    }
    override fun initListener() {
        supportFragmentManager.setFragmentResultListener("FR_SUCCESS", this) { _, _ ->
            finish()
        }

        binding.btnSubmit.setOnClickListener {
            saveFirebaseStore()
            PreferencesUtils.putBoolean("NOT_SUBMIT_FEATURE_REQUEST", false)
            try {
                FeatureRequestSuccessDialog().apply {
                    listener = {
                        finish()
                    }
                }.show(supportFragmentManager, FeatureRequestSuccessDialog::class.java.name)
            } catch (e : Exception) {
                Log.e(TAG, "Error showing success dialog: ${e.message}")
            }
        }
        binding.ivBack.setOnClickListener {
            if (FirebaseRemoteConfigUtil.getInstance().isShowAdsMain()) {
                showAdsInterstitial(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_setting")){
                    finish()
                }
            } else {
                finish()
            }
        }
    }

    private fun setupToggleCard(card: MaterialCardView) {
        styleCard(card, selected = false)

        card.setOnClickListener {
            val id = card.id
            if (selectedCardIds.contains(id)) {
                selectedCardIds.remove(id)
            } else {
                selectedCardIds.add(id)
            }
            styleCard(card, selected = selectedCardIds.contains(id))
            updateSubmitState()
        }
    }
    private fun saveFirebaseStore() {

        var featuresArray = ""
        selectedCardIds.forEach { id ->
            featureNames[id]?.let { featuresArray += "$it, " }
        }

        // Text từ etOther nếu có
        val otherText = binding.etOther.text?.toString()?.trim() ?: ""

        val feedbackData = FeedbackData(
            type = "feature_request",
            message = otherText,
            problem = featuresArray,

        )
        FirebaseFirestore.getInstance().collection("feedback").add(feedbackData)

    }


    private fun styleCard(card: MaterialCardView, selected: Boolean) {
        val strokeWidthDp = if (selected) 2f else 1f
        val strokeWidthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            strokeWidthDp,
            resources.displayMetrics
        ).toInt()

        card.strokeWidth = strokeWidthPx
        card.strokeColor = if (selected) strokeSelected else strokeUnselected
        card.setCardBackgroundColor(if (selected) bgSelected else bgUnselected)
        card.isChecked = selected
        card.isClickable = true
    }

    private fun updateSubmitState() {
        val anyCardSelected = selectedCardIds.isNotEmpty()
        val hasReason = binding.etOther.text?.toString()?.trim()?.isNotEmpty() == true

        val enable = anyCardSelected || hasReason
        applySubmitEnabled(enable)
    }

    private fun applySubmitEnabled(enable: Boolean) {
        binding.btnSubmit.isEnabled = enable
        if (enable) {
            binding.btnSubmit.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primaryColor)
            binding.btnSubmit.setTextColor(Color.WHITE)
        } else {
            binding.btnSubmit.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
            binding.btnSubmit.setTextColor(Color.WHITE)
        }
    }
}

