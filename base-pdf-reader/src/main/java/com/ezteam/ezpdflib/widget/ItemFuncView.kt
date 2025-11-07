package com.ezteam.ezpdflib.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.databinding.LibItemFuncBinding

class ItemFuncView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val binding: LibItemFuncBinding

    companion object {
        private const val DEFAULT_TEXT_COLOR = Color.BLACK
        private const val DEFAULT_TEXT_SELECTED_COLOR = Color.RED
    }

    var textColor = DEFAULT_TEXT_COLOR
    var textSelectedColor = DEFAULT_TEXT_SELECTED_COLOR
    var iconResId = 0
        set(value) {
            field = value
            if (value != 0) {
                binding.icFunc.setImageResource(value)
            }
        }

    var title = ""
        set(value) {
            field = value
            binding.tvFuncName.text = value
        }

    var isFuncSelected = false
        set(value) {
            field = value
            binding.tvFuncName.setTextColor(if (value) textSelectedColor else textColor)
        }

    init {
        val view: View = LayoutInflater.from(context).inflate(R.layout.lib_item_func, this)
        binding = LibItemFuncBinding.bind(view)
        setupAttributes(attrs)
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.ItemFunctionView,
            0, 0
        )

        textColor =
            typedArray.getColor(R.styleable.ItemFunctionView_fvTextColor, resources.getColor(R.color.text_color))
        textSelectedColor = typedArray.getColor(
            R.styleable.ItemFunctionView_fvTextSelectedColor,
            DEFAULT_TEXT_SELECTED_COLOR
        )
        title = typedArray.getString(R.styleable.ItemFunctionView_fvTitle).toString()
        iconResId = typedArray.getResourceId(R.styleable.ItemFunctionView_fvIcon, 0)
        setupViews()
    }

    private fun setupViews() {
        binding.tvFuncName.setTextColor(textColor)
    }
}