package officepro.document.reader.viewer.editor.screen.image

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.print.PrintManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.brian.base_iap.utils.FirebaseRemoteConfigUtil
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.PathUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.ezpdflib.util.print.PdfDocumentAdapter
import com.nlbn.ads.banner.BannerPlugin
import com.nlbn.ads.util.Admob
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.ActivityImageBinding
import officepro.document.reader.viewer.editor.screen.base.PdfBaseActivity
import officepro.document.reader.viewer.editor.screen.create.BottomSheetCreatePdf
import officepro.document.reader.viewer.editor.screen.create.CreateSuccessActivity
import officepro.document.reader.viewer.editor.screen.main.MainViewModel
import officepro.document.reader.viewer.editor.screen.func.BottomSheetImageFunction
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import officepro.document.reader.viewer.editor.common.FunctionState
import officepro.document.reader.viewer.editor.dialog.DeleteImageDialog
import officepro.document.reader.viewer.editor.dialog.DetailImageDialog
import officepro.document.reader.viewer.editor.dialog.RenameImageDialog
import officepro.document.reader.viewer.editor.utils.FileSaveManager
import officepro.document.reader.viewer.editor.utils.createPdf.OnPDFCreatedInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import java.util.Locale

class ImageActivity : PdfBaseActivity<ActivityImageBinding>() {
    private val viewModel by inject<MainViewModel>()
    private var isSingleImage = false
    private var imagePath: String? = null
    private var isZoomed = false
    private var currentRotation = 0f
    companion object {
        fun start(activity: FragmentActivity) {
            val intent = Intent(activity, ImageActivity::class.java)
            activity.startActivity(intent)
        }
        
        fun startWithImagePath(activity: FragmentActivity, imagePath: String) {
            val intent = Intent(activity, ImageActivity::class.java)
            intent.putExtra("imagePath", imagePath)
            intent.putExtra("isSingleImage", true)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        EzAdControl.getInstance(this).showAds()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    private fun loadNativeNomedia() {
        if (IAPUtils.isPremium()) {
            binding.layoutNative.visibility = View.GONE
            return
        }

        if (SystemUtils.isInternetAvailable(this)) {
            binding.layoutNative.visibility = View.VISIBLE
            val loadingView = LayoutInflater.from(this)
                .inflate(com.ezteam.ezpdflib.R.layout.ads_native_loading_short, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            val callback = object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    super.onNativeAdLoaded(nativeAd)

                    val layoutRes = com.ezteam.ezpdflib.R.layout.ads_native_bot_no_media_short
                    val adView = LayoutInflater.from(this@ImageActivity)
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
                findViewById(com.ezteam.ezpdflib.R.id.banner_container),
                findViewById(com.ezteam.ezpdflib.R.id.shimmer_container_banner),
                config
            )
        } else binding.bannerContainer.visibility = View.GONE

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
    override fun initView() {
        isSingleImage = intent.getBooleanExtra("isSingleImage", false)
        imagePath = intent.getStringExtra("imagePath")

        if (Locale.getDefault().language == "ar") {
            binding.toolbar.icBack.rotationY = 180f
        } else {
            binding.toolbar.icBack.rotationY = 0f
        }

        val title = if (!imagePath.isNullOrEmpty()) {
            File(imagePath!!).name
        } else {
            getString(R.string.image)
        }
        binding.toolbar.tvTitle.text = title
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
    }

    override fun initData() {
        lifecycleScope.launch {
            isSingleImage = intent.getBooleanExtra("isSingleImage", false)
            imagePath = intent.getStringExtra("imagePath")
            
            if (isSingleImage && !imagePath.isNullOrEmpty()) {
                showSingleImage()
            } else {
                // Hiển thị danh sách file như cũ
            }
        }
    }

    private fun showSingleImage() {
        binding.imageView.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE

        loadImage()
    }

    private fun loadImage() {
        imagePath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(path)
                    binding.imageView.setImageBitmap(bitmap)
                } else {
                    Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.app_error), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun rotateImage() {
        currentRotation += 90f
        if (currentRotation >= 360f) {
            currentRotation = 0f
        }
        
        binding.imageView.rotation = currentRotation
    }

    private fun shareImage() {
        imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                shareFile(file)
            } else {
                Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun shareFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val name = file.name.lowercase(Locale.getDefault())
            val mimeType = when {
                name.endsWith(".jpg") || name.endsWith(".jpeg") -> "image/jpeg"
                name.endsWith(".png") -> "image/png"
                name.endsWith(".webp") -> "image/webp"
                name.endsWith(".gif") -> "image/gif"
                name.endsWith(".bmp") -> "image/bmp"
                name.endsWith(".heic") -> "image/heic"
                name.endsWith(".pdf") -> "application/pdf"
                name.endsWith(".doc") || name.endsWith(".dot") -> "application/msword"
                name.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                name.endsWith(".xls") -> "application/vnd.ms-excel"
                name.endsWith(".xlsx") -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                name.endsWith(".ppt") -> "application/vnd.ms-powerpoint"
                name.endsWith(".pptx") -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                name.endsWith(".txt") -> "text/plain"
                else -> "application/octet-stream"
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.app_error), Toast.LENGTH_SHORT).show()
        }
    }


    private fun convertImageToPdf() {
        imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.provider",
                    file
                )

                val listUri = arrayListOf(uri.toString())
                showPopupCreatePdf(listUri)
            } else {
                Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show()
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
                            this@ImageActivity,
                            path
                        )
                        uriFile?.let {
                            val realPath =
                                PathUtils.getRealPathFromUri(this@ImageActivity, it)
                            viewModel.createFile(realPath) {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    CreateSuccessActivity.start(
                                        this@ImageActivity,
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
                Log.e("ImageActivity", "Error showing BottomSheetCreatePdf: ${e.message}")
            }
        },500)
    }

    override fun initListener() {
        binding.toolbar.icBack.setOnClickListener {
            if (FirebaseRemoteConfigUtil.getInstance().isShowAdsMain()) {
                showAdsInterstitial(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_home")){
                    finish()
                }
            } else {
                finish()
            }
        }
        binding.imageView.setOnClickListener {
            if (!isZoomed) {
                binding.toolbar.root.visibility = View.GONE
                binding.bottomSection.visibility = View.GONE
            } else {
                binding.toolbar.root.visibility = View.VISIBLE
                binding.bottomSection.visibility = View.VISIBLE
            }
            isZoomed = !isZoomed
        }
        binding.toolbar.ivSetting.setOnClickListener {
            imagePath?.let { path ->
                try {
                    val file = File(path)
                    if (file.exists()) {
                        val bottomSheetImageFunction =
                            BottomSheetImageFunction(path) {
                                onSelectedFunction(path, it)
                            }
                        bottomSheetImageFunction.show(
                            supportFragmentManager,
                            BottomSheetImageFunction::javaClass.name
                        )
                    } else {
                        Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, getString(R.string.app_error), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        binding.navigationDetail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btn_rotate -> {
                    rotateImage()
                    true
                }
                R.id.btn_image_to_pdf -> {
                    convertImageToPdf()
                    true
                }
                R.id.btn_share -> {
                    shareImage()
                    true
                }
                else -> false
            }
        }
    }

    private fun onSelectedFunction(filePath: String?, state: FunctionState) {
        when (state) {
            FunctionState.DELETE -> {
                val deleteImageDialog = DeleteImageDialog(imagePath = imagePath);
                deleteImageDialog.setOnDeletedListener {
                    finish()
                }
                try {
                    deleteImageDialog.show(supportFragmentManager, "DetailPageDialog")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("PdfBaseActivity", "Error showing DetailPageDialog: ${e.message}", e)
                }
            }

            FunctionState.RENAME -> {
                val renameImageDialog = RenameImageDialog(imagePath)
                renameImageDialog.setOnRenamedListener {
                    finish()
                }
                try {
                    renameImageDialog.show(supportFragmentManager, "RenameImageDialog")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("PdfBaseActivity", "Error showing RenameImageDialog: ${e.message}", e)
                }
            }

            FunctionState.DETAIL -> {
                val detailImageDialog = DetailImageDialog(imagePath = imagePath);
                try {
                    detailImageDialog.show(supportFragmentManager, "DetailPageDialog")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("PdfBaseActivity", "Error showing DetailPageDialog: ${e.message}", e)
                }
            }
            FunctionState.PRINT -> {
                printImage(filePath)
            }

            else -> {}
        }
    }
    fun printImage(filePath: String?) {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = getString(com.ezteam.ezpdflib.R.string.app_name)
        try {
            printManager.print(
                jobName, PdfDocumentAdapter(this, File(filePath)), null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun viewBinding(): ActivityImageBinding {
        return ActivityImageBinding.inflate(LayoutInflater.from(this))
    }

}