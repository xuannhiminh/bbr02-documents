package com.ezstudio.pdftoolmodule.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.ezstudio.pdftoolmodule.databinding.ItemToolFileBinding
import com.ezstudio.pdftoolmodule.model.FileModel
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import com.ezteam.baseproject.listener.EzItemListener
import com.ezteam.baseproject.utils.DateUtils

class FileToolItemAdapter(
    context: Context,
    list: List<FileModel>
) : BaseRecyclerAdapter<FileModel, FileToolItemAdapter.ViewHolder>(context, list), Filterable {

    var itemClickListener: ((FileModel) -> Unit)? = null
    var lstConstant = mutableListOf<FileModel>()
    var lstSelected = mutableListOf<FileModel>()
    var maxSelectItem = -1
    var allowSelect = false
    var allowMoveFile = false
    var allowSwipeRight = false
    var dragClickListener: ((ViewHolder, Int) -> Unit)? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position], position)
        holder.binding.ivMove.setOnTouchListener { v, event ->
            dragClickListener?.invoke(holder, position)
            true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemToolFileBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(
        var binding: ItemToolFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(model: FileModel, position: Int) {
            binding.tvTitle.text = model.name
            @SuppressLint("SetTextI18n")
            binding.tvCreateDate.text =
                "${
                    DateUtils.longToDateString(
                        model.date,
                        DateUtils.DATE_FORMAT_7
                    )
                } | ${model.sizeString}"

            binding.ivMove.visibility = if (allowMoveFile) {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.swipeLayout.isSwipeEnabled = allowSwipeRight

            binding.content.setOnClickListener {
                if (allowSelect) {
                    when (maxSelectItem) {
                        1 -> {
                            if (lstSelected.contains(model)) {
                                lstSelected.clear()
                            } else {
                                lstSelected.clear()
                                lstSelected.add(model)
                            }
                            notifyDataSetChanged()
                        }
                        -1 -> {
                            if (lstSelected.contains(model)) {
                                lstSelected.remove(model)
                            } else {
                                lstSelected.add(model)
                            }
                            notifyItemChanged(position)
                        }
                        else -> {
                            if (lstSelected.contains(model)) {
                                lstSelected.remove(model)
                                notifyItemChanged(position)
                            } else {
                                if (lstSelected.size == maxSelectItem) {
                                    lstSelected.removeAt(0)
                                }
                                lstSelected.add(model)
                                notifyDataSetChanged()
                            }
                        }
                    }
                }
                itemClickListener?.invoke(model)
            }

            binding.rdSelected.visibility = if (lstSelected.contains(model)) {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.tvDelete.setOnClickListener {
                lstConstant.remove(model)
                list.remove(model)
                lstSelected.remove(model)
                binding.swipeLayout.animateReset()
                notifyDataSetChanged()
            }

            binding.tvDuplicate.setOnClickListener {
                lstConstant.add(position, model)
                list.add(position, model)
                binding.swipeLayout.animateReset()
                notifyDataSetChanged()
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = mutableListOf<FileModel>()
                if (constraint == null || constraint.isEmpty()) {
                    filteredList.addAll(lstConstant)
                } else {
                    for (item in lstConstant) {
                        if (item.name.toString().lowercase()
                                .startsWith(constraint.toString().lowercase())
                        ) {
                            filteredList.add(item)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                setList(results?.values as MutableList<FileModel>?)
                notifyDataSetChanged()
            }

        }
    }
}
