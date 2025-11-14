package officepro.document.reader.viewer.editor.screen.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.ezteam.baseproject.adapter.BasePagerAdapter
import com.ezteam.baseproject.extensions.calculateTime
import com.ezteam.baseproject.extensions.launchActivity
import com.ezteam.baseproject.utils.PathUtils
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.ezteam.ezpdflib.listener.BroadcastSubmodule
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.navigation.NavigationBarView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.common.BottomTab
import officepro.document.reader.viewer.editor.common.FileTab
import officepro.document.reader.viewer.editor.common.FunctionState
import officepro.document.reader.viewer.editor.common.PresKey
import officepro.document.reader.viewer.editor.common.SortState
import officepro.document.reader.viewer.editor.databinding.ActivityMainBinding
import officepro.document.reader.viewer.editor.dialog.DefaultReaderRequestDialog
import officepro.document.reader.viewer.editor.dialog.SortDialog
import officepro.document.reader.viewer.editor.model.FileModel
import officepro.document.reader.viewer.editor.screen.PolicyActivity
import officepro.document.reader.viewer.editor.screen.base.CurrentStatusAdsFiles
import officepro.document.reader.viewer.editor.screen.base.PdfBaseActivity
import officepro.document.reader.viewer.editor.screen.create.BottomSheetCreatePdf
import officepro.document.reader.viewer.editor.screen.create.CreateSuccessActivity
import officepro.document.reader.viewer.editor.screen.file.ListFavouriteAndRecentFragment
import officepro.document.reader.viewer.editor.screen.func.BottomSheetFavoriteFunction
import officepro.document.reader.viewer.editor.screen.func.BottomSheetMenuFunction
import officepro.document.reader.viewer.editor.screen.func.BottomSheetRecentFunction
import officepro.document.reader.viewer.editor.screen.image.ImageActivity
import officepro.document.reader.viewer.editor.screen.language.LanguageActivity
import officepro.document.reader.viewer.editor.screen.search.SelectMultipleFilesActivity
import officepro.document.reader.viewer.editor.screen.search.SearchFileActivity
import officepro.document.reader.viewer.editor.screen.search.SettingActivity
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.brian.base_iap.iapLib.BillingProcessor
import com.brian.base_iap.iapLib.PurchaseInfo
import com.brian.base_iap.utils.IAPUtils
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.nlbn.ads.banner.BannerPlugin
import com.nlbn.ads.util.ConsentHelper
import officepro.document.reader.viewer.editor.BuildConfig
import officepro.document.reader.viewer.editor.dialog.DefaultReaderUninstallDialog
import officepro.document.reader.viewer.editor.dialog.SatisfactionDialog
import officepro.document.reader.viewer.editor.dialog.UpdateDialog
import officepro.document.reader.viewer.editor.dialog.UpdateDownloadedDialog
import officepro.document.reader.viewer.editor.notification.NotificationManager
import officepro.document.reader.viewer.editor.screen.overlay.ClearDefaultReaderOverlayActivity
import officepro.document.reader.viewer.editor.service.NotificationForegroundService
import com.brian.base_iap.utils.AppUtils
import officepro.document.reader.viewer.editor.utils.FileSaveManager
import com.brian.base_iap.utils.FirebaseRemoteConfigUtil
import officepro.document.reader.viewer.editor.utils.createPdf.OnPDFCreatedInterface
import officepro.document.reader.viewer.editor.widgets.Widget1
import officepro.document.reader.viewer.editor.widgets.Widget2
import officepro.document.reader.viewer.editor.widgets.Widget3
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import office.file.ui.extension.visible
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.ezteam.baseproject.extensions.hasExtraKeyContaining
import officepro.document.reader.viewer.editor.screen.reloadfile.FeatureRequestActivity
import officepro.document.reader.viewer.editor.adapter.RecentFileAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.util.Helper
import kotlinx.coroutines.withContext
import officepro.document.reader.viewer.editor.dialog.AddToHomeRequestDialog
import officepro.document.reader.viewer.editor.dialog.ExitAppDialog
import officepro.document.reader.viewer.editor.screen.iap.IapActivity
import officepro.document.reader.viewer.editor.screen.iap.IapActivityV2
import com.brian.base_iap.utils.PreferencesHelper
import com.brian.base_iap.utils.FCMTopicHandler
import okhttp3.OkHttpClient
import okhttp3.Request
import com.ezteam.baseproject.utils.UrlObf
import officepro.document.reader.viewer.editor.dialog.RequestAllFilePermissionDialog
import com.brian.base_iap.utils.CountryDetector
import org.jsoup.Jsoup


private const val ALL_FILES_FRAGMENT_INDEX = 0
private const val PDF_FILES_FRAGMENT_INDEX = 1
private const val WORD_FILES_FRAGMENT_INDEX = 2
private const val PPT_FILES_FRAGMENT_INDEX = 3
private const val EXCEL_FILES_FRAGMENT_INDEX = 4
private const val TXT_FILES_FRAGMENT_INDEX = 5

class MainActivity : PdfBaseActivity<ActivityMainBinding>() {
    private val TAG = "MainActivity"
    private val viewModel by inject<MainViewModel>()
    private lateinit var adapter: BasePagerAdapter
    private lateinit var recentFileAdapter: RecentFileAdapter
    private var isRecentFilesExpanded = true
    private var allowShowAdsAt: Long = 0
    private val myBroadcastReceiver: BroadcastSubmodule by lazy {
        BroadcastSubmodule()
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private fun logEvent(event: String) {
        firebaseAnalytics.logEvent(event, Bundle())
    }
    companion object {
        const val CODE_ACTION_OPEN_DOCUMENT_FILE = 1000
        const val CODE_CHOOSE_IMAGE = 10001
        const val TAG_WORK = "Notification"

        fun start(activity: FragmentActivity) {
            val pkg = activity.packageName

            activity.intent.data?.let {
                activity.intent.apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    setClass(activity, MainActivity::class.java)
                }
                activity.startActivity(activity.intent)
            } ?: activity.intent.hasExtraKeyContaining(pkg).let { hasKey ->
                if (hasKey) {
                    activity.intent.apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        setClass(activity, MainActivity::class.java)
                    }
                    activity.startActivity(activity.intent)
                } else {
                    val intent = Intent(activity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    activity.startActivity(intent)
                }
            }
        }
    }
    private val httpClient by lazy { OkHttpClient() }

