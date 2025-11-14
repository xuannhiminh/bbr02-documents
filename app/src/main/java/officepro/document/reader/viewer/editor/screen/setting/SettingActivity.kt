package officepro.document.reader.viewer.editor.screen.search
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.ezteam.baseproject.extensions.launchActivity
import com.brian.base_iap.utils.IAPUtils
import com.ezteam.baseproject.utils.PathUtils
import com.brian.base_iap.utils.PreferencesUtils
import com.brian.base_iap.utils.TemporaryStorage
import com.nlbn.ads.util.AppOpenManager
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.common.FunctionState
import officepro.document.reader.viewer.editor.databinding.ActivitySettingsBinding
import officepro.document.reader.viewer.editor.dialog.AboutUsDialog
import officepro.document.reader.viewer.editor.dialog.AddToHomeRequestDialog
import officepro.document.reader.viewer.editor.dialog.DefaultReaderRequestDialog
import officepro.document.reader.viewer.editor.dialog.DefaultReaderUninstallDialog
import officepro.document.reader.viewer.editor.screen.setting.RateUsDialog
import officepro.document.reader.viewer.editor.screen.PolicyActivity
import officepro.document.reader.viewer.editor.screen.TermAndConditionsActivity
import officepro.document.reader.viewer.editor.screen.base.PdfBaseActivity
import officepro.document.reader.viewer.editor.screen.create.BottomSheetCreatePdf
import officepro.document.reader.viewer.editor.screen.create.CreateSuccessActivity
import com.brian.base_iap.iap.IapActivity
import com.brian.base_iap.iap.IapActivityV2
import officepro.document.reader.viewer.editor.screen.language.LanguageActivity
import officepro.document.reader.viewer.editor.screen.main.MainActivity.Companion.CODE_ACTION_OPEN_DOCUMENT_FILE
import officepro.document.reader.viewer.editor.screen.main.MainActivity.Companion.CODE_CHOOSE_IMAGE
import officepro.document.reader.viewer.editor.screen.main.MainViewModel
import officepro.document.reader.viewer.editor.screen.overlay.ClearDefaultReaderOverlayActivity
import officepro.document.reader.viewer.editor.screen.reloadfile.FeatureRequestActivity
import officepro.document.reader.viewer.editor.utils.FileSaveManager
import com.brian.base_iap.utils.FirebaseRemoteConfigUtil
import officepro.document.reader.viewer.editor.utils.createPdf.OnPDFCreatedInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.core.text.TextUtilsCompat
import java.util.*

class SettingActivity : PdfBaseActivity<ActivitySettingsBinding>() {
    private val viewModel by inject<MainViewModel>()

