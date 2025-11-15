package office.pdf.document.reader.viewer.editor.dialog

import android.content.Context
import android.view.LayoutInflater
import office.pdf.document.reader.viewer.editor.adapter.LanguageItemAdapter
import office.pdf.document.reader.viewer.editor.databinding.DialogChangeLanguageBinding
import office.pdf.document.reader.viewer.editor.model.LanguageModel
import com.ezteam.baseproject.dialog.BaseDialog
import com.ezteam.baseproject.dialog.BuilderDialog

class ChangeLanguageDialog(
    context: Context,
    builder: ExtendBuilder
) : BaseDialog<DialogChangeLanguageBinding, ChangeLanguageDialog.ExtendBuilder>(builder, context) {
    private lateinit var adapter: LanguageItemAdapter
    private val listLanguage: List<LanguageModel> = mutableListOf(
        LanguageModel("English", "en"),
        LanguageModel("اللغة العربية", "ar"),
        LanguageModel("简体中文", "zh"),
        LanguageModel("Français", "fr"),
        LanguageModel("Deutsch", "de"),
        LanguageModel("हिंदी", "hi"),
        LanguageModel("Indonesia", "in"),
        LanguageModel("Melayu", "ms"),
        LanguageModel("Netherlands", "nl"),
        LanguageModel("русский", "ru"),
        LanguageModel("한국어", "ko"),
        LanguageModel("Español", "es"),
        LanguageModel("Türkçe", "tr"),
        LanguageModel("Українська", "uk"),
        LanguageModel("Português", "pt"),
        LanguageModel("日本語", "ja"),
        LanguageModel("Tiếng Việt", "vi")
    )

    class ExtendBuilder(context: Context) : BuilderDialog(context) {
        internal var listener: ((String) -> Unit)? = null
        override fun build(): BaseDialog<*, *> {
            return ChangeLanguageDialog(context, this)
        }

        fun setListener(listener: (String) -> Unit) : ExtendBuilder {
            this.listener = listener
            return this
        }
    }

    override val viewBinding: DialogChangeLanguageBinding
        get() = DialogChangeLanguageBinding.inflate(LayoutInflater.from(context))

    override fun initView() {
        super.initView()
        adapter = LanguageItemAdapter(context, listLanguage) {
            builder.listener?.invoke(it.languageCode)
            dismiss()
        }

        binding.rcvLanguage.adapter = adapter
    }

    override fun initListener() {

    }
}