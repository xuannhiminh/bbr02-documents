package com.ezstudio.pdftoolmodule.adapter

import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.databinding.ItemToolImageBinding
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter

class ImageAdapter(context: Context?, list: MutableList<Uri>?) :
    BaseRecyclerAdapter<Uri, ImageAdapter.ViewHolder>(context, list) {

    var removedListener: ((Uri) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = layoutInflater.inflate(R.layout.item_tool_image, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemToolImageBinding.bind(itemView)

        fun bindData(uri: Uri) {
            Glide.with(mContext)
                .load(uri)
                .into(binding.ivPhoto)

            binding.ivRemove.setOnClickListener {
                list.remove(uri)
                notifyItemRemoved(adapterPosition)
                removedListener?.invoke(uri)
            }
        }
    }
}