package com.ezteam.baseproject.photopicker

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import com.ezteam.baseproject.databinding.ItemImageBinding

class ImageAlbumAdapter(
    var context: Context,
    list: ArrayList<Photo>,
    columnNumber: Int,
    var isShowRemove: Boolean = false,
    var onItemSelected: (Photo) -> Unit
) : BaseRecyclerAdapter<Photo, ImageAlbumAdapter.ViewHolder>(context, list) {
    private var imageSize = 0

    init {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)
        val widthPixels = metrics.widthPixels
        imageSize = widthPixels / columnNumber
    }

    open inner class ViewHolder(
        private val binding: ItemImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("CheckResult")
        fun bindData(imageModel: Photo) {
            Glide.with(context)
                .load(imageModel.path)
                .thumbnail(0.6f)
                .error {
                    Log.e("LoadImage", "Error")
                }
                .into(binding.imageView)

            if (isShowRemove) {
                binding.ivSelected.isVisible = true
            }

            binding.root.setOnClickListener {
                onItemSelected.invoke(imageModel)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImageBinding.inflate(layoutInflater, parent, false)
        binding.imageView.layoutParams.width = imageSize
        binding.imageView.layoutParams.height = imageSize
        return ViewHolder(binding)
    }
}