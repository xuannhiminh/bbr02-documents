package office.pdf.document.reader.viewer.editor.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import office.pdf.document.reader.viewer.editor.databinding.DialogAlertBinding
import com.ezteam.baseproject.dialog.BaseDialog
import com.ezteam.baseproject.dialog.BuilderDialog

class AlertDialog(
    context: Context,
    extendBuilder: ExtendBuilder
) : BaseDialog<DialogAlertBinding, AlertDialog.ExtendBuilder>(extendBuilder, context) {

    class ExtendBuilder(context: Context) : BuilderDialog(context) {
        override fun build(): BaseDialog<*, *> {
            return AlertDialog(context, this)
        }
    }

    override fun initView() {
        super.initView()
        binding.tvPositive.isVisible = binding.tvPositive.text.isNotEmpty()
        binding.tvNegative.isVisible = binding.tvNegative.text.isNotEmpty()
    }

    override fun handleClickNegativeButton(view: View) {
        super.handleClickNegativeButton(view)
    }

    override val viewBinding: DialogAlertBinding
        get() = DialogAlertBinding.inflate(LayoutInflater.from(context))

    override val title: TextView
        get() = binding.tvTitle

    override val message: TextView
        get() = binding.tvMessage

    override val positiveButton: TextView
        get() = binding.tvPositive

    override val negativeButton: TextView
        get() = binding.tvNegative

    override fun initListener() {

    }
}