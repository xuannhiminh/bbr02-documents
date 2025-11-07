package com.ezstudio.pdftoolmodule.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.databinding.ItemFunctionBinding
import com.ezstudio.pdftoolmodule.model.Function
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import java.util.*

class FunctionAdapter(context: Context?, list: MutableList<Function>?) :
    BaseRecyclerAdapter<Function, FunctionAdapter.ViewHolder>(context, list) {

    var itemClickListener: ((Function) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = layoutInflater.inflate(R.layout.item_function, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemFunctionBinding.bind(itemView)

        fun bindData(function: Function) {
            binding.tvTitle.text = mContext.getString(function.title)
            Glide.with(mContext)
                .load(function.icon)
                .into(binding.ivIcon)

            binding.ivIcon.setColorFilter(
                mContext.resources.getColor(R.color.color_1D1E2C),
                PorterDuff.Mode.SRC_IN
            )
            binding.ivBackground.setColorFilter(
                mContext.resources.getColor(function.color),
                PorterDuff.Mode.SRC_IN
            )

            itemView.setOnClickListener {
                itemClickListener?.invoke(function)
            }
        }
    }
}