package officepro.document.reader.viewer.editor.dialog

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import officepro.document.reader.viewer.editor.databinding.DialogInputPasswordBinding
import com.ezteam.baseproject.dialog.BaseDialog
import com.ezteam.baseproject.dialog.BuilderDialog

class InputPasswordDialog(
    context: Context,
    extendBuilder: ExtendBuilder
) : BaseDialog<DialogInputPasswordBinding, InputPasswordDialog.ExtendBuilder>(extendBuilder, context) {
    companion object {
        const val INPUT_PASSWORD = "input_password"
    }

    class ExtendBuilder(context: Context) : BuilderDialog(context) {
        override fun build(): BaseDialog<*, *> {
            return InputPasswordDialog(context, this)
        }
    }

    override fun initView() {
        super.initView()
    }

    override fun initListener() {

    }

    override fun handleClickPositiveButton(data: HashMap<String?, Any?>) {
        val inputName = binding.edtInputPassword.text.toString()
        if (inputName.isNotEmpty()) {
            data[INPUT_PASSWORD] = inputName
            super.handleClickPositiveButton(data)
        }
    }

    override val viewBinding: DialogInputPasswordBinding
        get() = DialogInputPasswordBinding.inflate(LayoutInflater.from(context))

    override val title: TextView
        get() = binding.tvTitle

    override val positiveButton: TextView
        get() = binding.tvPositive

    override val negativeButton: TextView
        get() = binding.tvNegative
}