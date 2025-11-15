package office.pdf.document.reader.viewer.editor.screen.func

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import office.pdf.document.reader.viewer.editor.R
import office.pdf.document.reader.viewer.editor.common.FunctionState
import office.pdf.document.reader.viewer.editor.common.PresKey
import office.pdf.document.reader.viewer.editor.databinding.BottomSheetDetailsFuncBinding
import office.pdf.document.reader.viewer.editor.model.FileModel
import com.ezteam.baseproject.listener.EzItemListener
import com.ezteam.baseproject.utils.DateUtils
import com.brian.base_iap.utils.PreferencesUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetDetailFunction(
    var fileModel: FileModel?,
    var listener: EzItemListener<FunctionState>
) : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetDetailsFuncBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetDetailsFuncBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
    }


    private fun initViews() {
        fileModel?.let {
            binding.tvTitle.text = it.name
            @SuppressLint("SetTextI18n")
            binding.tvCreateDate.text = "${DateUtils.longToDateString(it.date, DateUtils.DATE_FORMAT_7)} - ${it.sizeString}"

            if (it.isFavorite) {
                binding.funcAddFavorite.iconResId = R.drawable.ic_remove_favorite
                binding.funcAddFavorite.title = resources.getString(R.string.remove_from_fav)
            } else {
                binding.funcAddFavorite.iconResId = R.drawable.ic_add_favorite
                binding.funcAddFavorite.title = resources.getString(R.string.add_in_fav)
            }

            if (!it.fromDatabase) {
                binding.funcCommons.isVisible = false
                binding.itemView.isVisible = false
            }

            binding.funcPrint.isVisible = !it.hasPassword
        } ?: kotlin.run {
            binding.funcCommons.isVisible = false
            binding.itemView.isVisible = false
        }

        val isHorizontal = PreferencesUtils.getBoolean(PresKey.PDF_VIEWER_HORIZONTAL, false)
        binding.funcContinuousPage.isFuncSelected = !isHorizontal
        binding.funcPageByPage.isFuncSelected = isHorizontal

        val nightModeEnable = PreferencesUtils.getBoolean(PresKey.PDF_VIEWER_NIGHT_MODE, false)
        binding.nightMoveView.swNightMode.isChecked = nightModeEnable
    }

    private fun initListener() {
        binding.funcContinuousPage.setOnClickListener {
            listener.onListener(FunctionState.CONTINUOUS_PAGE)
            dismiss()
        }

        binding.funcPageByPage.setOnClickListener {
            listener.onListener(FunctionState.PAGE_BY_PAGE)
            dismiss()
        }

        binding.nightMoveView.funcNightMode.setOnClickListener {
            listener.onListener(FunctionState.NIGHT_MODE)
            dismiss()
        }

        binding.nightMoveView.swNightMode.setOnCheckedChangeListener { _, _ ->
            listener.onListener(FunctionState.NIGHT_MODE)
            dismiss()
        }

        binding.funcGoToPage.setOnClickListener {
            listener.onListener(FunctionState.GO_PAGE)
            dismiss()
        }

        binding.funcAddFavorite.setOnClickListener {
            listener.onListener(FunctionState.FAVORITE)
            dismiss()
        }

        binding.funcRename.setOnClickListener {
            listener.onListener(FunctionState.RENAME)
            dismiss()
        }

        binding.funcPrint.setOnClickListener {
            listener.onListener(FunctionState.PRINT)
            dismiss()
        }

        binding.funcDelete.setOnClickListener {
            listener.onListener(FunctionState.DELETE)
            dismiss()
        }

        binding.funcInfo.setOnClickListener {
            listener.onListener(FunctionState.DETAIL)
            dismiss()
        }
    }
}