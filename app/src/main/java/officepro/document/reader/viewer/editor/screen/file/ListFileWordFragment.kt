package officepro.document.reader.viewer.editor.screen.file

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.brian.base_iap.utils.IAPUtils
import com.ezteam.baseproject.utils.PDFConstants.Companion.ADS_ITEM_INDEX
import com.google.android.gms.ads.nativead.NativeAd
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.adapter.FileItemAdapter
import officepro.document.reader.viewer.editor.common.FunctionState
import officepro.document.reader.viewer.editor.common.LoadingState
import officepro.document.reader.viewer.editor.databinding.FragmentListFileTypeBinding
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


open class ListFileWordFragment(private val filesLiveData: LiveData<List<FileModel>>) : PdfBaseFragment<FragmentListFileTypeBinding>(),
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
            filesLiveData.observe(requireActivity()) { files ->
                adapter.setList(files)
                adapter.notifyDataSetChanged()

                if (files.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.recentlyAddedSection.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.GONE
                    binding.recentlyAddedSection.visibility = View.VISIBLE
                }
            }
        }
        viewModel.loadTotalFiles("WORD").observe(this) { totalNumber ->
            binding.tvTotalFiles.text = "$totalNumber "
            val recentlyAddedSection = binding.recentlyAddedSection
            val params = recentlyAddedSection.layoutParams as ConstraintLayout.LayoutParams

            if (totalNumber > 9) {
                params.height = 0
                params.bottomToTop = binding.bottomSection.id
            } else {
                params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                params.bottomToTop = ConstraintLayout.LayoutParams.UNSET
            }
            recentlyAddedSection.layoutParams = params
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
    ): FragmentListFileTypeBinding {
        return FragmentListFileTypeBinding.inflate(inflater, container, false)
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