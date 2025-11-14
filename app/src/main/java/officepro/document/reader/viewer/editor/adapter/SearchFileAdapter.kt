package officepro.document.reader.viewer.editor.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import officepro.document.reader.viewer.editor.databinding.ItemFileBinding
import officepro.document.reader.viewer.editor.model.FileModel
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import com.ezteam.baseproject.listener.EzItemListener
import com.ezteam.baseproject.utils.DateUtils
import com.brian.base_iap.utils.IAPUtils
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.ItemSearchFileBinding
import java.util.Locale

class SearchFileAdapter(
    private val context: Context,   // <- để dùng trong toàn adapter
    list: List<FileModel>,
    var onClickListener: EzItemListener<FileModel>,
    var onSelectedFuncListener: EzItemListener<FileModel>,
    var listener: EzItemListener<FileModel>
) : BaseRecyclerAdapter<FileModel, SearchFileAdapter.ViewHolder>(context, list) {



    private var isCheckMode = false
    var onSelectedCountChangeListener: ((Int) -> Unit)? = null
    private var searchQuery: String = ""



    inner class ViewHolder(
        var binding: ItemSearchFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(model: FileModel) {
            val fileIconRes = when {
                model.path.lowercase().endsWith(".pdf") -> R.drawable.icon_main_pdf
                model.path.lowercase().endsWith(".pdf") -> R.drawable.icon_main_txt

                model.path.lowercase().endsWith(".ppt") || model.path.lowercase().endsWith(".pptx") -> R.drawable.icon_main_ppt

                model.path.lowercase().endsWith(".doc") || model.path.lowercase().endsWith(".docx") -> R.drawable.icon_main_word

                model.path.lowercase().endsWith(".xls") || model.path.lowercase().endsWith(".xlsx") || model.path.lowercase().endsWith(".xlsm") -> R.drawable.icon_main_excel
                else -> R.drawable.icon_main_pdf
            }
           // binding.tvTitle.text = model.name
            binding.tvTitle.text = highlightText(model.name ?: "", searchQuery)
            binding.tvTitle.isSelected = true
            val sizeParts = model.sizeString.split(" ")
            val sizeValue = sizeParts.getOrNull(0)?.toDoubleOrNull()
            val sizeUnit = sizeParts.getOrNull(1) ?: ""
            val roundedSize = if (sizeValue != null) {
                sizeValue.toInt().toString()
            } else {
                model.sizeString
            }

            binding.parent.setOnClickListener {
                onClickListener.onListener(model)
            }


            val favoriteIcon = if (model.isFavorite) {
                R.drawable.icon_favourite_3
            } else {
                R.drawable.icon_favourite_2
            }
        }
    }
    fun setSearchQuery(query: String) {
        searchQuery = query
        notifyDataSetChanged()
    }


    fun highlightText(fullText: String, query: String): SpannableString {
        val spannable = SpannableString(fullText)
        if (query.isEmpty()) return spannable

        val startIndex = fullText.lowercase().indexOf(query.lowercase())
        if (startIndex >= 0) {
            val endIndex = startIndex + query.length

            // In đậm
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val color = ContextCompat.getColor(context, R.color.text1)
            spannable.setSpan(
                android.text.style.ForegroundColorSpan(color),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchFileBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }
    fun toggleCheckMode(isCheckMode: Boolean) {
        this.isCheckMode = isCheckMode
//        notifyDataSetChanged()
    }
    private val chosenPositions = mutableSetOf<Int>()

override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val file = list[position]

    holder.bindData(file)

}
    fun getSelectedFiles(): List<FileModel> {
        return chosenPositions.map { list[it] }
    }
    // Hàm chọn tất cả
    fun selectAll() {
        chosenPositions.clear()
        for (i in list.indices) {
            chosenPositions.add(i)
        }
        onSelectedCountChangeListener?.invoke(chosenPositions.size)
        notifyDataSetChanged()
    }

    // Hàm bỏ chọn tất cả
    fun deselectAll() {
        chosenPositions.clear()
        onSelectedCountChangeListener?.invoke(chosenPositions.size)
        notifyDataSetChanged()
    }
}
