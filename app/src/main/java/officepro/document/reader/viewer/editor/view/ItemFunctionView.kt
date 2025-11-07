package officepro.document.reader.viewer.editor.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StyleableRes
import androidx.constraintlayout.widget.ConstraintLayout
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.ItemFuncBinding

class ItemFunctionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @StyleableRes defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, 0) {
    private val binding: ItemFuncBinding

    companion object {
        private const val DEFAULT_TEXT_COLOR = Color.BLACK
        private const val DEFAULT_TEXT_SELECTED_COLOR = Color.BLUE
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
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_func, this)
        binding = ItemFuncBinding.bind(view)
        setupAttributes(attrs)
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        try {
            val typedArray = context.theme.obtainStyledAttributes(
                attrs, R.styleable.ItemFunctionView,
                0, 0
            )

            textColor =
                typedArray.getColor(R.styleable.ItemFunctionView_fvTextColor, DEFAULT_TEXT_COLOR)
            textSelectedColor = typedArray.getColor(
                R.styleable.ItemFunctionView_fvTextSelectedColor,
                DEFAULT_TEXT_SELECTED_COLOR
            )
            typedArray.getString(R.styleable.ItemFunctionView_fvTitle)?.let { title = it }
            iconResId = typedArray.getResourceId(R.styleable.ItemFunctionView_fvIcon, 0)
        } catch (e: Exception) {
            Log.e("ItemFunctionView", "Error reading attributes", e)
        } finally {
            setupViews()
        }
    }

    private fun setupViews() {
        binding.tvFuncName.setTextColor(textColor)
    }
}