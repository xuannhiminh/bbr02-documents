package com.ezteam.ezpdflib.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.CleanUtils
import com.ezstudio.pdftoolmodule.activity.ExtractActivity
import com.ezstudio.pdftoolmodule.dialog.AddWatermarkDialog
import com.ezteam.baseproject.extensions.uriToBitmap
import com.ezteam.baseproject.photopicker.PickImageActivity
import com.ezteam.baseproject.utils.FirebaseRemoteConfigUtil
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.PresKey
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.activity.outline.OutlineActivity
import com.ezteam.ezpdflib.activity.outline.SingleOutline
import com.ezteam.ezpdflib.activity.signature.SignatureActivity
import com.ezteam.ezpdflib.activity.thubnail.ThumbnailActivity
import com.ezteam.ezpdflib.adapter.PdfPageAdapter
import com.ezteam.ezpdflib.bottomsheet.BottomSheetAddText
import com.ezteam.ezpdflib.bottomsheet.BottomSheetAnnotation
import com.ezteam.ezpdflib.bottomsheet.BottomSheetDetailFunction
import com.ezteam.ezpdflib.bottomsheet.BottomSheetEdit
import com.ezteam.ezpdflib.bottomsheet.BottomSheetNote
import com.ezteam.ezpdflib.bottomsheet.BottomSheetTool
import com.ezteam.ezpdflib.databinding.LibActivityPdfDetailBinding
import com.ezteam.ezpdflib.databinding.LibLayoutDetailToolbarBinding
import com.ezteam.ezpdflib.dialog.GoToPageDialog
import com.ezteam.ezpdflib.extension.bitmapToFileCachePng
import com.ezteam.ezpdflib.extension.launchActivity
import com.ezteam.ezpdflib.extension.reverstBitmap
import com.ezteam.ezpdflib.guide.GuideEditDialog
import com.ezteam.ezpdflib.listener.BroadcastSubmodule
import com.ezteam.ezpdflib.listener.BroadcastSubmodule.Companion.ALLOW_EDIT
import com.ezteam.ezpdflib.listener.BroadcastSubmodule.Companion.IS_READ_DONE
import com.ezteam.ezpdflib.model.PagePdf
import com.ezteam.ezpdflib.util.Config
import com.ezteam.ezpdflib.util.FileSaveManager
import com.ezteam.ezpdflib.util.KeyboardUtils
import com.ezteam.ezpdflib.util.PreferencesKey
import com.ezteam.ezpdflib.util.PreferencesUtils
import com.ezteam.ezpdflib.util.Utils
import com.ezteam.ezpdflib.util.ViewUtils
import com.ezteam.ezpdflib.widget.MyLayoutManager
import com.ezteam.ezpdflib.widget.MyRecyclerView
import com.ezteam.ezpdflib.widget.stickerView.TextSticker
import com.ezteam.nativepdf.MuPDFCore
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.nlbn.ads.banner.BannerPlugin
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.ConsentHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.io.FilenameUtils
import java.io.File
import kotlin.math.max