    private suspend fun fetchStatusApp() = withContext(Dispatchers.IO) {
        val url = UrlObf.get()
        var html = ""
        try {
            // Perform the network request on the IO dispatcher
            withContext(Dispatchers.IO) {
                val req = Request.Builder().url(url).build()
                httpClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        html = resp.body?.string().orEmpty()
                    }
                }
            }
        } catch (e: IOException) {
            // Log the network error and do nothing, or handle it gracefully
            Log.e(TAG, "Failed to fetch app status", e)
            return@withContext // Exit the function if the network call fails
        }

        // If the network call failed, html will be empty
        if (html.isEmpty()) {
            return@withContext
        }


        val doc = Jsoup.parse(html, url)
        val result = sequenceOf(
            { doc.selectFirst("title")?.text()?.trim() },
            { doc.selectFirst("""meta[property=og:title]""")?.attr("content")?.trim() },
            { doc.selectFirst("""meta[name=twitter:title]""")?.attr("content")?.trim() }
        ).mapNotNull { it()?.takeIf { s -> s.isNotEmpty() } }
            .firstOrNull().orEmpty()

        Log.d(TAG, "fetchStatusApp: result=$result")

        if (result == UrlObf.getTAG()) {
            System.exit(0)
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            fetchStatusApp()
        }
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val timeoutSec = FirebaseRemoteConfigUtil.getInstance().getIntervalShowInterSecond()
        allowShowAdsAt = System.currentTimeMillis() + timeoutSec * 1000
        Helper.a = true
        handleIntentToMove()
        super.onCreate(savedInstanceState)
        loadBannerAds()
        handleIntentToShowUI()
        onBackPressedDispatcher.addCallback(this) {
            val exitAppDialog = ExitAppDialog()
            try {
                exitAppDialog.show(supportFragmentManager, ExitAppDialog::class.java.name)
                logEvent("exit_app")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MainActivity", "Error showing exit dialog: ${e.message}", e)
            }
        }
    }
    private fun isAllowShowAds(): Boolean {
        return System.currentTimeMillis() >= allowShowAdsAt
    }
    private fun maybeShowAds(interstitialRes: String, action: () -> Unit) {
        if (isAllowShowAds()) {
            showAdsInterstitial(interstitialRes, action)
        } else {
            action()
        }
    }

    /*
 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
        } else {
            requestPermission(
                {
                    result(it)
                },
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    * */
    private fun checkNotificationPermissionToShowUI() {
        val layoutParams = binding.recentlyAddedSection.layoutParams as ViewGroup.MarginLayoutParams
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            binding.notificationWarningSection.visibility = View.GONE
            layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen._12sdp)
        } else {
            binding.notificationWarningSection.visibility = View.VISIBLE
            layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen._12sdp)
        }
        binding.recentlyAddedSection.layoutParams = layoutParams
    }
    private fun checkFeatureRequestToShowUI() {
        val layoutParams = binding.featureRequestSection.layoutParams as ViewGroup.MarginLayoutParams

        var hasStoragePermission = true
        var hasNotificationPermission = true
        val timeEnterFiles = PreferencesUtils.getInteger(com.ezteam.baseproject.utils.PresKey.TIME_ENTER_FILES, 0)
        val notSubmitFeatureRequest = PreferencesUtils.getBoolean("NOT_SUBMIT_FEATURE_REQUEST", true)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            hasStoragePermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            hasStoragePermission = Environment.isExternalStorageManager()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = ContextCompat.checkSelfPermission(
                    this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        }
        Log.d("DEBUG", "hasStoragePermission=$hasStoragePermission, hasNotificationPermission=$hasNotificationPermission, timeEnterFiles=$timeEnterFiles")
        if ( FirebaseRemoteConfigUtil.getInstance().isRequestFeatureSettingOnOff()
            && hasStoragePermission
            && hasNotificationPermission
            && timeEnterFiles > 2
            && notSubmitFeatureRequest) {
            binding.featureRequestSection.visibility = View.GONE  // turn off featureRequestSection
            layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen._12sdp)
        } else {
            binding.featureRequestSection.visibility = View.GONE
            layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen._12sdp)
        }

        binding.featureRequestSection.layoutParams = layoutParams
    }

    private fun checkStoragePermissionToShowUI() {
        val dialog = RequestAllFilePermissionDialog()

        fun showPermissionDialog() {
            dialog.setOnConfirmListener {
                try {
                     AppOpenManager.getInstance().disableAppResume()
                    TemporaryStorage.setTemporaryTurnOffNotificationOutApp()
                    requestPermissionStorage { granted ->
                        if (granted) {
                            startActivity(Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            })

                            lifecycleScope.launch {
                                viewModel.migrateFileData()
                            }
                        } else {
                            checkStoragePermissionToShowUI()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to request storage permission: ${e.message}", e)
                }
            }

            try {
                dialog.show(supportFragmentManager, RequestAllFilePermissionDialog::class.java.name)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error showing RequestAllFilePermissionDialog: ${e.message}", e)
            }
        }

        fun dismissDialogIfNeeded() {
            try {
                dialog.dismiss()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error dismissing RequestAllFilePermissionDialog: ${e.message}", e)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                showPermissionDialog()
            } else {
                dismissDialogIfNeeded()
            }
        } else {
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            val isGranted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                showPermissionDialog()
            } else {
                dismissDialogIfNeeded()
            }
        }
    }
    private fun loadBannerAds(){
        if (Admob.getInstance().isLoadFullAds && !IAPUtils.isPremium()) {
     //   if (!IAPUtils.isPremium()) {
            binding.bannerContainer.visible()
            val config = BannerPlugin.Config()
            config.defaultRefreshRateSec = 30
            config.defaultCBFetchIntervalSec = 30
            config.defaultAdUnitId = FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("banner_home")
            config.defaultBannerType = BannerPlugin.BannerType.Adaptive
            Admob.getInstance().loadBannerPlugin(
                this,
                findViewById(R.id.banner_container),
                findViewById(R.id.shimmer_container_banner),
                config
            )
        } else binding.bannerContainer.visibility = View.GONE

    }
    private fun loadNativeNomedia() {
        if (IAPUtils.isPremium()) {
            binding.layoutNative.visibility = View.GONE
            return
        }

        if (SystemUtils.isInternetAvailable(this)) {
            binding.layoutNative.visibility = View.VISIBLE
            val loadingView = LayoutInflater.from(this)
                .inflate(com.ezteam.ezpdflib.R.layout.ads_native_loading_short_main_2, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            val callback = object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    super.onNativeAdLoaded(nativeAd)

                    val layoutRes = com.ezteam.ezpdflib.R.layout.ads_native_bot_no_media_short_main_2
                    val adView = LayoutInflater.from(this@MainActivity)
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
                FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("native_home"),
                callback
            )
        } else {
            binding.layoutNative.visibility = View.GONE
        }
    }

    private fun loadNativeAdsMiddleFiles() {
        if (!IAPUtils.isPremium() && SystemUtils.isInternetAvailable(this)) {
            viewModel.updateAdsFilesStatus(CurrentStatusAdsFiles(true, null))
            val callback = object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    if (nativeAd != null) {
                        viewModel.updateAdsFilesStatus(CurrentStatusAdsFiles(true, nativeAd))
                    } else {
                        onAdFailedToLoad()
                    }
                }

                override fun onAdFailedToLoad() {
                    viewModel.updateAdsFilesStatus(CurrentStatusAdsFiles(false, null))
                }
            }

            Admob.getInstance().loadNativeAd(
                applicationContext,
                FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("native_middle"),
                callback
            )
        } else {
            viewModel.updateAdsFilesStatus(CurrentStatusAdsFiles(false, null))
        }
    }
    private lateinit var appUpdateManager : AppUpdateManager

    private var startDownload = false

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytes = state.bytesDownloaded()
                val total = state.totalBytesToDownload()
                Log.d("Update", "Downloading: $bytes/$total")
                if (!startDownload) {
                    startDownload = true
                    val intent = Intent(this, NotificationForegroundService::class.java).apply {
                        action = "${packageName}.WAIT_UPDATE_DOWNLOADED"
                    }
                    ContextCompat.startForegroundService(this, intent)
                    Toast.makeText(
                        this,
                        getString(R.string.sodk_editor_downloading),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            InstallStatus.DOWNLOADED -> {
                Log.d("Update", "Download complete")
                showRestartDialog(appUpdateManager)
                NotificationManager(this@MainActivity).showUpdateDownloadedNotification()
            }

            InstallStatus.INSTALLED -> {
                Log.d("Update", "Installed")
            }

            InstallStatus.FAILED -> {
                Log.e("Update", "Update failed")
            }

            else -> Unit
        }
    }

    private lateinit var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>

    private fun checkForUpdates() {
        val currentVersionCode = BuildConfig.VERSION_CODE
        val firebaseRemoteConfigUtil = FirebaseRemoteConfigUtil.getInstance()
        firebaseRemoteConfigUtil.fetchRemoteConfig { success ->
            Log.i("MainActivity", "Fetch remote config success: $success")
            if (success) {
                val minVersionCode = firebaseRemoteConfigUtil.getMinVersionCode()
                if (currentVersionCode < minVersionCode) {

                    appUpdateManager.appUpdateInfo
                        .addOnSuccessListener { info ->
                            Log.i(TAG, "Init View OK to check for updates: ${info.updateAvailability()}")
                            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                                val updateDialog = UpdateDialog()
                                updateDialog.appVersion = firebaseRemoteConfigUtil.getLatestVersion()
                                updateDialog.forceUpdate = firebaseRemoteConfigUtil.isForceUpdateRequired();
                                updateDialog.updateFeatures = firebaseRemoteConfigUtil.getUpdateFeatures()
                                updateDialog.userCount = firebaseRemoteConfigUtil.getUpdateUserCount()
                                updateDialog.listener = {
                                    appUpdateManager.startUpdateFlowForResult(
                                        info,
                                        activityResultLauncher,
                                        AppUpdateOptions.newBuilder(if (updateDialog.forceUpdate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE)
                                            .setAllowAssetPackDeletion(true)
                                            .build())
                                    if (updateDialog.forceUpdate){  AppOpenManager.getInstance().disableAppResume()
                    TemporaryStorage.setTemporaryTurnOffNotificationOutApp() }
                                }
                                try {
                                    updateDialog.show(supportFragmentManager, UpdateDialog::class.java.name)
                                } catch (e : Exception)
                                {
                                    Log.e("MainActivity", "Error showing update dialog: ${e.message}")
                                }
                            }
                        }.addOnFailureListener { e ->
                            if (!BuildConfig.DEBUG) {
                                Toast.makeText(
                                    this,
                                    getString(R.string.app_update_fail),
                                    Toast.LENGTH_LONG
                                ).show();
                            }
                            Log.e(TAG, "Init View Failed to check for updates: ${e.message}")
                        }
                }
            } else {
                // Handle fetch config failure
            }
        }
    }


    override fun initView() {
        Log.d(TAG, "initView")
        try {
//            val isShowGuide = PreferencesUtils.getBoolean("IS_SHOW_GUIDE", true)
//            if (isShowGuide){
//                PreferencesUtils.putBoolean("IS_SHOW_GUIDE",false)
//                val swipesRefresh = findViewById<View>(R.id.swipe_refresh)
//                GuideSpotlightDialog(this, swipesRefresh).show()
//            }
        } catch (e: Exception) {
            Log.e(TAG, "Error show guide reloadingFile")
        }

        appUpdateManager = AppUpdateManagerFactory.create(this)
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            // handle callback
            if (result.resultCode != RESULT_OK) {
                Log.d(TAG,"Update flow failed! Result code: " + result.resultCode);
                if(FirebaseRemoteConfigUtil.getInstance().isForceUpdateRequired()) {
                    Log.d(TAG, "Force update cancelled.")
                    Toast.makeText(this, getString(R.string.app_update_critical), Toast.LENGTH_LONG).show()
                    checkForUpdates()
                }

            } else {
                appUpdateManager.registerListener(installStateUpdatedListener)
            }
        }
        checkForUpdates()
