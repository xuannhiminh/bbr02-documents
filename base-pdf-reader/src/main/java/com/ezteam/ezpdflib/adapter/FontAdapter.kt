package com.ezteam.ezpdflib.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.databinding.LibItemFontBinding

class FontAdapter(var context: Context, list: MutableList<String>) :
    BaseRecyclerAdapter<String, FontAdapter.ViewHolder>(context, list) {

    var fontSelected: String = ""
    var fontSelectListener: ((String) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = layoutInflater.inflate(R.layout.lib_item_font, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = LibItemFontBinding.bind(itemView)

        fun bindData(font: String) {
            binding.tvName.text = font
            binding.tvName.typeface = Typeface.createFromAsset(context.assets, "font/${font}")
            if (fontSelected == font) {
                binding.ivStage.setBackgroundResource(R.drawable.bg_item_selected)
            } else {
                binding.ivStage.setBackgroundResource(R.drawable.bg_item_unselected)
            }

            binding.ctrContainer.setOnClickListener {
                fontSelectListener?.invoke(font)
                fontSelected = font
                notifyDataSetChanged()
            }
        }
    }
}