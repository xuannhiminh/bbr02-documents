package com.ezteam.ezpdflib.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.databinding.LibDialogGotoPageBinding

class GoToPageDialog(
    context: Context, builder: ExtendBuilder?
) : BaseDialog<LibDialogGotoPageBinding, GoToPageDialog.ExtendBuilder>(context, builder) {
    companion object {
        const val PAGE_NUMBER = "page_number"
    }

    class ExtendBuilder(context: Context?) : BuilderDialog(context) {
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

    @SuppressLint("SetTextI18n")
    override fun initView() {
        super.initView()
        window?.attributes?.softInputMode =
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        binding.edtPageNumber.hint =
            "${context.resources.getString(R.string.enter_page_no)} (1-${builder.pageNumber})"
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
        dismiss()
    }

    override fun handleClickNegativeButton() {
        super.handleClickNegativeButton()
        dismiss()
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

    override fun getViewBinding(): LibDialogGotoPageBinding {
        return LibDialogGotoPageBinding.inflate(LayoutInflater.from(context))
    }
}