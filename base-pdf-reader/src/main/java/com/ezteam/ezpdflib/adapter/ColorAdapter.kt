package com.ezteam.ezpdflib.adapter

import android.graphics.Color
import android.graphics.PorterDuff
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.databinding.LibItemColorBinding

class ColorAdapter(var arrColor: ArrayList<String>) :
    RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

    var itemSelectListener: ((String) -> Unit)? = null
    var colorSelect: String = "#FF0000"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lib_item_color, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindata(arrColor[position])
    }

    override fun getItemCount(): Int {
        return arrColor.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = LibItemColorBinding.bind(itemView)

        fun bindata(color: String) {
            if (!TextUtils.isEmpty(color)) {
                binding.imColor.setImageResource(R.drawable.lib_circle_color)
                binding.imColor.setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN)
                binding.imSelect.visibility = if (color == colorSelect) View.VISIBLE else View.GONE
            } else {
                binding.imColor.setImageResource(R.drawable.lib_ic_rgb)
                binding.imColor.clearColorFilter()
                binding.imSelect.visibility = View.GONE
            }

            itemView.setOnClickListener {
                itemSelectListener?.invoke(color)
                colorSelect = color
                notifyDataSetChanged()
            }
        }
    }
}