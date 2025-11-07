package officepro.document.reader.viewer.editor.screen.file

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.ezteam.baseproject.utils.PDFConstants.Companion.ADS_ITEM_INDEX
import com.google.android.gms.ads.nativead.NativeAd
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.adapter.FileItemAdapter
import officepro.document.reader.viewer.editor.common.FileTab
import officepro.document.reader.viewer.editor.common.FunctionState
import officepro.document.reader.viewer.editor.common.LoadingState
import officepro.document.reader.viewer.editor.databinding.FragmentListFileBinding
import officepro.document.reader.viewer.editor.model.FileModel
import officepro.document.reader.viewer.editor.screen.base.IAdsControl
import officepro.document.reader.viewer.editor.screen.base.PdfBaseActivity
import officepro.document.reader.viewer.editor.screen.base.PdfBaseFragment
import officepro.document.reader.viewer.editor.screen.main.MainViewModel
import officepro.document.reader.viewer.editor.screen.start.SplashActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FilenameUtils
import org.koin.android.ext.android.inject


open class ListFavouriteAndRecentFragment(private val filesLiveData: LiveData<List<FileModel>>) : PdfBaseFragment<FragmentListFileBinding>(),
    IAdsControl {
    protected val viewModel by inject<MainViewModel>()
    private lateinit var adapter: FileItemAdapter
  //  private val sharedViewModel by MainViewModel<SharedViewModel>()

    override fun initView() {
        adapter =
            FileItemAdapter(requireContext(), mutableListOf(), ::onItemClick, ::onSelectedFunc, ::onReactFavorite )
        binding.rcvListFile.adapter = adapter
    }


    override fun initData() {
        lifecycleScope.launch {
            filesLiveData.observe(requireActivity()) { it ->
                adapter.setList(it)
//                if(!IAPUtils.isPremium() && viewModel.currentAdsFilesStatus.value?.currentStatusAdsFiles == true &&
//                    (it.size == 1 || (it.isNotEmpty() &&  !adapter.getList()[ADS_ITEM_INDEX].isAds)))
//                    adapter.addAds( FileModel().apply { isAds = true }, ADS_ITEM_INDEX)
                adapter.notifyDataSetChanged()
                val handler = Handler(Looper.getMainLooper())
                val runnable = {
                    if(filesLiveData.value.isNullOrEmpty()){
                        binding.layoutEmpty.alpha = 0f
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.layoutEmpty.animate().alpha(1f).setDuration(600).start()
                        binding.animationView.playAnimation()
                    }

                }
                if (it.isEmpty()) {
                    handler.postDelayed(runnable, 600)
                } else {
                    handler.removeCallbacks(runnable)
                    binding.layoutEmpty.visibility = View.GONE
                    binding.animationView.cancelAnimation()
                }
            }
        }
        viewModel.currentAdsFilesStatus.observe(this) { currentStatusAdsFiles ->
            if (currentStatusAdsFiles.currentStatusAdsFiles) {
              onNativeAdLoaded(currentStatusAdsFiles.nativeAd)
            } else {
                onAdFailedToLoad()
            }
        }
    }
    override fun initListener() {
        viewModel.loadingObservable.observe(this) {
            when (it) {
                LoadingState.START -> {
                    binding.rcvListFile.visibility = View.GONE
                    binding.layoutLoadingFile.visibility = View.VISIBLE
                    binding.animationLoadingView.playAnimation()
                }
                LoadingState.FINISH -> {
                    binding.rcvListFile.visibility = View.VISIBLE
                    binding.layoutLoadingFile.visibility = View.GONE
                    binding.animationLoadingView.cancelAnimation()

                }

                else -> {}
            }
        }
    }
    protected open fun fromTab(): FileTab {
        return FileTab.ALL_FILE
    }

    private fun onItemClick(fileModel: FileModel) {
        openFile(fileModel)
    }

    private fun onSelectedFunc(fileModel: FileModel, state: FunctionState) {
        onSelectedFunction(fileModel, state)
    }

    private fun onReactFavorite(fileModel: FileModel) {
        fileModel.isFavorite = !fileModel.isFavorite
        viewModel.reactFavorite(fileModel)
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

            FunctionState.CREATE_SHORTCUT -> {
                val intent = Intent(requireContext(), SplashActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    type = "application/pdf"
                    addCategory(Intent.CATEGORY_DEFAULT)
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    putExtra(SplashActivity.FILE_PATH, fileModel.path)
                }
                val shortcut = ShortcutInfoCompat.Builder(requireContext(), fileModel.path)
                    .setShortLabel(FilenameUtils.getBaseName(fileModel.path))
                    .setLongLabel(FilenameUtils.getBaseName(fileModel.path))
                    .setIcon(
                        IconCompat.createWithResource(
                            requireContext(),
                            R.drawable.ic_pdf
                        )
                    )
                    .setIntent(
                        intent
                    )
                    .build()

                ShortcutManagerCompat.requestPinShortcut(requireContext(), shortcut, null)
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
               showDetailFile(fileModel)
            }

            else -> {}
        }
    }
    fun showDetailFile(fileModel: FileModel) {
        (requireActivity() as PdfBaseActivity<*>).showDetailFile(fileModel, viewModel)
    }
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentListFileBinding {
        return FragmentListFileBinding.inflate(inflater, container, false)
    }

    override fun onNativeAdLoaded(nativeAd: NativeAd?) {
        if (::adapter.isInitialized) {
            adapter.nativeAd = nativeAd
            adapter.notifyItemChanged(ADS_ITEM_INDEX)
        }
    }

    override fun onAdFailedToLoad() {
        if (::adapter.isInitialized) {
            if (adapter.getList().size > ADS_ITEM_INDEX && adapter.getList()[ADS_ITEM_INDEX].isAds) {
                adapter.getList().removeAt(ADS_ITEM_INDEX)
                adapter.notifyItemRemoved(ADS_ITEM_INDEX)
            }
        }
    }
}