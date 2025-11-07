package officepro.document.reader.viewer.editor.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import officepro.document.reader.viewer.editor.R
import officepro.document.reader.viewer.editor.databinding.DialogGoPageBinding
import com.ezteam.baseproject.dialog.BaseDialog
import com.ezteam.baseproject.dialog.BuilderDialog

class GoToPageDialog(
    context: Context,
    extendBuilder: ExtendBuilder
) : BaseDialog<DialogGoPageBinding, GoToPageDialog.ExtendBuilder>(extendBuilder, context) {
    companion object {
        const val PAGE_NUMBER = "page_number"
    }

    class ExtendBuilder(context: Context) : BuilderDialog(context) {
        private var listener: ((Int) -> Unit)? = null
        internal var pageNumber: Int = 0

        override fun build(): BaseDialog<*, *> {
            return GoToPageDialog(context, this)
        }

        fun setListener(listener: (Int) -> Unit): ExtendBuilder {
            this.listener = listener
            return this
        }

        fun setPageNumber(pageNumber: Int): ExtendBuilder {
            this.pageNumber = pageNumber
            return this
        }
    }

    override fun initView() {
        super.initView()
        @SuppressLint("SetTextI18n")
        binding.tvRangePage.text = "(1-${builder.pageNumber})"
    }

    override fun initListener() {

    }

    override fun handleClickPositiveButton(data: HashMap<String?, Any?>) {
        val positionInput = binding.edtPageNumber.text.toString().toIntOrNull()
        positionInput?.let {
            if (it < 1 || it > builder.pageNumber) {
                binding.edtPageNumber.setText("")
                binding.edtPageNumber.error = context.resources.getString(R.string.enter_page_no)
            } else {
                data[PAGE_NUMBER] = it
                super.handleClickPositiveButton(data)
            }
        } ?: kotlin.run {
            binding.edtPageNumber.error = context.resources.getString(R.string.enter_page_no)
        }
    }

    override val viewBinding: DialogGoPageBinding
        get() = DialogGoPageBinding.inflate(LayoutInflater.from(context))

    override val title: TextView
        get() = binding.tvTitle

    override val positiveButton: TextView
        get() = binding.tvPositive

    override val negativeButton: TextView
        get() = binding.tvNegative
}