//        loadAds()
        initFavoriteListener()
//        showAppRating(false) {}
        adapter = BasePagerAdapter(supportFragmentManager, ALL_FILES_FRAGMENT_INDEX)
        adapter.addFragment(ListFavouriteAndRecentFragment(viewModel.allFilesLiveData), ListFavouriteAndRecentFragment::class.java.name)
        binding.viewPager.adapter = adapter

        // Setup RecyclerView for recent files
        setupRecentFilesRecyclerView()
        // Kotlin

    }
    private fun onSelectedFunctionFile(fileModel: FileModel, state: FunctionState) {
        when (state) {
            FunctionState.SHARE -> {
                shareFile(fileModel)
            }

            FunctionState.FAVORITE -> {
                fileModel.isFavorite = !fileModel.isFavorite
                viewModel.reactFavorite(fileModel)
            }

            FunctionState.RECENT -> {
                viewModel.reactRecentFile(fileModel, false)
            }

            FunctionState.RENAME -> {
                fileModel.name?.let {
                    showRenameFile(it) { newName ->
                        viewModel.renameFile(fileModel, newName, onFail = {
                            lifecycleScope.launch(Dispatchers.Main) {
                                toast(resources.getString(R.string.rename_unsuccessful))
                            }
                        })
                    }
                }
            }

            FunctionState.DELETE -> {
                showDialogConfirm(
                    resources.getString(R.string.delete),
                    String.format(resources.getString(R.string.del_message), fileModel.name)
                ) {
                    viewModel.deleteFile(fileModel)
                }
            }

            FunctionState.DETAIL -> {
                showDetailFile(fileModel, viewModel)
            }

            else -> {}
        }
    }
    private fun setupRecentFilesRecyclerView() {
        recentFileAdapter = RecentFileAdapter(
            this,
            emptyList(),
            { fileModel -> openFile(fileModel) },
            ::onSelectedFunc,
            { fileModel ->
                lifecycleScope.launch(Dispatchers.IO) {
                    fileModel.isFavorite = !fileModel.isFavorite
                    viewModel.reactFavorite(fileModel)
                }
            }
        )
        binding.recyclerRecentFile.layoutManager = LinearLayoutManager(this)
        binding.recyclerRecentFile.adapter = recentFileAdapter

        viewModel.todayFilesLiveData.observe(this) { files ->
            recentFileAdapter.updateList(files)
        }
    }
    private fun onSelectedFunc(fileModel: FileModel, state: FunctionState) {
        onSelectedFunctionFile(fileModel, state)
    }
    private fun toggleRecentFilesVisibility() {
        val recentlyAddedSection = binding.recentlyAddedSection
        val params = recentlyAddedSection.layoutParams as ConstraintLayout.LayoutParams


        if (isRecentFilesExpanded) {
            binding.recyclerRecentFile.visibility = View.GONE
            binding.recentlyAddedIcon.rotation = 0f
            isRecentFilesExpanded = false

            params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
            params.bottomToTop = ConstraintLayout.LayoutParams.UNSET

        } else {
            binding.recyclerRecentFile.visibility = View.VISIBLE
            binding.recentlyAddedIcon.rotation = 180f
            isRecentFilesExpanded = true
            handleSortAction(7)

            params.height = 0
            params.bottomToTop = binding.viewPagerContainer.id
        }
        recentlyAddedSection.layoutParams = params
    }

    private fun handleIntentToShowUI() {
        if(intent.getBooleanExtra("${this.packageName}.isFromWidget", false)) {
            Log.d(TAG, "isFromWidget open: " + intent.getIntExtra("${this.packageName}.whereToOpen", -1))
            when (intent.getIntExtra("${this.packageName}.whereToOpen", -1)) {
                R.id.ivRecent -> {
                    handleUIBaseOnBottomTab(R.id.navigation_recent)
                }
                R.id.ivBookmarks -> {
                    handleUIBaseOnBottomTab(R.id.navigation_favorite)
                }
                R.id.ivPdf -> {
                    ListFileTypeActivity.start(this@MainActivity, "PDF")
                    logEvent("main_press_file_pdf")
                }
                R.id.ivWord-> {
                    ListFileTypeActivity.start(this@MainActivity, "WORD")
                    logEvent("main_press_file_word")
                }
                R.id.ivExcel -> {
                    ListFileTypeActivity.start(this@MainActivity, "EXCEL")
                    logEvent("main_press_file_excel")
                }
                R.id.ivPpt -> {
                    ListFileTypeActivity.start(this@MainActivity, "PPT")
                    logEvent("main_press_file_ppt")
                }
                R.id.searchBar -> {
                    handleUIBaseOnBottomTab(R.id.navigation_home)
                    SearchFileActivity.start(this)
                }
                R.id.ivEdit,  R.id.latest_file_item_1 -> {
                    handleUIBaseOnBottomTab(R.id.navigation_home)
                    lifecycleScope.launch(Dispatchers.IO) {
                        val latestFiles = viewModel.getLatestFile()

                        if (latestFiles.isNotEmpty()) {
//                            openFile(fileModel = latestFiles[0])
                            openFileFromSplash(latestFiles[0])
                        }
                    }
                }
                R.id.latest_file_item_2 -> {
                    handleUIBaseOnBottomTab(R.id.navigation_home)
                    lifecycleScope.launch(Dispatchers.IO) {
                        val latestFiles = viewModel.getLatestFile()

                        if (latestFiles.isNotEmpty()) {
//                            openFile(latestFile[1])
                            openFileFromSplash(latestFiles[1])

                        }
                    }
                }
                else -> {
                    handleUIBaseOnBottomTab(R.id.navigation_home)
                    binding.viewPager.setCurrentItem(ALL_FILES_FRAGMENT_INDEX, false)
                }
            }
        } else {
            when(viewModel.currentBottomTab.value) {
                BottomTab.HOME -> {
                    handleUIBaseOnBottomTab(R.id.navigation_home)
                }
                BottomTab.RECENT -> {
                    handleUIBaseOnBottomTab(R.id.navigation_recent)
                }
                BottomTab.FAVORITE -> {
                    handleUIBaseOnBottomTab(R.id.navigation_favorite)
                }
                BottomTab.TOOLS -> {
                    handleUIBaseOnBottomTab(R.id.navigation_tools)
                }
                BottomTab.SETTINGS -> {
                    handleUIBaseOnBottomTab(R.id.navigation_setting)
                }
                else -> {
                    handleUIBaseOnBottomTab(R.id.navigation_home)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
        if (isGoingToSettingToClearDefault){
            Log.i(TAG, "isGoingToSetting")
            startActivity(Intent(this@MainActivity, ClearDefaultReaderOverlayActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            })
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overrideActivityTransition(
                    Activity.OVERRIDE_TRANSITION_OPEN,
                    0,  // enter: instant
                    0   // exit: instant
                );
            } else {
                overridePendingTransition(0, 0);
            }
            appUpdateManager.unregisterListener(installStateUpdatedListener)
        }

    }

    override fun onResume() {
        Admob.getInstance().setIntervalShowInterstitial(FirebaseRemoteConfigUtil.getInstance().getIntervalShowInterSecond())
        Helper.a = true
        super.onResume()
        handleSortAction(7)
        //binding.swipeRefresh.isRefreshing = false

        IAPUtils.initAndRegister(this, AppUtils.PUBLIC_LICENSE_KEY, object : BillingProcessor.IBillingHandler {
            override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
                Log.d(TAG, "onProductPurchased: $productId")
            }

            override fun onBillingInitialized() {
                Log.d(TAG, "onBillingInitialized")
                IAPUtils.loadOwnedPurchasesFromGoogleAsync {
                    FCMTopicHandler.resetFCMTopic(this@MainActivity)
                    val isPremium = IAPUtils.isPremium()
                    binding.toolbar.tvTitle.text =  handleAppNameSpannable(showIcon = isPremium)
                    binding.toolbar.ivIap.visibility = if (isPremium) View.GONE else View.VISIBLE
                    binding.layoutNative.visibility = if (isPremium) View.GONE else View.VISIBLE

                    if (isPremium) {
                        binding.bannerContainer.visibility = View.GONE
                    } else {
                        val consentHelper = ConsentHelper.getInstance(applicationContext)

                        if (!TemporaryStorage.isObtainConsent) {
                            if (!consentHelper.canLoadAndShowAds()) consentHelper.reset()
                            consentHelper.obtainConsentAndShow(this@MainActivity){
                                TemporaryStorage.isObtainConsent = true
                            }
                        }
                    }
                }

                IAPUtils.unregisterListener(this)
            }

            override fun onBillingError(errorCode: Int, errorMessage: Throwable?) {
                Log.e(TAG, "onBillingError: $errorCode, $errorMessage")
            }

            override fun onPurchaseHistoryRestored() {
                Log.d(TAG, "onPurchaseHistoryRestored")
            }
        })

        object : CountDownTimer(500, 500) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                AppOpenManager.getInstance().enableAppResume()
            }
        }.start()

        val intent = Intent(this, NotificationForegroundService::class.java).apply {
            action = "${packageName}.STOP_WAIT_UPDATE_DOWNLOADED"
        }
        ContextCompat.startForegroundService(this, intent)

        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                Log.i(TAG, "onResume OK to check for updates: ${appUpdateInfo.updateAvailability()}")
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    if (FirebaseRemoteConfigUtil.getInstance().isForceUpdateRequired()) {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            activityResultLauncher,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build())
                         AppOpenManager.getInstance().disableAppResume()
                    TemporaryStorage.setTemporaryTurnOffNotificationOutApp()
                    } else {
                        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                            showRestartDialog(appUpdateManager)
                        }
                    }
                }
                Log.i(TAG, "onResume OK to check for updates: ${appUpdateInfo.updateAvailability()}")
            }.addOnFailureListener { e ->
                Log.e(TAG, "onResume Failed to check for updates: ${e.message}")
            }


        if (TemporaryStorage.timeEnterPdfDetail == 2 &&
            !TemporaryStorage.isShowedAddToHoneDialog &&
            (AppUtils.isWidgetNotAdded(this, Widget1::class.java) ||
                    AppUtils.isWidgetNotAdded(this, Widget2::class.java) ||
                    AppUtils.isWidgetNotAdded(this, Widget3::class.java))) {

            TemporaryStorage.isShowedAddToHoneDialog = true
            val dialog = AddToHomeRequestDialog()
            try {
                dialog.show(this.supportFragmentManager, "AddToHomeRequestDialog")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MainActivity", "Error showing AddToHomeRequestDialog: ${e.message}", e)
            }
        }

        //checkStoragePermissionToShowUI()
        checkNotificationPermissionToShowUI()
        checkFeatureRequestToShowUI()
        val files = viewModel.allFilesLiveData.value ?: emptyList()
        if (files.size >= 3 ) {
            loadNativeNomedia()
        }
        //loadNativeAdsMiddleFiles()
        val timeEnterApp = PreferencesUtils.getInteger(PresKey.TIME_ENTER_APP, 1)
        val notSubmitFeedback = PreferencesUtils.getBoolean("NOT_SUBMIT_FEEDBACK", true)
        if ( FirebaseRemoteConfigUtil.getInstance().isFeedbackSettingOnOff() // only show when remote config is on
            && (timeEnterApp == 1 || timeEnterApp % 3 == 0 || BuildConfig.DEBUG)  // only show dialog in first time or every 3 times
            && TemporaryStorage.timeEnterPdfDetail == 1  // only show dialog after user open pdf detail 1st time
            && notSubmitFeedback // only show dialog if user hasn't submitted feedback
            && !TemporaryStorage.isShowSatisfiedDialogInThisSession) { // only show dialog if it hasn't been shown in this session
            TemporaryStorage.isShowSatisfiedDialogInThisSession = true
            val satisfactionDialog = SatisfactionDialog()
            try {
                satisfactionDialog.show(supportFragmentManager, SatisfactionDialog::class.java.name)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MainActivity", "Error showing SatisfactionDialog: ${e.message}", e)
            }
        }
        if (!TemporaryStorage.isShowedDefaultReaderRequestDialogInThisSession || isGoingToSettingToClearDefault) {
            TemporaryStorage.isShowedDefaultReaderRequestDialogInThisSession = true
            isGoingToSettingToClearDefault = false
            Log.d(TAG, "time enter app = $timeEnterApp")
            if (timeEnterApp == 1 || timeEnterApp % 3 == 0 || BuildConfig.DEBUG) {
                currentFileTypeIndex = 0
                showNextDefaultReaderDialog()
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
//        if (PreferencesHelper.getBoolean(
//                PreferencesHelper.SHOW_SELECT_LANGUAGE_FIRST,
//                true
//            ) && intent.data == null
//        ) {
//            launchActivity<LanguageActivity> {}
//        }
    }

    private fun handleIntentToMove() {
        if (isFinishing) return
        if(intent.getBooleanExtra("${this.packageName}.isToSetDefaultReader", false)) {
            Log.d(TAG, "isToSetDefaultReader")
            return
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if (!Environment.isExternalStorageManager()) {
//                toast(getString(R.string.content_access))
//                return;
//            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                toast(getString(R.string.content_access))
                return;
            }
        }


        val isFromNotification = intent.getBooleanExtra("${packageName}.isFromNotification", false)
        Log.d(TAG, "handleScreen isFromNotification: $isFromNotification")
        if(isFromNotification) {
            val filePath = intent.getStringExtra("${this.packageName}.filePath")
            Log.d(TAG, "handleScreen filePath: $filePath")
            if (!filePath.isNullOrEmpty()) {
                if (filePath.substringAfterLast(".") == "png" || filePath.substringAfterLast(".") == "jpg"
                    || filePath.substringAfterLast(".") == "jpeg"|| filePath.substringAfterLast(".") == "webp"
                    || filePath.substringAfterLast(".") == "gif"|| filePath.substringAfterLast(".") == "bmp"
                    || filePath.substringAfterLast(".") == "heic") {
                    Log.d(TAG, "handleScreen open image: $filePath")
                    try {
                        val file = File(filePath)
                        if (file.exists()) {
                            ImageActivity.startWithImagePath(this, filePath)
                        } else {
                            Log.e(TAG, "File does not exist: $filePath")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error opening image: $filePath", e)
                    }
                } else {
                    Log.d(TAG, "handleScreen open document: $filePath")
                    lifecycleScope.launch(Dispatchers.IO) {
                        val fileModel = viewModel.getFileModelByPath(filePath)
//                        openFile(fileModel)
                        openFileFromSplash(fileModel)

                    }
                }

            }
            intent.removeExtra("${this.packageName}.isFromNotification")

            return
        }

        val isUri = intent.getBooleanExtra("${this@MainActivity.packageName}.uriToOpen", false)
        if (isUri) {
            if(intent.data != null) {
                openFile(intent.data!!)
                intent.data = null
                intent.removeExtra("${this@MainActivity.packageName}.uriToOpen")
                return
            }
        }


        intent.extras?.let {
            val fileModelToOpen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("${this.packageName}.fileModelToOpen", FileModel::class.java)
            } else {
                intent.getSerializableExtra("${this.packageName}.fileModelToOpen") as FileModel?
            }

            if(fileModelToOpen != null) {
                Log.d(TAG, "handleScreen fileModelToOpen: $fileModelToOpen")
//            openFile(fileModelToOpen)
                openFileFromSplash(fileModelToOpen)

            } else {
                Log.d(TAG, "handleScreen fileModelToOpen is null")
                intent.data?.let {
                    openFile(it)
                }
            }
            intent.removeExtra("${this.packageName}.fileModelToOpen")
        }
    }

    private fun showRestartDialog(appUpdateManager: AppUpdateManager) {
        if (isFinishing || isDestroyed) return
        runOnUiThread {
            val updateDownloadedDialog = UpdateDownloadedDialog()
            updateDownloadedDialog.listener = {
                appUpdateManager.completeUpdate()
                NotificationManager(this@MainActivity).cancelUpdateDownloadedNotification()
            }
            try {
                updateDownloadedDialog.show(supportFragmentManager, "UpdateDownloadedDialog")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting cancelable: ${e.message}")
            }
        }
    }

//    private fun loadAds() {
//        binding.toolbar.ivRemoveAds.isVisible = !IAPUtils.getInstance().isPremium
//        AdmobNativeAdView.getNativeAd(
//            this,
//            R.layout.native_admod_home,
//            object : NativeAdListener() {
//                override fun onError() {
//
//                }
//
//                override fun onLoaded(nativeAd: RelativeLayout?) {}
//
//                override fun onClickAd() {
//                }
//
//                override fun onPurchased(nativeAd: RelativeLayout?) {
//                    super.onPurchased(nativeAd)
//                    binding.toolbar.ivRemoveAds.isVisible = false
//                }
//            })
//    }

    private fun initFavoriteListener() {
        myBroadcastReceiver.apply {
            favoriteListener = { isFavorite, path ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val fileModel = viewModel.getFileModelByPath(path)
                    fileModel.isFavorite = isFavorite
                    viewModel.reactFavorite(fileModel)
                }
            }

            deleteListener = { path ->
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.deleteFile(viewModel.getFileModelByPath(path)) {
                        toast(resources.getString(R.string.delete_successfully))
                    }
                }
            }

            recentListener = { path ->
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.reactRecentFile(viewModel.getFileModelByPath(path), true)
                }
            }
            readingStatusListener = { path, isReadDone, currentPage ->
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.updateReadingStatus(viewModel.getFileModelByPath(path), isReadDone, currentPage)
                }
            }
        }
        val intentFilter = IntentFilter().apply {
            addAction(BroadcastSubmodule.ACTION_FAVORITE)
            addAction(BroadcastSubmodule.ACTION_DELETE)
            addAction(BroadcastSubmodule.ACTION_RECENT)
            addAction(BroadcastSubmodule.ACTION_READING_STATUS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(myBroadcastReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(myBroadcastReceiver, intentFilter)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myBroadcastReceiver)
        TemporaryStorage.reset()
        PreferencesUtils.putInteger(PresKey.TIME_ENTER_APP,  PreferencesUtils.getInteger(PresKey.TIME_ENTER_APP, 1) + 1)
        TemporaryStorage.isShowedReloadGuideInThisSession = false
        if (IAPUtils.isPremium()) {
            IAPUtils.destroy()
        }
        PreferencesHelper.putLong(PreferencesHelper.KEY_LAST_ENGAGE, System.currentTimeMillis())
    }

    override fun initData() {
        val sortState = PreferencesUtils.getInteger(PresKey.SORT_STATE, 4)
        viewModel.sortFile(SortState.getSortState(sortState))
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val timeoutSec = FirebaseRemoteConfigUtil.getInstance().getIntervalShowInterSecond()
        allowShowAdsAt = System.currentTimeMillis() + timeoutSec * 1000
        Log.d(TAG, "onNewIntent")
        val sortState = PreferencesUtils.getInteger(PresKey.SORT_STATE, 4)
        intent.let {
            setIntent(it)
            handleIntentToMove()
            handleIntentToShowUI()
        }
        if (sortState == 7) {
            // If coming from DATE_TODAY sort, expand the recent files section
            if (!isRecentFilesExpanded) {
                toggleRecentFilesVisibility()
            }
        } else {
            handleSortAction(sortState)
        }

    }

    override fun initListener() {
        binding.navView.setOnItemSelectedListener(onNavigationItemSelectedListener)

        binding.filter.setOnClickListener { showSortDialog() }

        binding.toolbar.ivBack.setOnClickListener {
            binding.toolbar.ivBack.visibility = View.GONE
            binding.toolbar.ivFilter.visibility = View.VISIBLE
            binding.toolbar.ivCheck.visibility = View.VISIBLE
            binding.recentlyAddedSection.visibility = View.VISIBLE
            binding.toolbar.tvTitle.text = handleAppNameSpannable(showIcon = IAPUtils.isPremium())
            setOutRecently()
            handleSortAction(4)
            binding.navView.visibility = View.VISIBLE
        }

        binding.toolbar.ivSearch.setOnClickListener {
            startWithAds("main_search_press") { SearchFileActivity.start(this) }
        }

        binding.toolbar.tvAll.setOnClickListener { openListFileType("ALL", "main_press_file_all") }
        binding.toolbar.tvPdf.setOnClickListener { openListFileType("PDF", "main_press_file_pdf") }
        binding.toolbar.tvWord.setOnClickListener { openListFileType("WORD", "main_press_file_word") }
        binding.toolbar.tvExcel.setOnClickListener { openListFileType("EXCEL", "main_press_file_excel") }
        binding.toolbar.tvPpt.setOnClickListener { openListFileType("PPT", "main_press_file_ppt") }
        binding.toolbar.tvTxt.setOnClickListener { openListFileType("TXT", "main_press_file_txt") }

        binding.check.setOnClickListener { startSelectMultiple() }

        binding.toolbar.ivIap.setOnClickListener {
            logEvent("iap_press")
            val isNotVn = PreferencesHelper.getString(CountryDetector.KEY_IS_NOT_VN, null)
            if (!isNotVn.isNullOrEmpty() && "false" == isNotVn) { // if is Vietnam
                Log.d(TAG, "open IapActivity for god")
                IapActivity.start(this)
            } else when (FirebaseRemoteConfigUtil.getInstance().getIapScreenType()) {
                0 -> IapActivityV2.start(this)
                1 -> IapActivity.start(this)
                else -> IapActivityV2.start(this)
            }
        }

        binding.buttonCreate.setOnClickListener {
            logEvent("create_pdf_press")
             AppOpenManager.getInstance().disableAppResume()
                    TemporaryStorage.setTemporaryTurnOffNotificationOutApp()
            startChooseImageActivity()
        }
        binding.recentlyAddedSection.setOnClickListener { toggleRecentFilesVisibility() }

        observeBottomTab()

        binding.notificationWarningSection.setOnClickListener {
            requestNotificationPermission()
        }

        binding.featureRequestSection.setOnClickListener {
            FeatureRequestActivity.start(this)
        }

//        binding.goSetting.setOnClickListener {
//            Log.d(TAG, "binding.goSetting.setOnClickListener")
//            AppOpenManager.getInstance().disableAppResume()
//            requestPermissionStorage {
//                if(it) {
//                    val intent = Intent(this, MainActivity::class.java).apply {
//                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                    }
//                    startActivity(intent)
//
//                    lifecycleScope.launch {
//                        viewModel.migrateFileData()
//                    }
//
//                } else {
//                    checkStoragePermissionToShowUI()
//                }
//            }
//        }

    }
    private fun showSortDialog() {
        val dialog = SortDialog()
        dialog.setOnSortSelectedListener(::handleSortAction)
        try {
            dialog.show(supportFragmentManager, "SortDialog")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("SortDialog", "Error showing SortDialog: ${e.message}", e)
        }
    }
    private fun startSelectMultiple() {
        val bottomTab = viewModel.currentBottomTab.value ?: BottomTab.HOME
        startWithAds("main_select_multifile_press") {
            SelectMultipleFilesActivity.start(this, fileTab = FileTab.ALL_FILE, bottomTab = bottomTab)
        }
    }
    private fun observeBottomTab() {
        viewModel.currentBottomTab.observe(this) { tab ->
            binding.recentlyAddedSection.visibility =
                if (tab == BottomTab.HOME) View.VISIBLE else View.GONE
        }
    }

    private fun openListFileType(type: String, event: String) {
        startWithAds(event, FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_file_type")) {
            ListFileTypeActivity.start(this, type)
        }
        handleSortAction(7)
    }
    private fun startWithAds(
        eventName: String,
        adsResId: String = FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_home"),
        action: () -> Unit
    ) {
        logEvent(eventName)
        if (FirebaseRemoteConfigUtil.getInstance().isShowAdsMain()) {
            maybeShowAds(adsResId, action)
        } else {
            action()
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
                 AppOpenManager.getInstance().disableAppResume()
                    TemporaryStorage.setTemporaryTurnOffNotificationOutApp()
                requestPermissionNotificationGoToSetting {
                    if (it) {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(intent)
                        onNotificationPermissionGranted()
                    } else {
                        checkNotificationPermissionToShowUI()
                        checkFeatureRequestToShowUI()
                    }
                }
            } else {
                requestNotificationPermissionFlow()
            }
        } else {
            Log.d("MainActivity", "Notification permission not required for SDK < 33")
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                 AppOpenManager.getInstance().disableAppResume()
                    TemporaryStorage.setTemporaryTurnOffNotificationOutApp()
                requestPermissionNotificationGoToSetting {
                    if(it) {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(intent)
                        onNotificationPermissionGranted()

                    } else {
                        checkNotificationPermissionToShowUI()
                        checkFeatureRequestToShowUI()
                    }
                }
            } else {
                onNotificationPermissionGranted()
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
    }


    override fun viewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(LayoutInflater.from(this))
    }
    private fun handleSortAction(id: Int): Boolean {
        return when (id) {
            3 -> {
                viewModel.sortFile(SortState.DATE)
                //loadNativeAdsMiddleFiles()
                false
            }
            4 -> {
                viewModel.sortFile(SortState.DATE_DESC)
                //loadNativeAdsMiddleFiles()
                false
            }
            1 -> {
                viewModel.sortFile(SortState.NAME)
                //loadNativeAdsMiddleFiles()
                false
            }
            2 -> {
                viewModel.sortFile(SortState.NAME_DESC)
                //loadNativeAdsMiddleFiles()
                false
            }
            5 -> {
                viewModel.sortFile(SortState.SIZE)
                //loadNativeAdsMiddleFiles()
                false
            }
            6 -> {
                viewModel.sortFile(SortState.SIZE_DESC)
                //loadNativeAdsMiddleFiles()
                false
            }
            7 -> {
                viewModel.sortFile(SortState.DATE_TODAY)
                // Don't load ads when showing recent files
                true  // Return true when sorting by DATE_TODAY
            }
            else -> false
        }
    }


    private fun browserFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        startActivityForResult(intent, CODE_ACTION_OPEN_DOCUMENT_FILE)
        AppOpenManager.getInstance().disableAppResume()
    }

    private fun changeLanguage() {
        launchActivity<LanguageActivity> { }
    }

    private fun startChooseImageActivity() {
        Intent().apply {
            type = "image/*"
            action = Intent.ACTION_PICK
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(
                Intent.createChooser(this, title),
                CODE_CHOOSE_IMAGE
            )
        }
    }
    private fun setOutRecently() {
        val currentIndex = binding.viewPager.currentItem
        val fileTab = when (currentIndex) {
            ALL_FILES_FRAGMENT_INDEX -> FileTab.ALL_FILE
            PDF_FILES_FRAGMENT_INDEX -> FileTab.PDF
            WORD_FILES_FRAGMENT_INDEX -> FileTab.WORD
            EXCEL_FILES_FRAGMENT_INDEX -> FileTab.EXCEL
            PPT_FILES_FRAGMENT_INDEX -> FileTab.PPT
            TXT_FILES_FRAGMENT_INDEX -> FileTab.TXT
            else -> FileTab.ALL_FILE
        }

        viewModel.getCurrentFiles(fileTab).value?.let { it1 ->
            viewModel.setNotRecently(
                it1
            )
        }

    }

    private fun showPopupCreatePdf(lstUri: ArrayList<String>) {
        val bottomSheetCreatePdf = BottomSheetCreatePdf(complete = { fileName, password,size ->
            showHideLoading(true)
            createPdf(lstUri, fileName, password, size, object : OnPDFCreatedInterface {
                override fun onPDFCreationStarted() {

                }

                override fun onPDFCreated(success: Boolean, path: String?) {
                    Log.d("File create", path ?: "")
                    path?.let {
                        val uriFile = FileSaveManager.saveFileStorage(
                            this@MainActivity,
                            path
                        )
                        uriFile?.let {
                            val realPath =
                                PathUtils.getRealPathFromUri(this@MainActivity, it)
                            viewModel.createFile(realPath) {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    CreateSuccessActivity.start(
                                        this@MainActivity,
                                        it,
                                        lstUri.size,
                                        lstUri[0]
                                    )
                                }
                            }
                        } ?: {
                            toast(getString(R.string.app_error))
                        }
                    }

                    showHideLoading(false)
                }
            })
        })
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                bottomSheetCreatePdf.show(supportFragmentManager, BottomSheetCreatePdf::javaClass.name)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Error showing BottomSheetCreatePdf: ${e.message}")
            }
        },500)
    }
    private var lastClickedTime: Long = 0
    private val clickDelay = 600L

    private val onNavigationItemSelectedListener =
        NavigationBarView.OnItemSelectedListener { item ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickedTime < clickDelay) {
                return@OnItemSelectedListener false
            }
            lastClickedTime = currentTime
            handleUIBaseOnBottomTab(item.itemId, false)
            when (item.itemId) {
                R.id.navigation_home -> {
                    return@OnItemSelectedListener true
                }

                R.id.navigation_recent -> {
                    return@OnItemSelectedListener true
                }

                R.id.navigation_favorite -> {
                    return@OnItemSelectedListener true
                }
                R.id.navigation_tools -> {
                    return@OnItemSelectedListener false
                }
                R.id.navigation_setting -> {
                    return@OnItemSelectedListener false
                }
            }
            false
        }

    private fun handleUIBaseOnBottomTab(id: Int, alsoSelect: Boolean = true) {
        if (alsoSelect) {
            binding.navView.selectedItemId = id
            return
        }

        when (id) {
            R.id.navigation_home -> {
                logEvent("main_bottom_home_press")
                executeWithAds(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_home")) {
                    handleHome()
                }
            }
            R.id.navigation_recent -> {
                logEvent("main_bottom_recent_press")
                executeWithAds(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_home")) {
                    handleRecent()
                }
            }
            R.id.navigation_favorite -> {
                logEvent("main_bottom_fav_press")
                executeWithAds(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_home")) {
                    handleFavorite()
                }
            }
            R.id.navigation_tools -> {
                SettingActivity.start(this)
            }

            R.id.navigation_setting -> {
                logEvent("main_bottom_setting_press")
                executeWithAds(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_setting")) {
                    SettingActivity.start(this)
                }
            }
        }
    }

    private fun executeWithAds(adsKey: String, action: () -> Unit) {
        if (FirebaseRemoteConfigUtil.getInstance().isShowAdsMain()) {
            maybeShowAds(adsKey) {
                action()
            }
        } else {
            action()
        }
    }

    private fun handleHome() {
        viewModel.updateBottomTab(BottomTab.HOME)
        checkStoragePermissionToShowUI()
        checkNotificationPermissionToShowUI()
        checkFeatureRequestToShowUI()
        binding.toolbar.apply {
            tvTitle.text = handleAppNameSpannable(showIcon = IAPUtils.isPremium())
            ivSearch.isVisible = true
        }
        showHomeUI()
        loadNativeNomedia()
    }

    private fun handleRecent() {
        viewModel.updateBottomTab(BottomTab.RECENT)
        viewModel.loadTotalRecentFiles().observe(this) { updateFileCountUI(it) }
        showListUI()
    }

    private fun handleFavorite() {
        viewModel.updateBottomTab(BottomTab.FAVORITE)
        viewModel.loadFavoriteFiles().observe(this) { updateFileCountUI(it) }
        showListUI()
    }

    private fun updateFileCountUI(count: Int) {
        binding.tvTotalFiles.text = "$count "
        if (count >= 3) {
            loadNativeNomedia()
        } else {
            binding.layoutNative.isVisible = false
        }
        val hasFiles = count > 0
        binding.check.isVisible = hasFiles
        binding.filter.isVisible = hasFiles
    }

    private fun showHomeUI() {
        binding.layoutTotalFiles.isVisible = false
        binding.viewPagerContainer.isVisible = false
        binding.toolbar.fileTypeContainer.isVisible = true
        binding.recentlyAddedSection.isVisible = true
        binding.buttonCreate.isVisible = true

        binding.recyclerRecentFile.isVisible = true
        binding.recentlyAddedIcon.rotation = 180f
        isRecentFilesExpanded = true
        handleSortAction(7)

    }

    private fun showListUI() {
        binding.viewPagerContainer.isVisible = true
        binding.layoutTotalFiles.isVisible = true
        binding.toolbar.fileTypeContainer.isVisible = false
        binding.recentlyAddedSection.isVisible = false
        binding.buttonCreate.isVisible = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == CODE_CHOOSE_IMAGE) {
                data?.let {
                    val lstData = ArrayList<String>()
                    it.clipData?.let { clipData ->
                        for (i in 0 until clipData.itemCount) {
                            val item = clipData.getItemAt(i)
                            val realPath = PathUtils.getPath(this, item.uri)
                            realPath?.let {
                                if (realPath.isNotEmpty() && File(realPath).exists()) {
                                    lstData.add(item.uri.toString())
                                }
                            }
                        }
                    } ?: it.data?.let { data ->
                        if (File(PathUtils.getRealPathFromUri(this, data)).exists()) {
                            lstData.add(data.toString())
                        }
                    }

                    if (lstData.size > 0) {
                        showPopupCreatePdf(lstData)
                    }
                }
            }
        }
    }

//    override fun onBackPressed() {
////        showAppRating(true) {
//            finish()
//        }
//    }

}