package officepro.document.reader.viewer.editor.screen.main

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.ezteam.baseproject.adapter.BasePagerAdapter
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.PathUtils
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.banner.BannerPlugin
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import officepro.document.reader.viewer.editor.BuildConfig
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.common.BottomTab
import officepro.document.reader.viewer.editor.common.FileTab
import officepro.document.reader.viewer.editor.common.PresKey
import officepro.document.reader.viewer.editor.screen.base.PdfBaseActivity
import org.koin.android.ext.android.inject
import java.util.Locale
import officepro.document.reader.viewer.editor.common.SortState
import officepro.document.reader.viewer.editor.databinding.ActivityListFileTypeBinding
import officepro.document.reader.viewer.editor.dialog.AddToHomeRequestDialog
import officepro.document.reader.viewer.editor.dialog.SatisfactionDialog
import officepro.document.reader.viewer.editor.dialog.SortDialog
import officepro.document.reader.viewer.editor.screen.create.BottomSheetCreatePdf
import officepro.document.reader.viewer.editor.screen.create.CreateSuccessActivity
import officepro.document.reader.viewer.editor.screen.main.MainActivity.Companion.CODE_ACTION_OPEN_DOCUMENT_FILE
import officepro.document.reader.viewer.editor.screen.main.MainActivity.Companion.CODE_CHOOSE_IMAGE
import officepro.document.reader.viewer.editor.screen.reloadfile.ReloadLoadingActivity
import officepro.document.reader.viewer.editor.screen.search.SelectMultipleFilesActivity
import officepro.document.reader.viewer.editor.service.NotificationForegroundService
import com.brian.base_iap.utils.AppUtils
import officepro.document.reader.viewer.editor.utils.FileSaveManager
import com.ezteam.baseproject.utils.FirebaseRemoteConfigUtil
import com.google.firebase.analytics.FirebaseAnalytics
import officepro.document.reader.viewer.editor.screen.file.ListFileAllFragment
import officepro.document.reader.viewer.editor.screen.file.ListFileExcelFragment
import officepro.document.reader.viewer.editor.screen.file.ListFilePdfFragment
import officepro.document.reader.viewer.editor.screen.file.ListFilePptFragment
import officepro.document.reader.viewer.editor.screen.file.ListFileWordFragment
import officepro.document.reader.viewer.editor.utils.createPdf.OnPDFCreatedInterface
import officepro.document.reader.viewer.editor.widgets.Widget1
import officepro.document.reader.viewer.editor.widgets.Widget2
import officepro.document.reader.viewer.editor.widgets.Widget3
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import office.file.ui.extension.visible
import officepro.document.reader.viewer.editor.screen.file.ListFileTxtFragment
import java.io.File

private const val ALL_FILES_FRAGMENT_INDEX = 0
private const val PDF_FILES_FRAGMENT_INDEX = 1
private const val WORD_FILES_FRAGMENT_INDEX = 2
private const val EXCEL_FILES_FRAGMENT_INDEX = 3
private const val PPT_FILES_FRAGMENT_INDEX = 4
private const val TXT_FILES_FRAGMENT_INDEX = 5
class ListFileTypeActivity : PdfBaseActivity<ActivityListFileTypeBinding>() {
    private val viewModel by inject<MainViewModel>()
    private lateinit var adapter: BasePagerAdapter
    private val TAG = "ListFileTypeActivity"
    companion object {
        private const val EXTRA_FILE_TYPE = "extra_file_type"

        fun start(activity: FragmentActivity, fileType: String) {
            val intent = Intent(activity, ListFileTypeActivity::class.java)
            intent.putExtra(EXTRA_FILE_TYPE, fileType)
            activity.startActivity(intent)
        }
    }