    private val TAG = "SettingActivity"
    companion object {
        fun start(activity: FragmentActivity) {
            val intent = Intent(activity, SettingActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onStart() {
        super.onStart()
        applyKeepScreenOnState()
        //loadNativeNomedia()
        loadSavedSwitchState()
       // checkNotificationState()
    }

    private fun checkFeatureRequestToShowUI() {
        binding.funcFeatureRequest.visibility = View.VISIBLE
    }

    override fun initView() {
        val isNotificationEnabled = PreferencesUtils.getBoolean("NOTIFICATION", false)
        binding.switchNotifications.isChecked = isNotificationEnabled
        binding.funcNotification.visibility = View.GONE
        binding.funcNotificationLine.visibility = View.GONE
        val defaultPdfViewerResolveInfo = getDefaultPdfViewerClass()
        if (defaultPdfViewerResolveInfo?.activityInfo?.name?.contains(packageName) == true) {
            binding.funcSetDefault.visibility = View.GONE
        }
        binding.tvIapTitle.text = getString(R.string.uprange_to_premium)
        checkFeatureRequestToShowUI()
        val locale: Locale = this.resources.configuration.locales[0]

        val currentLayoutDirection = TextUtilsCompat.getLayoutDirectionFromLocale(locale)
        val isRtl = currentLayoutDirection == View.LAYOUT_DIRECTION_RTL

        binding.bgIap.rotationY = if (isRtl) 180f else 0f
    }
    override fun initData() {

    }
    private fun browserFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("*/*")
        intent.putExtra(
            Intent.EXTRA_MIME_TYPES, arrayOf( // open the mime-types we know about
                "application/pdf",
                "application/vnd.ms-xpsdocument",
                "application/oxps",
                "application/x-cbz",
                "application/vnd.comicbook+zip",
                "application/epub+zip",
                "application/x-fictionbook",
                "application/x-mobipocket-ebook",
                "application/octet-stream",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
                "application/msword", // .doc
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
                "application/vnd.ms-excel", // .xls
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
                "application/vnd.ms-powerpoint" // .ppt
            )
        )
        startActivityForResult(intent, CODE_ACTION_OPEN_DOCUMENT_FILE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == CODE_ACTION_OPEN_DOCUMENT_FILE) {
                data?.data?.let {
                    val path = viewModel.getPathFromUri(it)
                    if (path.isNotEmpty()) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val rs = viewModel.checkIfOurAppRecognizeThisFile(path)
                            if (rs) {
                                val fileModel = viewModel.getFileModelByPath(path)
                                openFileFromSplash(fileModel)
                            } else {
                                val fileModel = viewModel.importUriToDownloadAllPDFTripSoft(it)
                                if (fileModel != null) {
                                    openFileFromSplash(fileModel)
                                } else {
                                    toast(getString(R.string.cant_open_file))
                                }
                            }
                        }
                    } else {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val fileModel = viewModel.importUriToDownloadAllPDFTripSoft(it)
                            if (fileModel != null) {
                                openFileFromSplash(fileModel)
                            } else {
                                toast(getString(R.string.cant_open_file))
                            }
                        }
                    }
                }
            } else if (requestCode == CODE_CHOOSE_IMAGE) {
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
    private fun getDefaultPdfViewerClass(): ResolveInfo? {
        val fileName = "file_example_PDF.pdf"
        val assetManager = assets
        val file = File(File(filesDir, "defaultFiles").apply { mkdirs() }, fileName)

        // Copy file từ assets nếu chưa tồn tại
        if (!file.exists()) {
            try {
                assetManager.open(fileName).use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }

        val uri = Uri.fromFile(file)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        Log.d("DefaultReader", "resolveInfo: $resolveInfo")

        return resolveInfo
    }
    private fun showPopupCreatePdf(lstUri: ArrayList<String>) {
        val bottomSheetCreatePdf = BottomSheetCreatePdf(complete = { fileName, password,size ->
            showHideLoading(true)
            createPdf(lstUri, fileName, password,size, object : OnPDFCreatedInterface {
                override fun onPDFCreationStarted() {

                }

                override fun onPDFCreated(success: Boolean, path: String?) {
                    Log.d("File create", path ?: "")
                    path?.let {
                        val uriFile = FileSaveManager.saveFileStorage(
                            this@SettingActivity,
                            path
                        )
                        uriFile?.let {
                            val realPath =
                                PathUtils.getRealPathFromUri(this@SettingActivity, it)
                            viewModel.createFile(realPath) {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    CreateSuccessActivity.start(
                                        this@SettingActivity,
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
        bottomSheetCreatePdf.show(supportFragmentManager, BottomSheetCreatePdf::javaClass.name)
    }
    private fun onSelectedFunction(state: FunctionState) {
        when (state) {
            FunctionState.BROWSE_FILE -> {
                 AppOpenManager.getInstance().disableAppResume()
                    TemporaryStorage.setTemporaryTurnOffNotificationOutApp()
                browserFile()
            }

            FunctionState.RATE_US -> {
                 AppOpenManager.getInstance().disableAppResume()
                    TemporaryStorage.setTemporaryTurnOffNotificationOutApp()
                val rateUsDialog = RateUsDialog();
                try {
                    rateUsDialog.show(this.supportFragmentManager, "RateUsDialog")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("SettingActivity", "Error showing RateUsDialog: ${e.message}", e)
                }
            }

            FunctionState.FEEDBACK -> {
                if (FirebaseRemoteConfigUtil.getInstance().isShowAdsMain()) {
                    showAdsInterstitial(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_setting")){
                        launchActivity<FeedBackActivity> { }
                    }
                } else {
                    launchActivity<FeedBackActivity> { }
                }
            }

            FunctionState.SHARE_APP -> {
                //shareApp()
            }

            FunctionState.PRIVACY_POLICY -> {
                if (FirebaseRemoteConfigUtil.getInstance().isShowAdsMain()) {
                    showAdsInterstitial(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_setting")){
                        PolicyActivity.start(this)
                    }
                } else {
                    PolicyActivity.start(this)
                }
            }

            FunctionState.CHANGE_LANGUAGE -> {
                changeLanguage()
            }
            else -> {
            }
        }
    }

    override fun initListener() {
        
        binding.layoutIap.setOnClickListener {
            when (FirebaseRemoteConfigUtil.getInstance().getIapScreenType()) {
                0 -> IapActivityV2.start(this)
                1 -> IapActivity.start(this)
                else -> IapActivityV2.start(this)
            }
        }
        binding.btnGoPremium.setOnClickListener {
            when (FirebaseRemoteConfigUtil.getInstance().getIapScreenType()) {
                0 -> IapActivityV2.start(this)
                1 -> IapActivity.start(this)
                else -> IapActivityV2.start(this)
            }
        }
        checkFeatureRequestToShowUI()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                binding.funcBrowseMoreFiles.visibility = View.VISIBLE
            } else {
                binding.funcBrowseMoreFiles.visibility = View.GONE
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE )
                != PackageManager.PERMISSION_GRANTED) {
                binding.funcBrowseMoreFiles.visibility = View.GONE
            } else {
                binding.funcBrowseMoreFiles.visibility = View.VISIBLE
            }
        }

        binding.funcBrowseMoreFiles.setOnClickListener {
            onSelectedFunction(FunctionState.BROWSE_FILE)
        }

//        binding.funcRateUs.setOnClickListener {
//            onSelectedFunction(FunctionState.RATE_US)
//        }
        binding.funcSendFeedback.setOnClickListener {
            onSelectedFunction(FunctionState.FEEDBACK)
        }

//        binding.funcShare.setOnClickListener {
//            onSelectedFunction(FunctionState.SHARE_APP)
//        }

        binding.funcPrivacy.setOnClickListener {
            onSelectedFunction(FunctionState.PRIVACY_POLICY)
        }

        binding.funcChangeLanguage.setOnClickListener {
            onSelectedFunction(FunctionState.CHANGE_LANGUAGE)
            TemporaryStorage.shouldLoadAdsLanguageScreen = true
        }

        binding.funcTerms.setOnClickListener {
            if (FirebaseRemoteConfigUtil.getInstance().isShowAdsMain()) {
                showAdsInterstitial(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_setting")){
                    TermAndConditionsActivity.start(this)
                }
            } else {
                TermAndConditionsActivity.start(this)
            }
        }
        binding.funcFeatureRequest.setOnClickListener {
            if (FirebaseRemoteConfigUtil.getInstance().isShowAdsMain()) {
                showAdsInterstitial(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_setting")){
                    FeatureRequestActivity.start(this)
                }
            } else {
                FeatureRequestActivity.start(this)
            }
        }

        binding.funcAboutUs.setOnClickListener {
            val dialog = AboutUsDialog();
            try {
                dialog.show(this.supportFragmentManager, "AboutUsDialog")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("SettingActivity", "Error showing AboutUsDialog: ${e.message}", e)
            }
        }

        binding.funcAddWidget.setOnClickListener {
            TemporaryStorage.isShowedAddToHoneDialog = true
            val dialog = AddToHomeRequestDialog();
            try {
                dialog.show(this.supportFragmentManager, "AddToHomeRequestDialog")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("SettingActivity", "Error showing AddToHomeRequestDialog: ${e.message}", e)
            }
        }
        binding.funcSetDefault.setOnClickListener {
            currentFileTypeIndex = 0
            showNextDefaultReaderDialog()
        }

        binding.switchKeepScreen.setOnCheckedChangeListener { _, isChecked ->
            TemporaryStorage.keepScreenOn = isChecked
            applyKeepScreenOnState()
        }

        // Switch: Push notifications
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            val currentlyEnabled = PreferencesUtils.getBoolean("NOTIFICATION", false)
            val denialCount = PreferencesUtils.getInteger("NOTIF_DENIAL_COUNT", 0)

            if (!isChecked && currentlyEnabled) {
                // ON→OFF: simply disable
                PreferencesUtils.putBoolean("NOTIFICATION", false)

            } else if (isChecked && !currentlyEnabled) {
                // OFF→ON: if already denied twice, go to settings
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (denialCount >= 2) {
                        startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, packageName);
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                        waitingNotificationResult = true
                         AppOpenManager.getInstance().disableAppResume()
                    TemporaryStorage.setTemporaryTurnOffNotificationOutApp()
                    } else {
                        requestNotificationPermissionFlow()
                    }
                } else {
                    Log.d("SettingActivity", "Notification permission not required for SDK < 33")
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, packageName);
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                        waitingNotificationResult = true
                         AppOpenManager.getInstance().disableAppResume()
                    TemporaryStorage.setTemporaryTurnOffNotificationOutApp()
                    } else {
                        onNotificationPermissionGranted()
                    }
                }
            }
        }

        // Switch: Night mode
        binding.switchNightMode.setOnCheckedChangeListener { _, isChecked ->
            TemporaryStorage.isNightMode = isChecked
            nightModeState()
        }

        binding.ivBack.setOnClickListener {
            if (FirebaseRemoteConfigUtil.getInstance().isShowAdsMain()) {
                showAdsInterstitial(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_home")){
                    finish()
                }
            } else {
                finish()
            }
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

    private fun changeLanguage() {
        launchActivity<LanguageActivity> { }
    }

    private fun loadSavedSwitchState() {
        binding.switchKeepScreen.isChecked = TemporaryStorage.keepScreenOn
        binding.switchNightMode.isChecked = TemporaryStorage.isNightMode
    }
    private var waitingNotificationResult = false
    override fun onResume() {
        super.onResume()

        if (IAPUtils.isPremium()) {
            binding.layoutIap.visibility = View.GONE
            binding.layoutIapContainer.visibility = View.GONE
        } else {
            binding.layoutIap.visibility = View.VISIBLE
            binding.layoutIapContainer.visibility = View.VISIBLE
        }
        checkFeatureRequestToShowUI()
        applyKeepScreenOnState()
        nightModeState()
//        if (TemporaryStorage.isRateFullStar){
//            binding.funcRateUs.visibility = View.INVISIBLE
//        }
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
        val defaultPdfViewerResolveInfo = getDefaultPdfViewerClass()
        if(isGoingToSettingToClearDefault) {
            isGoingToSettingToClearDefault = false
            if (defaultPdfViewerResolveInfo?.activityInfo == null || defaultPdfViewerResolveInfo.activityInfo.name.contains("internal.app.ResolverActivity")) {// default reader isn't set => show dialog to set default
                val dialog = DefaultReaderRequestDialog();
                try {
                    dialog.show(this.supportFragmentManager, "RequestDefaultReaderDialog")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("SettingActivity", "Error showing RequestDefaultReaderDialog: ${e.message}", e)
                }
            }else if(!defaultPdfViewerResolveInfo.activityInfo.name.contains(packageName) ) { // default reader is set but not our app => show dialog to clear default
                val fragmentManager = supportFragmentManager
                val existingDialog = fragmentManager.findFragmentByTag("DefaultReaderUninstallDialog")
                if (existingDialog == null) {
                    val dialog = DefaultReaderUninstallDialog()
                    dialog.defaultViewer = defaultPdfViewerResolveInfo
                    dialog.listener = {
                        isGoingToSettingToClearDefault = true
                    }
                    try {
                        dialog.show(fragmentManager, "DefaultReaderUninstallDialog")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("SettingActivity", "Error showing DefaultReaderUninstallDialog: ${e.message}", e)
                    }
                }
            } else { // default reader is our app => do nothing
                Log.d("DefaultReader", "defaultPdfViewer: $defaultPdfViewerResolveInfo")
            }
        }

    }
    private fun applyKeepScreenOnState() {
        if (TemporaryStorage.keepScreenOn) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    private fun nightModeState() {
        if (TemporaryStorage.isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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



    private fun onNotificationPermissionGranted() {
        Log.d("SettingActivity", "Notification permission granted")
        binding.switchNotifications.isChecked = true
        PreferencesUtils.putBoolean(
            "NOTIFICATION", true
        )
        PreferencesUtils.putInteger("NOTIF_DENIAL_COUNT", 0)
    }

    private fun onNotificationPermissionDenied() {
        binding.switchNotifications.isChecked = false
        Log.e("SettingActivity", "Notification permission denied")
        PreferencesUtils.putBoolean(
            "NOTIFICATION", false
        )
        PreferencesUtils.putInteger("NOTIF_DENIAL_COUNT",
            PreferencesUtils.getInteger("NOTIF_DENIAL_COUNT", 0) + 1
        )

    }

    override fun viewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(LayoutInflater.from(this))
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
        if (isGoingToSettingToClearDefault){
            Log.i(TAG, "isGoingToSetting")
            startActivity(Intent(this@SettingActivity, ClearDefaultReaderOverlayActivity::class.java).apply {
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
        }

    }
}
