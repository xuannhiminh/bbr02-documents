package com.ezstudio.pdftoolmodule.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.databinding.ItemPageEditBinding
import com.ezstudio.pdftoolmodule.extension.swap
import com.ezstudio.pdftoolmodule.model.EditPageModel
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import com.ezteam.baseproject.extensions.angleRotate
import com.ezteam.baseproject.extensions.invertBitmap
import com.ezteam.baseproject.extensions.uriToBitmap
import com.ezteam.baseproject.photopicker.PickImageActivity

class PageEditAdapter(var context: Context, list: MutableList<EditPageModel>) :
    BaseRecyclerAdapter<EditPageModel, PageEditAdapter.ViewHolder>(context, list) {

    var pageSelect = -1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position], position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = layoutInflater.inflate(R.layout.item_page_edit, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemPageEditBinding.bind(itemView)

        fun bindData(editPageModel: EditPageModel, position: Int) {
            val bitmapPage = editPageModel.btmEdit ?: editPageModel.thumbnail
            Glide.with(context)
                .load(bitmapPage)
                .thumbnail(0.3f)
                .into(binding.ivPhoto)

            binding.tvCount.text = (position + 1).toString()

            if (pageSelect == position) {
                binding.ivSetting.visibility = View.GONE
                binding.lnPageOption.visibility = View.VISIBLE
            } else {
                binding.ivSetting.visibility = View.VISIBLE
                binding.lnPageOption.visibility = View.GONE
            }

            binding.ivSetting.setOnClickListener {
                binding.lnPageOption.visibility = View.VISIBLE
                binding.ivSetting.visibility = View.GONE
                val oldPosition = pageSelect
                pageSelect = position
                notifyItemChanged(oldPosition)
            }

            binding.ivClose.setOnClickListener {
                binding.lnPageOption.visibility = View.GONE
                binding.ivSetting.visibility = View.VISIBLE
                pageSelect = -1
            }

            binding.ivPageRotate.setOnClickListener {
                editPageModel.apply {
                    btmEdit = bitmapPage.angleRotate(90f)
                    rotate = if (rotate == 270) {
                        0
                    } else {
                        rotate + 90
                    }
                }
                notifyItemChanged(position)
            }

            binding.ivPageInvert.setOnClickListener {
                editPageModel.apply {
                    btmEdit = bitmapPage.invertBitmap()
                    isInvert = !isInvert
                }
                notifyItemChanged(position)
            }

            binding.ivPageUp.setOnClickListener {
                if (position > 0) {
                    list.swap(position, position - 1)
                    notifyItemChanged(position)
                    notifyItemChanged(position - 1)
                    pageSelect = position - 1
                }
            }

            binding.ivPageDown.setOnClickListener {
                if (position < list.size - 1) {
                    list.swap(position, position + 1)
                    notifyItemChanged(position)
                    notifyItemChanged(position + 1)
                    pageSelect = position + 1
                }
            }

            binding.ivPageDelete.setOnClickListener {
                list.remove(editPageModel)
                pageSelect = -1
                notifyDataSetChanged()
            }

            binding.ivPageAdd.setOnClickListener {
                PickImageActivity.start(context as BaseActivity<*>, 1, 10) {
                    if (it.isNotEmpty()) {
                        it.forEachIndexed { index, uri ->
                            uri.uriToBitmap(context)?.let {
                                val page = EditPageModel(it, -2, btmEdit = it)
                                list.add(position + 1 + index, page)
                            }
                        }
                        notifyDataSetChanged()
                    }
                }
            }

        }

    }
}