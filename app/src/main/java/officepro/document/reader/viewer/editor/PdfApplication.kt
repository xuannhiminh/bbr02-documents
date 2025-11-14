package officepro.document.reader.viewer.editor

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.icu.util.TimeZone
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.akexorcist.localizationactivity.core.LocalizationApplicationDelegate
import com.brian.base_iap.iap.IapRouter
import com.brian.base_iap.utils.FCMTopicHandler
import com.brian.base_iap.iap.IapNavigationHandler
import com.brian.base_iap.utils.PreferencesHelper
import com.brian.base_iap.utils.PresKey
import com.ezstudio.pdftoolmodule.di.toolModule
import com.ezteam.baseproject.BuildConfig
import com.ezteam.baseproject.di.baseModule
import com.brian.base_iap.utils.IAPUtils
import com.brian.base_iap.utils.PreferencesUtils
import com.facebook.ads.AdSettings
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.nlbn.ads.util.AppFlyer
import com.nlbn.ads.util.AppOpenManager
import officepro.document.reader.viewer.editor.common.LocaleManager
import officepro.document.reader.viewer.editor.di.appModule
import officepro.document.reader.viewer.editor.notification.NotificationManager
import com.brian.base_iap.iap.IapActivity
import officepro.document.reader.viewer.editor.screen.start.SplashActivity
import com.brian.base_iap.utils.FirebaseRemoteConfigUtil
import office.file.ui.MyLibApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.analytics.FirebaseAnalytics
import com.brian.base_iap.iap.IapActivityV2
import officepro.document.reader.viewer.editor.screen.language.LanguageActivity
import officepro.document.reader.viewer.editor.screen.main.MainActivity

class PdfApplication: MyLibApplication(), DefaultLifecycleObserver {
    companion object {
        private const val TAG = "PdfApplication"
    }

    private val delegate = LocalizationApplicationDelegate()

