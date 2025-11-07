package officepro.document.reader.viewer.editor.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import officepro.document.reader.viewer.editor.databinding.ItemFileBinding
import officepro.document.reader.viewer.editor.model.FileModel
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import com.ezteam.baseproject.listener.EzItemListener
import com.ezteam.baseproject.utils.DateUtils
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.common.FunctionState
import officepro.document.reader.viewer.editor.screen.func.FileFunctionPopup
import java.util.Locale

class RecentFileAdapter(
    context: Context,
    list: List<FileModel>,
    var onClickListener: EzItemListener<FileModel>,
    var onSelectedFuncListener: (FileModel, FunctionState) -> Unit,
    var listener: EzItemListener<FileModel>
) : BaseRecyclerAdapter<FileModel, RecentFileAdapter.ViewHolder>(context, list) {

    inner class ViewHolder(
        var binding: ItemFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(model: FileModel) {
            val fileIconRes = when {
                model.path.lowercase().endsWith(".pdf") -> officepro.document.reader.viewer.editor.R.drawable.icon_main_pdf
                model.path.lowercase().endsWith(".txt") -> officepro.document.reader.viewer.editor.R.drawable.icon_main_txt
                model.path.lowercase().endsWith(".ppt") || model.path.lowercase().endsWith(".pptx") -> officepro.document.reader.viewer.editor.R.drawable.icon_main_ppt
                model.path.lowercase().endsWith(".doc") || model.path.lowercase().endsWith(".docx") -> officepro.document.reader.viewer.editor.R.drawable.icon_main_word
                model.path.lowercase().endsWith(".xls") || model.path.lowercase().endsWith(".xlsx") || model.path.lowercase().endsWith(".xlsm") -> officepro.document.reader.viewer.editor.R.drawable.icon_main_excel
                else -> officepro.document.reader.viewer.editor.R.drawable.icon_main_pdf
            }
            binding.selectCheckbox.visibility = View.GONE
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = list[position]
        holder.bindData(file)
        holder.bindData(list[position])

    }
    
    fun updateList(newList: List<FileModel>) {
        setList(newList)
        notifyDataSetChanged()
    }
}