@Keep
open class PdfDetailActivity : BasePdfViewerActivity(), MyRecyclerView.TouchListener,
    View.OnClickListener, ColorPickerDialogListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

     val binding: LibActivityPdfDetailBinding by lazy {
        LibActivityPdfDetailBinding.inflate(LayoutInflater.from(this))
    }

    private val toolbarBinding: LibLayoutDetailToolbarBinding by lazy {
        binding.llToolbar
    }

    var urlFile: String? = null
    private var muPDFCore = MuPDFCore()
    private var totalPage = -1
    private var pageAdapter: PdfPageAdapter? = null
    private var lstPage = mutableListOf<PagePdf>()
    private lateinit var rcvManager: MyLayoutManager
    private val snapHelper = PagerSnapHelper()
    private var selectedAnnotationIndex: Int? = null
    private var firstInit = true
    var isFavorite = false
    var allowEdit = true
    var isReadDone = false

    var hasPassword = false
    var password: String? = null
    var hasOutline = false

    private val TAG = "PdfDetailActivity"
    private fun logEvent(event: String) {
        try {
            FirebaseAnalytics.getInstance(this).logEvent(event, Bundle())
        } catch (e: Exception) {
            Log.e("DefaultReaderGuideDialog", "Error initializing FirebaseAnalytics $e")
        }
    }
    companion object {
        val IS_FAVORITE = "is_favorite"
        fun start(
            activity: Context,
            filePath: String,
            isFavorite: Boolean = false,
            isReadDone: Boolean = false,
            allowEdit: Boolean = true,
            resultCode: Int? = null,
        ) {
            val intent = Intent(activity, PdfDetailActivity::class.java).apply {
                data = Uri.parse(filePath)
                putExtra(IS_FAVORITE, isFavorite)
                putExtra(IS_READ_DONE, isReadDone)
                putExtra(ALLOW_EDIT, allowEdit)
            }
            resultCode?.let {
                if (activity is AppCompatActivity) {
                    activity.startActivityForResult(intent, it)
                }
            } ?: run {
                activity.startActivity(intent)
            }
        }

        fun start(
            activity: Context,
            uri: Uri?,
            fromSplash: Boolean = false,
            resultCode: Int? = null
        ) {
            val intent = Intent(activity, PdfDetailActivity::class.java).apply {
                data = uri
            }
            resultCode?.let {
                if (activity is AppCompatActivity) {
                    activity.startActivityForResult(intent, it)
                }
            } ?: run {
                activity.startActivity(intent)
            }
        }

        fun start(activity: Context, intent: Intent, fromSplash: Boolean = false) {
            intent.setClass(activity, PdfDetailActivity::class.java)
            activity.startActivity(intent)
        }
    }
    private fun loadBannerAds(){
        if (Admob.getInstance().isLoadFullAds && !IAPUtils.isPremium()) {
            binding.bannerContainer.visibility = View.VISIBLE
            val config = BannerPlugin.Config()
            config.defaultRefreshRateSec = 30
            config.defaultCBFetchIntervalSec = 30
            config.defaultAdUnitId = FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("banner_filedetail")
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
                .inflate(R.layout.ads_native_loading_short, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            val callback = object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    super.onNativeAdLoaded(nativeAd)

                    val layoutRes = R.layout.ads_native_bot_no_media_short
                    val adView = LayoutInflater.from(this@PdfDetailActivity)
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
                FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("native_filedetail"),
                callback
            )
        } else {
            binding.layoutNative.visibility = View.GONE
        }
    }
    private fun loadCollapsibleBannerNomedia() {
        if (IAPUtils.isPremium()) {
            binding.bannerContainer.visibility = View.GONE
            return
        }

        if (Admob.getInstance().isLoadFullAds && SystemUtils.isInternetAvailable(this)) {
            binding.bannerContainer.visibility = View.VISIBLE


            val config = BannerPlugin.Config()
            config.defaultRefreshRateSec = FirebaseRemoteConfigUtil.getInstance().getTimeDelayShowingExtendAds()
            config.defaultCBFetchIntervalSec = FirebaseRemoteConfigUtil.getInstance().getTimeDelayShowingExtendAds()
            config.defaultAdUnitId = FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("banner_filedetail")
            config.defaultBannerType = BannerPlugin.BannerType.CollapsibleBottom
            Admob.getInstance().loadBannerPlugin(
                this,
                findViewById(R.id.banner_container),
                findViewById(R.id.shimmer_container_banner),
                config
            )
        } else {
            binding.bannerContainer.visibility = View.GONE
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        PreferencesUtils.init(this)
//        EzAdControl.getInstance(this).showAds()
        binding.root.post {
            readData()
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                //   left = insets.left,
                top = insets.top,
                // right = insets.right,
                bottom = insets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun readData() {
        muPDFCore.readData(intent, null, this, result = { muPDFCore, urlFile ->
            this.muPDFCore = muPDFCore
            this.urlFile = urlFile
            viewmodel.getFileStatus(urlFile)
//            pageAdapter?.ratioPDF = muPDFCore.getRatio()
        })
        if (muPDFCore.globals == -2L) {
            toast(getString(R.string.update_required))
            finish()
            return
        }
        if (muPDFCore.globals != 0L) {
            if (muPDFCore.needsPassword()) {
                hasPassword = true
                showDialogPassword(getString(R.string.enter_password), responseListener = {
                    if (it.isNullOrEmpty()) {
                        finish()
                    } else {
                        if (muPDFCore.authenticatePassword(it)) {
                            password = it
                            initView()
                        } else {
                            toast(getString(R.string.password_error))
                            finish()
                        }
                    }
                })
            } else {
                hasPassword = false
                initView()
            }
        } else {
            val fileBackup = FileSaveManager.checkFileOnBackup(this, urlFile)
            fileBackup?.let {
                showDialogConfirm(
                    getString(R.string.app_name),
                    getString(R.string.apply_backup_file)
                ) {
                    if (it) {
                        FileSaveManager.copyFile(fileBackup, File(urlFile).parent) {
                            readData()
                        }
                    } else {
                        finish()
                    }
                }
            } ?: run {
                finish()
            }
        }
    }

    override fun initView() {
        super.initView()
        if (FirebaseRemoteConfigUtil.getInstance().getTypeAdsDetail() == 0){
            loadNativeNomedia()
            binding.bannerContainer.visibility = View.GONE
        } else if (FirebaseRemoteConfigUtil.getInstance().getTypeAdsDetail() == 1){
            loadCollapsibleBannerNomedia()
            binding.layoutNative.visibility = View.GONE
        } else if (FirebaseRemoteConfigUtil.getInstance().getTypeAdsDetail() == 2){
            loadBannerAds()
            binding.layoutNative.visibility = View.GONE
        } else {
            loadNativeNomedia()
            binding.bannerContainer.visibility = View.GONE
        }

        isFavorite = intent.getBooleanExtra(IS_FAVORITE, false)
        allowEdit = intent.getBooleanExtra(ALLOW_EDIT, true)
        isReadDone = intent.getBooleanExtra(IS_READ_DONE, false)
        totalPage = muPDFCore.countPages()
        for (i in 0 until muPDFCore.countPages()) {
            lstPage.add(PagePdf(null, i))
        }
        pageAdapter = PdfPageAdapter(this, lstPage, viewmodel)
        initRcv()
        binding.rcvPdf.adapter = pageAdapter
        binding.rcvPdf.clipToPadding = false
        (binding.rcvPdf.parent as ViewGroup).clipChildren = false

        urlFile?.let {
            toolbarBinding.tvTitle.text = FilenameUtils.getBaseName(it)
            if (allowEdit)
                sendBroadcast(
                    Intent().apply {
                        action = BroadcastSubmodule.ACTION_RECENT
                        putExtra(BroadcastSubmodule.PATH, urlFile)
                    }
                )

        }

        if (muPDFCore.globals != 0L && muPDFCore.countPages() == 0) {
            toast(getString(R.string.cant_open_file))
        }

        if (muPDFCore.globals != 0L) {
            hasOutline = muPDFCore.hasOutline()
        }
        initData()
        initListener()
        val bottomNav = findViewById<BottomNavigationView>(R.id.navigation_detail)

        bottomNav.post {
            if (IAPUtils.isPremium()) return@post

            val menuView = bottomNav.getChildAt(0) as? ViewGroup ?: return@post

            for (i in 0 until menuView.childCount) {
                val itemView = menuView.getChildAt(i)
                val menuItem = bottomNav.menu.getItem(i)

                // chỉ thêm cho 2 item cần gắn premium
                if (menuItem.itemId == R.id.menu_signature || menuItem.itemId == R.id.menu_add_image) {

                    val iconView = findImageView(itemView) ?: continue
                    val parent = iconView.parent as? ViewGroup ?: continue

                    if (parent.findViewWithTag<View>("premium_frame") != null) continue

                    val frame = FrameLayout(bottomNav.context).apply { tag = "premium_frame" }
                    frame.layoutParams = iconView.layoutParams

                    val oldIndex = parent.indexOfChild(iconView)
                    parent.removeView(iconView)
                    frame.addView(iconView)

                    // tạo icon premium nhỏ
                    val premiumIcon = ImageView(bottomNav.context).apply {
                        setImageResource(R.drawable.ic_premium_small)
                        val size = resources.getDimensionPixelSize(R.dimen._12sdp)
                        layoutParams = FrameLayout.LayoutParams(size, size).apply {
                            marginStart = size/9*11
                            topMargin  = -size/9*2
                        }
                    }
                    frame.addView(premiumIcon)
                    parent.addView(frame, oldIndex)
                }
            }
        }
    }
    fun findImageView(view: View): ImageView? {
        if (view is ImageView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val result = findImageView(view.getChildAt(i))
                if (result != null) return result
            }
        }
        return null
    }

    private fun initData() {
        when (resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                PreferencesUtils.putBoolean(
                    PreferencesKey.KeyPress.PDF_VIEWER_NIGHT_MODE, true
                )
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                PreferencesUtils.putBoolean(
                    PreferencesKey.KeyPress.PDF_VIEWER_NIGHT_MODE, false
                )
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                PreferencesUtils.putBoolean(
                    PreferencesKey.KeyPress.PDF_VIEWER_NIGHT_MODE, false
                )
            }
        }
        CleanUtils.cleanCustomDir(this@PdfDetailActivity.cacheDir.path)
        val startPage = viewmodel.currentPage.value
            ?: PreferencesUtils.getInteger(urlFile, 0)
        viewmodel.currentPage.postValue(startPage)

        viewmodel.getBitmapPage(
            muPDFCore,
            startPage,
            success = { page, uri ->
                lstPage.indexOfFirst {
                    it.indexFilePdf == page
                }.let {
                    pageAdapter?.updateItem(it, uri)
                }
            })
        initSeekbar()
        PreferencesUtils.putInteger(PresKey.TIME_ENTER_FILES, PreferencesUtils.getInteger(PresKey.TIME_ENTER_FILES, 0) + 1)
        PreferencesUtils.putInteger(PresKey.TIME_ENTER_FILE_PDF, PreferencesUtils.getInteger(PresKey.TIME_ENTER_FILE_PDF, 0) + 1)
        goPage(startPage)
        if (pageAdapter?.lstPagerAds.isNullOrEmpty()) {
//            initNativeAds(startPage)
        }
    }

    private fun initSeekbar() {
        pageAdapter?.let {
            val smax = max(it.itemCount - 1, 1)
            viewmodel.pageSliderRes.value = (10 + smax - 1) / smax * 2
            binding.sbPage.max = (it.itemCount - 1) * viewmodel.getPageSliderRes()
            binding.sbPage.isEnabled = binding.sbPage.max != 0
        }
    }

//    @SuppressLint("SetTextI18n")
//    private fun initNativeAds(startPage: Int) {
//        pageAdapter?.let {
//            val indexAds: Int
//            if (!lstPage.isNullOrEmpty()) {
//                indexAds = if (lstPage.size == 1 || lstPage.size == 2) {
//                    1
//                } else if (startPage == lstPage.size - 1) {
//                    startPage
//                } else {
//                    startPage + 1
//                }
//
//                loadNativeByIndex(it, indexAds, loadListener = { success ->
//                    initSeekbar()
//                    loadNativeByIndex(it, indexAds - 9, loadListener = { success ->
//                        initSeekbar()
//                        var countAds = 0
//                        if (success) {
//                            countAds++
//                            it.lstPagerAds.remove(indexAds)
//                            it.lstPagerAds.add(indexAds + 1)
//                            it.lstPagerAdsView[indexAds + 1] = it.lstPagerAdsView[indexAds]
//                            it.lstPagerAdsView.remove(indexAds)
//                        }
//                        loadNativeByIndex(it, indexAds + 9, loadListener = { success ->
//                            initSeekbar()
//                            binding.tvPage.text = (max(
//                                startPage + 1 + countAds,
//                                1
//                            )).toString() + "/" + it.lstPage.size
//
//                            if (lstPage.size == 2) {
//                                pageAdapter?.notifyDataSetChanged()
//                            }
//                        })
//                    })
//                })
//            }
//        }
//    }

//    private fun loadNativeByIndex(
//        it: PdfPageAdapter,
//        indexAds: Int,
//        loadListener: (Boolean) -> Unit
//    ) {
//        if (indexAds < 0 || indexAds > it.lstPage.size) {
//            loadListener(false)
//        } else {
//            AdmobNativeAdView.getNativeAd(
//                this@PdfDetailActivity, com.google.android.gms.ads.ez.R.layout.native_admob_mu,
//                object : NativeAdListener() {
//                    override fun onError() {
//                        loadListener(false)
//                    }
//
//                    override fun onLoaded(nativeAd: RelativeLayout?) {
//                        it.lstPagerAds.add(Integer.valueOf(indexAds))
//                        it.lstPagerAdsView[indexAds] = nativeAd
//                        it.add(indexAds, PagePdf(null, -1))
//                        if (it.itemCount < 3) {
//                            it.notifyDataSetChanged()
//                        }
//                        loadListener(true)
//                    }
//                })
//        }
//    }

    private fun initRcv() {
        val params = binding.rnRcv.layoutParams
        params.width = Utils.getWidthScreen(this)
        params.height = Utils.geHeightScreen(this)
        binding.rnRcv.layoutParams = params
        if (!PreferencesUtils.getBoolean(PreferencesKey.KeyPress.PDF_VIEWER_CONTINOUOUS, true)) {
            binding.rcvPdf.canTouchArea = true
            snapHelper.attachToRecyclerView(binding.rcvPdf)
            binding.rcvPdf.isNestedScrollingEnabled = false
            rcvManager = MyLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            pageAdapter?.isHorizontal = true
        } else {
            binding.rcvPdf.canTouchArea = false
            snapHelper.attachToRecyclerView(null)
            binding.rcvPdf.isNestedScrollingEnabled = true
            rcvManager = MyLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
            pageAdapter?.isHorizontal = false
        }
        binding.rcvPdf.apply {
            layoutManager = rcvManager
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    // debounce full-page re-render
                    zoomJob?.cancel()
                    zoomJob = lifecycleScope.launch {
                      handleZoom()
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    snapHelper.findSnapView(layoutManager)?.let {
                        viewmodel.currentPage.postValue(
                            binding.rcvPdf.getChildAdapterPosition(it)
                        )
                    }
                }
            })
        }
    }
    private suspend fun handleZoom(forceZoom : Boolean = false) {
        if (viewmodel.mode.value == Mode.Search && !forceZoom) {
            // don't re-render while searching
            return
        }
        delay(150) // wait until zoom stabilizes
        Log.i(TAG, "Zoom level: $viewmodel.currentZoom - rerender start")
        val lm = binding.rcvPdf.layoutManager as? LinearLayoutManager ?: return
        val first = lm.findFirstVisibleItemPosition()
        val last = lm.findLastVisibleItemPosition()
        for (pos in first..last) {
            if (pos < 0 || pos >= lstPage.size) continue
            val pageIndex = lstPage[pos].indexFilePdf

            val shouldRender = when {
                // zoom dropped back near 1x after having rendered high-res: allow fallback (optional)
                viewmodel.currentZoom < MAX_ZOOM -> true
                // significant change since last render
                else -> false
            }

            if (!shouldRender) continue

            // record that we're rendering at this zoom
            lastRenderedZoom[pageIndex] = viewmodel.currentZoom

            viewmodel.coroutineFunction.loadZoomedPage(
                muPDFCore,
                pageIndex,
                viewmodel.currentZoom
            ) { _, uri ->
                val holder =
                    binding.rcvPdf.findViewHolderForAdapterPosition(pos) as? PdfPageAdapter.ViewHolder
                if (holder == null) return@loadZoomedPage
                val bmp = uri.uriToBitmap(this@PdfDetailActivity)
//                lstPage[pos].uriImage = uri
                bmp?.let {
                    holder.itemBinding.imPage.apply {
                        setImageBitmap(it)
                    }
                }
            }
        }
    }

    private var zoomJob: Job? = null
    private val lastRenderedZoom = mutableMapOf<Int, Float>() // pageIndex -> last zoom we rendered
    private val MAX_ZOOM= 3.5f

    @SuppressLint("SetTextI18n")
    private fun initListener() {
        binding.llToolbar.ivRemoveAds.setOnClickListener {

        }
        if (!isAcceptManagerStorage()) {
            binding.navigationDetail.visibility = View.GONE
        } else {
            binding.navigationDetail.setOnNavigationItemSelectedListener(this)
        }
        binding.sbPage.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                goPage((seekBar.progress + viewmodel.getPageSliderRes() / 2) / viewmodel.getPageSliderRes())
            }
        })
        pageAdapter?.textClickListener = {
            showBottomAddText()
        }
        viewmodel.currentPage.observe(this) {
//            currentPage = it
            binding.tvPage.text = (it + 1).toString() + "/" + lstPage.size
            PreferencesUtils.putInteger(
                urlFile,
                viewmodel.convertSavePage(it, pageAdapter?.lstPagerAds)
            )
            pageAdapter?.let { pageAdapter ->
                binding.sbPage.max = (pageAdapter.itemCount - 1) * viewmodel.getPageSliderRes()
                binding.sbPage.progress = it * viewmodel.getPageSliderRes()
            }
        }

        viewmodel.mode.observe(this) {
            if (!firstInit) {
                updateMode(it)
            }
            firstInit = false
        }

        pageAdapter?.annotationSelect = {
            selectedAnnotationIndex = it
            toolbarBinding.imAccept.apply {
                alpha = 0.5f
                isEnabled = false
                it?.let {
                    alpha = 1.0f
                    isEnabled = true
                }
            }
        }

        pageAdapter?.cleanSignature = {
            if (viewmodel.mode.value != Mode.Normal) {
                viewmodel.mode.postValue(Mode.Normal)
            }
            pageAdapter?.fileSignature = null
        }

        toolbarBinding.edtSearch.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val textEmpty = TextUtils.isEmpty(s)
                    toolbarBinding.imCleanSearch.visibility =
                        if (textEmpty) View.INVISIBLE else View.VISIBLE
                    toolbarBinding.imNext.apply {
                        alpha = if (textEmpty) 0.5f else 1.0f
                        isEnabled = !textEmpty
                    }
                    toolbarBinding.imPrevious.apply {
                        alpha = if (textEmpty) 0.5f else 1.0f
                        isEnabled = !textEmpty
                    }
                }

                override fun afterTextChanged(s: Editable) {}
            })

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!TextUtils.isEmpty(
                            toolbarBinding.edtSearch.text.toString()
                        )
                    ) {
                        goSearch(1)
                    }
                }
                false
            }
        }
        toolbarBinding.apply {
            ivBack.setOnClickListener {
                onBackPressed()
            }
            ivSetting.setOnClickListener(this@PdfDetailActivity)
            ivSearch.setOnClickListener(this@PdfDetailActivity)
            imCloseSearch.setOnClickListener(this@PdfDetailActivity)
            imCleanSearch.setOnClickListener(this@PdfDetailActivity)
            imNext.setOnClickListener(this@PdfDetailActivity)
            imPrevious.setOnClickListener(this@PdfDetailActivity)
            imCloseAnnotation.setOnClickListener(this@PdfDetailActivity)
            imAccept.setOnClickListener(this@PdfDetailActivity)
            imPickColor.setOnClickListener(this@PdfDetailActivity)
            imUndo.setOnClickListener(this@PdfDetailActivity)
            imRedo.setOnClickListener(this@PdfDetailActivity)
        }
        binding.rcvPdf.touchListener = this

        binding.zoomlayout.apply {
            zoomListener = { scale ->
                viewmodel.currentZoom = scale
//                val previousZoom = viewmodel.currentZoom
//
//                // Log khi zoom thay đổi
//                Log.i(TAG, "Zoom changed: $previousZoom -> $scale")
//
//                // Log khi zoom tự động reset về giới hạn
//                val minZoom = getMinZoom()
//                val maxZoom = getMaxZoom()
//                if (scale <= minZoom) {
//                    Log.w(TAG, "Zoom automatically reset to minimum: $scale (min: $minZoom)")
//                } else if (scale >= maxZoom) {
//                    Log.w(TAG, "Zoom automatically reset to maximum: $scale (max: $maxZoom)")
//                }

                binding.rcvPdf.clickCount = 0
                if (viewmodel.mode.value == Mode.Normal) {
                    if (scale > 1f) {
                        // When zoomed in: allow horizontal pan in-page, block vertical pan so RecyclerView scrolls
                        setScrollEnabled(true)
                        setHorizontalPanEnabled(true)
                        setOneFingerScrollEnabled(true)

//                        setVerticalPanEnabled(true)
                    } else {
                        // At 1x: disable ZoomLayout panning entirely so RecyclerView handles all drags
                         setScrollEnabled(true)
                        setHorizontalPanEnabled(false)
//                        setVerticalPanEnabled(false)
                    }
                }
                zoomJob?.cancel()
                zoomJob = lifecycleScope.launch {
                    handleZoom()
                }
            }
        }
    }

    private fun keepDisplayPage(keep: Boolean = false) {
        pageAdapter?.apply {
            canDrawPage = keep
            currentIndex = viewmodel.getCurrentPage()
            mode = viewmodel.mode.value ?: Mode.Normal
            notifyDataSetChanged()
        }
        try {
            rcvManager.isScrollEnabled = !keep
            binding.rcvPdf.canTouchAble = !keep
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    private fun updateMode(it: Mode) {
        ViewUtils.hideView(true, toolbarBinding.llMain, 300)
        ViewUtils.hideView(true, toolbarBinding.llSearch, 300)
        ViewUtils.hideView(true, toolbarBinding.llAccept, 300)
        keepDisplayPage()
        val index = if (lstPage.size > viewmodel.getCurrentPage()) {
            lstPage[viewmodel.getCurrentPage()].indexFilePdf
        } else {
            0
        }
        if (index != -1) {
            viewmodel.getAnnotation(muPDFCore, index)
            viewmodel.getTextPage(muPDFCore, index)
        }
        when (it) {
            Mode.Search -> {
                binding.zoomlayout.setHasClickableChildren(false)
                binding.zoomlayout.setScrollEnabled(false)
              //  binding.zoomlayout.isInterceptTouchEvent = false

                if (PreferencesUtils.getBoolean(
                        PreferencesKey.KeyPress.PDF_VIEWER_CONTINOUOUS,
                        true
                    )
                ) {
                    binding.rcvPdf.scrollToPosition(viewmodel.getCurrentPage())
                    zoomJob?.cancel()
                    zoomJob = lifecycleScope.launch {
                        handleZoom(forceZoom = true)
                    }
                }
                ViewUtils.hideView(false, binding.crvBottomControl, 300)
                keepDisplayPage(true)
                ViewUtils.showView(true, toolbarBinding.llSearch, 300)
                KeyboardUtils.showSoftKeyboard(this)
                toolbarBinding.edtSearch.requestFocus()
            }

            Mode.Normal -> {
                binding.zoomlayout.setHasClickableChildren(false)
                binding.zoomlayout.onlyAllow2Fingers = false
                // Khôi phục zoom khi quay về mode Normal
                //binding.zoomlayout.setZoomEnabled(true)
                KeyboardUtils.hideSoftKeyboard(this)
                ViewUtils.showView(true, toolbarBinding.llMain, 300)
                ViewUtils.showView(false, binding.crvBottomControl, 300)
                zoomJob?.cancel()
                zoomJob = lifecycleScope.launch {
                    handleZoom()
                }
            }

            Mode.Delete -> {
                binding.zoomlayout.setScrollEnabled(false)
                binding.zoomlayout.setHasClickableChildren(true)
                binding.zoomlayout.onlyAllow2Fingers = true
                if (PreferencesUtils.getBoolean(
                        PreferencesKey.KeyPress.PDF_VIEWER_CONTINOUOUS,
                        true
                    )
                ) {
                    binding.rcvPdf.scrollToPosition(viewmodel.getCurrentPage())
                }
                keepDisplayPage(true)
                ViewUtils.hideView(false, binding.crvBottomControl, 300)
                ViewUtils.showView(true, toolbarBinding.llAccept, 300)
                toolbarBinding.tvFunctionName.text = getString(R.string.delete)
                toolbarBinding.imPickColor.visibility = View.GONE
                toolbarBinding.imUndo.visibility = View.GONE
                toolbarBinding.imRedo.visibility = View.GONE
                toolbarBinding.imAccept.apply {
                    setImageResource(R.drawable.lib_ic_delete_note)
                    alpha = 0.5f
                    isEnabled = false
                }
            }

            Mode.Ink, Mode.Unline, Mode.HighLight, Mode.Strikeout, Mode.CopyText,
            Mode.Signature, Mode.AddImage, Mode.AddText -> {
                keepDisplayPage(true)
                binding.zoomlayout.setScrollEnabled(false)
                binding.zoomlayout.setHasClickableChildren(false)
                binding.zoomlayout.onlyAllow2Fingers = true

                // Reset zoom về 1:1 và vô hiệu hóa zoom cho các mode Signature, AddImage, AddText
//                if (it == Mode.Signature || it == Mode.AddImage || it == Mode.AddText) {
//                    binding.zoomlayout.realZoomTo(1.0f, true)
//                    binding.zoomlayout.setZoomEnabled(false)
//                } else {
//                    binding.zoomlayout.setZoomEnabled(true)
//                }
                if (PreferencesUtils.getBoolean(
                        PreferencesKey.KeyPress.PDF_VIEWER_CONTINOUOUS,
                        true
                    )
                ) {
                    binding.rcvPdf.scrollToPosition(viewmodel.getCurrentPage())
                    zoomJob?.cancel()
                    zoomJob = lifecycleScope.launch {
                        handleZoom()
                    }
                }
                ViewUtils.hideView(false, binding.crvBottomControl, 300)
                ViewUtils.showView(true, toolbarBinding.llAccept, 300)
                toolbarBinding.imUndo.visibility = View.GONE
                toolbarBinding.imRedo.visibility = View.GONE
                toolbarBinding.imPickColor.visibility = View.VISIBLE
                toolbarBinding.tvFunctionName.text = when (it) {
                    Mode.Ink -> {
                        toolbarBinding.imUndo.visibility = View.VISIBLE
                        toolbarBinding.imRedo.visibility = View.VISIBLE
                        getString(R.string.brush)
                    }

                    Mode.Unline -> {
                        getString(R.string.underline)
                    }

                    Mode.HighLight -> {
                        getString(R.string.high_light)
                    }

                    Mode.Strikeout -> {
                        getString(R.string.strike_throught)
                    }

                    Mode.CopyText -> {
                        toolbarBinding.imPickColor.visibility = View.GONE
                        getString(R.string.copy)
                    }

                    Mode.Signature -> {
                        toolbarBinding.imPickColor.visibility = View.GONE
                        getString(R.string.add_signature)
                    }

                    Mode.AddImage -> {
                        toolbarBinding.imPickColor.visibility = View.GONE
                        getString(R.string.add_image)
                    }

                    Mode.AddText -> {
                        toolbarBinding.imPickColor.visibility = View.GONE
                        getString(R.string.add_text)
                    }

                    else -> {
                        ""
                    }
                }
                toolbarBinding.imAccept.apply {
                    setImageResource(R.drawable.lib_ic_accept)
                    alpha = 1.0f
                    isEnabled = true
                }
            }

        }

        if (it == Mode.AddText) {
            Handler(Looper.getMainLooper()).postDelayed({
                showBottomAddText()
            }, 500)
        }
    }

    private fun showBottomAddText() {
        val stickerView =
            (binding.rcvPdf.findViewHolderForAdapterPosition(viewmodel.getCurrentPage())
                    as PdfPageAdapter.ViewHolder?)?.itemBinding?.viewSignature
        if (stickerView?.handlingSticker == null)
            return
        val stickerText = stickerView.handlingSticker as TextSticker
        val bottomAddText = BottomSheetAddText(stickerText).apply {
            try {
                showDialogColor = {
                    var color = if (it.isEmpty()) {
                        "#000000"
                    } else {
                        it
                    }
                    ColorPickerDialog.newBuilder()
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(false)
                        .setDialogId(0)
                        .setColor(Color.parseColor(color))
                        .setShowAlphaSlider(false)
                        .show(this@PdfDetailActivity)
                }

                colorSelectListener = {
                    stickerText.apply {
                        setTextColor(it)
                        resizeText()
                    }
                    stickerView.replace(stickerText)
                }
                fontSelectListener = {
                    if (!TextUtils.isEmpty(it)) {
                        stickerText.apply {
                            fontName = it
                            setTypeface(
                                Typeface.createFromAsset(resources.assets, "font/${fontName}")
                            )
                        }

                    } else {
                        stickerText.setTypeface(Typeface.DEFAULT)
                    }
                    stickerText.resizeText()
                    stickerView.invalidate()
                }

                textChangeListener = {
                    stickerText.apply {
                        setText(it)
                        resizeText()
                    }
                    stickerView.invalidate()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        if (!isDestroyed && !isFinishing) {
            bottomAddText.show(
                supportFragmentManager,
                BottomSheetAddText::class.java.name
            )
        }
    }

    private fun goSearch(direction: Int) {
        KeyboardUtils.hideSoftKeyboard(this)
        if (TextUtils.isEmpty(toolbarBinding.edtSearch.toString())) {
            return
        }
        viewmodel.searchText(
            toolbarBinding.edtSearch.text.toString(),
            direction,
            muPDFCore,
            result = { index, currentPage ->
                goPage(index)
                pageAdapter?.notifyItemChanged(currentPage)
                pageAdapter?.notifyItemChanged(index)
            })
    }

    private fun goPage(index: Int) {
        viewmodel.currentPage.postValue(
            when {
                index < 0 -> {
                    index
                }

                index >= lstPage.size -> {
                    lstPage.size - 1
                }

                else -> {
                    index
                }
            }
        )
        binding.rcvPdf.scrollToPosition(index)
    }

    override fun onClickNextPage() {
        try {
            val newIndex = snapHelper.findSnapView(rcvManager)?.let {
                binding.rcvPdf.getChildAdapterPosition(it) + 1
            } ?: 0
            binding.rcvPdf.smoothScrollToPosition(
                if (newIndex > pageAdapter!!.itemCount - 1)
                    pageAdapter!!.itemCount - 1
                else newIndex
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onClickPreviousPage() {
        try {
            val newIndex = snapHelper.findSnapView(rcvManager)?.let {
                binding.rcvPdf.getChildAdapterPosition(it) - 1
            } ?: 0
            binding.rcvPdf.smoothScrollToPosition(
                if (newIndex < 0) 0 else newIndex
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun doubleClickMainArea() {
        binding.zoomlayout.apply {
            when {
                realZoom < 1.4f -> realZoomTo(1.5f, true)
                realZoom < 1.9f -> realZoomTo(2.0f, true)
                else -> realZoomTo(1f, true)
            }
        }
    }

    override fun onClickMainArea() {
        showHideToolbar(!toolbarBinding.container.isShown)
    }

    private fun showHideToolbar(isShow: Boolean) {
        if (isShow) {
            val paddingTop = resources.getDimensionPixelSize(R.dimen._60sdp)
            binding.rnRcv.setPadding(0, paddingTop, 0, 0)
            ViewUtils.showView(true, toolbarBinding.container, 300)
            ViewUtils.showView(false, binding.tvPage, 300)
            ViewUtils.showView(false, binding.crvBottomControl, 300)
        } else {
            binding.rnRcv.setPadding(0, 0, 0, 0)
            binding.rnRcv.setPadding(0, 0, 0, 0)
            ViewUtils.hideView(true, toolbarBinding.container, 300)
            ViewUtils.hideView(false, binding.tvPage, 300)
            ViewUtils.hideView(false, binding.crvBottomControl, 300)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_setting -> {
                logEvent("detail_setting")
                if (aVoidDoubleClick() || isFinishing || isDestroyed)
                    return
                val bottomSheet = BottomSheetDetailFunction(this::bottomFuncListener)
                bottomSheet.show(
                    supportFragmentManager,
                    BottomSheetDetailFunction::class.java.name
                )
            }

            R.id.iv_search -> {
                logEvent("detail_search")
                viewmodel.mode.postValue(Mode.Search)
            }

            R.id.im_close_search -> {
                viewmodel.mode.postValue(Mode.Normal)
                viewmodel.lastIndexSearch?.let {
                    pageAdapter?.notifyItemChanged(it)
                }
                viewmodel.lastIndexSearch = null
                viewmodel.uriSearch = null
                toolbarBinding.edtSearch.setText("")
            }

            R.id.im_clean_search -> {
                viewmodel.uriSearch = null
                viewmodel.lastIndexSearch = null
                pageAdapter?.notifyItemChanged(viewmodel.getCurrentPage())
                toolbarBinding.edtSearch.setText("")
            }

            R.id.im_next -> {
                goSearch(1)
            }

            R.id.im_previous -> {
                goSearch(-1)
            }

            R.id.im_close_annotation -> {
                viewmodel.mode.postValue(Mode.Normal)
            }

            R.id.im_accept -> {
                saveFileBackup()
                when (viewmodel.mode.value) {
                    Mode.Delete -> {
                        viewmodel.deleteAnnotation(
                            muPDFCore,
                            if (lstPage.size > viewmodel.getCurrentPage()) {
                                lstPage[viewmodel.getCurrentPage()].indexFilePdf
                            } else {
                                null
                            },
                            selectedAnnotationIndex,
                            success = { page, uriImage ->
                                pageAdapter?.updateItem(viewmodel.getCurrentPage(), uriImage)
                                toolbarBinding.imAccept.apply {
                                    alpha = 0.5f
                                    isEnabled = false
                                }
                            })
                    }

                    Mode.Ink -> {
                        if (binding.rcvPdf.findViewHolderForAdapterPosition(viewmodel.getCurrentPage()) == null)
                            return
                        val itemHolder =
                            binding.rcvPdf.findViewHolderForAdapterPosition(viewmodel.getCurrentPage())
                                    as PdfPageAdapter.ViewHolder
                        viewmodel.saveDraw(
                            muPDFCore,
                            itemHolder.itemBinding.imPage.drawing,
                            itemHolder.itemBinding.imPage.paintDraw,
                            if (lstPage.size > viewmodel.getCurrentPage()) {
                                lstPage[viewmodel.getCurrentPage()].indexFilePdf
                            } else {
                                null
                            },
                            success = { page, uriImage ->
                                pageAdapter?.updateItem(viewmodel.getCurrentPage(), uriImage)
                                viewmodel.mode.postValue(Mode.Normal)
                            })
                    }

                    Mode.CopyText -> {
                        if (binding.rcvPdf.findViewHolderForAdapterPosition(viewmodel.getCurrentPage()) == null)
                            return
                        val itemHolder =
                            binding.rcvPdf.findViewHolderForAdapterPosition(viewmodel.getCurrentPage())
                                    as PdfPageAdapter.ViewHolder
                        val result = itemHolder.itemBinding.imPage.copyText()
                        if (!TextUtils.isEmpty(result)) {
                            Utils.copyText(this, result)
                            toast(getString(R.string.copyed))
                        }
                        viewmodel.mode.postValue(Mode.Normal)
                    }

                    Mode.Unline, Mode.Strikeout, Mode.HighLight -> {
                        if (binding.rcvPdf.findViewHolderForAdapterPosition(viewmodel.getCurrentPage()) == null)
                            return
                        val itemHolder =
                            binding.rcvPdf.findViewHolderForAdapterPosition(viewmodel.getCurrentPage())
                                    as PdfPageAdapter.ViewHolder
                        val quadPoints = itemHolder.itemBinding.imPage.getMarkupSelectionPoint()
                        viewmodel.markupSelection(
                            quadPoints,
                            muPDFCore,
                            if (lstPage.size > viewmodel.getCurrentPage()) {
                                lstPage[viewmodel.getCurrentPage()].indexFilePdf
                            } else {
                                null
                            },
                            success = { page, uriImage ->
                                pageAdapter?.updateItem(viewmodel.getCurrentPage(), uriImage)
                                viewmodel.mode.postValue(Mode.Normal)
                            })
                    }

                    Mode.Signature, Mode.AddImage, Mode.AddText -> {
                        val itemHolder =
                            binding.rcvPdf.findViewHolderForAdapterPosition(viewmodel.getCurrentPage())
                                    as PdfPageAdapter.ViewHolder?
                        val indexPdf = lstPage[viewmodel.getCurrentPage()].indexFilePdf
                        if (itemHolder == null)
                            return
                        itemHolder.itemBinding.viewSignature.apply {
                            val arrPoint = FloatArray(8)
                            getStickerPoints(handlingSticker, arrPoint)
                            val fileAdd =
                                if (PreferencesUtils.getBoolean(PreferencesKey.KeyPress.PDF_VIEWER_NIGHT_MODE)) {
                                    createBitmap().reverstBitmap()
                                        .bitmapToFileCachePng(this@PdfDetailActivity)
                                } else {
                                    createBitmap().bitmapToFileCachePng(this@PdfDetailActivity)
                                }
                            handlingSticker = null
                            viewmodel.saveInternal(muPDFCore, success = {
                                viewmodel.signaturePDF2(
                                    urlFile,
                                    fileAdd,
                                    if (lstPage.size > viewmodel.getCurrentPage()) {
                                        indexPdf
                                    } else {
                                        null
                                    },
                                    result = { success ->
                                        if (success) {
                                            setResult(Activity.RESULT_OK)
                                            password?.let {
                                                addPasswordBackground(File(urlFile), it) {
                                                    viewmodel.mode.postValue(Mode.Normal)

                                                    Handler(Looper.getMainLooper()).post {
                                                        recreate()
                                                    }
                                                }
                                            } ?: run {
                                                viewmodel.mode.postValue(Mode.Normal)
                                                Handler(Looper.getMainLooper()).post {
                                                    recreate()
                                                }
                                            }
                                        }
                                    })
                            })
                        }
                    }

                    else -> {}
                }
            }

            R.id.im_pick_color -> {
                if (aVoidDoubleClick())
                    return
                showBottomSelectAnnotaion()
            }

            R.id.im_undo -> {
                if (binding.rcvPdf.findViewHolderForAdapterPosition(viewmodel.getCurrentPage()) == null)
                    return
                val itemHolder =
                    binding.rcvPdf.findViewHolderForAdapterPosition(viewmodel.getCurrentPage())
                            as PdfPageAdapter.ViewHolder
                itemHolder.itemBinding.imPage.undoListener(true)
            }

            R.id.im_redo -> {
                if (binding.rcvPdf.findViewHolderForAdapterPosition(viewmodel.getCurrentPage()) == null)
                    return
                val itemHolder =
                    binding.rcvPdf.findViewHolderForAdapterPosition(viewmodel.getCurrentPage())
                            as PdfPageAdapter.ViewHolder
                itemHolder.itemBinding.imPage.undoListener(false)
            }
        }
    }

    private fun showBottomSelectAnnotaion() {
        viewmodel.mode.value?.let {
            val bottomSheet = BottomSheetAnnotation(it, showDilogColor = { colorDefault ->
                ColorPickerDialog.newBuilder()
                    .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                    .setAllowPresets(false)
                    .setDialogId(0)
                    .setColor(Color.parseColor(colorDefault))
                    .setShowAlphaSlider(false)
                    .show(this)
            })
            bottomSheet.show(
                supportFragmentManager,
                BottomSheetAnnotation::class.java.name
            )
        }
    }

    private fun bottomFuncListener(function: BottomSheetDetailFunction.FunctionState) {
        when (function) {
            BottomSheetDetailFunction.FunctionState.GO_PAGE -> {
                GoToPageDialog.ExtendBuilder(this)
                    .setPageNumber(muPDFCore.countPages() + (pageAdapter?.lstPagerAds?.size ?: 0))
                    .setTitle(resources.getString(R.string.goto_page))
                    .onSetPositiveButton(
                        resources.getString(R.string.ok)
                    ) { _, data ->
//                        EzAdControl.getInstance(this).showAds()
                        data[GoToPageDialog.PAGE_NUMBER]?.let { page ->
                            goPage((page as Int) - 1)
                        }
                    }
                    .onSetNegativeButton(resources.getString(R.string.cancel)) {}
                    .build()
                    .show()
            }

            BottomSheetDetailFunction.FunctionState.CONTINUOUS_PAGE,
            BottomSheetDetailFunction.FunctionState.ORIENTATION -> {
                initRcv()
                binding.rcvPdf.scrollToPosition(viewmodel.getCurrentPage())
            }

            BottomSheetDetailFunction.FunctionState.NIGHT_MODE -> {
                when (resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }

                    Configuration.UI_MODE_NIGHT_NO -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
            }

            BottomSheetDetailFunction.FunctionState.NOTE -> {
                showBottomNote()
            }

            BottomSheetDetailFunction.FunctionState.ADD_PASSWORD -> {
                if (!isAcceptManagerStorage()) {
                    toast(getString(R.string.accept_all_file_permission_edit))
                    return
                }
                showDialogEditPassword(File(urlFile), false) {
                    it?.let {
//                        EzAdControl.getInstance(this).showAds()
                        setResult(Activity.RESULT_OK)
                        hasPassword = true
                    }
                }
            }

            BottomSheetDetailFunction.FunctionState.REMOVE_PASSWORD -> {
                if (!isAcceptManagerStorage()) {
                    toast(getString(R.string.accept_all_file_permission_edit))
                    return
                }
                showDialogEditPassword(File(urlFile), true) {
                    it?.let {
//                        EzAdControl.getInstance(this).showAds()
                        setResult(Activity.RESULT_OK)
                        hasPassword = false
                    }
                }
            }

            BottomSheetDetailFunction.FunctionState.PRINT -> {
                if (!isAcceptManagerStorage()) {
                    toast(getString(R.string.accept_all_file_permission_edit))
                    return
                }

                urlFile?.let {
                    printDoc(it, password)
                }
            }

            BottomSheetDetailFunction.FunctionState.DETAIL -> {
                urlFile?.let {
                    showDetail(it)
                }
            }

            BottomSheetDetailFunction.FunctionState.BOOKMASK -> {
                urlFile?.let {
                    val isbookmark: Boolean =
                        viewmodel.isbookmark(viewmodel.getCurrentPage())
                    viewmodel.updatebookmark(it, !isbookmark, viewmodel.getCurrentPage())
                }
            }

            BottomSheetDetailFunction.FunctionState.DELETE -> {
                if (!isAcceptManagerStorage()) {
                    toast(getString(R.string.accept_all_file_permission_edit))
                    return
                }
                urlFile?.let {
                    showDialogConfirm(
                        resources.getString(R.string.delete),
                        String.format(
                            resources.getString(R.string.del_message),
                            FilenameUtils.getBaseName(it)
                        )
                    ) {
                        if (it) {
                            if (allowEdit)
                                sendBroadcast(
                                    Intent().apply {
                                        action = BroadcastSubmodule.ACTION_DELETE
                                        putExtra(BroadcastSubmodule.PATH, urlFile)
                                    }
                                )
                            backMain()
                        }
                    }
                }
            }

            BottomSheetDetailFunction.FunctionState.FAVORITE -> {
                if (allowEdit) {
                    isFavorite = !isFavorite
                    sendBroadcast(
                        Intent().apply {
                            action = BroadcastSubmodule.ACTION_FAVORITE
                            putExtra(BroadcastSubmodule.IS_FAVORITE, isFavorite)
                            putExtra(BroadcastSubmodule.PATH, urlFile)
                        }
                    )
                }
            }

            BottomSheetDetailFunction.FunctionState.THUMBNAIL -> {
                Intent(this, ThumbnailActivity::class.java).apply {
                    putExtra(
                        Config.Constant.DATA_MU_PDF_CORE,
                        viewmodel.mapUriPage
                    )
                    startActivityForResult(
                        this,
                        Config.IntentResult.SELECT_PAGE
                    )
                }
            }

            BottomSheetDetailFunction.FunctionState.OUTLINE -> {
                SingleOutline.getInstance().lstOutline = muPDFCore.getOutline()
                Intent(this, OutlineActivity::class.java).apply {
                    startActivityForResult(
                        this,
                        Config.IntentResult.SELECT_PAGE
                    )
                }
            }

            BottomSheetDetailFunction.FunctionState.SIGNATURE -> {
                launchActivity<SignatureActivity>(Config.IntentResult.SELECT_SIGNATURE) {}
            }

            BottomSheetDetailFunction.FunctionState.SHARE -> {
                if (!isAcceptManagerStorage()) {
                    toast(getString(R.string.accept_all_file_permission_edit))
                    return
                }
                if (!TextUtils.isEmpty(urlFile)) {
                    val uri = FileProvider.getUriForFile(
                        this@PdfDetailActivity,
                        applicationContext.packageName.toString() + ".provider",
                        File(urlFile)
                    )
                    val share = Intent()
                    share.action = Intent.ACTION_SEND
                    share.type = "application/pdf"
                    share.putExtra(Intent.EXTRA_STREAM, uri)
                    startActivity(share)
                }
            }

            BottomSheetDetailFunction.FunctionState.WATERMARK -> {
                if (!isAcceptManagerStorage()) {
                    toast(getString(R.string.accept_all_file_permission_edit))
                    return
                }
                urlFile?.let { urlFile ->
                    val dialog = AddWatermarkDialog.ExtendBuilder(this)
                        .setFileName(FilenameUtils.getBaseName(urlFile))
                        .setTitle(resources.getString(com.ezstudio.pdftoolmodule.R.string.add_watermark))
                        .onSetPositiveButton(resources.getString(com.ezstudio.pdftoolmodule.R.string.save)) { _, _ -> }
                        .onSetNegativeButton(resources.getString(com.ezstudio.pdftoolmodule.R.string.cancel)) { }
                        .build() as AddWatermarkDialog
                    dialog.result = {
                        viewmodel.addWatermark(urlFile, it) { success ->
                            if (success) {
                                toast(getString(R.string.add_watermark_success))
                                Handler(Looper.getMainLooper()).post {
                                    recreate()
                                }
                            } else {
                                toast(getString(com.ezteam.baseproject.R.string.app_error))
                            }
                        }
                    }
                    dialog.show()
                }

            }

            BottomSheetDetailFunction.FunctionState.EXTRACT_IMAGE -> {
                if (!isAcceptManagerStorage()) {
                    toast(getString(R.string.accept_all_file_permission_edit))
                    return
                }
                launchActivity<ExtractActivity> {
                    putExtra(ExtractActivity.FILE_PATH, urlFile)
                }
            }

            BottomSheetDetailFunction.FunctionState.ADD_IMAGE -> {
                launchActivity<PickImageActivity>(Config.IntentResult.SELECT_IMAGE) {
                    putExtra(PickImageActivity.KEY_PICK_ONE, true)
                }
            }

            BottomSheetDetailFunction.FunctionState.ADD_TEXT -> {
                viewmodel.mode.postValue(Mode.AddText)
            }

            BottomSheetDetailFunction.FunctionState.TOOL -> {
                if (!isAcceptManagerStorage()) {
                    toast(getString(R.string.accept_all_file_permission_edit))
                    return
                }
                val bottomSheet = BottomSheetTool(this::bottomFuncListener)
                bottomSheet.show(supportFragmentManager, BottomSheetTool::class.java.name)
            }

            BottomSheetDetailFunction.FunctionState.EDIT -> {
                if (!isAcceptManagerStorage()) {
                    toast(getString(R.string.accept_all_file_permission_edit))
                    return
                }
                val bottomSheet = BottomSheetEdit(this::bottomFuncListener)
                bottomSheet.show(supportFragmentManager, BottomSheetEdit::class.java.name)
            }

            BottomSheetDetailFunction.FunctionState.SETTING -> {
                try {
                    val c = Class.forName("com.ezstudio.pdfreaderver4.activity.SettingActivity")
                    val intent = Intent(this, c).apply {
                        putExtra("reset app", false)
                    }
                    startActivity(intent)
                } catch (ignored: ClassNotFoundException) {
                }
            }

            else -> {

            }
        }
    }
//    private var isGoingToSettingToClearDefaultPdf = false
//    override fun onResume() {
//        super.onResume()
//        val timeEnterFilePdf = PreferencesUtils.getInteger(PresKey.TIME_ENTER_FILE_PDF, 1)
//        if (!TemporaryStorage.isShowedDefaultReaderRequestPdfDialogInThisSession || isGoingToSettingToClearDefaultPdf) {
//            TemporaryStorage.isShowedDefaultReaderRequestPdfDialogInThisSession = true
//        isGoingToSettingToClearDefaultPdf = false
//            Log.d(TAG, "time enter file pdf = $timeEnterFilePdf")
//            if (timeEnterFilePdf == 1 || timeEnterFilePdf % 3 == 0 || BuildConfig.DEBUG) {
//                val defaultPdfViewerResolveInfo = getDefaultPdfViewerClass()
//                Log.i("DefaultReader", "defaultPdfViewer: $defaultPdfViewerResolveInfo")
//                if (defaultPdfViewerResolveInfo?.activityInfo == null || defaultPdfViewerResolveInfo.activityInfo.name.contains("internal.app.ResolverActivity")
//                    || defaultPdfViewerResolveInfo.activityInfo.name.contains("com.android.intentresolver")) { // default reader isn't set => show dialog to set default
//                    val dialog = DefaultReaderRequestDialog.newInstance("PDF")
//                    dialog.show(this.supportFragmentManager, "RequestDefaultReaderDialog")
//                } else if(!defaultPdfViewerResolveInfo.activityInfo.name.contains(packageName) ) { // default reader is set but not our app => show dialog to clear default
//                    val fragmentManager = supportFragmentManager
//                    val existingDialog = fragmentManager.findFragmentByTag("DefaultReaderUninstallDialog")
//                    if (existingDialog == null) {
//                        val dialog = DefaultReaderUninstallDialog()
//                        dialog.defaultPdfViewer = defaultPdfViewerResolveInfo
//                        dialog.listener = {
//                            isGoingToSettingToClearDefaultPdf = true
//                        }
//                        dialog.show(fragmentManager, "DefaultReaderUninstallDialog")
//                    }
//                } else { // default reader is our app => do nothing
//                    Log.d("DefaultReader", "defaultPdfViewer: $defaultPdfViewerResolveInfo")
//                }
//            }
//       }
//    }
//    private fun getDefaultPdfViewerClass(): ResolveInfo? {
//        val fileName = "file_example_PDF.pdf"
//        val assetManager = assets
//        val file = File(File(filesDir, "defaultFiles").apply { mkdirs() }, fileName)
//
//        // Copy file từ assets nếu chưa tồn tại
//        if (!file.exists()) {
//            try {
//                assetManager.open(fileName).use { inputStream ->
//                    FileOutputStream(file).use { outputStream ->
//                        inputStream.copyTo(outputStream)
//                    }
//                }
//            } catch (e: IOException) {
//                e.printStackTrace()
//                return null
//            }
//        }
//
//        val uri = Uri.fromFile(file)
//
//        val intent = Intent(Intent.ACTION_VIEW).apply {
//            setDataAndType(uri, "application/pdf")
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        }
//
//        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
//        Log.d("DefaultReader", "resolveInfo: $resolveInfo")
//
//        return resolveInfo
//    }
    private fun showBottomNote() {
        val bottomSheet = BottomSheetNote(
            listener = {
                viewmodel.mode.postValue(it)
            },
            onShowGuide = { steps ->
                if (steps.isNotEmpty()) {
                    GuideEditDialog(this, steps).show()
                }
            },
            pdfContentView = binding.rcvPdf,
            acceptButton = toolbarBinding.imAccept
        )
        bottomSheet.show(supportFragmentManager, BottomSheetNote::class.java.name)
    }

    override fun onBackPressed() {
        when {
            viewmodel.mode.value != Mode.Normal -> {
                viewmodel.mode.postValue(Mode.Normal)
            }

            muPDFCore.globals != 0L && muPDFCore.countPages() == 0 -> {
                backMain()
            }

            muPDFCore.globals != 0L && muPDFCore.hasChanges() -> {
                showDialogConfirm(
                    getString(R.string.app_name),
                    getString(R.string.document_has_changes_save_them),
                    onConfirm = {
                        if (it) {
                            viewmodel.saveInternal(muPDFCore, success = {
                                backMain()
                            })
                        } else {
                            backMain()
                        }
                    }
                )
            }

            else -> {
                backMain()
            }
        }
    }

    private fun backMain() {
        showAdsInterstitial { finish() }
    }
    private fun showAdsInterstitial(complete: (() -> Unit)) {
        if (IAPUtils.isPremium() || !Admob.getInstance().isLoadFullAds || !ConsentHelper.getInstance(
                this.applicationContext
            ).canRequestAds()
        ) {
            return complete()
        }
//        if (IAPUtils.isPremium() || !ConsentHelper.getInstance(
//                this.applicationContext
//            ).canRequestAds()
//        ) {
//            return complete()
//        }
        val interCallback: AdCallback = object : AdCallback() {
            override fun onNextAction() {
                Admob.getInstance().setOpenActivityAfterShowInterAds(true)
                return complete()
            }

            override fun onAdFailedToLoad(var1: LoadAdError?) {
                Log.e("TAG", "onAdFailedToLoad: ${var1?.message}")
                return complete()
            }
        }
        Admob.getInstance().setOpenActivityAfterShowInterAds(false)
        Admob.getInstance().loadAndShowInter(
            this,
            FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_filedetail"),
            100, 100, interCallback
        )
    }

    override fun onStop() {
        if (!isReadDone && allowEdit) {
            sendBroadcast(
                Intent().apply {
                    action = BroadcastSubmodule.ACTION_READING_STATUS
                    putExtra(BroadcastSubmodule.PATH, urlFile)
                    val currentPage = viewmodel.getCurrentPage()
                    putExtra(BroadcastSubmodule.IS_READ_DONE, totalPage - 1 == currentPage)
                    putExtra(BroadcastSubmodule.CURRENT_PAGE, currentPage)
                }
            )
        }
        super.onStop()
    }

    override fun onDestroy() {
        CleanUtils.cleanCustomDir(this@PdfDetailActivity.cacheDir.path)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onDestroy()
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        val hexColor = String.format("#%06X", (0xFFFFFF and color))
        if (viewmodel.mode.value == Mode.AddText) {
            val stickerView =
                (binding.rcvPdf.findViewHolderForAdapterPosition(viewmodel.getCurrentPage())
                        as PdfPageAdapter.ViewHolder?)?.itemBinding?.viewSignature
            val stickerText = (stickerView?.handlingSticker as TextSticker)
            stickerText.setTextColor(hexColor)
            stickerText.resizeText()
            stickerView.replace(stickerText)
        } else {
            try {
                val valueAnnotation =
                    PreferencesUtils.getAnnotation(Config.getPreferencesKeyByMode(viewmodel.mode.value))
                valueAnnotation.color = hexColor
                PreferencesUtils.setAnnotation(
                    valueAnnotation,
                    Config.getPreferencesKeyByMode(viewmodel.mode.value)
                )
            } catch (ex: Exception) {

            }
        }
    }

    override fun onDialogDismissed(dialogId: Int) {
        if (viewmodel.mode.value == Mode.AddText) {

        } else {
            showBottomSelectAnnotaion()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Config.IntentResult.SELECT_PAGE) {
            if (resultCode == Activity.RESULT_OK) {
                val page =
                    data!!.getIntExtra(Config.Constant.DATA_PAGE, 0)
                goPage(viewmodel.convertDisplayPage(page, pageAdapter?.lstPagerAds))
            }
        } else if (requestCode == Config.IntentResult.PRINT_REQUEST) {
            if (resultCode == Activity.RESULT_CANCELED) toast(getString(R.string.print_failed))
        } else if (requestCode == Config.IntentResult.SELECT_SIGNATURE) {
            if (resultCode == Activity.RESULT_OK) {
                Handler(Looper.getMainLooper()).postDelayed({
                    pageAdapter?.fileSignature =
                        data?.getStringExtra(SignatureActivity.SIGNATURE_SELECT)?.let { File(it) }
                    viewmodel.mode.value = Mode.Signature
                }, 300)
            }
        } else if (requestCode == Config.IntentResult.SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Handler(Looper.getMainLooper()).postDelayed({
                    pageAdapter?.fileSignature =
                        data?.getParcelableExtra<Uri>(PickImageActivity.URI_IMAGES_RESULT)?.path?.let {
                            File(it)
                        }
                    viewmodel.mode.value = Mode.AddImage
                }, 300)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (aVoidDoubleClick()) return false
        when (item.itemId) {
            R.id.menu_note -> {
                logEvent("func_detail_note")
                showBottomNote()
            }

            R.id.menu_signature -> {
                logEvent("func_detail_sign_pdf")
                launchActivity<SignatureActivity>(Config.IntentResult.SELECT_SIGNATURE) {}
            }

            R.id.menu_add_image -> {
                logEvent("func_detail_add_image")
                launchActivity<PickImageActivity>(Config.IntentResult.SELECT_IMAGE) {
                    putExtra(PickImageActivity.KEY_PICK_ONE, true)
                }
            }

            R.id.menu_add_text -> {
                logEvent("func_detail_scanner")
                viewmodel.mode.postValue(Mode.AddText)
            }
        }
        return false
    }

    private fun saveFileBackup() {
        FileSaveManager.copyFileToBackup(this, urlFile)
    }
}