    override fun onCreate() {
        FCMTopicHandler.isDebug = BuildConfig.DEBUG
        super<MyLibApplication>.onCreate()
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity::class.java)
        AppOpenManager.getInstance().disableAppResumeWithActivity(IapActivity::class.java)
        AppOpenManager.getInstance().disableAppResumeWithActivity(IapActivityV2::class.java)
        //AppFlyer.getInstance().initAppFlyer(this, getString(R.string.app_flyer_id), true, false, true) // thay
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        PreferencesUtils.init(this)
        setupKoin()
        PreferencesHelper.init(this)
        if (PreferencesHelper.getLong(PreferencesHelper.KEY_FIRST_OPEN, 0L) == 0L) {
            PreferencesHelper.putLong(PreferencesHelper.KEY_FIRST_OPEN, System.currentTimeMillis())
        }
        initLanguage()
        if (!BuildConfig.DEBUG) {
            Log.i(TAG, "onCreate: init Firebase")
            FirebaseApp.initializeApp(this)
            FirebaseAppCheck.getInstance()
                .installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
            FirebaseMessaging.getInstance()
                .subscribeToTopic("release_devices").addOnCompleteListener {
                    Log.d(TAG,"Subscribed to topic: release_devices")
                }
        } else {
//            FirebaseAppCheck.getInstance()
//                .installAppCheckProviderFactory(
//                    DebugAppCheckProviderFactory.getInstance()
//                )
            FirebaseMessaging.getInstance().subscribeToTopic("debug_device").addOnCompleteListener {
                Log.d(TAG,"Subscribed to topic: debug_device")
            }

            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)
        }
        subscribeToTimezoneTopic()

        FirebaseRemoteConfigUtil.getInstance().fetchRemoteConfig { Log.d(TAG, "fetched") }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("MyFirebaseMessagingService", "Token: $token")
            }
        FirebaseFirestore.getInstance()
        NotificationManager(this).createNotificationChannel()

        // Register lifecycle observer (replaces deprecated @OnLifecycleEvent)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        val testDeviceIds = listTestDeviceId
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()

        MobileAds.setRequestConfiguration(configuration)
        applyAdMobAPI35WorkaroundIfNeeded(this)
        AdSettings.addTestDevice("5ea02202-adb6-4eaa-8761-d4b29ad58851")
        RequestConfiguration.Builder().setTestDeviceIds(listTestDeviceId)
        IapRouter.navigationHandler = object : IapNavigationHandler {
            override fun goToNextScreen(activity: AppCompatActivity) {

                val isFirstStart = PreferencesHelper.getBoolean(PresKey.GET_START, true)

                if (isFirstStart) {
                    LanguageActivity.start(activity)
                } else {
                    MainActivity.start(activity)
                }
            }
        }
    }

    override fun enableAdsResume(): Boolean {
        return !IAPUtils.isPremium()
    }

    override fun getKeyRemoteIntervalShowInterstitial(): String? {
        return null
    }

    override fun getListTestDeviceId(): MutableList<String>? {
        return listOf("9C9F13F96407265FD1AD8B7217DFBB4F", "19CF3EF910710E71DA25CC42219EC340", "21EA4B3FCEDFE792A5CE1655F1B92B1F", "6877F63B09F390A8A9C2FBF6C0A89106",
            "DB2BB2022DEE9BB6351662532A8F6F05","7BF8EBE42FEB24B9C0231207C37B53FD", "87BCCFEB97F85D17C4D63F0257B0CBE5", "772B2E85D9A498676884AA26D2E687E7", "87BCCFEB97F85D17C4D63F0257B0CBE5") as MutableList<String>?
    }

    override fun getResumeAdId(): String {
        return FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("open_all")
    }


    override fun buildDebug(): Boolean {
        return BuildConfig.DEBUG
    }

    override fun isForceShowFullAdsTest(): Boolean {
        return true
    }

    override fun isPurchased(): Boolean {
        return IAPUtils.isPremium()
    }

    private fun initLanguage() {
        if (TextUtils.isEmpty(PreferencesHelper.getString(PreferencesHelper.KEY_LANGUAGE))) {
            officepro.document.reader.viewer.editor.screen.language.LocaleManager.getInstance(this).prefLanguage =
                officepro.document.reader.viewer.editor.screen.language.LocaleManager.LANGUAGE_DEFAULT
            val currentLanguage = resources.configuration.locales.get(0).language
            for (i in officepro.document.reader.viewer.editor.screen.language.LocaleManager.lstCodeLanguage.indices) {
                if (currentLanguage == officepro.document.reader.viewer.editor.screen.language.LocaleManager.lstCodeLanguage[i]) {
                    PreferencesHelper.putString(PreferencesHelper.KEY_LANGUAGE, currentLanguage)
                    delegate.setDefaultLanguage(this, resources.configuration.locales.get(0))
                    return
                }
            }
        }
    }
    
    override fun attachBaseContext(base: Context?) {
        base?.let {
            LocaleManager.init(it)
            super.attachBaseContext(LocaleManager.setLocale(it))
        } ?: kotlin.run {
            super.attachBaseContext(base)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleManager.setLocale(this)
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@PdfApplication)
            modules(
                appModule,
                baseModule,
                toolModule
            )
        }
    }


    fun subscribeToTimezoneTopic() {
        val tz = TimeZone.getDefault()
        val offsetHours = (tz.rawOffset + tz.dstSavings) / (1000 * 60 * 60)

        val topic = "utc${offsetHours}" // Example: "utc7"

        FirebaseMessaging.getInstance()
            .subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG,"Subscribed to topic: $topic")
                }
            }
    }
    private fun applyAdMobAPI35WorkaroundIfNeeded(application: Application) {
        if (Build.VERSION.SDK_INT < 35) return

        application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {
                applyAPI35WorkaroundToActivity(activity)
            }

            override fun onActivityResumed(activity: Activity) {
                applyAPI35WorkaroundToActivity(activity)
            }

            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun applyAPI35WorkaroundToActivity(activity: Activity) {
        if (activity.javaClass.name != "com.google.android.gms.ads.AdActivity") return

        val controller = activity.window.insetsController
        controller?.hide(WindowInsets.Type.systemBars())
        controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
    }

    override fun onStop(owner: LifecycleOwner) {
        // App in background
        NotificationManager(this).showCallUseAppNotificationWhenOutApp()

    }

    override fun onStart(owner: LifecycleOwner) {
        // App in foreground
    }

}
