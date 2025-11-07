package com.ezteam.ezpdflib.adapter

import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.databinding.LibItemSignatureBinding
import com.ezteam.ezpdflib.extension.reverstBitmap
import com.ezteam.ezpdflib.extension.uriToBitmap
import java.io.File

class SignatureAdapter(context: Context?, list: MutableList<File>?) :
    BaseRecyclerAdapter<File, SignatureAdapter.ViewHolder>(context, list) {

    var itemSelected: ((File) -> Unit)? = null
    var itemDelete: ((File, Int) -> Unit)? = null
    var isNightMode = false

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position], position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = layoutInflater.inflate(R.layout.lib_item_signature, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = LibItemSignatureBinding.bind(itemView)

        fun bindData(file: File, position: Int) {
            if (isNightMode) {
                Glide.with(mContext)
                    .load(Uri.fromFile(file).uriToBitmap(mContext).reverstBitmap())
                    .into(binding.ivSignature)
            } else {
                Glide.with(mContext)
                    .load(file.path)
                    .into(binding.ivSignature)
            }

            binding.itemMain.setOnClickListener {
                itemSelected?.invoke(file)
            }

            binding.ivDelete.setOnClickListener {
                itemDelete?.invoke(file, position)
            }
        }
    }
}