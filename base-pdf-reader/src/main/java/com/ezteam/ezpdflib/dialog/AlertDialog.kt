package com.ezteam.ezpdflib.dialog

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.view.isVisible
import com.ezteam.ezpdflib.databinding.LibDialogNotifiBinding

class AlertDialog(
    context: Context, builder: ExtendBuilder?
) : BaseDialog<LibDialogNotifiBinding, AlertDialog.ExtendBuilder>(context, builder) {

    class ExtendBuilder(context: Context?) : BuilderDialog(context) {
        override fun build(): BaseDialog<*, *> {
            return AlertDialog(context, this)
        }
    }

    override fun initView() {
        super.initView()
        binding.tvPositive.isVisible = binding.tvPositive.text.isNotEmpty()
        binding.tvNegative.isVisible = binding.tvNegative.text.isNotEmpty()
    }

    override fun handleClickPositiveButton(data: HashMap<String?, Any?>) {
        super.handleClickPositiveButton(data)
        dismiss()
    }

    override fun initListener() {

    }

    override fun handleClickNegativeButton() {
        super.handleClickNegativeButton()
        dismiss()
    }

    override fun getMessage(): TextView {
        return binding.tvMessage
    }

    override fun getPositiveButton(): TextView {
        return binding.tvPositive
    }

    override fun getNegativeButton(): TextView {
        return binding.tvNegative
    }

    override fun getViewBinding(): LibDialogNotifiBinding {
        return LibDialogNotifiBinding.inflate(LayoutInflater.from(context))
    }
}