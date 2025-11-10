package officepro.document.reader.viewer.editor.screen.search

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.ezteam.baseproject.utils.FirebaseRemoteConfigUtil
import com.ezteam.baseproject.utils.IAPUtils
import com.ezteam.baseproject.utils.PDFConstants.Companion.ADS_ITEM_INDEX
import com.ezteam.baseproject.utils.SystemUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.adapter.FileItemAdapter
import officepro.document.reader.viewer.editor.common.FileTab
import officepro.document.reader.viewer.editor.common.FunctionState
import officepro.document.reader.viewer.editor.databinding.ActivityCheckFileBinding
import officepro.document.reader.viewer.editor.model.FileModel
import officepro.document.reader.viewer.editor.screen.base.PdfBaseActivity
import officepro.document.reader.viewer.editor.screen.main.MainViewModel
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import officepro.document.reader.viewer.editor.common.BottomTab
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.Locale

class SelectMultipleFilesActivity : PdfBaseActivity<ActivityCheckFileBinding>() {
    private val viewModel by inject<MainViewModel>()
    private lateinit var adapter: FileItemAdapter
    private var fileTab: FileTab = FileTab.ALL_FILE
    companion object {
        fun start(activity: FragmentActivity, fileTab: FileTab, bottomTab: BottomTab = BottomTab.HOME) {
            val intent = Intent(activity, SelectMultipleFilesActivity::class.java)
            intent.putExtra("FileTab", fileTab)
            intent.putExtra("BottomTab", bottomTab)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        EzAdControl.getInstance(this).showAds()
    }

    override fun onStart() {
        super.onStart()
        loadNativeNomedia()
        if (TemporaryStorage.isLoadAds) {
            loadNativeAdsMiddleFiles()
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    val callback = object : NativeCallback() {
        override fun onNativeAdLoaded(nativeAd: NativeAd?) {
            if (nativeAd != null) {
                this@SelectMultipleFilesActivity.onNativeAdLoaded(nativeAd)
            } else {
                this@SelectMultipleFilesActivity.onAdFailedToLoad()
            }
        }
        override fun onAdFailedToLoad() {
            this@SelectMultipleFilesActivity.onAdFailedToLoad()
        }
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
                    val adView = LayoutInflater.from(this@SelectMultipleFilesActivity)
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
                FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("native_bot_selectfiles"),
                callback
            )
        } else {
            binding.layoutNative.visibility = View.GONE
        }
    }
    private fun loadNativeAdsMiddleFiles() {
        Admob.getInstance().loadNativeAd(
            applicationContext,
            FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("native_between_files_selectfiles"),
            callback
        )
    }
    override fun initView() {
        adapter = FileItemAdapter(this, mutableListOf(), ::onItemClick, ::onSelectedFunc, ::onReactFavorite)
        adapter.toggleCheckMode(true)
        binding.rcvListFile.adapter = adapter
        updateNavMenuState(false)

        adapter.onSelectedCountChangeListener = { count ->
            binding.tvTotalFiles.text = "$count "

            val enabled = count > 0
            updateNavMenuState(enabled)
            binding.toolbar.checkboxAll.isSelected = count == adapter.itemCount
        }
        if (Locale.getDefault().language == "ar") {
            binding.toolbar.ivBack.rotationY = 180f
        } else {
            binding.toolbar.ivBack.rotationY = 0f
        }
    }

    private lateinit var bottomTab: BottomTab

    override fun initData() {
        lifecycleScope.launch {
            fileTab = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("FileTab", FileTab::class.java)
            } else {
                intent.getSerializableExtra("FileTab") as? FileTab
            } ?: FileTab.ALL_FILE // hoặc giá trị mặc định

            bottomTab = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("BottomTab", BottomTab::class.java)
            } else {
                intent.getSerializableExtra("BottomTab") as? BottomTab
            } ?: BottomTab.HOME
            val liveData = if (bottomTab ==  BottomTab.HOME) {
                viewModel.getListFileBaseOnFileTab(fileTab)
            } else {
                viewModel.allFilesLiveData
            }
            liveData.observe(this@SelectMultipleFilesActivity) {
                adapter.setList(it)
                adapter.notifyDataSetChanged()

                if (it.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.recentlyAddedSection.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.GONE
                    binding.recentlyAddedSection.visibility = View.VISIBLE
                }
            }
        }
        val fileIcon = when (fileTab) {
            FileTab.PDF -> R.drawable.icon_main_pdf
            FileTab.WORD -> R.drawable.icon_main_word
            FileTab.PPT ->  R.drawable.icon_main_ppt
            FileTab.EXCEL ->  R.drawable.icon_main_excel
            FileTab.TXT ->  R.drawable.icon_main_txt
            else ->  R.drawable.icon_main_all
        }
        val fileName = when (fileTab) {
            FileTab.PDF -> R.string.pdf
            FileTab.WORD -> R.string.word
            FileTab.PPT ->  R.string.ppt
            FileTab.EXCEL ->  R.string.excel
            FileTab.TXT ->  R.string.txt
            else ->  R.string.all
        }

