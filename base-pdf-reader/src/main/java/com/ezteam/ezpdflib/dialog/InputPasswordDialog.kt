package com.ezteam.ezpdflib.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.databinding.LibDialogPasswordBinding

class InputPasswordDialog(
    context: Context, builder: ExtendBuilder?
) : BaseDialog<LibDialogPasswordBinding, InputPasswordDialog.ExtendBuilder>(context, builder) {
    companion object {
        const val INPUT_PASSWORD = "input_password"
    }

    class ExtendBuilder(context: Context?) : BuilderDialog(context) {
        override fun build(): BaseDialog<*, *> {
            return InputPasswordDialog(context, this)
        }
    }

    override fun initView() {
        super.initView()
        window?.attributes?.softInputMode =
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
    }

    override fun initListener() {

    }

    override fun handleClickPositiveButton(data: HashMap<String?, Any?>) {
        val inputName = binding.edtInputPassword.text.toString()
        if (inputName.isNotEmpty()) {
            data[INPUT_PASSWORD] = inputName
            super.handleClickPositiveButton(data)
            dismiss()
        } else {
            Toast.makeText(
                context,
                context.resources.getString(R.string.please_input_password),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getTitle(): TextView {
        return binding.tvTitle
    }

    override fun getPositiveButton(): TextView {
        return binding.tvPositive
    }

    override fun getNegativeButton(): TextView {
        return binding.tvNegative
    }

    override fun getViewBinding(): LibDialogPasswordBinding {
        return LibDialogPasswordBinding.inflate(LayoutInflater.from(context))
    }
}