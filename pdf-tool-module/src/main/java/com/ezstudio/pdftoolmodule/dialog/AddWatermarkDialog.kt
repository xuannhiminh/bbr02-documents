package com.ezstudio.pdftoolmodule.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.TextView
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.databinding.DialogAddWatermarkBinding
import com.ezstudio.pdftoolmodule.utils.Config
import com.ezstudio.pdftoolmodule.utils.pdftool.WatermarkModel
import com.ezteam.baseproject.dialog.BaseDialog
import com.ezteam.baseproject.dialog.BuilderDialog
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Font


class AddWatermarkDialog(
    context: Context, builder: ExtendBuilder
) : BaseDialog<DialogAddWatermarkBinding, AddWatermarkDialog.ExtendBuilder>(builder, context) {

    var result: ((WatermarkModel) -> Unit)? = null

    class ExtendBuilder(context: Context) : BuilderDialog(context) {

        var fileNameDefault = "Pfd_created_${System.currentTimeMillis()}"

        override fun build(): BaseDialog<*, *> {
            return AddWatermarkDialog(context, this)
        }


        fun setFileName(fileName: String): ExtendBuilder {
            this.fileNameDefault = fileName
            return this
        }

    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        super.initView()

        window?.attributes?.softInputMode =
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        if (TextUtils.isEmpty(builder.fileNameDefault)) {
            binding.edtFileName.visibility = View.GONE
        } else {
            binding.edtFileName.visibility = View.VISIBLE
            binding.edtFileName.setText(builder.fileNameDefault)
        }

        binding.spnFont.adapter = ArrayAdapter(
            context, android.R.layout.simple_spinner_dropdown_item,
            Font.FontFamily.values()
        )
        binding.spnStyle.adapter = ArrayAdapter(
            context, android.R.layout.simple_spinner_dropdown_item,
            context.resources.getStringArray(R.array.fontStyles)
        )

        binding.edtFileName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.inputName.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        binding.edtWatermarkText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.inputText.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        binding.edtAngle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.inputAngle.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

    }

    override fun handleClickNegativeButton(view: View) {
        super.handleClickNegativeButton(view)
        dismiss()
    }

    override fun handleClickPositiveButton(data: HashMap<String?, Any?>) {
        val name = binding.edtFileName.text.toString().trim()
        val text = binding.edtWatermarkText.text.toString().trim()
        val angle = binding.edtAngle.text.toString().trim()

        when {
            name.isEmpty() -> {
                binding.inputName.error =
                    context.resources.getString(R.string.invalid_value)
            }
            text.isEmpty() -> {
                binding.inputText.error =
                    context.resources.getString(R.string.invalid_value)
            }
            angle.isEmpty() -> {
                binding.inputAngle.error =
                    context.resources.getString(R.string.invalid_value)
            }
            else -> {
                val watermark = WatermarkModel().apply {
                    fileName = name
                    watermarkText = text
                    fontFamily = binding.spnFont.selectedItem as Font.FontFamily
                    fontStyle =
                        Config.getStyleValueFromName(binding.spnStyle.selectedItem as String)
                    rotationAngle = binding.edtAngle.text.toString().toFloat()
                    textSize = binding.edtFontSize.text.toString().toFloat()
                    textColor = BaseColor(
                        Color.red(binding.watermarkColor.color),
                        Color.green(binding.watermarkColor.color),
                        Color.blue(binding.watermarkColor.color),
                        Color.alpha(binding.watermarkColor.color)
                    )
                }
                result?.invoke(watermark)
                super.handleClickPositiveButton(data)
                dismiss()
            }
        }
    }

    override val positiveButton: TextView
        get() = binding.tvPositive

    override val negativeButton: TextView
        get() = binding.tvNegative

    override val title: TextView
        get() = binding.tvTitle

    override val container: View
        get() = binding.container

    override fun initListener() {

    }

    override val viewBinding: DialogAddWatermarkBinding
        get() = DialogAddWatermarkBinding.inflate(LayoutInflater.from(context))
}