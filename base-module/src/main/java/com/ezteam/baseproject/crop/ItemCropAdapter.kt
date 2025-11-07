package com.ezteam.baseproject.crop

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import com.ezteam.baseproject.R
import com.ezteam.baseproject.databinding.ItemBaseCropBinding

class ItemCropAdapter(var context: Context, list: MutableList<ItemCrop>) :
    BaseRecyclerAdapter<ItemCrop, ItemCropAdapter.ViewHolder>(
        context, list as List<ItemCrop>
    ) {

    var itemSelected = list[0]
    var itemSelectListener: ((ItemCrop) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = layoutInflater.inflate(R.layout.item_base_crop, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding = ItemBaseCropBinding.bind(itemView)

        fun bindData(itemCrop: ItemCrop) {
            binding.tvTitle.text = itemCrop.title
            Glide.with(context)
                .load(itemCrop.icon)
                .into(binding.imCrop)
            if (itemCrop == itemSelected) {
                binding.tvTitle.setTextColor(context.resources.getColor(R.color.color_FBB418))
                binding.viewLine.visibility = View.VISIBLE
            } else {
                binding.tvTitle.setTextColor(context.resources.getColor(R.color.tokenWhite100))
                binding.viewLine.visibility = View.INVISIBLE
            }
            itemView.setOnClickListener {
                itemSelected = itemCrop
                notifyDataSetChanged()
                itemSelectListener?.let {
                    it(itemCrop)
                }
            }
        }
    }

}