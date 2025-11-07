package com.ezteam.ezpdflib.dialog

import android.content.Context
import android.view.LayoutInflater
import com.ezteam.ezpdflib.databinding.LibDialogLoadingBinding

class DialogLoading(builder: ExtendBuilder, context: Context) :

    BaseDialog<LibDialogLoadingBinding, DialogLoading.ExtendBuilder>(context, builder) {

    class ExtendBuilder(context: Context?) : BuilderDialog(context) {

        override fun build(): BaseDialog<*, *> {
            return DialogLoading(this,context)
        }

    }

    override fun initView() {
        super.initView()
    }

    override fun initListener() {

    }

    override fun getViewBinding(): LibDialogLoadingBinding {
        return LibDialogLoadingBinding.inflate(LayoutInflater.from(context))
    }

}