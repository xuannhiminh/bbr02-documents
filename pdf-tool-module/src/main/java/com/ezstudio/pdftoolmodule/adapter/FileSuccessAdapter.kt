package com.ezstudio.pdftoolmodule.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.databinding.ItemFileSuccessBinding
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import org.apache.commons.io.FilenameUtils

class FileSuccessAdapter(context: Context?, list: MutableList<String>?) :
    BaseRecyclerAdapter<String, FileSuccessAdapter.ViewHolder>(context, list) {

    var shareListener: ((String) -> Unit)? = null
    var itemClickListener: ((String) -> Unit)? = null

    override fun onBindViewHolder(holder: FileSuccessAdapter.ViewHolder, position: Int) {
        holder.bindData(list[position])
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FileSuccessAdapter.ViewHolder {
        val view = layoutInflater.inflate(R.layout.item_file_success, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemFileSuccessBinding.bind(itemView)

        fun bindData(filePath: String) {

            binding.tvPage.text = FilenameUtils.getBaseName(filePath)

            if (FilenameUtils.getExtension(filePath) == "pdf") {
                binding.ivPhotoPdf.visibility = View.VISIBLE
                binding.ivPhoto.visibility = View.GONE
            } else {
                binding.ivPhotoPdf.visibility = View.GONE
                binding.ivPhoto.visibility = View.VISIBLE
                Glide.with(mContext)
                    .load(filePath)
                    .into(binding.ivPhoto)
            }

            binding.ivShare.setOnClickListener {
                shareListener?.invoke(filePath)
            }

            itemView.setOnClickListener {
                itemClickListener?.invoke(filePath)
            }
        }
    }
}