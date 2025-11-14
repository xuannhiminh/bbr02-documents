package officepro.document.reader.viewer.editor.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import officepro.document.reader.viewer.editor.databinding.ItemFileBinding
import officepro.document.reader.viewer.editor.model.FileModel
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import com.ezteam.baseproject.listener.EzItemListener
import com.ezteam.baseproject.utils.DateUtils
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.util.Admob
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.common.FunctionState
import java.util.Locale
import officepro.document.reader.viewer.editor.screen.func.FileFunctionPopup

class FileItemAdapter(
    context: Context,
    list: List<FileModel>,
    var onClickListener: EzItemListener<FileModel>,
    var onSelectedFuncListener: (FileModel, FunctionState) -> Unit,
    var listener: EzItemListener<FileModel>
) : BaseRecyclerAdapter<FileModel, FileItemAdapter.ViewHolder>(context, list) {


    private var isCheckMode = false
    var onSelectedCountChangeListener: ((Int) -> Unit)? = null


    inner class ViewHolder(
        var binding: ItemFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(model: FileModel) {

            if (model.isAds) {
                binding.content.visibility = View.GONE
                this.binding.layoutNative.visibility = View.VISIBLE
                val loadingView = LayoutInflater.from(mContext)
                    .inflate(R.layout.ads_native_loading_middle, null)
                binding.layoutNative.removeAllViews()
                binding.layoutNative.addView(loadingView)

                nativeAd?.let {
                    val layoutRes = R.layout.ads_native_middle_files
                    val adView = LayoutInflater.from(mContext)
                        .inflate(layoutRes, null) as NativeAdView

                    binding.layoutNative.removeAllViews()
                    binding.layoutNative.addView(adView)

                    // Gán dữ liệu quảng cáo vào view
                    Admob.getInstance().pushAdsToViewCustom(it, adView)
                    } ?: kotlin.run {
                        binding.layoutNative.removeAllViews()
                        binding.layoutNative.addView(loadingView)
                    }
                    return
                }

            binding.layoutNative.visibility = View.GONE
            binding.content.visibility = View.VISIBLE


            val fileIconRes = when {
                model.path.lowercase().endsWith(".pdf") -> R.drawable.icon_main_pdf

                model.path.lowercase().endsWith(".txt") -> R.drawable.icon_main_txt

                model.path.lowercase().endsWith(".ppt") || model.path.lowercase().endsWith(".pptx") -> R.drawable.icon_main_ppt

                model.path.lowercase().endsWith(".doc") || model.path.lowercase().endsWith(".docx") -> R.drawable.icon_main_word

                model.path.lowercase().endsWith(".xls") || model.path.lowercase().endsWith(".xlsx") || model.path.lowercase().endsWith(".xlsm") -> R.drawable.icon_main_excel
                else -> R.drawable.icon_main_pdf
            }
            binding.fileIcon.setImageResource(fileIconRes)
            binding.tvTitle.text = model.name
            binding.tvTitle.isSelected = true
            val sizeParts = model.sizeString.split(" ")
            val sizeValue = sizeParts.getOrNull(0)?.toDoubleOrNull()
            val sizeUnit = sizeParts.getOrNull(1) ?: ""
            val roundedSize = if (sizeValue != null) {
                sizeValue.toInt().toString()
            } else {
                model.sizeString
            }
            binding.tvCreateDate.text =
                "${DateUtils.longToDateString(model.date, DateUtils.DATE_FORMAT_7)} | ${"$roundedSize $sizeUnit".uppercase(Locale.ROOT)}"

            binding.parent.setOnClickListener {
                onClickListener.onListener(model)
            }

            binding.icFunc.setOnClickListener { view ->
                val fileFunctionPopup = FileFunctionPopup(view.context, model)
                fileFunctionPopup.functionListener = { state ->
                    onSelectedFuncListener.invoke(model, state)
                }
                try {
                    fileFunctionPopup.show(view)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("FileItemAdapter", "Error showing fileFunctionPopup: ${e.message}", e)
                }
            }

            val favoriteIcon = if (model.isFavorite) {
                R.drawable.icon_favourite_3
            } else {
                R.drawable.icon_favourite_2
            }
            binding.ivFavorite.setImageResource(favoriteIcon)

            binding.ivFavorite.setOnClickListener {
                listener.onListener(model)
                notifyItemChanged(adapterPosition)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFileBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }
    fun toggleCheckMode(isCheckMode: Boolean) {
        this.isCheckMode = isCheckMode
//        notifyDataSetChanged()
    }
    private val selectedFilePaths = mutableSetOf<String>()

    // Override setList to update selection when list changes
    override fun setList(list: List<FileModel>) {
        // Remove selected paths that no longer exist in the new list
        val newListPaths = list.map { it.path }.toSet()
        selectedFilePaths.removeAll { it !in newListPaths }

        super.setList(list)
        onSelectedCountChangeListener?.invoke(selectedFilePaths.size)

        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = list[position]

        holder.bindData(file)
        holder.binding.selectCheckbox.visibility = if (isCheckMode) View.VISIBLE else View.GONE

        // Cập nhật trạng thái checkbox dựa trên danh sách chọn (skip ads)
        val isSelected = !file.isAds && selectedFilePaths.contains(file.path)
        holder.binding.selectCheckbox.isSelected = isSelected

        holder.binding.selectCheckbox.setOnClickListener {
            // Don't allow selection of ads
            if (file.isAds) {
                return@setOnClickListener
            }

            it.isSelected = !it.isSelected
            if (it.isSelected) {
                selectedFilePaths.add(file.path)
            } else {
                selectedFilePaths.remove(file.path)
            }
            onSelectedCountChangeListener?.invoke(selectedFilePaths.size)
        }


    }
    fun getSelectedFiles(): List<FileModel> {
        return list.filter { selectedFilePaths.contains(it.path) }
    }
    // Hàm chọn tất cả
    fun selectAll() {
        selectedFilePaths.clear()
        // Only select non-ads files
        list.forEach { file ->
            if (!file.isAds) {
                selectedFilePaths.add(file.path)
            }
        }
        onSelectedCountChangeListener?.invoke(selectedFilePaths.size)
        notifyDataSetChanged()
    }

    // Hàm bỏ chọn tất cả
    fun deselectAll() {
        selectedFilePaths.clear()
        onSelectedCountChangeListener?.invoke(selectedFilePaths.size)
        notifyDataSetChanged()
    }

    // Get count of selectable items (non-ads files)
    fun getSelectableItemCount(): Int {
        return list.count { !it.isAds }
    }
}
