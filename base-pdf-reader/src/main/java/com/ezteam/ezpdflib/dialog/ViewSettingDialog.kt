package com.ezteam.ezpdflib.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.ezpdflib.R
import com.ezteam.ezpdflib.databinding.ViewSettingDialogBinding

class ViewSettingDialog : DialogFragment() {
    private var _binding: ViewSettingDialogBinding? = null
    private val binding get() = _binding!!

    private var callBack: ((Int) -> Unit)? = null // Thêm biến lưu callback

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ViewSettingDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.landscape.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
            setDimAmount(0.5f)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Hàm thiết lập callback
    fun setOnSortSelectedListener(callback: (Int) -> Unit) {
        this.callBack = callback
    }
}

