package office.pdf.document.reader.viewer.editor.screen.create

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import office.pdf.document.reader.viewer.editor.R
import office.pdf.document.reader.viewer.editor.databinding.ActivityCreateSuccessBinding
import office.pdf.document.reader.viewer.editor.model.FileModel
import office.pdf.document.reader.viewer.editor.screen.base.PdfBaseActivity
import office.pdf.document.reader.viewer.editor.screen.main.MainViewModel
import com.ezteam.baseproject.extensions.autoRotate
import com.ezteam.baseproject.extensions.resizeBitmapByCanvas
import com.brian.base_iap.utils.FirebaseRemoteConfigUtil
import com.brian.base_iap.utils.IAPUtils
import com.ezteam.baseproject.utils.SystemUtils
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
//import com.google.android.gms.ads.ez.listenner.NativeAdListener
//import com.google.android.gms.ads.ez.nativead.AdmobNativeAdView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.lang.Exception

class CreateSuccessActivity : PdfBaseActivity<ActivityCreateSuccessBinding>() {
    companion object {
        const val FILE_MODEL = "file_model"
        const val PAGE_NUMBER = "page_number"
        const val URI_PREVIEW = "uri_preview"
        fun start(
            activity: FragmentActivity,
            fileModel: FileModel,
            numberPage: Int,
            uriPreview: String
        ) {
            val intent = Intent(activity, CreateSuccessActivity::class.java).apply {
                putExtra(FILE_MODEL, fileModel)
                putExtra(PAGE_NUMBER, numberPage)
                putExtra(URI_PREVIEW, uriPreview)
            }
            activity.startActivity(intent)
        }
    }

    private val viewModel by inject<MainViewModel>()

    private val fileModel: FileModel by lazy {
        intent?.getSerializableExtra(FILE_MODEL) as FileModel
    }

    private val pageNumber by lazy {
        intent?.getIntExtra(PAGE_NUMBER, 0)
    }

    private val uriPreview by lazy {
        intent?.getStringExtra(URI_PREVIEW)
    }
    private fun loadNativeNomedia() {
        if (IAPUtils.isPremium()) {
            binding.layoutNative.visibility = View.GONE
            return
        }
        if (SystemUtils.isInternetAvailable(this)) {
            binding.layoutNative.visibility = View.VISIBLE
            val loadingView = LayoutInflater.from(this)
                .inflate(R.layout.ads_native_bot, null)
            binding.layoutNative.removeAllViews()
            binding.layoutNative.addView(loadingView)

            val callback = object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    super.onNativeAdLoaded(nativeAd)

                    val layoutRes = R.layout.ads_native_bot_loading
                    val adView = LayoutInflater.from(this@CreateSuccessActivity)
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
    override fun initView() {
        binding.toolbar.tvTitle.text = resources.getString(R.string.app_name)
        binding.tvNumberPage.text = pageNumber.toString()
        binding.tvFileName.text = fileModel.name
        binding.tvPath.text = fileModel.path
        loadNativeNomedia()
        uriPreview?.let {
            val uri = Uri.parse(it)
            try {
                val launch = lifecycleScope.launch(Dispatchers.IO) {
                    var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    bitmap?.let { bitmap ->
                        var bmtDisplay = bitmap
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            bmtDisplay = bitmap.autoRotate(this@CreateSuccessActivity)
                        }
                        bmtDisplay = bmtDisplay.resizeBitmapByCanvas(300.0f, 300.0f)
                        lifecycleScope.launch(Dispatchers.Main) {
                            Glide.with(this@CreateSuccessActivity)
                                .load(bmtDisplay)
                                .into(binding.ivPreview)
                        }
                    }
                }
                launch
            } catch (e: Exception) {
            }
        }

    }

//    override fun initData() {
//        TODO("Not yet implemented")
//    }

    override fun initData() {
//        AdmobNativeAdView.getNativeAd(
//            this,
//            R.layout.native_admod_home,
//            object : NativeAdListener() {
//                override fun onError() {
//                }
//
//                override fun onLoaded(nativeAd: RelativeLayout?) {
//                    binding.adsView.addView(nativeAd)
//                }
//
//                override fun onClickAd() {
//                }
//
//            })
    }

    override fun initListener() {
        binding.toolbar.icBack.setOnClickListener {
            showAdsInterstitial(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_createpdf")) {
                finish()
            }
        }

        binding.buttonShare.setOnClickListener {
            shareFile(fileModel)
        }

        binding.buttonOpen.setOnClickListener {
            openFile(fileModel)
            finish()
        }

        binding.icEdit.setOnClickListener {
            fileModel.name?.let {
                showRenameFile(it) { newName ->
                    viewModel.renameFile(fileModel, newName, onFail = {
                        toast(resources.getString(R.string.rename_unsuccessful))
                    }, onSuccess = {
                        lifecycleScope.launch {
                            binding.tvFileName.text = newName
                        }
                    })
                }
            }
        }
    }

    override fun viewBinding(): ActivityCreateSuccessBinding {
        return ActivityCreateSuccessBinding.inflate(LayoutInflater.from(this))
    }
}