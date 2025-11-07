package officepro.document.reader.viewer.editor.dialog

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import officepro.document.reader.viewer.editor.databinding.DialogInputNameBinding
import com.ezteam.baseproject.dialog.BaseDialog
import com.ezteam.baseproject.dialog.BuilderDialog

class InputNameDialog(
    context: Context,
    extendBuilder: ExtendBuilder
) : BaseDialog<DialogInputNameBinding, InputNameDialog.ExtendBuilder>(extendBuilder, context) {
    companion object {
        const val INPUT_NAME = "input_name"
    }

    class ExtendBuilder(context: Context) : BuilderDialog(context) {
        internal var fileName: String = ""

        override fun build(): BaseDialog<*, *> {
            return InputNameDialog(context, this)
        }

        fun setFileName(fileName: String): ExtendBuilder {
            this.fileName = fileName
            return this
        }
    }

    override fun initView() {
        super.initView()
        binding.editInputName.setText(builder.fileName)
        binding.editInputName.selectAll()
    }

    override fun initListener() {

    }

    override fun handleClickPositiveButton(data: HashMap<String?, Any?>) {
        val inputName = binding.editInputName.text.toString()
        if (inputName.isNotEmpty()) {
            data[INPUT_NAME] = inputName
            super.handleClickPositiveButton(data)
        }
    }

    override val viewBinding: DialogInputNameBinding
        get() = DialogInputNameBinding.inflate(LayoutInflater.from(context))

    override val title: TextView
        get() = binding.tvTitle

    override val positiveButton: TextView
        get() = binding.tvPositive

    override val negativeButton: TextView
        get() = binding.tvNegative
}