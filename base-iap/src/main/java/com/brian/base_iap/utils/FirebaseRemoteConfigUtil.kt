package com.brian.base_iap.utils

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.json.JSONObject
import java.util.Locale

class FirebaseRemoteConfigUtil private constructor() {

    companion object {

        enum class StartUpType(val value: Int) {
            ADS_OPEN_LANGUAGE(0), // Splash -> ads open -> Language
            ADS_OPEN_IAP_LANGUAGE(1), // Splash -> ads open -> IAP -> Language
            IAP_ADS_INTER_LANGUAGE(2); // Splash -> IAP -> ads inter -> Language
        }
        private const val DEFAULT_FORCE_UPDATE = false
        private const val DEFAULT_MIN_VERSION_CODE = 0
        private const val DEFAULT_LATEST_VERSION = "N/A"
        private const val DEFAULT_UPDATE_USER_COUNT = 10000
        private const val DEFAULT_UPDATE_FEATURES = "✔ Fix minor bugs"
        private const val DEFAULT_VERSION_CODE_REVIEWING = 0
        private const val DEFAULT_FEEDBACK_TYPE = 0
        private const val DEFAULT_NOTIFICATION_FREQUENCY_MINUTES = 60L
        private const val DEFAULT_PDF_DETAIL_TYPE = 0
        private const val DEFAULT_REQUEST_FEATURE_SETTING_ON_OFF = false
        private const val DEFAULT_FEEDBACK_SETTING_ON_OFF = false
        private const val DEFAULT_REQUEST_NOTI_ACTIVITY_ON_OFF = false
        private const val DEFAULT_SHOW_ADS_RELOAD_FILE_INTER = true
        private const val DEFAULT_NOTIFICATION_STEP_INCREASE = 30L
        private const val DEFAULT_NOTIFICATION_STEP_DECREASE = 30L
        private const val DEFAULT_NOTIFICATION_STEP_ORGANIC = 15L
        private const val DEFAULT_NOTIFICATION_MIN_TIMEOUT = 120L
        private const val DEFAULT_NOTIFICATION_MAX_TIMEOUT = 24 * 60L
        private const val DEFAULT_DIALOG_CANCEL_ON_TOUCH_OUTSIDE = true
        private const val DEFAULT_NOTIFICATION_MAX_HOUR = 23 // 11 PM
        private const val DEFAULT_NOTIFICATION_MIN_HOUR = 8
        private const val DEFAULT_DURATION_RELOADING_FILE = 4000L
        private const val DEFAULT_TIME_DELAY_SHOWING_RELOAD_GUIDE = 3000L
        private const val DEFAULT_TIME_SHOWING_RELOAD_GUIDE = 4L
        private const val DEFAULT_INTERVAL_SHOW_INTER_SECOND = 30L // 30s
        private const val DEFAULT_TIMEOUT_LOAD_INTER_SECOND = 30L // 30s
        private val DEFAULT_TYPE_OF_START_UP = StartUpType.IAP_ADS_INTER_LANGUAGE.value.toLong()
        private const val DEFAULT_PRELOAD_NATIVE_LANGUAGE = true // 30s
        private const val DEFAULT_LOG_PURCHASE_EVENT = false // 30s
        private const val DEFAULT_TIME_BLOCK_DEFAULT_READER = 1L
        private const val DEFAULT_TURN_OFF_NOTI_SERVICE_IF_PREMIUM = false
        private const val DEFAULT_ALWAYS_ASK_NOTI_WHEN_ENTER_APP = false
        private const val DEFAULT_IAP_SCREEN_TYPE = 0
        private const val DEFAULT_LANGUAGE_ADAPTER_TYPE = 0
        private const val DEFAULT_SHOW_ADS_MAIN = false
        private const val DEFAULT_ALLOW_SAVE_EXCEL_TO_PDF = false
        private const val DEFAULT_IS_BIG_ADS = true
        private const val DEFAULT_IS_SHOW_NOTIFICATION_OUT_APP= true
        private const val DEFAULT_NOTIFICATION_OUT_APP_INTERVAL_SECOND = 10
        private const val DEFAULT_TIME_DELAY_SHOWING_EXTEND_ADS = 30
        private const val DEFAULT_TYPE_ADS_DETAIL = 0
        private const val DEFAULT_FREE_TRIAL_BUTTON_TEXT = "Start free trial"
        private const val DEFAULT_IAP_BUTTON_TEXT = "Start now"
        private const val DEFAULT_FREE_TRIAL_EXPLAIN_TEXT = "%s/Week after FREE 3-day Trial"
        private const val DEFAULT_IAP_EXPLAIN_TEXT =        "%s/week, feel FREE to cancel anytime"
        private const val DEFAULT_IAP_BASE_PLAN = "release-weekly-plan"
        private const val DEFAULT_IS_FREE_TRIAL_ENABLE = true
        private const val DEFAULT_SAVE_FILE_NEED_PREMIUM = true
        private const val DEFAULT_ADS_CONFIG = """
            {
              "open_splash": "ca-app-pub-5904408074441373/2634978210",
              "inter_splash": "ca-app-pub-5904408074441373/7567740697",
              "native_language": "ca-app-pub-5904408074441373/4829896712",
              "banner_home": "ca-app-pub-5904408074441373/6771294653",
              "native_middle": "ca-app-pub-5904408074441373/6254659026",
              "native_home": "ca-app-pub-5904408074441373/1598859025",
              "native_filedetail": "ca-app-pub-5904408074441373/7972695689",
              "inter_filedetail": "ca-app-pub-5904408074441373/6659614019",
              "banner_filedetail": "ca-app-pub-5904408074441373/6778817919",
              "open_all": "ca-app-pub-5904408074441373/9890651709",
              "native_bot_selectfiles": "ca-app-pub-5904408074441373/9288194083",
              "native_between_files_selectfiles": "ca-app-pub-5904408074441373/5763704432",
              "inter_createpdf": "ca-app-pub-5904408074441373/6382651538",
              "native_bot_createpdf": "ca-app-pub-5904408074441373/4450622765",
              "native_keep_user": "ca-app-pub-5904408074441373/7264488364",
              "native_survey_user": "ca-app-pub-5904408074441373/2832049645",
              "inter_setting": "ca-app-pub-5904408074441373/6490462743",
              "inter_file_type": "ca-app-pub-5904408074441373/3756488199",
              "native_file_type": "ca-app-pub-5904408074441373/8765272293",
              "banner_file_type": "ca-app-pub-5904408074441373/7975112410",
              "inter_reload_file_success": "ca-app-pub-5904408074441373/2527344616",
              "native_reload_file_success": "ca-app-pub-5904408074441373/6662030740",
              "native_exit_app": "ca-app-pub-5904408074441373/5837965863",
              "inter_home": "ca-app-pub-5904408074441373/4783726883"
            }
        """



        private const val REMOTE_KEY_FORCE_UPDATE = "force_update"
        private const val REMOTE_KEY_MIN_VERSION_CODE = "min_version_code"
        private const val REMOTE_KEY_LATEST_VERSION = "latest_version_name"
        private const val REMOTE_KEY_UPDATE_USER_COUNT = "update_user_count"
        private const val REMOTE_KEY_UPDATE_FEATURES = "update_features"
        private const val REMOTE_KEY_VERSION_CODE_REVIEWING = "version_code_reviewing"
        private const val REMOTE_KEY_FEEDBACK_TYPE = "feedback_type"
        private const val REMOTE_KEY_NOTIFICATION_FREQUENCY_MINUTES = "notification_frequency_minutes"
        private const val REMOTE_KEY_PDF_DETAIL_TYPE = "pdf_detail_type"
        private const val REMOTE_KEY_REQUEST_FEATURE_SETTING_ON_OFF = "request_feature_setting_on_off"
        private const val REMOTE_KEY_FEEDBACK_SETTING_ON_OFF = "feedback_setting_on_off"
        private const val REMOTE_KEY_NOTI_ACTIVITY_ON_OFF = "request_noti_activity_on_off"
        private const val REMOTE_KEY_SHOW_ADS_RELOAD_FILE_INTER = "show_ads_reload_file_inter"
        private const val REMOTE_KEY_NOTIFICATION_STEP_INCREASE = "notification_step_increase"
        private const val REMOTE_KEY_NOTIFICATION_STEP_DECREASE = "notification_step_decrease"
        private const val REMOTE_KEY_NOTIFICATION_STEP_ORGANIC = "notification_step_organic"
        private const val REMOTE_KEY_NOTIFICATION_MIN_TIMEOUT = "notification_min_timeout"
        private const val REMOTE_KEY_NOTIFICATION_MAX_TIMEOUT = "notification_max_timeout"
        private const val REMOTE_KEY_DIALOG_CANCEL_ON_TOUCH_OUTSIDE = "dialog_cancel_on_touch_outside"
        private const val REMOTE_KEY_NOTIFICATION_MAX_HOUR = "notification_max_hour"
        private const val REMOTE_KEY_NOTIFICATION_MIN_HOUR = "notification_min_hour"
        private const val REMOTE_KEY_DURATION_RELOADING_FILE = "duration_reloading_file"
        private const val REMOTE_KEY_DURATION_DELAY_SHOWING_RELOAD_GUIDE = "duration_delay_showing_reload_guide"
        private const val REMOTE_KEY_TIME_SHOWING_RELOAD_GUIDE = "time_showing_reload_guide"
        private const val REMOTE_KEY_INTERVAL_SHOW_INTER_SECOND = "interval_show_inter_second"
        private const val REMOTE_KEY_TIMEOUT_LOAD_INTER_SECOND = "timeout_load_inter_second"
        private const val REMOTE_KEY_TYPE_OF_START_UP = "type_of_start_up"
        private const val REMOTE_KEY_PRELOAD_NATIVE_LANGUAGE = "preload_native_language"
        private const val REMOTE_KEY_LOG_PURCHASE_EVENT = "log_purchase_event"
        private const val REMOTE_KEY_TIME_BLOCK_DEFAULT_READER = "time_block_default_reader"
        private const val REMOTE_KEY_TURN_OFF_NOTI_SERVICE_IF_PREMIUM = "turn_off_noti_service_if_premium"
        private const val REMOTE_KEY_ALWAYS_REQUEST_NOTI_WHEN_ENTER_APP= "always_request_noti_when_enter_app"
        private const val REMOTE_KEY_IAP_SCREEN_TYPE = "iap_screen_type"
        private const val REMOTE_KEY_TYPE_ADS_DETAIL = "type_ads_detail"
        private const val REMOTE_KEY_LANGUAGE_ADAPTER_TYPE = "language_adapter_type"
        private const val REMOTE_KEY_SHOW_ADS_MAIN = "show_ads_main"
        private const val REMOTE_KEY_ALLOW_SAVE_EXCEL_TO_PDF = "allow_save_excel_to_pdf"
        private const val REMOTE_KEY_IS_BIG_ADS = "is_big_ads"
        private const val REMOTE_KEY_NOTIFICATION_OUT_APP_INTERVAL_SECOND = "notification_out_app_interval_second"
        private const val REMOTE_KEY_IS_SHOW_NOTIFICATION_OUT_APP = "is_show_notification_out_app"
        private const val REMOTE_KEY_ADS_CONFIG = "ads_config"
        private const val REMOTE_KEY_TIME_DELAY_SHOWING_EXTEND_ADS = "time_delay_showing_extend_ads"
        private const val REMOTE_KEY_FREE_TRIAL_BUTON_TEXT = "free_trial_button_text"
        private const val REMOTE_KEY_IAP_BUTON_TEXT = "iap_button_text"
        private const val REMOTE_KEY_IAP_EXPLAIN_TEXT = "iap_explain_text"
        private const val REMOTE_KEY_FREE_TRIAL_EXPLAIN_TEXT = "free_trial_explain_text"
        private const val REMOTE_KEY_IAP_BASE_PLAN= "iap_weekly_plan"
        private const val REMOTE_KEY_IS_FREE_TRIAL_ENABLE = "is_free_trial_enable"
        private const val REMOTE_KEY_SAVE_FILE_NEED_PREMIUM = "save_file_need_premium"








        private var instance: FirebaseRemoteConfigUtil? = null

        @Synchronized
        fun getInstance(): FirebaseRemoteConfigUtil {
            if (instance == null) {
                instance = FirebaseRemoteConfigUtil()
            }
            return instance!!
        }
    }