        when (bottomTab) {
            BottomTab.RECENT -> {
                binding.toolbar.tvIcon.visibility = View.GONE
                binding.toolbar.tvTitle.text = getString(R.string.select_from_recent)
                binding.buttonFavouriteContainer.visibility = View.GONE
                binding.buttonRecentContainer.visibility = View.VISIBLE
            }
            BottomTab.FAVORITE -> {
                binding.toolbar.tvIcon.visibility = View.GONE
                binding.toolbar.tvTitle.text = getString(R.string.select_from_mark)
                binding.buttonFavouriteContainer.visibility = View.VISIBLE
                binding.buttonRecentContainer.visibility = View.GONE
            }
            else -> {
                binding.toolbar.tvIcon.visibility = View.VISIBLE
                binding.toolbar.tvIcon.setImageResource(fileIcon)
                binding.toolbar.tvTitle.setText(fileName)
                binding.buttonFavouriteContainer.visibility = View.GONE
                binding.buttonRecentContainer.visibility = View.VISIBLE
            }
        }
        val fileType = when (fileTab) {
            FileTab.PDF -> "PDF"
            FileTab.WORD -> "WORD"
            FileTab.PPT ->  "PPT"
            FileTab.EXCEL ->  "EXCEL"
            FileTab.TXT ->  "TXT"
            else ->  "ALL"
        }
        viewModel.loadTotalFiles(fileType).observe(this) { totalNumber ->
            val recentlyAddedSection = binding.recentlyAddedSection
            val params = recentlyAddedSection.layoutParams as ConstraintLayout.LayoutParams

            if (totalNumber > 11) {
                params.height = 0
                params.bottomToTop = binding.bottomSection.id
            } else {
                params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                params.bottomToTop = ConstraintLayout.LayoutParams.UNSET
            }
            recentlyAddedSection.layoutParams = params
        }
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

        binding.toolbar.checkboxAll.setOnClickListener {
            it.isSelected = !it.isSelected
            if (it.isSelected) {
                adapter.selectAll()
            } else {
                adapter.deselectAll()
            }
        }

