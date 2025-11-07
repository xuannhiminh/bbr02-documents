package com.ezstudio.pdftoolmodule.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.databinding.DialogToolCreateFileBinding
import com.ezstudio.pdftoolmodule.databinding.DialogToolRotateBinding
import com.ezteam.baseproject.dialog.BaseDialog
import com.ezteam.baseproject.dialog.BuilderDialog

class RotatePageDialog(
    context: Context, builder: ExtendBuilder
) : BaseDialog<DialogToolRotateBinding, RotatePageDialog.ExtendBuilder>(builder, context) {

    companion object {
        const val KEY_FILE_NAME = "KEY_FILE_NAME"
        const val KEY_ANGLE = "KEY_ANGLE"
    }

    class ExtendBuilder(context: Context) : BuilderDialog(context) {

        var fileNameDefault = "Pfd_created_${System.currentTimeMillis()}"
        var hint = context.getString(R.string.enter_file_name)

        override fun build(): BaseDialog<*, *> {
            return RotatePageDialog(context, this)
        }

        fun setHint(hint: String): ExtendBuilder {
            this.hint = hint
            return this
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
        binding.edtFileName.setText(builder.fileNameDefault)

        binding.edtFileName.hint = builder.hint
        binding.inputFileNameView.hint = builder.hint


        binding.edtFileName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.inputFileNameView.isErrorEnabled = false
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
        val fileName = binding.edtFileName.text.toString().trim()

        if (fileName.isEmpty()) {
            binding.inputFileNameView.error =
                context.resources.getString(R.string.invalid_value)
        } else {
            data[KEY_FILE_NAME] = fileName
            data[KEY_ANGLE] = when {
                binding.rd90.isChecked -> {
                    90
                }
                binding.rd180.isChecked -> {
                    180
                }
                else -> {
                    270
                }
            }
            super.handleClickPositiveButton(data)
            dismiss()
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

    override val viewBinding: DialogToolRotateBinding
        get() = DialogToolRotateBinding.inflate(LayoutInflater.from(context))
}