    private val firebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // Fetch every hour
            .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)

        firebaseRemoteConfig.setDefaultsAsync(
            mapOf(
                REMOTE_KEY_FORCE_UPDATE to DEFAULT_FORCE_UPDATE,
                REMOTE_KEY_MIN_VERSION_CODE to DEFAULT_MIN_VERSION_CODE,
                REMOTE_KEY_LATEST_VERSION to DEFAULT_LATEST_VERSION,
                REMOTE_KEY_UPDATE_USER_COUNT to DEFAULT_UPDATE_USER_COUNT,
                REMOTE_KEY_UPDATE_FEATURES to DEFAULT_UPDATE_FEATURES,
                REMOTE_KEY_VERSION_CODE_REVIEWING to DEFAULT_VERSION_CODE_REVIEWING,
                REMOTE_KEY_FEEDBACK_TYPE to DEFAULT_FEEDBACK_TYPE,
                REMOTE_KEY_NOTIFICATION_FREQUENCY_MINUTES to DEFAULT_NOTIFICATION_FREQUENCY_MINUTES,
                REMOTE_KEY_PDF_DETAIL_TYPE to DEFAULT_PDF_DETAIL_TYPE,
                REMOTE_KEY_REQUEST_FEATURE_SETTING_ON_OFF to DEFAULT_REQUEST_FEATURE_SETTING_ON_OFF,
                REMOTE_KEY_FEEDBACK_SETTING_ON_OFF to DEFAULT_FEEDBACK_SETTING_ON_OFF,
                REMOTE_KEY_NOTI_ACTIVITY_ON_OFF to DEFAULT_REQUEST_NOTI_ACTIVITY_ON_OFF,
                REMOTE_KEY_SHOW_ADS_RELOAD_FILE_INTER to DEFAULT_SHOW_ADS_RELOAD_FILE_INTER,
                REMOTE_KEY_NOTIFICATION_STEP_INCREASE to DEFAULT_NOTIFICATION_STEP_INCREASE,
                REMOTE_KEY_NOTIFICATION_STEP_DECREASE to DEFAULT_NOTIFICATION_STEP_DECREASE,
                REMOTE_KEY_NOTIFICATION_STEP_ORGANIC to DEFAULT_NOTIFICATION_STEP_ORGANIC,
                REMOTE_KEY_NOTIFICATION_MIN_TIMEOUT to DEFAULT_NOTIFICATION_MIN_TIMEOUT,
                REMOTE_KEY_NOTIFICATION_MAX_TIMEOUT to DEFAULT_NOTIFICATION_MAX_TIMEOUT,
                REMOTE_KEY_DIALOG_CANCEL_ON_TOUCH_OUTSIDE to DEFAULT_DIALOG_CANCEL_ON_TOUCH_OUTSIDE,
                REMOTE_KEY_NOTIFICATION_MAX_HOUR to DEFAULT_NOTIFICATION_MAX_HOUR,
                REMOTE_KEY_NOTIFICATION_MIN_HOUR to DEFAULT_NOTIFICATION_MIN_HOUR,
                REMOTE_KEY_DURATION_RELOADING_FILE to DEFAULT_DURATION_RELOADING_FILE,
                REMOTE_KEY_DURATION_DELAY_SHOWING_RELOAD_GUIDE to DEFAULT_TIME_DELAY_SHOWING_RELOAD_GUIDE,
                REMOTE_KEY_TIME_SHOWING_RELOAD_GUIDE to DEFAULT_TIME_SHOWING_RELOAD_GUIDE,
                REMOTE_KEY_INTERVAL_SHOW_INTER_SECOND to DEFAULT_INTERVAL_SHOW_INTER_SECOND,
                REMOTE_KEY_TIMEOUT_LOAD_INTER_SECOND to DEFAULT_TIMEOUT_LOAD_INTER_SECOND,
                REMOTE_KEY_TYPE_OF_START_UP to DEFAULT_TYPE_OF_START_UP,
                REMOTE_KEY_PRELOAD_NATIVE_LANGUAGE to DEFAULT_PRELOAD_NATIVE_LANGUAGE,
                REMOTE_KEY_LOG_PURCHASE_EVENT to DEFAULT_LOG_PURCHASE_EVENT,
                REMOTE_KEY_TIME_BLOCK_DEFAULT_READER to DEFAULT_TIME_BLOCK_DEFAULT_READER,
                REMOTE_KEY_TURN_OFF_NOTI_SERVICE_IF_PREMIUM to DEFAULT_TURN_OFF_NOTI_SERVICE_IF_PREMIUM,
                REMOTE_KEY_ALWAYS_REQUEST_NOTI_WHEN_ENTER_APP to DEFAULT_ALWAYS_ASK_NOTI_WHEN_ENTER_APP,
                REMOTE_KEY_IAP_SCREEN_TYPE to DEFAULT_IAP_SCREEN_TYPE,
                REMOTE_KEY_TYPE_ADS_DETAIL to DEFAULT_TYPE_ADS_DETAIL,
                REMOTE_KEY_LANGUAGE_ADAPTER_TYPE to DEFAULT_LANGUAGE_ADAPTER_TYPE,
                REMOTE_KEY_SHOW_ADS_MAIN to DEFAULT_SHOW_ADS_MAIN,
                REMOTE_KEY_IS_BIG_ADS to DEFAULT_IS_BIG_ADS,
                REMOTE_KEY_ALLOW_SAVE_EXCEL_TO_PDF to DEFAULT_ALLOW_SAVE_EXCEL_TO_PDF,
                REMOTE_KEY_ADS_CONFIG to DEFAULT_ADS_CONFIG,
                REMOTE_KEY_TIME_DELAY_SHOWING_EXTEND_ADS to DEFAULT_TIME_DELAY_SHOWING_EXTEND_ADS,
                REMOTE_KEY_NOTIFICATION_OUT_APP_INTERVAL_SECOND to DEFAULT_NOTIFICATION_OUT_APP_INTERVAL_SECOND,
                REMOTE_KEY_IS_SHOW_NOTIFICATION_OUT_APP to DEFAULT_IS_SHOW_NOTIFICATION_OUT_APP,
                REMOTE_KEY_FREE_TRIAL_BUTON_TEXT to DEFAULT_FREE_TRIAL_BUTTON_TEXT,
                REMOTE_KEY_IAP_BUTON_TEXT to DEFAULT_IAP_BUTTON_TEXT,
                REMOTE_KEY_IAP_EXPLAIN_TEXT to DEFAULT_IAP_EXPLAIN_TEXT,
                REMOTE_KEY_FREE_TRIAL_EXPLAIN_TEXT to DEFAULT_FREE_TRIAL_EXPLAIN_TEXT,
                REMOTE_KEY_IAP_BASE_PLAN to DEFAULT_IAP_BASE_PLAN,
                REMOTE_KEY_IS_FREE_TRIAL_ENABLE to DEFAULT_IS_FREE_TRIAL_ENABLE,
                REMOTE_KEY_SAVE_FILE_NEED_PREMIUM to DEFAULT_SAVE_FILE_NEED_PREMIUM

            )
        )
    }

    fun fetchRemoteConfig(onComplete: (Boolean) -> Unit) {
        firebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseUtil", "Remote config fetch successful!")
                    onComplete(true)
                } else {
                    Log.e("FirebaseUtil", "Remote config fetch failed!")
                    onComplete(false)
                }
            }
    }

    fun isForceUpdateRequired(): Boolean {
        return firebaseRemoteConfig.getBoolean(REMOTE_KEY_FORCE_UPDATE)
    }

    fun getMinVersionCode(): Int {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_MIN_VERSION_CODE).toInt()
    }

    fun getLatestVersion(): String {
        return firebaseRemoteConfig.getString(REMOTE_KEY_LATEST_VERSION)
    }

    fun getUpdateUserCount(): Number {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_UPDATE_USER_COUNT)
    }

    fun getUpdateFeatures(): String {
        return firebaseRemoteConfig.getString(REMOTE_KEY_UPDATE_FEATURES)
    }

    private fun getVersionCodeReviewing(): Number {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_VERSION_CODE_REVIEWING)
    }

    fun getFeedbackType(): Number {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_FEEDBACK_TYPE)
    }
    fun getNotificationFrequencyMinutes(): Long {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_NOTIFICATION_FREQUENCY_MINUTES)
    }
    fun getPDFDetailType(): Long {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_PDF_DETAIL_TYPE)
    }
    fun isRequestFeatureSettingOnOff(): Boolean {
        return firebaseRemoteConfig.getBoolean(REMOTE_KEY_REQUEST_FEATURE_SETTING_ON_OFF)
    }
    fun isFeedbackSettingOnOff(): Boolean {
        return firebaseRemoteConfig.getBoolean(REMOTE_KEY_FEEDBACK_SETTING_ON_OFF)
    }
    fun isRequestNotiActivityOnOff(): Boolean {
        return firebaseRemoteConfig.getBoolean(REMOTE_KEY_NOTI_ACTIVITY_ON_OFF)
    }
    fun isShowAdsReloadFileInter(): Boolean {
        return firebaseRemoteConfig.getBoolean(REMOTE_KEY_SHOW_ADS_RELOAD_FILE_INTER)
    }
    fun getLong(key: String): Long {
        return firebaseRemoteConfig.getLong(key)
    }
    fun getNotificationStepIncrease(): Long {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_NOTIFICATION_STEP_INCREASE)
    }
    fun getNotificationStepDecrease(): Long {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_NOTIFICATION_STEP_DECREASE)
    }
    fun getNotificationStepOrganic(): Long {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_NOTIFICATION_STEP_ORGANIC)
    }
    fun getNotificationMinTimeout(): Long {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_NOTIFICATION_MIN_TIMEOUT)
    }
    fun getNotificationMaxTimeout(): Long {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_NOTIFICATION_MAX_TIMEOUT)
    }
    fun isDialogCancelOnTouchOutside(): Boolean {
        return firebaseRemoteConfig.getBoolean(REMOTE_KEY_DIALOG_CANCEL_ON_TOUCH_OUTSIDE)
    }
    fun getNotificationMaxHour(): Int {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_NOTIFICATION_MAX_HOUR).toInt()
    }
    fun getNotificationMinHour(): Int {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_NOTIFICATION_MIN_HOUR).toInt()
    }
    fun getDurationReloadingFile(): Long {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_DURATION_RELOADING_FILE)
    }
    fun getDurationDelayShowingReloadGuide(): Long {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_DURATION_DELAY_SHOWING_RELOAD_GUIDE)
    }
    fun getTimeShowingReloadGuide(): Int {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_TIME_SHOWING_RELOAD_GUIDE).toInt()
    }
    fun getIntervalShowInterSecond(): Int {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_INTERVAL_SHOW_INTER_SECOND).toInt()
    }
    fun getTimeoutLoadInterMillisecond() : Long {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_TIMEOUT_LOAD_INTER_SECOND)*1000L
    }
    fun getTypeOfStartUp(): Int {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_TYPE_OF_START_UP).toInt()
    }
    fun isPreloadNativeLanguage(): Boolean {
        return firebaseRemoteConfig.getBoolean(REMOTE_KEY_PRELOAD_NATIVE_LANGUAGE)
    }
    fun isLogPurchaseEvent(): Boolean {
        return firebaseRemoteConfig.getBoolean(REMOTE_KEY_LOG_PURCHASE_EVENT)
    }
    fun getTimeBlockDefaultReader(): Int {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_TIME_BLOCK_DEFAULT_READER).toInt()
    }
    fun isTurnOffNotiServiceIfPremium(): Boolean {
        return firebaseRemoteConfig.getBoolean(REMOTE_KEY_TURN_OFF_NOTI_SERVICE_IF_PREMIUM)
    }
    fun isAlwaysRequestNotiWhenEnterApp(): Boolean {
        return firebaseRemoteConfig.getBoolean(REMOTE_KEY_ALWAYS_REQUEST_NOTI_WHEN_ENTER_APP)
    }
    fun getIapScreenType(): Int {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_IAP_SCREEN_TYPE).toInt()
    }
    fun getTypeAdsDetail(): Int {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_TYPE_ADS_DETAIL).toInt()
    }
    fun getLanguageAdapterType(): Int {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_LANGUAGE_ADAPTER_TYPE).toInt()
    }
    fun isShowAdsMain(): Boolean {
        return firebaseRemoteConfig.getBoolean(REMOTE_KEY_SHOW_ADS_MAIN)
    }
    fun isAllowSaveExcelToPDF(): Boolean {
        return firebaseRemoteConfig.getBoolean(REMOTE_KEY_ALLOW_SAVE_EXCEL_TO_PDF)
    }
    fun isBigAds(): Boolean {
        return firebaseRemoteConfig.getBoolean(REMOTE_KEY_IS_BIG_ADS)
    }
    fun isShowNotificationOutApp(): Boolean {
        return firebaseRemoteConfig.getBoolean(REMOTE_KEY_IS_SHOW_NOTIFICATION_OUT_APP)
    }
    fun getNotificationOutAppIntervalSecond(): Int {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_NOTIFICATION_OUT_APP_INTERVAL_SECOND).toInt()
    }
    fun getTimeDelayShowingExtendAds(): Int {
        return firebaseRemoteConfig.getLong(REMOTE_KEY_TIME_DELAY_SHOWING_EXTEND_ADS).toInt()
    }
    fun getAdsConfigValue(key: String): String {
        return try {
            val remoteJsonString = firebaseRemoteConfig.getString(REMOTE_KEY_ADS_CONFIG)
            val remoteJson = JSONObject(remoteJsonString)

            // Nếu Firebase không có key -> fallback sang default
            if (remoteJson.has(key)) {
                remoteJson.optString(key, "")
            } else {
                val defaultJson = JSONObject(DEFAULT_ADS_CONFIG)
                defaultJson.optString(key, "")
            }
        } catch (e: Exception) {
            try {
                // fallback hoàn toàn về default khi lỗi parse
                val defaultJson = JSONObject(DEFAULT_ADS_CONFIG)
                defaultJson.optString(key, "")
            } catch (_: Exception) {
                ""
            }
        }
    }
    fun getFreeTrialButtonText(): String {
        return firebaseRemoteConfig.getString(REMOTE_KEY_FREE_TRIAL_BUTON_TEXT).ifBlank { DEFAULT_FREE_TRIAL_BUTTON_TEXT}
    }
    fun getIapButtonText(): String {
        return firebaseRemoteConfig.getString(REMOTE_KEY_IAP_BUTON_TEXT).ifBlank { DEFAULT_IAP_BUTTON_TEXT}
    }
    fun getIapExplainText(price: String): String {
        val explainTextRaw = firebaseRemoteConfig.getString(REMOTE_KEY_IAP_EXPLAIN_TEXT)
        val explainText = explainTextRaw.ifBlank { DEFAULT_IAP_EXPLAIN_TEXT }

        return try {
            String.format(Locale.US, explainText, price)
        } catch (e: Exception) {
            // Fallback: if the template contains `%s`, replace occurrences; otherwise return the template as-is
            if (explainText.contains("%s")) explainText.replace("%s", price) else explainText
        }
    }

    fun getFreeTrialExplainText(price: String): String {
        val explainTextRaw = firebaseRemoteConfig.getString(REMOTE_KEY_FREE_TRIAL_EXPLAIN_TEXT)
        val explainText = explainTextRaw.ifBlank { DEFAULT_FREE_TRIAL_EXPLAIN_TEXT }

        return try {
            String.format(Locale.US, explainText, price)
        } catch (e: Exception) {
            // Fallback: if the template contains `%s`, replace occurrences; otherwise return the template as-is
            if (explainText.contains("%s")) explainText.replace("%s", price) else explainText
        }
    }
    fun getIapBasePlan(): String {
        return firebaseRemoteConfig.getString(REMOTE_KEY_IAP_BASE_PLAN).ifBlank { DEFAULT_IAP_BASE_PLAN }
    }
    fun isFreeTrialEnable(): Boolean {
        return firebaseRemoteConfig.getBoolean(REMOTE_KEY_IS_FREE_TRIAL_ENABLE)
    }
    fun saveFileNeedPremium(): Boolean {
        return firebaseRemoteConfig.getBoolean(REMOTE_KEY_SAVE_FILE_NEED_PREMIUM)
    }
}