        binding.btnShare.setOnClickListener {
            val selectedFiles = adapter.getSelectedFiles()
            if (selectedFiles.isNotEmpty()) {
                shareFiles(selectedFiles)
            } else {
                Toast.makeText(this, getString(R.string.please_choose_file), Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnShareFavourite.setOnClickListener {
            val selectedFiles = adapter.getSelectedFiles()
            if (selectedFiles.isNotEmpty()) {
                shareFiles(selectedFiles)
            } else {
                Toast.makeText(this, getString(R.string.please_choose_file), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDelete.setOnClickListener {
            val selectedFiles = adapter.getSelectedFiles()
            if (selectedFiles.isNotEmpty()) {
                showDialogConfirm(
                    resources.getString(R.string.delete),
                    getString(R.string.delete_all)
                ) {
                    viewModel.deleteFiles(selectedFiles) {
                        toast(resources.getString(R.string.delete_successfully))
                    }
                    adapter.deselectAll()
                }
            } else {
                Toast.makeText(this, getString(R.string.please_choose_file), Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnDeleteFavourite.setOnClickListener {
            val selectedFiles = adapter.getSelectedFiles()
            if (selectedFiles.isNotEmpty()) {
                showDialogConfirm(
                    resources.getString(R.string.delete),
                    getString(R.string.delete_all)
                ) {
                    viewModel.deleteFiles(selectedFiles) {
                        toast(resources.getString(R.string.delete_successfully))
                    }
                    adapter.deselectAll()
                }
            } else {
                Toast.makeText(this, getString(R.string.please_choose_file), Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnRemoveMark.setOnClickListener {
            val selectedFiles = adapter.getSelectedFiles()
            if (selectedFiles.isNotEmpty()) {
                showDialogRemove(
                    resources.getString(R.string.are_you_sure),
                    getString(R.string.remove_mark_content)
                ) {
                    viewModel.removeFavouriteFiles(selectedFiles)
                    adapter.deselectAll()
                }
            } else {
                Toast.makeText(this, getString(R.string.please_choose_file), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }
    private fun updateNavMenuState(enabled: Boolean) {
        binding.btnShare.isEnabled = enabled
        binding.btnDelete.isEnabled = enabled
        binding.btnShareFavourite.isEnabled = enabled
        binding.btnDeleteFavourite.isEnabled = enabled
        binding.btnRemoveMark.isEnabled = enabled

        val colorEnabled = resources.getColor(R.color.text1, theme)
        val colorDisabled = resources.getColor(R.color.textInput, theme)

        val colorDangerEnabled = resources.getColor(R.color.error, theme)
        val colorDangerDisabled = resources.getColor(R.color.error_cancel, theme)

        val colorShareEnabled = Color.parseColor("#397FF8")
        val colorShareDisabled = Color.parseColor("#BEDAFF")

        fun setTextAndIconColor(textView: TextView, imageView: ImageView, enabledColor: Int, disabledColor: Int) {
            val color = if (enabled) enabledColor else disabledColor
            textView.setTextColor(color)
            imageView.setColorFilter(color)
        }

        setTextAndIconColor(binding.tvText, binding.ivIcon, colorEnabled, colorDisabled)
        setTextAndIconColor(binding.tvText4, binding.ivIcon4, colorEnabled, colorDisabled)
        setTextAndIconColor(binding.tvText5, binding.ivIcon5, colorDangerEnabled, colorDangerDisabled)

        fun setButtonBackground(button: View, enabledColor: Int, disabledColor: Int) {
            (button.background as? GradientDrawable)?.setColor(
                if (enabled) enabledColor else disabledColor
            )
        }

        setButtonBackground(binding.btnShare, colorShareEnabled, colorShareDisabled)
        setButtonBackground(binding.btnShareFavourite, colorShareEnabled, colorShareDisabled)
    }



    private fun onItemClick(fileModel: FileModel) {
        openFile(fileModel)
    }
    private fun onReactFavorite(fileModel: FileModel) {
        fileModel.isFavorite = !fileModel.isFavorite
        viewModel.reactFavorite(fileModel)
    }
    private fun onSelectedFunc(fileModel: FileModel, state: FunctionState) {
        onSelectedFunction(fileModel, state)
    }

    private fun onSelectedFunction(fileModel: FileModel, state: FunctionState) {
        when (state) {
            FunctionState.SHARE -> {
                shareFile(fileModel)
            }

            FunctionState.FAVORITE -> {
                fileModel.isFavorite = !fileModel.isFavorite
                viewModel.reactFavorite(fileModel)
            }


            FunctionState.RENAME -> {
                fileModel.name?.let {
                    showRenameFile(it) { newName ->
                        viewModel.renameFile(fileModel, newName, onFail = {
                            toast(resources.getString(R.string.rename_unsuccessful))
                        })
                    }
                }
            }


            FunctionState.DELETE -> {
                showDialogConfirm(
                    resources.getString(R.string.delete),
                    String.format(resources.getString(R.string.del_message), fileModel.name)
                ) {
                    viewModel.deleteFile(fileModel) {
                        toast(resources.getString(R.string.delete_successfully))
                    }
                }
            }

            FunctionState.DETAIL -> {
                showDetailFile(fileModel, viewModel)
            }

            else -> {}
        }
    }

    override fun viewBinding(): ActivityCheckFileBinding {
        return ActivityCheckFileBinding.inflate(LayoutInflater.from(this))
    }

     fun onNativeAdLoaded(nativeAd: NativeAd?) {
        if (::adapter.isInitialized) {
            adapter.nativeAd = nativeAd
            adapter.notifyItemChanged(ADS_ITEM_INDEX)
        }
    }

     fun onAdFailedToLoad() {
        if (::adapter.isInitialized) {
            if (adapter.getList().size > ADS_ITEM_INDEX && adapter.getList()[ADS_ITEM_INDEX].isAds) {
                adapter.getList().removeAt(ADS_ITEM_INDEX)
                adapter.notifyItemRemoved(ADS_ITEM_INDEX)
            }
        }
    }
}