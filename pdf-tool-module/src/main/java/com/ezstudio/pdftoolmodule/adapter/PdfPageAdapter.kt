package com.ezstudio.pdftoolmodule.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.databinding.ItemPagePdfBinding
import com.ezstudio.pdftoolmodule.model.PdfPageModel
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter

class PdfPageAdapter(context: Context?, list: MutableList<PdfPageModel>?) :
    BaseRecyclerAdapter<PdfPageModel, PdfPageAdapter.ViewHolder>(context, list) {

    var dragClickListener: ((ViewHolder, Int) -> Unit)? = null

    override fun onBindViewHolder(holder: PdfPageAdapter.ViewHolder, position: Int) {
        holder.bindData(list[position], position)
        holder.itemView.setOnLongClickListener {
            dragClickListener?.invoke(holder, position)
            true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfPageAdapter.ViewHolder {
        val view = layoutInflater.inflate(R.layout.item_page_pdf, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemPagePdfBinding.bind(itemView)

        fun bindData(pdfPageModel: PdfPageModel, position: Int) {
            Glide.with(mContext)
                .load(pdfPageModel.thumbnail)
                .into(binding.ivPhoto)

            binding.ivSelected.visibility = if (pdfPageModel.selected) {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.tvPage.text = (pdfPageModel.page + 1).toString()

            binding.ivPhoto.setOnClickListener {
                pdfPageModel.selected = !pdfPageModel.selected
                notifyItemChanged(position)
            }
        }
    }
}