    private var fileType: String = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadBannerAds()
    }
    private fun logEvent(event: String) {
        try {
            FirebaseAnalytics.getInstance(this@ListFileTypeActivity).logEvent(event, Bundle())
        } catch (e: Exception) {
            Log.e("DefaultReaderGuideDialog", "Error initializing FirebaseAnalytics $e")
        }
    }
    override fun initView() {
        fileType = intent.getStringExtra(EXTRA_FILE_TYPE) ?: "ALL"
        if (Locale.getDefault().language == "ar") {
            binding.toolbar.ivBack.rotationY = 180f
        } else {
            binding.toolbar.ivBack.rotationY = 0f
        }
        handleSortAction(4)
        binding.toolbar.tvAll.text =  getString(R.string.all)
        binding.toolbar.tvWord.text =  getString(R.string.word)
        binding.toolbar.tvPpt.text =  getString(R.string.ppt)
        binding.toolbar.tvExcel.text =  getString(R.string.excel)
        binding.toolbar.tvPdf.text =  getString(R.string.pdf)
        adapter = BasePagerAdapter(supportFragmentManager,ALL_FILES_FRAGMENT_INDEX)
        adapter.addFragment(ListFileAllFragment(viewModel.allFilesLiveData), ListFileAllFragment::class.java.name)
        adapter.addFragment(ListFilePdfFragment(viewModel.pdfFilesLiveData), ListFilePdfFragment::class.java.name)
        adapter.addFragment(ListFileWordFragment(viewModel.wordFilesLiveData), ListFileWordFragment::class.java.name)
        adapter.addFragment(ListFileExcelFragment(viewModel.excelFilesLiveData), ListFileExcelFragment::class.java.name)
        adapter.addFragment(ListFilePptFragment(viewModel.pptFilesLiveData), ListFilePptFragment::class.java.name)
        adapter.addFragment(ListFileTxtFragment(viewModel.txtFilesLiveData), ListFileTxtFragment::class.java.name)
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 5
    }
    override fun onResume() {
        super.onResume()

        binding.swipeRefresh.isRefreshing = false

        val intent = Intent(this, NotificationForegroundService::class.java).apply {
            action = "${packageName}.STOP_WAIT_UPDATE_DOWNLOADED"
        }
        ContextCompat.startForegroundService(this, intent)

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
                Log.e("ListFileTypeActivity", "Error showing AddToHomeRequestDialog: ${e.message}", e)
            }
        }

        loadNativeNomedia()
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
                Log.e("ListFileTypeActivity", "Error showing SatisfactionDialog: ${e.message}", e)
            }
        }
        val showReloadFileGuideTime = PreferencesUtils.getInteger("SHOW_RELOAD_FILE_GUIDE_TIME",0)
        if (showReloadFileGuideTime < FirebaseRemoteConfigUtil.getInstance().getTimeShowingReloadGuide() // only show guide 4 times
            && !TemporaryStorage.isShowedReloadGuideInThisSession // only show dialog if it hasn't been shown in this session
            && isAcceptManagerStorage()) { // only show guide if user has accepted storage permission
            handler.postDelayed(checkNoDialogToShowReloadGuideRunnable,
                FirebaseRemoteConfigUtil.getInstance().getDurationDelayShowingReloadGuide()) // delay 5s if it's not first time
        }
    }
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(checkNoDialogToShowReloadGuideRunnable)
    }
    private fun loadBannerAds(){
        if (Admob.getInstance().isLoadFullAds && !IAPUtils.isPremium()) {
            binding.bannerContainer.visible()
            val config = BannerPlugin.Config()
            config.defaultRefreshRateSec = 30
            config.defaultCBFetchIntervalSec = 30
            config.defaultAdUnitId = FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("banner_file_type")
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
                .inflate(com.ezteam.ezpdflib.R.layout.ads_native_loading_short_main, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            val callback = object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    super.onNativeAdLoaded(nativeAd)

                    val layoutRes = com.ezteam.ezpdflib.R.layout.ads_native_bot_no_media_short_main
                    val adView = LayoutInflater.from(this@ListFileTypeActivity)
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
                FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("native_file_type"),
                callback
            )
        } else {
            binding.layoutNative.visibility = View.GONE
        }
    }
    override fun initData() {
        val fileIcon = when (fileType) {
            "PDF" -> R.drawable.icon_main_pdf
            "WORD" -> R.drawable.icon_main_word
            "PPT" ->  R.drawable.icon_main_ppt
            "EXCEL" ->  R.drawable.icon_main_excel
            "TXT" ->  R.drawable.icon_main_txt
            else ->  R.drawable.icon_main_all
        }
        val fileName = when (fileType) {
            "PDF" -> R.string.pdf
            "WORD" -> R.string.word
            "PPT" ->  R.string.ppt
            "EXCEL" ->  R.string.excel
            "TXT" ->  R.string.txt
            else ->  R.string.all
        }
        binding.toolbar.tvIcon.setImageResource(fileIcon)
        binding.toolbar.tvTitle.setText(fileName)

        binding.swipeRefresh.setProgressViewOffset(true, 150, 220)
        binding.swipeRefresh.setColorSchemeResources(
            R.color.primaryColor,
            R.color.primaryColor,
            R.color.primaryColor
        )
        when (fileType) {
            "ALL" -> {
                binding.viewPager.currentItem = ALL_FILES_FRAGMENT_INDEX
                handleUIBaseOnFileTab(binding.toolbar.tvAll)
            }
            "PDF" -> {
                binding.viewPager.currentItem = PDF_FILES_FRAGMENT_INDEX
                handleUIBaseOnFileTab(binding.toolbar.tvPdf)
            }
            "WORD" -> {
                binding.viewPager.currentItem = WORD_FILES_FRAGMENT_INDEX
                handleUIBaseOnFileTab(binding.toolbar.tvWord)
            }
            "PPT" -> {
                binding.viewPager.currentItem = PPT_FILES_FRAGMENT_INDEX
                handleUIBaseOnFileTab(binding.toolbar.tvPpt)
            }
            "EXCEL" -> {
                binding.viewPager.currentItem = EXCEL_FILES_FRAGMENT_INDEX
                handleUIBaseOnFileTab(binding.toolbar.tvExcel)
            }
            "TXT" -> {
                binding.viewPager.currentItem = TXT_FILES_FRAGMENT_INDEX
                handleUIBaseOnFileTab(binding.toolbar.tvTxt)
            }
            else -> {
                binding.viewPager.currentItem = ALL_FILES_FRAGMENT_INDEX
                handleUIBaseOnFileTab(binding.toolbar.tvAll)
            }
        }
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
    override fun initListener() {
        binding.toolbar.ivBack.setOnClickListener {
            if (FirebaseRemoteConfigUtil.getInstance().isShowAdsMain()) {
                showAdsInterstitial(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_home")){
                    finish()
                }
            } else {
                finish()
            }
        }
        binding.toolbar.ivFilter.setOnClickListener {
            val dialog = SortDialog()
            dialog.setOnSortSelectedListener(::handleSortAction)
            try {
                dialog.show(supportFragmentManager, "SortDialog")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ListFileTypeActivity", "Error showing SortDialog: ${e.message}", e)
            }
        }
        binding.buttonCreate.setOnClickListener {
             AppOpenManager.getInstance().disableAppResume()
                    TemporaryStorage.setTemporaryTurnOffNotificationOutApp()
            browserFile()
        }
        binding.toolbar.ivCheck.setOnClickListener {
            binding.toolbar.ivCheck.setOnClickListener {
                val fileTab = when (fileType) {
                    "PDF" -> FileTab.PDF
                    "WORD" -> FileTab.WORD
                    "PPT" -> FileTab.PPT
                    "EXCEL" -> FileTab.EXCEL
                    "TXT" -> FileTab.TXT
                    else -> FileTab.ALL_FILE
                }
                if (FirebaseRemoteConfigUtil.getInstance().isShowAdsMain()) {
                    showAdsInterstitial(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_file_type")) {
                        SelectMultipleFilesActivity.start(this, fileTab = fileTab, bottomTab = BottomTab.HOME)
                    }
                } else {
                    SelectMultipleFilesActivity.start(this, fileTab = fileTab, bottomTab = BottomTab.HOME)
                }

            }
        }
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            TemporaryStorage.isShowedReloadGuideInThisSession = true
            val intent = Intent(this, ReloadLoadingActivity::class.java)
            startActivity(intent)
        }

        setupFileTabListener(binding.toolbar.tvAll,  ALL_FILES_FRAGMENT_INDEX,  "file_type_press_all")
        setupFileTabListener(binding.toolbar.tvPdf,  PDF_FILES_FRAGMENT_INDEX,  "file_type_press_pdf")
        setupFileTabListener(binding.toolbar.tvWord, WORD_FILES_FRAGMENT_INDEX, "file_type_press_word")
        setupFileTabListener(binding.toolbar.tvExcel, EXCEL_FILES_FRAGMENT_INDEX,"file_type_press_excel")
        setupFileTabListener(binding.toolbar.tvPpt,  PPT_FILES_FRAGMENT_INDEX,  "file_type_press_ppt")
        setupFileTabListener(binding.toolbar.tvTxt,  TXT_FILES_FRAGMENT_INDEX,  "file_type_press_txt")
    }
    private fun setupFileTabListener(textView: TextView, index: Int, eventName: String) {
        textView.setOnClickListener {
            Log.d(TAG, "${textView.text} Clicked")

            executeWithAdsOrNot(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_file_type")) {
                binding.viewPager.currentItem = index
                handleUIBaseOnFileTab(textView)
            }

            logEvent(eventName)
        }
    }
    private fun executeWithAdsOrNot(adsKey: String, action: () -> Unit) {
        if (FirebaseRemoteConfigUtil.getInstance().isShowAdsMain()) {
            showAdsInterstitial(adsKey) { action() }
        } else {
            action()
        }
    }
    private fun handleUIBaseOnFileTab(selectedTextView: TextView) {
        when(selectedTextView) {
            binding.toolbar.tvAll -> viewModel.updateFileTab(FileTab.ALL_FILE)
            binding.toolbar.tvPdf -> viewModel.updateFileTab(FileTab.PDF)
            binding.toolbar.tvWord -> viewModel.updateFileTab(FileTab.WORD)
            binding.toolbar.tvPpt -> viewModel.updateFileTab(FileTab.PPT)
            binding.toolbar.tvExcel -> viewModel.updateFileTab(FileTab.EXCEL)
            binding.toolbar.tvTxt -> viewModel.updateFileTab(FileTab.TXT)
        }

        val allTextViews = listOf(
            binding.toolbar.tvAll,
            binding.toolbar.tvPdf,
            binding.toolbar.tvWord,
            binding.toolbar.tvExcel,
            binding.toolbar.tvPpt,
            binding.toolbar.tvTxt
        )

        for (textView in allTextViews) {
            textView.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL)
            textView.setTextColor(getColor(R.color.dark_gray))
            textView.background = null
        }
        val selectedColor = when (selectedTextView) {
            binding.toolbar.tvPpt -> R.color.ppt
            binding.toolbar.tvExcel -> R.color.excel
            binding.toolbar.tvWord -> R.color.word
            binding.toolbar.tvPdf -> R.color.ppt
            binding.toolbar.tvTxt -> R.color.txt
            else -> R.color.primaryColor
        }
        val underlineResource = when (selectedTextView) {
            binding.toolbar.tvPpt -> R.drawable.underline_orange
            binding.toolbar.tvWord -> R.drawable.underline_blue
            binding.toolbar.tvExcel -> R.drawable.underline_green
            binding.toolbar.tvPdf -> R.drawable.underline_red
            binding.toolbar.tvTxt -> R.drawable.underline_txt
            else -> R.drawable.underline
        }

        selectedTextView.setTypeface(null, Typeface.BOLD)
        selectedTextView.setTextColor(ContextCompat.getColor(this, selectedColor))
        selectedTextView.setBackgroundResource(underlineResource)
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

                    if (lstData.isNotEmpty()) {
                        showPopupCreatePdf(lstData)
                    }
                }
            }
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
                            this@ListFileTypeActivity,
                            path
                        )
                        uriFile?.let {
                            val realPath =
                                PathUtils.getRealPathFromUri(this@ListFileTypeActivity, it)
                            viewModel.createFile(realPath) {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    CreateSuccessActivity.start(
                                        this@ListFileTypeActivity,
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
    private fun handleSortAction(id: Int): Boolean {
        return when (id) {
            3 -> {
                viewModel.sortFile(SortState.DATE)
                false
            }
            4 -> {
                viewModel.sortFile(SortState.DATE_DESC)
                false
            }
            1 -> {
                viewModel.sortFile(SortState.NAME)
                false
            }
            2 -> {
                viewModel.sortFile(SortState.NAME_DESC)
                false
            }
            5 -> {
                viewModel.sortFile(SortState.SIZE)
                false
            }
            6 -> {
                viewModel.sortFile(SortState.SIZE_DESC)
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

    override fun viewBinding(): ActivityListFileTypeBinding {
        return ActivityListFileTypeBinding.inflate(LayoutInflater.from(this))
    }
}