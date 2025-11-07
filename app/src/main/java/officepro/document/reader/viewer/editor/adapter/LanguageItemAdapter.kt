package officepro.document.reader.viewer.editor.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import officepro.document.reader.viewer.editor.common.LocaleManager
import officepro.document.reader.viewer.editor.databinding.ItemLanguageBinding
import officepro.document.reader.viewer.editor.model.LanguageModel
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter

class LanguageItemAdapter(
    context: Context,
    list: List<LanguageModel>,
    var listener: (LanguageModel) -> Unit
) : BaseRecyclerAdapter<LanguageModel, LanguageItemAdapter.ViewHolder>(context, list) {
    class ViewHolder(
        var binding: ItemLanguageBinding,
        var listener: (LanguageModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(model: LanguageModel) {
            val currentCode = LocaleManager.language
            binding.radioButton.isChecked = model.languageCode == currentCode
            binding.tvCountry.text = model.languageName

            binding.radioButton.setOnCheckedChangeListener { _, _ ->
                listener.invoke(model)
            }

            binding.root.setOnClickListener {
                listener.invoke(model)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLanguageBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, listener)
    }
}